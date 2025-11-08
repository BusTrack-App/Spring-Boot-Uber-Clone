package com.mera.apirest.controllers;

import com.mera.apirest.dto.driver_position.DriverPositionRequest;
import com.mera.apirest.dto.driver_position.DriverPositionResponse;
import com.mera.apirest.models.DriverPosition;
import com.mera.apirest.services.DriverPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/drivers-position")
public class DriverPositionController {

    @Autowired
    private DriverPositionService driverPositionService;

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody DriverPositionRequest request) {
        try {
            DriverPosition response = driverPositionService.create(request);
            return ResponseEntity.ok(true);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.NOT_FOUND.value()
            ));
        }
    }



}

