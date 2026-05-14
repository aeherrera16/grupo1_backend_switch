package ec.edu.espe.banquito.switchpagos.service;

public interface IReceiptGeneratorService {
    byte[] generateReceipt(Integer batchId);
}
