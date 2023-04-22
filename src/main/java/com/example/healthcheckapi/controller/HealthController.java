package com.example.healthcheckapi.controller;

import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {


    @Autowired
    private StatsDClient statsDClient;

    private final static Logger logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/healthz")
    public void healthCheck() {
        statsDClient.incrementCounter("endpoint.healthz.get");
        logger.info("endpoint.healthz.get hit successfully");
    }
    
    @GetMapping("/health/ankita")
    public void healthCheck1() {
        statsDClient.incrementCounter("endpoint.healthz.get");
        logger.info("endpoint.healthz.get hit successfully");
    }
}
