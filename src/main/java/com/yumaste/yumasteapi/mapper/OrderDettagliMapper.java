package com.yumaste.yumasteapi.mapper;

import com.yumaste.yumasteapi.DTO.response.OrdiniDettagliDTO;
import com.yumaste.yumasteapi.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderDettagliMapper {

    // Passiamo tutte le entità necessarie al metodo!
    @Mapping(source = "ordine.id", target = "ordineid")
    @Mapping(source = "box.quantita", target = "quantita")

    @Mapping(source = "box.id", target = "boxid")
    @Mapping(source = "box.prezzoUnitario", target = "prezzounitario")

    @Mapping(source = "fattura.metodoPagamento", target = "metodopagamento")
    @Mapping(source = "fattura.dataPagamento", target = "datapagamento")
    @Mapping(source = "fattura.importo", target = "importo")

    @Mapping(source = "spedizione.corriere", target = "corriere")
    @Mapping(source = "spedizione.statoSpedizione", target = "statospedizione")

    // 👇 MAPPATURA NESTED: Prendiamo i campi da spedizione e li mettiamo dentro l'oggetto indirizzoresponsedto
    @Mapping(source = "spedizione.via", target = "indirizzoresponsedto.via")
    @Mapping(source = "spedizione.civico", target = "indirizzoresponsedto.civico")
    @Mapping(source = "spedizione.cap", target = "indirizzoresponsedto.cap")
    @Mapping(source = "spedizione.citta", target = "indirizzoresponsedto.citta")
    @Mapping(source = "spedizione.provincia", target = "indirizzoresponsedto.provincia")
    @Mapping(target="indirizzoresponsedto.stato", ignore=true)
    @Mapping(target="indirizzoresponsedto.id", ignore=true)
    @Mapping(source = "box.box.nome", target = "nomeBox")
    OrdiniDettagliDTO toDto(Ordine ordine, DettaglioOrdine box, Fattura fattura, Spedizione spedizione);
}