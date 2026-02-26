package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.DTO.response.AllergeneDTO;
import com.yumaste.yumasteapi.mapper.AllergeneMapper;
import com.yumaste.yumasteapi.models.Allergene;
import com.yumaste.yumasteapi.models.IngredienteAllergeneId;
import com.yumaste.yumasteapi.repositories.AllergeneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllergeneService {
    private final AllergeneRepository allergeneRepository;
    private final AllergeneMapper allergeneMapper;

        public List<AllergeneDTO> getAllAllergeni() {
            List<Allergene> allergeni = allergeneRepository.findAll();
            return allergeni.stream()
                    .map(allergeneMapper::toDto)
                    .collect(Collectors.toList());
        }





}
