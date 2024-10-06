package com.vocacional.prestamoinso.Mapper;


import com.vocacional.prestamoinso.DTO.ClienteDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    private final ModelMapper modelMapper;

    public ClienteMapper (ModelMapper modelMapper){
        this.modelMapper=modelMapper;
    }

    public ClienteDTO toDTO (Cliente cliente){
        return modelMapper.map(cliente,ClienteDTO.class);
    }

    public Cliente toEntity(ClienteDTO clienteDTO){
        return modelMapper.map(clienteDTO, Cliente.class);
    }
}
