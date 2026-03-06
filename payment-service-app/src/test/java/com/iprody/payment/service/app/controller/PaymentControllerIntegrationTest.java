package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.AbstractPostgresIntegrationTest;
import com.iprody.payment.service.app.TestJwtFactory;
import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PaymentControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnOnlyLiquibasePayments() throws Exception {
        mockMvc.perform(get("/payments/search")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "reader"))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000001')]").exists())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000002')]").exists())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000003')]").exists());
    }

    @Test
    void shouldCreatePaymentAndVerifyInDatabase() throws Exception {
        final Instant now = Instant.parse("2026-01-01T10:15:30Z");

        final PaymentDto dto = PaymentDto.builder()
                .guid(UUID.fromString("00000000-0000-0000-0000-000000000004"))
                .inquiryRefId(UUID.fromString("10000000-0000-0000-0000-000000000004"))
                .amount(new BigDecimal("123.45"))
                .currency("EUR")
                .status(PaymentStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .note("integration test payment")
                .build();

        final String json = objectMapper.writeValueAsString(dto);

        final String response = mockMvc.perform(post("/payments")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guid").exists())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(123.45))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final PaymentDto created = objectMapper.readValue(response, PaymentDto.class);
        final Optional<Payment> saved = paymentRepository.findById(created.getGuid());

        assertThat(saved).isPresent();
        assertThat(saved.get().getCurrency()).isEqualTo("EUR");
        assertThat(saved.get().getAmount()).isEqualByComparingTo("123.45");
        assertThat(saved.get().getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.get().getNote()).isEqualTo("integration test payment");
    }

    @Test
    void shouldReturnPaymentById() throws Exception {
        final UUID existingId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        mockMvc.perform(get("/payments/{id}", existingId)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "reader"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(existingId.toString()))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void shouldReturn404ForNonexistentPayment() throws Exception {
        final UUID nonexistentId = UUID.randomUUID();

        mockMvc.perform(get("/payments/{id}", nonexistentId)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "reader"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldUpdatePayment() throws Exception {
        final UUID existingId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        final Instant now = Instant.parse("2026-02-02T12:00:00Z");

        final PaymentDto dto = PaymentDto.builder()
                .guid(existingId)
                .inquiryRefId(UUID.fromString("10000000-0000-0000-0000-000000000004"))
                .amount(new BigDecimal("999.99"))
                .currency("USD")
                .status(PaymentStatus.RECEIVED)
                .createdAt(now)
                .updatedAt(now)
                .note("updated payment")
                .build();

        final String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/payments/{id}", existingId)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(existingId.toString()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(999.99))
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        final Payment saved = paymentRepository.findById(existingId).orElseThrow();
        assertThat(saved.getCurrency()).isEqualTo("USD");
        assertThat(saved.getAmount()).isEqualByComparingTo("999.99");
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.RECEIVED);
        assertThat(saved.getNote()).isEqualTo("updated payment");
    }

    @Test
    void shouldUpdatePaymentStatus() throws Exception {
        final UUID existingId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(patch("/payments/{id}/status", existingId)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "status": "RECEIVED"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(existingId.toString()))
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        final Payment saved = paymentRepository.findById(existingId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.RECEIVED);
    }

    @Test
    void shouldUpdatePaymentNote() throws Exception {
        final UUID existingId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(patch("/payments/{id}/note", existingId)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "note": "manually reviewed"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(existingId.toString()))
                .andExpect(jsonPath("$.note").value("manually reviewed"));

        final Payment saved = paymentRepository.findById(existingId).orElseThrow();
        assertThat(saved.getNote()).isEqualTo("manually reviewed");
    }

    @Test
    void shouldDeletePayment() throws Exception {
        final Instant now = Instant.parse("2026-03-03T10:00:00Z");

        final PaymentDto dto = PaymentDto.builder()
                .guid(UUID.fromString("00000000-0000-0000-0000-000000000006"))
                .inquiryRefId(UUID.fromString("10000000-0000-0000-0000-000000000006"))
                .amount(new BigDecimal("10.00"))
                .currency("EUR")
                .status(PaymentStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .note("to be deleted")
                .build();

        final String createResponse = mockMvc.perform(post("/payments")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final PaymentDto created = objectMapper.readValue(createResponse, PaymentDto.class);

        mockMvc.perform(delete("/payments/{id}", created.getGuid())
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "admin")))
                .andExpect(status().isNoContent());

        assertThat(paymentRepository.findById(created.getGuid())).isEmpty();
    }

    @Test
    void shouldForbidReaderToCreatePayment() throws Exception {
        final Instant now = Instant.parse("2026-01-01T10:15:30Z");

        final PaymentDto dto = PaymentDto.builder()
                .guid(UUID.fromString("00000000-0000-0000-0000-000000000005"))
                .inquiryRefId(UUID.fromString("10000000-0000-0000-0000-000000000005"))
                .amount(new BigDecimal("123.45"))
                .currency("EUR")
                .status(PaymentStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        mockMvc.perform(post("/payments")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "reader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbidReaderToDeletePayment() throws Exception {
        final UUID existingId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(delete("/payments/{id}", existingId)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user", "reader")))
                .andExpect(status().isForbidden());
    }
}
