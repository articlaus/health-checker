package com.walmart.econgo.controller;

import com.walmart.econgo.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by: Ganbat Bayar
 * On: 5/19/2019
 * Project: ServiceStatusChecker
 */
@RestController
@RequestMapping("/status")
public class StatusController {
    @Autowired
    HealthService service;

    @GetMapping("/info")
    public ResponseEntity getInfo() {
        return service.getInfo();
    }
}
