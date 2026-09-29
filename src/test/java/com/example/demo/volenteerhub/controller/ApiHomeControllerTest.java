package com.example.demo.volenteerhub.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiHomeControllerTest {

    @Test
    void apiPathShowsApiIndex() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ApiHomeController()).build();

        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("VolunteerHub REST API is running."))
                .andExpect(jsonPath("$.dashboard").value("/api/dashboard/summary"));
    }
}
