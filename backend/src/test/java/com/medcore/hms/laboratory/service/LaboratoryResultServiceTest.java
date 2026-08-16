package com.medcore.hms.laboratory.service;

import com.medcore.hms.exception.BadRequestException;
import com.medcore.hms.laboratory.domain.LaboratoryRequest;
import com.medcore.hms.laboratory.domain.LaboratoryRequestItem;
import com.medcore.hms.laboratory.domain.LaboratoryRequestStatus;
import com.medcore.hms.laboratory.domain.LaboratoryResult;
import com.medcore.hms.laboratory.dto.CreateLaboratoryResultRequest;
import com.medcore.hms.laboratory.repository.LaboratoryRequestItemRepository;
import com.medcore.hms.laboratory.repository.LaboratoryRequestRepository;
import com.medcore.hms.laboratory.repository.LaboratoryResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LaboratoryResultServiceTest {

    private LaboratoryResultRepository resultRepository;
    private LaboratoryRequestItemRepository itemRepository;
    private LaboratoryRequestRepository requestRepository;
    private LaboratoryResultService service;

    @BeforeEach
    void setUp() {
        resultRepository = mock(LaboratoryResultRepository.class);
        itemRepository = mock(LaboratoryRequestItemRepository.class);
        requestRepository = mock(LaboratoryRequestRepository.class);
        service = new LaboratoryResultService(
                resultRepository,
                itemRepository,
                requestRepository);
    }

    @Test
    void createsResult() {
        LaboratoryRequestItem item = mockItem(1L, 10L);
        LaboratoryRequest request = mockRequest(10L,
                LaboratoryRequestStatus.IN_PROGRESS);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(resultRepository.save(any(LaboratoryResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(
                new CreateLaboratoryResultRequest(
                        1L,
                        BigDecimal.valueOf(6.8),
                        null,
                        "10^9/L",
                        "4.0–11.0",
                        "Within normal range",
                        null),
                1L);

        assertThat(response.resultValue())
                .isEqualByComparingTo("6.8");
        assertThat(response.laboratoryRequestItemId()).isEqualTo(1L);
        verify(resultRepository).save(any(LaboratoryResult.class));
    }

    @Test
    void rejectsResultWithoutNumericValueOrText() {
        assertThatThrownBy(() -> service.create(
                new CreateLaboratoryResultRequest(
                        1L,
                        null,
                        " ",
                        null,
                        null,
                        null,
                        null),
                1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Either resultValue or resultText is required");

        verifyNoInteractions(itemRepository, requestRepository,
                resultRepository);
    }

    @Test
    void createsAmendmentAndReopensReleasedRequest() {
        LaboratoryRequestItem item = mockItem(1L, 10L);
        LaboratoryRequest request = mockRequest(10L,
                LaboratoryRequestStatus.RELEASED);
        LaboratoryResult original = LaboratoryResult.create();
        original.setLaboratoryRequestItemId(1L);
        original.setReleasedAt(Instant.parse("2026-08-11T12:00:00Z"));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(resultRepository.findById(1L)).thenReturn(Optional.of(original));
        when(resultRepository.save(any(LaboratoryResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(
                new CreateLaboratoryResultRequest(
                        1L,
                        BigDecimal.valueOf(7.1),
                        null,
                        "10^9/L",
                        "4.0–11.0",
                        "Updated result",
                        1L),
                1L);

        verify(request).setStatus(LaboratoryRequestStatus.RESULT_READY);
        verify(request).setReleasedAt(null);
        verify(requestRepository).save(request);
    }

    @Test
    void rejectsAmendmentOfUnreleasedResult() {
        LaboratoryRequestItem item = mockItem(1L, 10L);
        LaboratoryRequest request = mockRequest(10L,
                LaboratoryRequestStatus.IN_PROGRESS);
        LaboratoryResult original = LaboratoryResult.create();
        original.setLaboratoryRequestItemId(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(resultRepository.findById(1L)).thenReturn(Optional.of(original));

        assertThatThrownBy(() -> service.create(
                new CreateLaboratoryResultRequest(
                        1L,
                        BigDecimal.ONE,
                        null,
                        null,
                        null,
                        null,
                        1L),
                1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only a released laboratory result can be amended");

        verify(resultRepository, never()).save(any());
    }

    @Test
    void releasesFinalResultsAndMarksRequestReleased() {
        LaboratoryResult result = LaboratoryResult.create();
        result.setLaboratoryRequestItemId(1L);

        LaboratoryRequestItem item = mockItem(1L, 10L);
        LaboratoryRequest request = mockRequest(10L,
                LaboratoryRequestStatus.RESULT_READY);
        LaboratoryRequestItem secondItem = mockItem(2L, 10L);

        when(resultRepository.findById(1L)).thenReturn(Optional.of(result));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.findAllByLaboratoryRequestIdOrderBySortOrder(10L))
                .thenReturn(List.of(item, secondItem));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(resultRepository.save(any(LaboratoryResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(resultRepository.countByLaboratoryRequestItemId(1L))
                .thenReturn(1L);
        when(resultRepository.countByLaboratoryRequestItemId(2L))
                .thenReturn(1L);
        when(resultRepository
                .countByLaboratoryRequestItemIdAndReleasedAtIsNotNull(1L))
                .thenReturn(1L);
        when(resultRepository
                .countByLaboratoryRequestItemIdAndReleasedAtIsNotNull(2L))
                .thenReturn(1L);

        var response = service.release(1L, 1L);

        assertThat(response.releasedAt()).isNotNull();
        verify(request).setStatus(LaboratoryRequestStatus.RELEASED);
        verify(requestRepository).save(request);
    }

    @Test
    void rejectsDuplicateRelease() {
        LaboratoryResult result = LaboratoryResult.create();
        result.setReleasedAt(Instant.parse("2026-08-11T12:00:00Z"));

        when(resultRepository.findById(1L)).thenReturn(Optional.of(result));

        assertThatThrownBy(() -> service.release(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Laboratory result is already released");

        verifyNoInteractions(itemRepository, requestRepository);
    }

    private LaboratoryRequestItem mockItem(
            Long itemId,
            Long requestId) {
        LaboratoryRequestItem item = mock(LaboratoryRequestItem.class);
        when(item.getId()).thenReturn(itemId);
        when(item.getLaboratoryRequestId()).thenReturn(requestId);
        return item;
    }

    private LaboratoryRequest mockRequest(
            Long requestId,
            LaboratoryRequestStatus status) {
        LaboratoryRequest request = mock(LaboratoryRequest.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getStatus()).thenReturn(status);
        return request;
    }
}