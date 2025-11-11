package com.mera.apirest.controllers;

import com.mera.apirest.dto.driver_car_info.DriverCarInfoDTO;
import com.mera.apirest.services.DriverCarInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/driver-car-info")
public class DriverCarInfoController {

    @Autowired
    private DriverCarInfoService driverCarInfoService;

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody DriverCarInfoDTO request) {
        try {
            DriverCarInfoDTO response = driverCarInfoService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

    @GetMapping(value = "/{idDriver}")
    public ResponseEntity<?> findByIdDriver(@PathVariable Long idDriver) {
        try {
            DriverCarInfoDTO response = driverCarInfoService.findByIdDriver(idDriver);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

}
