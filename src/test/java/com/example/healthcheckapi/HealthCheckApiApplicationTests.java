package com.example.healthcheckapi;

import com.example.healthcheckapi.controller.HealthController;
import com.timgroup.statsd.StatsDClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(HealthController.class)
class HealthCheckApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private StatsDClient statsDClient;

    @Test
    public void testHealthCheck() throws Exception
    {
        mockMvc.perform(get("/healthz")).andExpect(status().isOk());
    }

}
