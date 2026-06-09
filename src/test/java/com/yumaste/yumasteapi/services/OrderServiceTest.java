package com.yumaste.yumasteapi.services;

import com.yumaste.yumasteapi.dto.request.CheckoutRequestDTO;
import com.yumaste.yumasteapi.dto.response.CartDTO;
import com.yumaste.yumasteapi.dto.response.CartItemDTO;
import com.yumaste.yumasteapi.dto.response.OrdineResponseDTO;
import com.yumaste.yumasteapi.dto.response.OrdiniDettagliDTO;
import com.yumaste.yumasteapi.exceptions.BusinessException;
import com.yumaste.yumasteapi.exceptions.ResourceNotFoundException;
import com.yumaste.yumasteapi.exceptions.UnauthorizedException;
import com.yumaste.yumasteapi.mapper.OrderDettagliMapper;
import com.yumaste.yumasteapi.models.*;
import com.yumaste.yumasteapi.repositories.*;
import com.yumaste.yumasteapi.services.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private CartService cartService;
    @Mock private CartRepository cartRepository;
    @Mock private BoxRepository boxRepository;
    @Mock private OrdineRepository ordineRepository;
    @Mock private DettaglioOrdineRepository dettaglioOrdineRepository;
    @Mock private SpedizioneRepository spedizioneRepository;
    @Mock private FatturaRepository fatturaRepository;
    @Mock private IndirizzoUtenteRepository indirizzoRepository;
    @Mock private OrderDettagliMapper orderDettagliMapper;
    @Mock private EmailService emailService;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private Utente utente;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@yumaste.it");
    }

    // =========================================================
    // checkout
    // =========================================================

    @Test
    @DisplayName("checkout - carrello vuoto lancia BusinessException")
    void checkout_emptyCart_throwsBusinessException() {
        CartDTO emptyCart = new CartDTO(List.of(), BigDecimal.ZERO, 0);
        when(cartService.getCarrelloDellUtente(utente)).thenReturn(emptyCart);

        CheckoutRequestDTO req = mock(CheckoutRequestDTO.class);

        assertThatThrownBy(() -> orderService.checkout(utente, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("carrello è vuoto");
    }

    @Test
    @DisplayName("checkout - indirizzo non trovato lancia ResourceNotFoundException")
    void checkout_addressNotFound_throwsResourceNotFoundException() {
        CartItemDTO item = new CartItemDTO(10L, "Box Test", 2, BigDecimal.TEN, BigDecimal.TEN, null);
        CartDTO cart = new CartDTO(List.of(item), BigDecimal.TEN, 2);
        when(cartService.getCarrelloDellUtente(utente)).thenReturn(cart);

        CheckoutRequestDTO req = mock(CheckoutRequestDTO.class);
        when(req.indirizzoId()).thenReturn(99L);
        when(indirizzoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout(utente, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("checkout - flusso completo: crea ordine, spedizione, fattura e ritorna DTO")
    void checkout_happyPath_returnsOrdineResponseDTO() {
        CartItemDTO item = new CartItemDTO(10L, "Box A", 1, BigDecimal.TEN, BigDecimal.TEN, null);
        CartDTO cart = new CartDTO(List.of(item), BigDecimal.TEN, 1);
        when(cartService.getCarrelloDellUtente(utente)).thenReturn(cart);

        IndirizzoUtente indirizzo = new IndirizzoUtente();
        indirizzo.setUtente(utente);
        indirizzo.setVia("Via Roma"); indirizzo.setCivico("1");
        indirizzo.setCap("00100"); indirizzo.setCitta("Roma"); indirizzo.setProvincia("RM");
        CheckoutRequestDTO req = mock(CheckoutRequestDTO.class);
        when(req.indirizzoId()).thenReturn(1L);
        when(req.metodoPagamento()).thenReturn("CARTA");
        when(indirizzoRepository.findById(1L)).thenReturn(Optional.of(indirizzo));

        Box box = new Box(); box.setId(10L);
        when(boxRepository.findAllById(List.of(10L))).thenReturn(List.of(box));

        Ordine ordineSalvato = new Ordine();
        ordineSalvato.setId(100L);
        ordineSalvato.setCodiceOrdine("ORD-ABCD1234");
        ordineSalvato.setDataOrdine(Instant.now());
        ordineSalvato.setTotalePrezzo(BigDecimal.TEN);
        ordineSalvato.setStatoOrdine("IN_ATTESA");
        ordineSalvato.setUtente(utente);
        when(ordineRepository.save(any(Ordine.class))).thenReturn(ordineSalvato);

        Spedizione spedizione = new Spedizione();
        spedizione.setStatoSpedizione("IN_PREPARAZIONE");
        when(spedizioneRepository.save(any(Spedizione.class))).thenReturn(spedizione);
        when(fatturaRepository.save(any(Fattura.class))).thenReturn(new Fattura());
        when(dettaglioOrdineRepository.saveAll(anyList())).thenReturn(List.of());
        when(cartRepository.findByUtente(utente)).thenReturn(List.of());
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);

        OrdineResponseDTO result = orderService.checkout(utente, req);

        assertThat(result).isNotNull();
        assertThat(result.codiceOrdine()).isEqualTo("ORD-ABCD1234");
        assertThat(result.statoOrdine()).isEqualTo("IN_ATTESA");
        assertThat(result.nomeUtente()).isEqualTo("Mario");
        verify(ordineRepository).save(any(Ordine.class));
        verify(spedizioneRepository).save(any(Spedizione.class));
        verify(fatturaRepository).save(any(Fattura.class));
        verify(kafkaTemplate).send(eq("order-created-topic"), any());
    }

    @Test
    @DisplayName("checkout - box non trovata nella mappa lancia ResourceNotFoundException")
    void checkout_boxNotFound_throwsResourceNotFoundException() {
        CartItemDTO item = new CartItemDTO(99L, "Missing", 1, BigDecimal.TEN, BigDecimal.TEN, null);
        CartDTO cart = new CartDTO(List.of(item), BigDecimal.TEN, 1);
        when(cartService.getCarrelloDellUtente(utente)).thenReturn(cart);

        IndirizzoUtente indirizzo = new IndirizzoUtente();
        indirizzo.setUtente(utente);
        indirizzo.setVia("Via Roma"); indirizzo.setCivico("1");
        indirizzo.setCap("00100"); indirizzo.setCitta("Roma"); indirizzo.setProvincia("RM");
        CheckoutRequestDTO req = mock(CheckoutRequestDTO.class);
        when(req.indirizzoId()).thenReturn(1L);
        when(req.metodoPagamento()).thenReturn("CARTA");
        when(indirizzoRepository.findById(1L)).thenReturn(Optional.of(indirizzo));
        when(boxRepository.findAllById(List.of(99L))).thenReturn(List.of()); // box assente

        Ordine ordineSalvato = new Ordine();
        ordineSalvato.setId(1L); ordineSalvato.setUtente(utente);
        when(ordineRepository.save(any(Ordine.class))).thenReturn(ordineSalvato);

        assertThatThrownBy(() -> orderService.checkout(utente, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Box non trovata");
    }

    // =========================================================
    // findAllOrdini (utente)
    // =========================================================

    @Test
    @DisplayName("findAllOrdini(utente) - restituisce lista ordini mappata")
    void findAllOrdini_byUtente_returnsMappedList() {
        Ordine ordine = buildOrdine();
        when(ordineRepository.findByUtente(utente)).thenReturn(List.of(ordine));
        when(spedizioneRepository.findByOrdine(ordine)).thenReturn(Optional.empty());

        List<OrdineResponseDTO> result = orderService.findAllOrdini(utente);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).codiceOrdine()).isEqualTo("ORD-TEST");
    }

    // =========================================================
    // findAllOrdini (admin)
    // =========================================================

    @Test
    @DisplayName("findAllOrdini() admin - restituisce tutti gli ordini")
    void findAllOrdini_admin_returnsAll() {
        Ordine ordine = buildOrdine();
        when(ordineRepository.findAll()).thenReturn(List.of(ordine));
        when(spedizioneRepository.findByOrdine(ordine)).thenReturn(Optional.empty());

        List<OrdineResponseDTO> result = orderService.findAllOrdini();

        assertThat(result).hasSize(1);
    }

    // =========================================================
    // getDettagliOrdineAdmin
    // =========================================================

    @Test
    @DisplayName("getDettagliOrdineAdmin - ordine non trovato lancia ResourceNotFoundException")
    void getDettagliOrdineAdmin_notFound_throws() {
        when(ordineRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getDettagliOrdineAdmin(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getDettagliOrdineAdmin - flusso OK: mappa e ritorna lista dettagli")
    void getDettagliOrdineAdmin_happyPath_returnsList() {
        Ordine ordine = buildOrdine();
        Spedizione spedizione = new Spedizione();
        Fattura fattura = new Fattura();
        DettaglioOrdine det = new DettaglioOrdine();
        OrdiniDettagliDTO dto = mock(OrdiniDettagliDTO.class);

        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));
        when(spedizioneRepository.findByOrdine(ordine)).thenReturn(Optional.of(spedizione));
        when(fatturaRepository.findByOrdine(ordine)).thenReturn(Optional.of(fattura));
        when(dettaglioOrdineRepository.findByOrdine_Id(1L)).thenReturn(List.of(det));
        when(orderDettagliMapper.toDto(ordine, det, fattura, spedizione)).thenReturn(dto);

        List<OrdiniDettagliDTO> result = orderService.getDettagliOrdineAdmin(1L);

        assertThat(result).containsExactly(dto);
    }

    // =========================================================
    // getDettagliOrdini (utente)
    // =========================================================

    @Test
    @DisplayName("getDettagliOrdini - ordine appartiene ad altro utente: lancia UnauthorizedException")
    void getDettagliOrdini_wrongUser_throwsUnauthorized() {
        Utente altroUtente = new Utente(); altroUtente.setId(999L);
        Ordine ordine = buildOrdine();
        ordine.setUtente(altroUtente);
        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));

        assertThatThrownBy(() -> orderService.getDettagliOrdini(utente, 1L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("getDettagliOrdini - flusso OK: ritorna dettagli ordine utente")
    void getDettagliOrdini_happyPath_returnsList() {
        Ordine ordine = buildOrdine();
        Spedizione spedizione = new Spedizione();
        Fattura fattura = new Fattura();
        DettaglioOrdine det = new DettaglioOrdine();
        OrdiniDettagliDTO dto = mock(OrdiniDettagliDTO.class);

        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));
        when(spedizioneRepository.findByOrdine(ordine)).thenReturn(Optional.of(spedizione));
        when(fatturaRepository.findByOrdine(ordine)).thenReturn(Optional.of(fattura));
        when(dettaglioOrdineRepository.findByOrdine_Id(1L)).thenReturn(List.of(det));
        when(orderDettagliMapper.toDto(ordine, det, fattura, spedizione)).thenReturn(dto);

        List<OrdiniDettagliDTO> result = orderService.getDettagliOrdini(utente, 1L);

        assertThat(result).containsExactly(dto);
    }

    // =========================================================
    // updateStatoOrdine
    // =========================================================

    @Test
    @DisplayName("updateStatoOrdine - ordine non trovato lancia ResourceNotFoundException")
    void updateStatoOrdine_notFound_throws() {
        when(ordineRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.updateStatoOrdine(1L, "SPEDITO", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateStatoOrdine - aggiorna solo statoOrdine (senza spedizione)")
    void updateStatoOrdine_onlyOrderStatus_updatesAndReturns() {
        Ordine ordine = buildOrdine();
        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));
        when(ordineRepository.save(ordine)).thenReturn(ordine);
        when(spedizioneRepository.findByOrdine(ordine)).thenReturn(Optional.empty());

        OrdineResponseDTO result = orderService.updateStatoOrdine(1L, "SPEDITO", null);

        assertThat(result.statoOrdine()).isEqualTo("SPEDITO");
        verify(spedizioneRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatoOrdine - aggiorna anche statoSpedizione")
    void updateStatoOrdine_withShippingStatus_updatesBoth() {
        Ordine ordine = buildOrdine();
        Spedizione spedizione = new Spedizione();
        spedizione.setStatoSpedizione("IN_PREPARAZIONE");

        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));
        when(spedizioneRepository.findByOrdine(ordine)).thenReturn(Optional.of(spedizione));
        when(ordineRepository.save(ordine)).thenReturn(ordine);

        OrdineResponseDTO result = orderService.updateStatoOrdine(1L, "CONSEGNATO", "CONSEGNATA");

        assertThat(result.statoOrdine()).isEqualTo("CONSEGNATO");
        verify(spedizioneRepository).save(spedizione);
    }

    @Test
    @DisplayName("updateStatoOrdine - statoOrdine blank non aggiorna lo stato")
    void updateStatoOrdine_blankStatus_doesNotChangeStatus() {
        Ordine ordine = buildOrdine();
        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));
        when(ordineRepository.save(ordine)).thenReturn(ordine);
        when(spedizioneRepository.findByOrdine(ordine)).thenReturn(Optional.empty());

        OrdineResponseDTO result = orderService.updateStatoOrdine(1L, "   ", null);

        assertThat(result.statoOrdine()).isEqualTo("IN_ATTESA");
    }

    // =========================================================
    // Helper
    // =========================================================

    private Ordine buildOrdine() {
        Ordine o = new Ordine();
        o.setId(1L);
        o.setCodiceOrdine("ORD-TEST");
        o.setDataOrdine(Instant.now());
        o.setTotalePrezzo(BigDecimal.TEN);
        o.setStatoOrdine("IN_ATTESA");
        o.setUtente(utente);
        return o;
    }
}
