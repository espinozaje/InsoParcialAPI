package com.vocacional.prestamoinso.Mapper;

import com.vocacional.prestamoinso.DTO.TrabajadorDTO;
import com.vocacional.prestamoinso.Entity.Trabajador;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class TrabajadorMappper {
    private final ModelMapper modelMapper;

    public TrabajadorMappper(ModelMapper modelMapper){
        this.modelMapper=modelMapper;
    }

    public TrabajadorDTO toDTO(Trabajador trabajador){
        return modelMapper.map(trabajador, TrabajadorDTO.class);
    }

    public Trabajador toEntity(TrabajadorDTO trabajadorDTO){
        return modelMapper.map(trabajadorDTO, Trabajador.class);
    }
}
