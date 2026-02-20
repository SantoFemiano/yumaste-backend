package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.repositories.testrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class testService{


    private final testrepository repo;

    public testService(testrepository repo) {
        this.repo = repo;
    }
    public Allergene findById(Long id) {
        return repo.findById(id).orElse(null);
    }
}
