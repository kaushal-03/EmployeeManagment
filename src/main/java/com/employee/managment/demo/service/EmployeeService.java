package com.employee.managment.demo.service;

import com.employee.managment.demo.EmployeeDto;
import com.employee.managment.demo.EmployeeSummary;
import com.employee.managment.demo.entity.EmployeeEntity;
import com.employee.managment.demo.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class EmployeeService {

    private EmployeeRepository employeeRepository;
    private WebClient webClient;


    public List<EmployeeEntity> getEmployees(){

    return employeeRepository.findAll();
    }

    public EmployeeEntity getEmployees(Long id){

        return employeeRepository.findByEmployeeId(id).orElseThrow(()  -> new OpenApiResourceNotFoundException("Employee not found with id: " + id));
    }

//    public EmployeeSummary getEmployeeSummary(Long id){
//
//        return employeeRepository.findByEmployeeId(id).orElseThrow();
//    }
    public EmployeeEntity insertEmployees(EmployeeEntity employeeEntity){

        return employeeRepository.save(employeeEntity);
    }

    @Transactional
    public void deleteEmployee(Long id){
        employeeRepository.deleteByEmployeeId(id);
    }

    @Transactional
    public int assignManager(Long id, int managerId){
        return  employeeRepository.updateManager(id, managerId);
    }

    @Async("excelExecutor")
    public CompletableFuture<List<String>> processExcelFile(MultipartFile file)  throws IOException {
        Map<String, Integer> headerMap = new HashMap<>();
        List<CompletableFuture<String>> futures = new ArrayList<>();
        InputStream is = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        List<String>emailData = new ArrayList<>();
        Row headerRow = rowIterator.next();
        for (Cell cell : headerRow) {
            headerMap.put(cell.getStringCellValue(), cell.getColumnIndex());
        }
        List<Map> excelData = new ArrayList<>();
        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            Cell nameCell = row.getCell(headerMap.get("mailId"));
            String email = nameCell.getStringCellValue();
            log.info(email);
            CompletableFuture<String> future = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/trigger-email")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .toFuture()
                    .handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send email to: " + email, ex);
                            return "Failed: " + email;
                        } else {
                            log.info("Email sent successfully to: " + email);
                            return "Success: " + email;
                        }
                    });

            futures.add(future);
        }
        CompletableFuture<Void> allDone = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]));

        return allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        );
    }
    }

//    @Async("notificationExecutor")
//    CompletableFuture<String> processBulkNotification(){
//
//    }


