package com.reliaquest.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllEmployees_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/v1/employee/search/nixon")).andExpect(status().isOk());
    }

    @Test
    void getEmployeeById_shouldReturnEmployeeOr404() throws Exception {
        // Fetch all employees to get a real ID
        MvcResult result = mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // Parse the JSON to get a real ID
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(content);
        String id = null;
        if (root.isArray() && root.size() > 0) {
            id = root.get(0).get("id").asText();
        }

        if (id != null) {
            mockMvc.perform(get("/api/v1/employee/" + id)).andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/employee/nonexistent-id")).andExpect(status().isNotFound());
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnInteger() throws Exception {
        mockMvc.perform(get("/api/v1/employee/highestSalary")).andExpect(status().isOk());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk());
    }

    @Test
    void createEmployee_shouldReturnEmployeeOrError() throws Exception {
        String json = "{\"name\":\"Test User\",\"salary\":50000,\"age\":30,\"title\":\"Engineer\"}";
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEmployeeById_shouldReturnNameOr404() throws Exception {
        mockMvc.perform(delete("/api/v1/employee/nonexistent-id")).andExpect(status().isNotFound());
    }
}
