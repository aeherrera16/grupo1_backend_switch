package ec.edu.espe.banquito.switchpagos.service;

import java.time.LocalTime;

public interface ICutoffTimeService {

    boolean isWithinIngestionWindow();

    LocalTime getCutoffTime();

    boolean isWithinIngestionWindow(LocalTime time);
}
