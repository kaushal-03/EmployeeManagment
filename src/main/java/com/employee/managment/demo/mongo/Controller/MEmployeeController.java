package com.employee.managment.demo.mongo.Controller;

import com.employee.managment.demo.mongo.DTO.FetchDTO;
import com.employee.managment.demo.mongo.DTO.LocationDTO;
import com.employee.managment.demo.mongo.DTO.MEmployeeDTO;
import com.employee.managment.demo.mongo.Entity.MEmployeeEntity;
import com.employee.managment.demo.mongo.Service.MEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/employee-mongo")
public class MEmployeeController {

    @Autowired
    MEmployeeService mEmployeeService;
    @Autowired
    Jedis redisClient;

    @PostMapping("/insertEmployee")
    ResponseEntity<Map> saveEmployee(@RequestBody MEmployeeDTO employee) {

        String id = "EMP_" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Point locationData = new Point(employee.getLocation().getX(), employee.getLocation().getY());
        MEmployeeEntity entity = new MEmployeeEntity();
        entity.setId(id);
        entity.setName(employee.getName());
        entity.setDepartment(employee.getDepartment());
        entity.setPhoneNumber(employee.getPhoneNumber());
        entity.setEmail(employee.getEmail());
        entity.setLocation(locationData);
        entity.setMetroStations(employee.getMetroStations());
        MEmployeeEntity data = mEmployeeService.saveEmployee(entity);
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fetchNearByRides")
    ResponseEntity<Map> fetchEmployees(@RequestParam double distance, @RequestParam double x, @RequestParam double y) {
        Map<String, Object> response = new HashMap<>();
        FetchDTO dto = new FetchDTO();
        dto.setDistance(distance);

        LocationDTO location = new LocationDTO();
        location.setX(x);
        location.setY(y);
        dto.setLocation(location);

        List<MEmployeeEntity> employeeData = mEmployeeService.findNearByRiders(dto);

        response.put("statusCode", HttpStatus.OK.value());
        response.put("data", employeeData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("fetchByMetro")
    ResponseEntity<Map> fetchByMetro(@RequestParam List<String> stations) {
        Map<String, Object> response = new HashMap<>();
        List<MEmployeeEntity> data = mEmployeeService.findByMetroStations(stations);
        response.put("statusCode", HttpStatus.OK.value());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/create-session")
    ResponseEntity<Map> sessionCreation() {
        Map<String, Object> response = new HashMap<>();
        try {
            UUID uuid = UUID.randomUUID();
            redisClient.setex(uuid.toString(), 60, "ACTIVE");
            response.put("statusCode", HttpStatus.OK.value());
            response.put("sessionId", uuid.toString());
            response.put("message", "Session created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Failed to create session");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/eventbriteoauth")
    void eventbriteOauth(@RequestParam Map<String, String> params) {
        params.forEach((key, value) -> {
            System.out.println(key + " = " + value);
        });
    }

    @PostMapping("/eventbrite-registration")
    public void registerEvent(@RequestBody Map<String, Object> body) {
        System.out.println("Body as map: " + body);
    }

}

