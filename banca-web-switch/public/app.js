const state = {
  session: null,
  customerType: "NATURAL",
  coreUserId: null,
  accounts: [],
  transactions: [],
  batches: [],
  charges: [],
  companyAccount: null
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

function formatMoney(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat("es-EC", {
    style: "currency",
    currency: "USD"
  }).format(amount);
}

function formatDate(value) {
  if (!value) return "Sin fecha";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("es-EC", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(date);
}

function statusClass(value) {
  const normalized = String(value || "").toUpperCase();
  if (["ACTIVO", "COMPLETADA", "SUCCESS", "PROCESADO", "APROBADO"].some((item) => normalized.includes(item))) {
    return "is-success";
  }
  if (["ERROR", "RECHAZ", "FALL", "BLOQUEADO", "INACTIVO"].some((item) => normalized.includes(item))) {
    return "is-danger";
  }
  return "is-neutral";
}

function compactAccount(value) {
  const text = String(value || "N/D");
  return text.length > 4 ? `**** ${text.slice(-4)}` : text;
}

function resolveCompanyAccountFallback() {
  const favorite = state.accounts.find((account) => account.isFavorite);
  return favorite?.accountNumber || state.accounts[0]?.accountNumber || null;
}

function movementClass(value) {
  return String(value || "").toUpperCase().includes("CREDITO") ? "is-credit" : "is-debit";
}

function shortId(value) {
  const text = String(value || "N/D");
  return text.length > 14 ? `${text.slice(0, 8)}...${text.slice(-4)}` : text;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function setMessage(element, text, type = "") {
  element.textContent = text || "";
  element.classList.toggle("is-error", type === "error");
  element.classList.toggle("is-success", type === "success");
}

async function api(path, options = {}) {
  const response = await fetch(path, options);
  const contentType = response.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const payload = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof payload === "object" ? payload.error || payload.detail || payload.message : payload;
    throw new Error(message || `Error HTTP ${response.status}`);
  }

  return payload;
}

async function download(path, fileName) {
  const response = await fetch(path);
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Error HTTP ${response.status}`);
  }

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = fileName;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(url);
}

async function checkServices() {
  try {
    const data = await api("/api/core/core/v1/auth/test-data");
    state.coreUserId = data.coreUserId || 1;
    $("#coreStatus").textContent = "Core conectado";
  } catch (error) {
    state.coreUserId = 1;
    $("#coreStatus").textContent = "Core sin conexion";
  }

  try {
    await api("/api/switch/api/switch/health");
    $("#switchStatus").textContent = "Switch conectado";
  } catch (error) {
    $("#switchStatus").textContent = "Switch sin conexion";
  }
}

async function login(event) {
  event.preventDefault();
  const loginMessage = $("#loginMessage");
  setMessage(loginMessage, "Validando credenciales en Core...");

  const form = new FormData(event.currentTarget);

  try {
    const session = await api("/api/core/core/v1/auth/customers/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: form.get("username"),
        password: form.get("password")
      })
    });

    const realType = session.customerType;
    if (!realType) {
      throw new Error("Core no devolvio el tipo de cliente. Reinicia el servicio Core para cargar los cambios.");
    }

    state.session = session;
    state.customerType = realType;
    localStorage.setItem("banquitoSession", JSON.stringify({ session, customerType: realType }));
    setMessage(loginMessage, "Ingreso correcto.", "success");
    showDashboard();
    await refreshAll();
  } catch (error) {
    setMessage(loginMessage, error.message || "No se pudo iniciar sesion.", "error");
  }
}

function showDashboard() {
  $("[data-view='login']").classList.add("is-hidden");
  $("[data-view='dashboard']").classList.remove("is-hidden");

  const isCompany = state.customerType === "JURIDICO";
  $("#sessionType").textContent = isCompany ? "Cliente juridico" : "Cliente natural";
  $("#sessionName").textContent = state.session?.customerName || state.session?.username || "Dashboard";
  $("#sessionMeta").textContent = `${state.session?.identificationType || "ID"} ${state.session?.identification || ""}`.trim();
  $("#sidebarType").textContent = isCompany ? "Perfil juridico" : "Perfil natural";

  $$(".company-only").forEach((element) => element.classList.toggle("is-hidden", !isCompany));
  $$(".natural-only").forEach((element) => element.classList.toggle("is-hidden", isCompany));

  const activeSection = $(".nav-item.is-active")?.dataset.section;
  if (!isCompany && ["payments", "reports"].includes(activeSection)) {
    activateSection("overview");
  }

  renderProfile();
}

function logout() {
  state.session = null;
  state.accounts = [];
  state.transactions = [];
  state.batches = [];
  state.charges = [];
  localStorage.removeItem("banquitoSession");
  $("#loginForm").reset();
  activateSection("overview");
  $("[data-view='dashboard']").classList.add("is-hidden");
  $("[data-view='login']").classList.remove("is-hidden");
}

function restoreSession() {
  const raw = localStorage.getItem("banquitoSession");
  if (!raw) return;

  try {
    const parsed = JSON.parse(raw);
    state.session = parsed.session;
    state.customerType = parsed.customerType || parsed.session?.customerType || "NATURAL";
    showDashboard();
    refreshAll();
  } catch (error) {
    localStorage.removeItem("banquitoSession");
  }
}

function activateSection(section) {
  const isCompany = state.customerType === "JURIDICO";
  if (!isCompany && ["payments", "reports"].includes(section)) section = "overview";

  $$(".nav-item").forEach((button) => button.classList.toggle("is-active", button.dataset.section === section));
  $$("[data-section-panel]").forEach((panel) => {
    panel.classList.toggle("is-hidden", panel.dataset.sectionPanel !== section);
  });
}

function renderProfile() {
  if (!state.session) return;

  const session = state.session;
  $("#profileName").textContent = session.customerName || "Informacion del cliente";
  $("#profileDetails").innerHTML = [
    ["Tipo", state.customerType === "JURIDICO" ? "Juridico" : "Natural"],
    ["Identificacion", `${session.identificationType || "N/D"} ${session.identification || ""}`.trim()],
    ["Usuario", session.username],
    ["Correo", session.email],
    ["Telefono", session.mobilePhone],
    ["Direccion", session.address],
    ["Estado credencial", session.status],
    ["Ultimo ingreso", formatDate(session.lastLogin)]
  ]
    .map(([label, value]) => `
      <div>
        <dt>${escapeHtml(label)}</dt>
        <dd>${escapeHtml(value || "N/D")}</dd>
      </div>
    `)
    .join("");
}

async function loadAccounts() {
  if (!state.session?.customerId) return;

  try {
    state.accounts = await api(`/api/core/core/v1/accounts/customer/${state.session.customerId}`, {
      headers: { "X-Core-User-Id": String(state.coreUserId || 1) }
    });
  } catch (error) {
    state.accounts = [];
    $("#accountsList").innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
  }

  renderAccounts();
}

async function loadTransactions() {
  if (!state.session?.customerId) return;

  try {
    state.transactions = await api(`/api/core/core/v1/accounts/customer/${state.session.customerId}/transactions`, {
      headers: { "X-Core-User-Id": String(state.coreUserId || 1) }
    });
  } catch (error) {
    state.transactions = [];
    $("#transactionsTable").innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
  }

  renderTransactions();
}

function renderAccounts() {
  $("#accountsMetric").textContent = state.accounts.length;
  const totalAvailable = state.accounts.reduce((sum, account) => sum + Number(account.availableBalance || 0), 0);
  $("#balanceMetric").textContent = formatMoney(totalAvailable);

  const container = $("#accountsList");
  if (!state.accounts.length) {
    container.innerHTML = '<div class="empty-state">No hay cuentas disponibles para este cliente.</div>';
    return;
  }

  container.innerHTML = state.accounts
    .map((account) => `
      <article class="account-card">
        <span>${escapeHtml(account.accountSubtypeDescription || "Cuenta")}</span>
        <strong>${escapeHtml(account.accountNumber || "Sin numero")}</strong>
        <dl>
          <div>
            <dt>Disponible</dt>
            <dd>${formatMoney(account.availableBalance)}</dd>
          </div>
          <div>
            <dt>Contable</dt>
            <dd>${formatMoney(account.accountingBalance)}</dd>
          </div>
          <div>
            <dt>Estado</dt>
            <dd><span class="badge ${statusClass(account.status)}">${escapeHtml(account.status || "N/D")}</span></dd>
          </div>
          <div>
            <dt>Agencia</dt>
            <dd>${escapeHtml(account.branchName || "N/D")}</dd>
          </div>
        </dl>
      </article>
    `)
    .join("");
}

function renderTransactions() {
  $("#transactionsMetric").textContent = state.transactions.length;
  const recent = $("#recentTransactions");
  const table = $("#transactionsTable");

  if (!state.transactions.length) {
    const empty = '<div class="empty-state">Sin transacciones registradas.</div>';
    recent.innerHTML = empty;
    table.innerHTML = empty;
    return;
  }

  const rows = state.transactions.map((transaction) => `
    <tr>
      <td>${escapeHtml(transaction.accountNumber || "N/D")}</td>
      <td><span class="badge ${movementClass(transaction.movementType)}">${escapeHtml(transaction.movementType || "N/D")}</span></td>
      <td>${formatMoney(transaction.amount)}</td>
      <td>${formatMoney(transaction.resultingBalance)}</td>
      <td><span class="badge ${statusClass(transaction.status)}">${escapeHtml(transaction.status || "N/D")}</span></td>
      <td>${formatDate(transaction.transactionDate)}</td>
      <td>${escapeHtml(transaction.message || "N/D")}</td>
      <td><span title="${escapeHtml(transaction.transactionUuid || "N/D")}">${escapeHtml(shortId(transaction.transactionUuid))}</span></td>
    </tr>
  `).join("");

  const markup = `
    <table>
      <thead>
        <tr>
          <th>Cuenta</th>
          <th>Movimiento</th>
          <th>Monto</th>
          <th>Saldo resultante</th>
          <th>Estado</th>
          <th>Fecha</th>
          <th>Descripcion</th>
          <th>UUID</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>
  `;

  table.innerHTML = markup;
  recent.innerHTML = markup;
}

async function loadBatches() {
  if (state.customerType !== "JURIDICO") return;

  try {
    state.batches = await api("/api/switch/api/payment-batch");
  } catch (error) {
    state.batches = [];
    $("#batchesTable").innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
  }

  renderBatches();
}

async function loadCharges() {
  if (state.customerType !== "JURIDICO") return;

  try {
    const response = await api("/api/switch/api/billing/charges");
    state.charges = response.cargos || [];
  } catch (error) {
    state.charges = [];
  }
  $("#chargesMetric").textContent = state.charges.length;
}

async function loadCompanyAccount() {
  if (state.customerType !== "JURIDICO") return;

  try {
    const response = await api("/api/switch/api/billing/empresa-account");
    state.companyAccount = response.cuentaEmpresa || null;
  } catch (error) {
    state.companyAccount = resolveCompanyAccountFallback();
  }

  if (!state.companyAccount) {
    state.companyAccount = resolveCompanyAccountFallback();
  }

  const value = compactAccount(state.companyAccount);
  $("#companyAccountMetric").textContent = value;
  $("#companyAccountHero").textContent = value;
}

function renderBatches() {
  $("#batchesMetric").textContent = state.batches.length;
  const table = $("#batchesTable");
  const recent = $("#recentBatches");

  if (!state.batches.length) {
    const empty = '<div class="empty-state">Sin lotes cargados todavia.</div>';
    table.innerHTML = empty;
    recent.innerHTML = empty;
    return;
  }

  const rows = state.batches
    .slice()
    .sort((a, b) => (b.id || 0) - (a.id || 0))
    .map((batch) => `
      <tr>
        <td>${escapeHtml(batch.id || "N/D")}</td>
        <td>${escapeHtml(batch.fileName || "Archivo CSV")}</td>
        <td>${escapeHtml(batch.ruc || "N/D")}</td>
        <td><span class="badge ${statusClass(batch.status)}">${escapeHtml(batch.status || "N/D")}</span></td>
        <td>${escapeHtml(batch.headerTotalRecords || 0)}</td>
        <td>${formatMoney(batch.headerTotalAmount)}</td>
        <td>${formatDate(batch.receivedAt)}</td>
        <td><button class="secondary-button" data-process="${escapeHtml(batch.id)}" type="button">Procesar</button></td>
      </tr>
    `)
    .join("");

  const markup = `
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Archivo</th>
          <th>RUC</th>
          <th>Estado</th>
          <th>Registros</th>
          <th>Monto</th>
          <th>Recibido</th>
          <th>Accion</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>
  `;

  table.innerHTML = markup;
  recent.innerHTML = markup;
}

async function uploadCsv(event) {
  event.preventDefault();
  const uploadMessage = $("#uploadMessage");

  if (state.customerType !== "JURIDICO") {
    setMessage(uploadMessage, "Solo clientes juridicos pueden enviar pagos masivos.", "error");
    return;
  }

  const file = $("#csvFile").files[0];
  if (!file) {
    setMessage(uploadMessage, "Selecciona un archivo CSV.", "error");
    return;
  }

  const form = new FormData();
  form.append("file", file);
  form.append("channel", "PORTAL");

  setMessage(uploadMessage, "Enviando archivo al Switch...");
  try {
    const response = await api("/api/switch/api/payment-batch/upload-csv", {
      method: "POST",
      body: form
    });
    setMessage(uploadMessage, `Resultado: ${response.validationResult || "procesado"} | Estado: ${response.batchStatus || "N/D"}`, "success");
    await refreshCompanyData();
  } catch (error) {
    setMessage(uploadMessage, error.message || "No se pudo cargar el CSV.", "error");
  }
}

async function processBatch(batchId) {
  if (state.customerType !== "JURIDICO") return;

  try {
    const response = await api(`/api/switch/api/payment-processor/process/${batchId}`, { method: "POST" });
    $("#reportOutput").textContent = typeof response === "string" ? response : JSON.stringify(response, null, 2);
    await refreshCompanyData();
  } catch (error) {
    $("#reportOutput").textContent = error.message;
  }
}

async function runReport(type) {
  const batchId = $("#batchIdInput").value.trim();
  if (!batchId) {
    $("#reportOutput").textContent = "Ingresa el ID del lote.";
    return;
  }

  const endpoints = {
    summary: `/api/switch/api/billing/batches/${batchId}/summary`,
    detail: `/api/switch/api/billing/batches/${batchId}/detail`,
    history: `/api/switch/api/billing/batches/${batchId}/history`,
    charge: `/api/switch/api/billing/batches/${batchId}/charge`,
    receipt: `/api/switch/api/billing/batches/${batchId}/receipt`
  };

  try {
    const data = await api(endpoints[type]);
    $("#reportOutput").textContent = JSON.stringify(data, null, 2);
  } catch (error) {
    $("#reportOutput").textContent = error.message;
  }
}

async function runDownload(type) {
  const batchId = $("#batchIdInput").value.trim();
  if (!batchId) {
    $("#reportOutput").textContent = "Ingresa el ID del lote.";
    return;
  }

  const endpoints = {
    "receipt-pdf": `/api/switch/api/payment-batch/${batchId}/receipt`,
    "billing-novelties": `/api/switch/api/billing/batches/${batchId}/novelties`
  };
  const fileNames = {
    "receipt-pdf": `recibo_lote_${batchId}.pdf`,
    "billing-novelties": `novedades_${batchId}.csv`
  };

  try {
    await download(endpoints[type], fileNames[type]);
    $("#reportOutput").textContent = `Descarga generada: ${fileNames[type]}`;
  } catch (error) {
    $("#reportOutput").textContent = error.message;
  }
}

async function refreshCompanyData() {
  if (state.customerType !== "JURIDICO") return;
  await Promise.all([loadBatches(), loadCharges(), loadCompanyAccount()]);
}

async function refreshAll() {
  await Promise.all([loadAccounts(), loadTransactions(), refreshCompanyData()]);
}

function bindEvents() {
  $("#loginForm").addEventListener("submit", login);
  $("#logoutButton").addEventListener("click", logout);
  $("#refreshButton").addEventListener("click", refreshAll);
  $("#globalSearch").addEventListener("input", (event) => filterVisibleRows(event.target.value));
  $("#uploadForm").addEventListener("submit", uploadCsv);
  $("#loadBatchesButton").addEventListener("click", loadBatches);
  $("#csvFile").addEventListener("change", (event) => {
    $("#fileName").textContent = event.target.files[0]?.name || "Seleccionar CSV";
  });

  $$(".nav-item").forEach((button) => {
    button.addEventListener("click", () => activateSection(button.dataset.section));
  });

  document.addEventListener("click", (event) => {
    const processButton = event.target.closest("[data-process]");
    if (processButton) processBatch(processButton.dataset.process);

    const reportButton = event.target.closest("[data-report]");
    if (reportButton) runReport(reportButton.dataset.report);

    const downloadButton = event.target.closest("[data-download]");
    if (downloadButton) runDownload(downloadButton.dataset.download);
  });
}

function filterVisibleRows(query) {
  const normalized = query.trim().toLowerCase();
  document.querySelectorAll("tbody tr, .account-card").forEach((element) => {
    const matches = !normalized || element.textContent.toLowerCase().includes(normalized);
    element.classList.toggle("is-filtered", !matches);
  });
}

bindEvents();
checkServices();
restoreSession();
