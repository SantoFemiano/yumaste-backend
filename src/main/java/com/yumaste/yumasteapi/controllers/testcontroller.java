package com.yumaste.yumasteapi.controllers;

import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.services.testService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api")
public class testcontroller {

    private final testService ser;

    public testcontroller(testService ser) {
        this.ser = ser;
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<Allergene> test(@PathVariable Long id) {
        Allergene allergene = ser.findById(id);
        if (allergene != null) {
            return ResponseEntity.ok(allergene);
        } else {
            return ResponseEntity.notFound().build();
        }

    }
}
