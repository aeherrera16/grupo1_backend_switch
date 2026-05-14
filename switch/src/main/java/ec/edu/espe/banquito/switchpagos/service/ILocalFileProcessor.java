package ec.edu.espe.banquito.switchpagos.service;

/**
 * Interface for local file processing services.
 * Provides methods for processing local CSV files.
 */
public interface ILocalFileProcessor {
    
    /**
     * Processes local files in the configured directory.
     * This method is typically called by a scheduled task.
     */
    void processLocalFiles();
}
