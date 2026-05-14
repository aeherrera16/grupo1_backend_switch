package ec.edu.espe.banquito.switchpagos.controller;

import ec.edu.espe.banquito.switchpagos.service.IPaymentDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174"})
@RequestMapping("/api/payment-processor")
public class PaymentDetailController {
    private final IPaymentDetailService paymentProcessorService;
    public PaymentDetailController(IPaymentDetailService paymentDetailService) {
        this.paymentProcessorService = paymentDetailService;
    }
    @PostMapping("/process/{batchId}")
    public ResponseEntity<String> processBatch(
            @PathVariable Integer batchId
    ) {

        paymentProcessorService.processBatch(batchId);

        return ResponseEntity.ok(
                "Lote procesado correctamente"
        );
    }
}
