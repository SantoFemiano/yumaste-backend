package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.CartItemDTO;
import com.yumaste.yumasteapi.models.Carrello;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    // Passiamo l'intera riga del carrello (entità Carrello)
    @Mapping(source = "id", target = "idRigaCarrello") // Mappa l'ID della riga
    @Mapping(source = "box.id", target = "boxId")      // "Scava" dentro la box per prendere il suo ID
    @Mapping(source = "box.nome", target = "nomeBox")  // "Scava" dentro la box per prendere il nome
    @Mapping(source = "box.immagineUrl", target = "immagineUrl")
    @Mapping(source = "box.prezzo", target = "prezzoUnitario")
    CartItemDTO toCartItemDTO(Carrello carrello);

}