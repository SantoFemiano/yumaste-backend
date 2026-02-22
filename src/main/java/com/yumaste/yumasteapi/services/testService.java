package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.models.Box;
import com.yumaste.yumasteapi.repositories.BoxRepository;
import com.yumaste.yumasteapi.repositories.testrepository;

import org.springframework.stereotype.Service;



@Service
public class testService{


    private final testrepository repo;
    private final BoxRepository boxRepo;

    //Repo injection
    public testService(testrepository repo, BoxRepository boxRepo) {
        this.repo = repo;
        this.boxRepo = boxRepo;
    }


    public Allergene findById(Long id) {
        return repo.findById(id).orElse(null);



    }
    public Box findByIdBox(Long id) {
        return boxRepo.findById(id).orElse(null);
    }
}
