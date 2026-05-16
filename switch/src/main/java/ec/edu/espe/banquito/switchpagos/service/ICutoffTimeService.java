package ec.edu.espe.banquito.switchpagos.service;

import java.time.LocalTime;

/**
 * RF-01: Cutoff window checks.
 */
public interface ICutoffTimeService {

    /**
     * Checks the current ingestion window.
     */
    boolean isWithinIngestionWindow();

    /**
     * Returns the configured cutoff time.
     */
    LocalTime getCutoffTime();

    /**
     * Checks a specific ingestion time.
     */
    boolean isWithinIngestionWindow(LocalTime time);
}
