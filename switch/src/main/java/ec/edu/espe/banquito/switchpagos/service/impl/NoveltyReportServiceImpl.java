package ec.edu.espe.banquito.switchpagos.service.impl;

import ec.edu.espe.banquito.switchpagos.service.INoveltyReportService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class NoveltyReportServiceImpl implements INoveltyReportService {

    private final BillingService billingService;

    public NoveltyReportServiceImpl(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public byte[] generateReport(Integer batchId) {

        String csv =
                billingService
                        .generateNoveltyReportCsv(batchId);

        return csv.getBytes(StandardCharsets.UTF_8);
    }
}
