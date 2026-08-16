package com.medcore.hms.laboratory.service;

import com.medcore.hms.laboratory.domain.LaboratoryPriority;
import com.medcore.hms.laboratory.domain.LaboratoryRequest;
import com.medcore.hms.laboratory.domain.LaboratoryRequestItem;
import com.medcore.hms.laboratory.domain.LaboratoryRequestStatus;
import com.medcore.hms.laboratory.dto.CancelLaboratoryRequest;
import com.medcore.hms.laboratory.dto.CreateLaboratoryRequest;
import com.medcore.hms.laboratory.dto.LaboratoryRequestResponse;
import com.medcore.hms.laboratory.repository.LaboratoryRequestItemRepository;
import com.medcore.hms.laboratory.repository.LaboratoryRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class LaboratoryRequestService {

        private final LaboratoryRequestRepository requestRepository;
        private final LaboratoryRequestItemRepository itemRepository;

        public LaboratoryRequestService(
                        LaboratoryRequestRepository requestRepository,
                        LaboratoryRequestItemRepository itemRepository) {

                this.requestRepository = requestRepository;
                this.itemRepository = itemRepository;
        }

        public LaboratoryRequestResponse create(
                        CreateLaboratoryRequest input,
                        Long actorUserId) {

                LaboratoryRequest request = LaboratoryRequest.create();

                request.setPatientId(input.patientId());
                request.setOrderingDoctorId(input.orderingDoctorId());
                request.setAppointmentId(input.appointmentId());
                request.setMedicalRecordId(input.medicalRecordId());

                request.setPriority(
                                input.priority() == null
                                                ? LaboratoryPriority.ROUTINE
                                                : input.priority());

                request.setClinicalNotes(input.clinicalNotes());
                request.setCreatedByUserId(actorUserId);
                request.setUpdatedByUserId(actorUserId);

                LaboratoryRequest savedRequest = requestRepository.save(request);

                List<LaboratoryRequestItem> items = input.items()
                                .stream()
                                .map(itemInput -> {
                                        LaboratoryRequestItem item = LaboratoryRequestItem.create();

                                        item.setLaboratoryRequestId(
                                                        savedRequest.getId());

                                        item.setTestCode(
                                                        itemInput.testCode().trim());

                                        item.setTestName(
                                                        itemInput.testName().trim());

                                        item.setSortOrder(
                                                        itemInput.sortOrder() == null
                                                                        ? 1
                                                                        : itemInput.sortOrder());

                                        if (item.getSortOrder() <= 0) {
                                                throw new ResponseStatusException(
                                                                HttpStatus.BAD_REQUEST,
                                                                "Test item sortOrder must be positive");
                                        }

                                        return item;
                                })
                                .toList();

                itemRepository.saveAll(items);

                return toResponse(savedRequest, items);
        }

        public LaboratoryRequestResponse cancel(
                        Long requestId,
                        CancelLaboratoryRequest input,
                        Long actorUserId) {

                LaboratoryRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Laboratory request not found"));

                if (request.getStatus() == LaboratoryRequestStatus.CANCELLED) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Laboratory request is already cancelled");
                }

                if (request.getStatus() == LaboratoryRequestStatus.RELEASED) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "A released laboratory request cannot be cancelled");
                }

                String reason = input.cancellationReason().trim();

                if (reason.isBlank()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Cancellation reason must not be blank");
                }

                Instant now = Instant.now();

                request.setStatus(
                                LaboratoryRequestStatus.CANCELLED);

                request.setCancelledAt(now);
                request.setCancellationReason(reason);
                request.setUpdatedAt(now);
                request.setUpdatedByUserId(actorUserId);

                LaboratoryRequest savedRequest = requestRepository.save(request);

                return toResponse(
                                savedRequest,
                                itemRepository
                                                .findAllByLaboratoryRequestIdOrderBySortOrder(
                                                                requestId));
        }

        @Transactional(readOnly = true)
        public LaboratoryRequestResponse getById(
                        Long requestId) {

                LaboratoryRequest request = requestRepository.findById(requestId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Laboratory request not found"));

                List<LaboratoryRequestItem> items = itemRepository
                                .findAllByLaboratoryRequestIdOrderBySortOrder(
                                                requestId);

                return toResponse(request, items);
        }

        @Transactional(readOnly = true)
        public Page<LaboratoryRequestResponse> findByPatientId(
                        Long patientId,
                        Pageable pageable) {

                return requestRepository
                                .findAllByPatientId(patientId, pageable)
                                .map(request -> {
                                        List<LaboratoryRequestItem> items = itemRepository
                                                        .findAllByLaboratoryRequestIdOrderBySortOrder(
                                                                        request.getId());

                                        return toResponse(request, items);
                                });
        }

        private LaboratoryRequestResponse toResponse(
                        LaboratoryRequest request,
                        List<LaboratoryRequestItem> items) {

                return new LaboratoryRequestResponse(
                                request.getId(),
                                request.getPatientId(),
                                request.getOrderingDoctorId(),
                                request.getAppointmentId(),
                                request.getMedicalRecordId(),
                                request.getPriority(),
                                request.getStatus(),
                                request.getClinicalNotes(),
                                request.getRequestedAt(),
                                request.getReleasedAt(),
                                request.getCancelledAt(),
                                request.getCancellationReason(),
                                items.stream()
                                                .map(item -> new LaboratoryRequestResponse.LaboratoryTestItemResponse(
                                                                item.getId(),
                                                                item.getTestCode(),
                                                                item.getTestName(),
                                                                item.getSortOrder()))
                                                .toList());
        }
}