package ec.edu.espe.banquito.switchpagos.service;

import java.time.LocalTime;

/**
 * Interfaz para el servicio de manejo de horarios de corte.
 * Proporciona métodos para verificar y gestionar ventanas de ingesta.
 */
public interface ICutoffTimeService {
    
    /**
     * Verifica si el tiempo actual está dentro de la ventana de ingesta.
     * 
     * @return true si está antes de la hora de corte, false otherwise
     */
    boolean isWithinIngestionWindow();
    
    /**
     * Obtiene la hora de corte configurada.
     * 
     * @return LocalTime con la hora de corte
     */
    LocalTime getCutoffTime();
    
    /**
     * Verifica si un tiempo específico está dentro de la ventana de ingesta.
     * 
     * @param time tiempo a verificar
     * @return true si está antes de la hora de corte, false otherwise
     */
    boolean isWithinIngestionWindow(LocalTime time);
}
