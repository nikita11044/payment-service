package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistency.PaymentFilterFactory;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID guid;
    private Payment payment;
    private PaymentDto paymentDto;

    @BeforeEach
    void setUp() {
        guid = UUID.randomUUID();

        payment = new Payment();
        payment.setGuid(guid);
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setTransactionRefId(UUID.randomUUID());
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setNote("initial note");
        payment.setCreatedAt(Instant.parse("2025-01-10T10:15:30Z"));
        payment.setUpdatedAt(Instant.parse("2025-01-10T10:15:31Z"));

        paymentDto = PaymentDto.builder()
                .guid(payment.getGuid())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .note(payment.getNote())
                .build();
    }

    @Test
    void get_shouldReturnPaymentById_whenExists() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        final PaymentDto result = paymentService.get(guid);

        // then
        assertEquals(guid, result.getGuid());
        assertEquals("USD", result.getCurrency());
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        verify(paymentRepository).findById(guid);
        verify(paymentMapper).toDto(payment);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void get_shouldThrowEntityNotFoundException_whenMissing() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException ex =
            assertThrows(EntityNotFoundException.class, () -> paymentService.get(guid));

        // then
        assertEquals("Payment not found for id: " + guid, ex.getMessage());
        verify(paymentRepository).findById(guid);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @ParameterizedTest
    @MethodSource("statusProvider")
    void get_shouldReturnPaymentWithDifferentStatuses(PaymentStatus status) {
        // given
        payment.setStatus(status);
        final  PaymentDto mapped = PaymentDto.builder()
            .guid(payment.getGuid())
            .currency(payment.getCurrency())
            .status(status)
            .build();

        when(paymentRepository.findById(guid)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(mapped);

        // when
        final PaymentDto result = paymentService.get(guid);

        // then
        assertEquals(status, result.getStatus());
        verify(paymentRepository).findById(guid);
        verify(paymentMapper).toDto(payment);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    static Stream<PaymentStatus> statusProvider() {
        return Stream.of(
                PaymentStatus.RECEIVED,
                PaymentStatus.PENDING,
                PaymentStatus.APPROVED,
                PaymentStatus.DECLINED,
                PaymentStatus.NOT_SENT
        );
    }

    @ParameterizedTest
    @MethodSource("filterProvider")
    void search_shouldFilterByDifferentCriteria(PaymentFilter filter) {
        // given
        final Pageable pageable = PageRequest.of(0, 25);

        @SuppressWarnings("unchecked")
        final Specification<Payment> spec = (Specification<Payment>) mock(Specification.class);
        final Page<Payment> paymentsPage = new PageImpl<>(List.of(payment), pageable, 1);

        try (MockedStatic<PaymentFilterFactory> mocked = mockStatic(PaymentFilterFactory.class)) {
            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);
            when(paymentRepository.findAll(eq(spec), eq(pageable))).thenReturn(paymentsPage);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            final Page<PaymentDto> result = paymentService.search(filter, pageable);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(guid, result.getContent().getFirst().getGuid());
            mocked.verify(() -> PaymentFilterFactory.fromFilter(filter));
            verify(paymentRepository).findAll(spec, pageable);
            verify(paymentMapper).toDto(payment);
            verifyNoMoreInteractions(paymentRepository, paymentMapper);
        }
    }

    static Stream<PaymentFilter> filterProvider() {
        final Instant after = Instant.parse("2026-01-01T10:15:30.00Z");
        final Instant before = Instant.parse("2026-02-02T10:15:30.00Z");

        return Stream.of(
            new PaymentFilter("USD", null, null, null, null, null),
            new PaymentFilter(null, new BigDecimal("10.00"), null, null, null, null),
            new PaymentFilter(null, null, new BigDecimal("999.99"), null, null, null),
            new PaymentFilter(null, new BigDecimal("10.00"), new BigDecimal("100.00"), null, null, null),
            new PaymentFilter(null, null, null, after, null, null),
            new PaymentFilter(null, null, null, null, before, null),
            new PaymentFilter(null, null, null, after, before, null),
            new PaymentFilter(null, null, null, null, null, PaymentStatus.APPROVED),
            new PaymentFilter("EUR", new BigDecimal("1.00"), new BigDecimal("2.00"),
                    after, before, PaymentStatus.PENDING)
        );
    }

    @ParameterizedTest
    @MethodSource("sortProvider")
    void search_shouldSupportSorting(Sort sort) {
        // given
        final PaymentFilter filter = new PaymentFilter("USD", null, null, null, null, null);
        final Pageable pageable = PageRequest.of(0, 25, sort);

        @SuppressWarnings("unchecked")
        final Specification<Payment> spec = (Specification<Payment>) mock(Specification.class);
        final Page<Payment> paymentsPage = new PageImpl<>(List.of(payment), pageable, 1);

        try (MockedStatic<PaymentFilterFactory> mocked = mockStatic(PaymentFilterFactory.class)) {
            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);
            when(paymentRepository.findAll(eq(spec), eq(pageable))).thenReturn(paymentsPage);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            final Page<PaymentDto> result = paymentService.search(filter, pageable);

            // then
            assertEquals(1, result.getTotalElements());
            assertEquals(sort, result.getPageable().getSort());
            mocked.verify(() -> PaymentFilterFactory.fromFilter(filter));
            verify(paymentRepository).findAll(spec, pageable);
            verify(paymentMapper).toDto(payment);
            verifyNoMoreInteractions(paymentRepository, paymentMapper);
        }
    }

    static Stream<Sort> sortProvider() {
        return Stream.of(
                Sort.by(Sort.Direction.ASC, "amount"),
                Sort.by(Sort.Direction.DESC, "amount"),
                Sort.by(Sort.Direction.ASC, "createdAt"),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    @Test
    void search_shouldWorkWithDefaultPagination_firstPage25() {
        // given
        final PaymentFilter filter = new PaymentFilter(null, null, null, null, null, PaymentStatus.APPROVED);
        final Pageable pageable = PageRequest.of(0, 25);

        @SuppressWarnings("unchecked")
        final Specification<Payment> spec = (Specification<Payment>) mock(Specification.class);
        final Page<Payment> paymentsPage = new PageImpl<>(List.of(payment), pageable, 1);

        try (MockedStatic<PaymentFilterFactory> mocked = mockStatic(PaymentFilterFactory.class)) {
            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);
            when(paymentRepository.findAll(eq(spec), eq(pageable))).thenReturn(paymentsPage);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            final Page<PaymentDto> result = paymentService.search(filter, pageable);

            // then
            assertEquals(0, result.getNumber());
            assertEquals(25, result.getSize());
            assertEquals(1, result.getTotalElements());
            mocked.verify(() -> PaymentFilterFactory.fromFilter(filter));
            verify(paymentRepository).findAll(spec, pageable);
            verify(paymentMapper).toDto(payment);
            verifyNoMoreInteractions(paymentRepository, paymentMapper);
        }
    }

    @Test
    void search_shouldSupportNonDefaultPagination() {
        // given
        final PaymentFilter filter = new PaymentFilter("USD", null, null, null, null, null);
        final Pageable pageable = PageRequest.of(2, 10);

        @SuppressWarnings("unchecked")
        final Specification<Payment> spec = (Specification<Payment>) mock(Specification.class);
        final Page<Payment> paymentsPage = new PageImpl<>(List.of(payment), pageable, 21);

        try (MockedStatic<PaymentFilterFactory> mocked = mockStatic(PaymentFilterFactory.class)) {
            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);
            when(paymentRepository.findAll(eq(spec), eq(pageable))).thenReturn(paymentsPage);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            final Page<PaymentDto> result = paymentService.search(filter, pageable);

            // then
            assertEquals(2, result.getNumber());
            assertEquals(10, result.getSize());
            assertEquals(21, result.getTotalElements());
            mocked.verify(() -> PaymentFilterFactory.fromFilter(filter));
            verify(paymentRepository).findAll(spec, pageable);
            verify(paymentMapper).toDto(payment);
            verifyNoMoreInteractions(paymentRepository, paymentMapper);
        }
    }

    @SuppressWarnings("checkstyle:Indentation")
    @Test
    void create_shouldSaveAndReturnDto() {
        // given
        final PaymentDto input = PaymentDto.builder()
            .inquiryRefId(UUID.randomUUID())
            .amount(new BigDecimal("10.00"))
            .currency("USD")
            .transactionRefId(UUID.randomUUID())
            .status(PaymentStatus.PENDING)
            .note("new payment")
            .build();

        final Payment entityToSave = new Payment();
        final Payment savedEntity = new Payment();
        savedEntity.setGuid(guid);

        final PaymentDto outDto = PaymentDto.builder()
            .guid(guid)
            .currency("USD")
            .status(PaymentStatus.PENDING)
            .build();

        when(paymentMapper.toEntity(input)).thenReturn(entityToSave);
        when(paymentRepository.save(entityToSave)).thenReturn(savedEntity);
        when(paymentMapper.toDto(savedEntity)).thenReturn(outDto);

        // when
        final PaymentDto result = paymentService.create(input);

        // then
        assertEquals(guid, result.getGuid());
        verify(paymentMapper).toEntity(input);
        verify(paymentRepository).save(entityToSave);
        verify(paymentMapper).toDto(savedEntity);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void update_shouldThrowIllegalArgumentException_whenMissing() {
        // given
        when(paymentRepository.existsById(guid)).thenReturn(false);

        // when
        final IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> paymentService.update(guid, paymentDto));

        // then
        assertEquals("Payment not found for id: " + guid, ex.getMessage());
        verify(paymentRepository).existsById(guid);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void update_shouldSaveWithGuidAndReturnDto_whenExists() {
        // given
        when(paymentRepository.existsById(guid)).thenReturn(true);

        final Payment mappedEntity = new Payment();
        final Payment savedEntity = new Payment();
        savedEntity.setGuid(guid);

        final PaymentDto out = PaymentDto.builder()
            .guid(guid)
            .currency("USD")
            .status(PaymentStatus.APPROVED)
            .build();

        when(paymentMapper.toEntity(paymentDto)).thenReturn(mappedEntity);

        final ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(captor.capture())).thenReturn(savedEntity);
        when(paymentMapper.toDto(savedEntity)).thenReturn(out);

        // when
        final PaymentDto result = paymentService.update(guid, paymentDto);

        // then
        assertEquals(guid, result.getGuid());

        final Payment argToSave = captor.getValue();
        assertEquals(guid, argToSave.getGuid(), "Service must set guid before save");

        verify(paymentRepository).existsById(guid);
        verify(paymentMapper).toEntity(paymentDto);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toDto(savedEntity);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void updateStatus_shouldUpdateAndReturnDto() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.of(payment));

        final PaymentDto out = PaymentDto.builder()
            .guid(guid)
            .currency(payment.getCurrency())
            .status(PaymentStatus.DECLINED)
            .build();

        final ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(captor.capture())).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(out);

        // when
        final PaymentDto result = paymentService.updateStatus(guid, PaymentStatus.DECLINED);

        // then
        assertEquals(PaymentStatus.DECLINED, result.getStatus());
        assertEquals(PaymentStatus.DECLINED, captor.getValue().getStatus());

        verify(paymentRepository).findById(guid);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toDto(payment);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void updateStatus_shouldThrowEntityNotFoundException_whenMissing() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> paymentService.updateStatus(guid, PaymentStatus.APPROVED)
        );

        // then
        assertEquals("Payment not found for id: " + guid, ex.getMessage());
        verify(paymentRepository).findById(guid);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void updateNote_shouldUpdateAndReturnDto() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.of(payment));

        final String newNote = "updated note";
        final PaymentDto out = PaymentDto.builder()
            .guid(guid)
            .currency(payment.getCurrency())
            .status(payment.getStatus())
            .note(newNote)
            .build();

        final ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(captor.capture())).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(out);

        // when
        final PaymentDto result = paymentService.updateNote(guid, newNote);

        // then
        assertEquals(newNote, result.getNote());
        assertEquals(newNote, captor.getValue().getNote());

        verify(paymentRepository).findById(guid);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toDto(payment);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void updateNote_shouldThrowEntityNotFoundException_whenMissing() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> paymentService.updateNote(guid, "note")
        );

        // then
        assertEquals("Payment not found for id: " + guid, ex.getMessage());
        verify(paymentRepository).findById(guid);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    // -------------------- DELETE --------------------

    @Test
    void delete_shouldDelete_whenExists() {
        // given
        when(paymentRepository.existsById(guid)).thenReturn(true);

        // when
        paymentService.delete(guid);

        // then
        verify(paymentRepository).existsById(guid);
        verify(paymentRepository).deleteById(guid);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenMissing() {
        // given
        when(paymentRepository.existsById(guid)).thenReturn(false);

        // when
        final EntityNotFoundException ex =
            assertThrows(EntityNotFoundException.class, () -> paymentService.delete(guid));

        // then
        assertEquals("Payment not found for id: " + guid, ex.getMessage());
        verify(paymentRepository).existsById(guid);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }
}
