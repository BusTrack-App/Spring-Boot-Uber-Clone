package com.mera.apirest.controllers;

import com.mera.apirest.dto.client_request.*;
import com.mera.apirest.services.ClientRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("client-requests")
public class ClientRequestController {

    @Autowired
    private ClientRequestService clientRequestService;

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody ClientRequestDTO request) {
        try {
            Long id = clientRequestService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(id);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

    @GetMapping(value = "/{originLat}/{originLng}/{destinationLat}/{destinationLng}")
    public ResponseEntity<?> getTimeAndDistance(
            @PathVariable double originLat,
            @PathVariable double originLng,
            @PathVariable double destinationLat,
            @PathVariable double destinationLng
    ) {
        try {
            DistanceMatrixResponse response = clientRequestService.getTimeAndDistance(
                    originLat,
                    originLng,
                    destinationLat,
                    destinationLng
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

    @GetMapping(value = "/{driverLat}/{driverLng}")
    public ResponseEntity<?> findNearbyClientRequest(@PathVariable double driverLat,  @PathVariable double driverLng) {
        try {
            List<NearbyClientRequestResponse> response = clientRequestService.findNearbyClientRequest(driverLat, driverLng);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getByClientRequest(@PathVariable Long id) {
        try {
            ClientRequestResponse response = clientRequestService.getByClientRequest(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }


    @PutMapping(value = "/updateDriverAssigned")
    public ResponseEntity<?> updateDriverAssigned(@RequestBody AssignDriverRequestDTO request) {
        try {
            boolean response = clientRequestService.updateDriverAssigned(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage(),
                    "statusCode", HttpStatus.BAD_REQUEST.value()
            ));
        }
    }

}
