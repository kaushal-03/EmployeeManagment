package com.employee.managment.demo.controller;

import com.employee.managment.demo.EmployeeDto;
import com.employee.managment.demo.EmployeeSummary;
import com.employee.managment.demo.entity.EmployeeEntity;
import com.employee.managment.demo.enums.Designation;
import com.employee.managment.demo.service.EmployeeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/employee")
//@AllArgsConstructor
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @GetMapping("/all")
    public ResponseEntity<?> getEmployees(){

//        logger.info("Request Started");
        List<EmployeeEntity> employeeEntityList =  employeeService.getEmployees();

//        log.info("Request Completed");
    return ResponseEntity.ok(employeeEntityList);
    }

    @GetMapping(path = "/employee-by-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeEntity> getEmployees(@PathVariable("id") Long id){

//        logger.info("Request Started");
        EmployeeEntity employeeEntity =  employeeService.getEmployees(id);

//        log.info("Request Completed");
        return ResponseEntity.ok(employeeEntity);
    }


//    @GetMapping(path = "/employee-summary-by-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<EmployeeSummary> getEmployeeSummary(@PathVariable("id") Long id){
//
////        logger.info("Request Started");
//        EmployeeSummary employeeSummary =  employeeService.getEmployeeSummary(id);
//
////        log.info("Request Completed");
//        return ResponseEntity.ok(employeeSummary);
//    }
    @PostMapping("/insert")
    public ResponseEntity<EmployeeEntity> insertEmployee(@RequestBody EmployeeEntity employeeEntity,@RequestParam Designation designation){

//        logger.info("Request Started");
        EmployeeEntity employeeEntityResult =  employeeService.insertEmployees(employeeEntity);

//        log.info("Request Completed");
        return ResponseEntity.ok(employeeEntityResult);
    }

    @DeleteMapping("delete-by-id")
    public ResponseEntity<String> deleteEmployee(@RequestParam Long id){
        EmployeeEntity employeeEntity =  employeeService.getEmployees(id);
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok("Employee deleted successfully.");
    }

    @PutMapping("assign-manager")
    public ResponseEntity<Map> assignManager(@RequestParam Long id, @RequestParam int managerId){
        EmployeeEntity employeeEntity =  employeeService.getEmployees(id);
        employeeService.assignManager(id, managerId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manager updated successfully");
        response.put("statusCode", HttpStatus.OK.value());
        //response.put("employeeId", updated.getEmployeeId());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "process-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public CompletableFuture<ResponseEntity<List<String>>> processExcelFile(@RequestBody MultipartFile file) throws IOException {

        return employeeService.processExcelFile(file)
                .thenApply(results -> ResponseEntity.ok(results));
    }

}

