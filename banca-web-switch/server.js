const http = require("http");
const fs = require("fs");
const path = require("path");

function loadEnvFile(filePath) {
  if (!fs.existsSync(filePath)) return;

  const lines = fs.readFileSync(filePath, "utf8").split(/\r?\n/);
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) continue;

    const separatorIndex = trimmed.indexOf("=");
    if (separatorIndex === -1) continue;

    const key = trimmed.slice(0, separatorIndex).trim();
    const rawValue = trimmed.slice(separatorIndex + 1).trim();
    const value = rawValue.replace(/^["']|["']$/g, "");

    if (key && process.env[key] === undefined) {
      process.env[key] = value;
    }
  }
}

loadEnvFile(path.join(__dirname, ".env"));

const PORT = Number(process.env.PORT || 5173);
const CORE_BASE_URL = process.env.CORE_BASE_URL || "http://localhost:8080";
const SWITCH_BASE_URL = process.env.SWITCH_BASE_URL || "http://localhost:8081";

const PUBLIC_DIR = path.join(__dirname, "public");

const mimeTypes = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "application/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".ico": "image/x-icon"
};

function sendJson(res, statusCode, payload) {
  res.writeHead(statusCode, { "Content-Type": "application/json; charset=utf-8" });
  res.end(JSON.stringify(payload));
}

function serveStatic(req, res) {
  const requestedPath = decodeURIComponent(new URL(req.url, `http://${req.headers.host}`).pathname);
  const safePath = requestedPath === "/" ? "/index.html" : requestedPath;
  const filePath = path.normalize(path.join(PUBLIC_DIR, safePath));

  if (!filePath.startsWith(PUBLIC_DIR)) {
    sendJson(res, 403, { error: "Ruta no permitida" });
    return;
  }

  fs.readFile(filePath, (error, data) => {
    if (error) {
      fs.readFile(path.join(PUBLIC_DIR, "index.html"), (fallbackError, fallbackData) => {
        if (fallbackError) {
          sendJson(res, 404, { error: "Archivo no encontrado" });
          return;
        }
        res.writeHead(200, { "Content-Type": mimeTypes[".html"] });
        res.end(fallbackData);
      });
      return;
    }

    const extension = path.extname(filePath).toLowerCase();
    res.writeHead(200, { "Content-Type": mimeTypes[extension] || "application/octet-stream" });
    res.end(data);
  });
}

function mapProxyTarget(req) {
  if (req.url.startsWith("/api/core/")) {
    return {
      baseUrl: CORE_BASE_URL,
      path: req.url.replace("/api/core", "")
    };
  }

  if (req.url.startsWith("/api/switch/v1")) {
    return {
      baseUrl: SWITCH_BASE_URL,
      path: req.url.replace("/api/switch/v1", "")
    };
  }

  return null;
}

function proxyRequest(req, res, target) {
  const targetUrl = new URL(target.path, target.baseUrl);
  const headers = { ...req.headers };
  delete headers.host;
  delete headers.connection;

  const proxy = http.request(
    targetUrl,
    {
      method: req.method,
      headers
    },
    (proxyRes) => {
      const responseHeaders = { ...proxyRes.headers };
      responseHeaders["Access-Control-Allow-Origin"] = "*";
      res.writeHead(proxyRes.statusCode || 500, responseHeaders);
      proxyRes.pipe(res);
    }
  );

  proxy.on("error", (error) => {
    sendJson(res, 502, {
      error: "No se pudo conectar con el servicio backend",
      detail: error.message,
      target: target.baseUrl
    });
  });

  req.pipe(proxy);
}

const server = http.createServer((req, res) => {
  if (req.method === "OPTIONS") {
    res.writeHead(204, {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET,POST,PATCH,PUT,DELETE,OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, X-Core-User-Id"
    });
    res.end();
    return;
  }

  const target = mapProxyTarget(req);
  if (target) {
    proxyRequest(req, res, target);
    return;
  }

  serveStatic(req, res);
});

server.listen(PORT, () => {
  console.log(`Banca Web Switch disponible en http://localhost:${PORT}`);
  console.log(`Core: ${CORE_BASE_URL}`);
  console.log(`Switch: ${SWITCH_BASE_URL}`);
});
