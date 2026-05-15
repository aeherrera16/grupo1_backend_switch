package ec.edu.espe.banquito.switchpagos.controller;


import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/switch/v1/switch")
public class SwitchPagosController {



    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "switch-pagos",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

}
