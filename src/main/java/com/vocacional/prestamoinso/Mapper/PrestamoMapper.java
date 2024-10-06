package com.vocacional.prestamoinso.Mapper;

import com.vocacional.prestamoinso.DTO.ClienteDTO;
import com.vocacional.prestamoinso.DTO.CronogramaPagosDTO;
import com.vocacional.prestamoinso.DTO.PrestamoDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.Prestamo;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import javax.swing.plaf.PanelUI;
import java.util.stream.Collectors;

@Component
public class PrestamoMapper {
    private final ModelMapper modelMapper;

    public PrestamoMapper (ModelMapper modelMapper){
        this.modelMapper=modelMapper;
    }

    public PrestamoDTO toDTO(Prestamo prestamo){
        PrestamoDTO dto = modelMapper.map(prestamo, PrestamoDTO.class);
        dto.setCliente(modelMapper.map(prestamo.getCliente(), ClienteDTO.class)); // Mapea el cliente
        dto.setCronogramaPagos(prestamo.getCronogramaPagos().stream()
                .map(c -> modelMapper.map(c, CronogramaPagosDTO.class))
                .collect(Collectors.toList()));
        return dto;
    }

    public Prestamo toEntity(PrestamoDTO prestamoDTO){
        return modelMapper.map(prestamoDTO, Prestamo.class);
    }
}

