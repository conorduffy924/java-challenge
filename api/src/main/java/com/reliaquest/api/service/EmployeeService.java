package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInputDto;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeService {
    private final String BASE_URL = "http://localhost:8112/api/v1/employee";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Employee> getAllEmployees() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(BASE_URL, Map.class);
            List<Map<String, Object>> data =
                    (List<Map<String, Object>>) response.getBody().get("data");
            return data.stream().map(this::mapToEmployee).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Employee> getEmployeesByNameSearch(String searchString) {
        List<Employee> all = getAllEmployees();
        return all.stream()
                .filter(e -> e.getEmployee_name() != null
                        && e.getEmployee_name().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Employee getEmployeeById(String id) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(BASE_URL + "/" + id, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return mapToEmployee(data);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getHighestSalaryOfEmployees() {
        return getAllEmployees().stream()
                .map(Employee::getEmployee_salary)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        return getAllEmployees().stream()
                .sorted(Comparator.comparing(
                        Employee::getEmployee_salary, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(Employee::getEmployee_name)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Employee createEmployee(EmployeeInputDto input) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("name", input.getName());
            body.put("salary", input.getSalary());
            body.put("age", input.getAge());
            body.put("title", input.getTitle());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(BASE_URL, request, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return mapToEmployee(data);
        } catch (HttpClientErrorException e) {
            System.out.println("Error response from mock server: " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String deleteEmployeeById(String id) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response =
                    restTemplate.exchange(BASE_URL + "/" + id, HttpMethod.DELETE, request, Map.class);
            // Assume the API returns the name in the response or true/false
            Object data = response.getBody().get("data");
            if (data instanceof Boolean && (Boolean) data) {
                Employee emp = getEmployeeById(id);
                return emp != null ? emp.getEmployee_name() : id;
            } else if (data instanceof String) {
                return (String) data;
            }
            return null;
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Employee mapToEmployee(Map<String, Object> map) {
        if (map == null) return null;
        Employee e = new Employee();
        e.setId((String) map.get("id"));
        e.setEmployee_name((String) map.get("employee_name"));
        e.setEmployee_salary(
                map.get("employee_salary") instanceof Number ? ((Number) map.get("employee_salary")).intValue() : null);
        e.setEmployee_age(
                map.get("employee_age") instanceof Number ? ((Number) map.get("employee_age")).intValue() : null);
        e.setEmployee_title((String) map.get("employee_title"));
        e.setEmployee_email((String) map.get("employee_email"));
        return e;
    }
}
