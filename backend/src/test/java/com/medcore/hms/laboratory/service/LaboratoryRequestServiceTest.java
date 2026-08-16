package com.medcore.hms.laboratory.service;

import com.medcore.hms.laboratory.domain.LaboratoryRequest;
import com.medcore.hms.laboratory.domain.LaboratoryRequestStatus;
import com.medcore.hms.laboratory.dto.CancelLaboratoryRequest;
import com.medcore.hms.laboratory.repository.LaboratoryRequestItemRepository;
import com.medcore.hms.laboratory.repository.LaboratoryRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LaboratoryRequestServiceTest {

    private LaboratoryRequestRepository requestRepository;
    private LaboratoryRequestItemRepository itemRepository;
    private LaboratoryRequestService service;

    @BeforeEach
    void setUp() {
        requestRepository = mock(LaboratoryRequestRepository.class);
        itemRepository = mock(LaboratoryRequestItemRepository.class);
        service = new LaboratoryRequestService(
                requestRepository,
                itemRepository);
    }

    @Test
    void cancelsRequestedRequestAndStoresAuditFields() {
        LaboratoryRequest request = mockRequest(
                2L,
                LaboratoryRequestStatus.REQUESTED);

        when(requestRepository.findById(2L))
                .thenReturn(Optional.of(request));
        when(requestRepository.save(request))
                .thenReturn(request);
        when(itemRepository
                .findAllByLaboratoryRequestIdOrderBySortOrder(2L))
                .thenReturn(List.of());

        service.cancel(
                2L,
                new CancelLaboratoryRequest(
                        "Patient appointment was cancelled"),
                1L);

        verify(request).setStatus(
                LaboratoryRequestStatus.CANCELLED);
        verify(request).setCancellationReason(
                "Patient appointment was cancelled");
        verify(request).setCancelledAt(any());
        verify(request).setUpdatedByUserId(1L);
        verify(request).setUpdatedAt(any());
        verify(requestRepository).save(request);
    }

    @Test
    void rejectsCancellationOfReleasedRequest() {
        LaboratoryRequest request = mockRequest(
                1L,
                LaboratoryRequestStatus.RELEASED);

        when(requestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.cancel(
                1L,
                new CancelLaboratoryRequest("Too late"),
                1L))
                .hasMessageContaining(
                        "A released laboratory request cannot be cancelled");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void rejectsCancellationOfAlreadyCancelledRequest() {
        LaboratoryRequest request = mockRequest(
                2L,
                LaboratoryRequestStatus.CANCELLED);

        when(requestRepository.findById(2L))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.cancel(
                2L,
                new CancelLaboratoryRequest("Duplicate cancellation"),
                1L))
                .hasMessageContaining(
                        "Laboratory request is already cancelled");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void rejectsBlankCancellationReason() {
        LaboratoryRequest request = mockRequest(
                2L,
                LaboratoryRequestStatus.REQUESTED);

        when(requestRepository.findById(2L))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.cancel(
                2L,
                new CancelLaboratoryRequest("   "),
                1L))
                .hasMessageContaining(
                        "Cancellation reason must not be blank");

        verify(requestRepository, never()).save(any());
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