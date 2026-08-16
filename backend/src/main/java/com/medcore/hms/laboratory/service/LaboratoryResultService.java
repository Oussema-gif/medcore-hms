package com.medcore.hms.laboratory.service;

import com.medcore.hms.exception.BadRequestException;
import com.medcore.hms.exception.ResourceNotFoundException;
import com.medcore.hms.laboratory.domain.LaboratoryRequest;
import com.medcore.hms.laboratory.domain.LaboratoryRequestItem;
import com.medcore.hms.laboratory.domain.LaboratoryRequestStatus;
import com.medcore.hms.laboratory.domain.LaboratoryResult;
import com.medcore.hms.laboratory.dto.CreateLaboratoryResultRequest;
import com.medcore.hms.laboratory.dto.LaboratoryResultResponse;
import com.medcore.hms.laboratory.repository.LaboratoryRequestItemRepository;
import com.medcore.hms.laboratory.repository.LaboratoryRequestRepository;
import com.medcore.hms.laboratory.repository.LaboratoryResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class LaboratoryResultService {

    private final LaboratoryResultRepository resultRepository;
    private final LaboratoryRequestItemRepository itemRepository;
    private final LaboratoryRequestRepository requestRepository;

    public LaboratoryResultService(
            LaboratoryResultRepository resultRepository,
            LaboratoryRequestItemRepository itemRepository,
            LaboratoryRequestRepository requestRepository) {

        this.resultRepository = resultRepository;
        this.itemRepository = itemRepository;
        this.requestRepository = requestRepository;
    }

    public LaboratoryResultResponse create(
            CreateLaboratoryResultRequest input,
            Long recordedByUserId) {

        validateValue(input);

        LaboratoryRequestItem item =
                itemRepository.findById(
                        input.laboratoryRequestItemId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Laboratory request item",
                                        input.laboratoryRequestItemId()));

        LaboratoryRequest request =
                requestRepository.findById(
                        item.getLaboratoryRequestId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Laboratory request",
                                        item.getLaboratoryRequestId()));

        if (request.getStatus()
                == LaboratoryRequestStatus.CANCELLED) {

            throw new BadRequestException(
                    "Cannot record a result for a cancelled request");
        }

        if (request.getStatus()
                == LaboratoryRequestStatus.RELEASED
                && input.amendedFromResultId() == null) {

            throw new BadRequestException(
                    "A released request can only receive an amendment");
        }

        validateAmendment(input, item);

        LaboratoryResult result =
                LaboratoryResult.create();

        result.setLaboratoryRequestItemId(
                item.getId());

        result.setResultValue(
                input.resultValue());

        result.setResultText(
                trimToNull(input.resultText()));

        result.setUnit(
                trimToNull(input.unit()));

        result.setReferenceRange(
                trimToNull(input.referenceRange()));

        result.setInterpretation(
                trimToNull(input.interpretation()));

        result.setRecordedByUserId(
                recordedByUserId);

        result.setAmendedFromResultId(
                input.amendedFromResultId());

        LaboratoryResult savedResult =
                resultRepository.save(result);

        if (input.amendedFromResultId() != null) {
            reopenRequestForAmendment(
                    request,
                    recordedByUserId);
        }

        return toResponse(savedResult);
    }

    @Transactional(readOnly = true)
    public LaboratoryResultResponse getById(
            Long resultId) {

        return resultRepository.findById(resultId)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Laboratory result",
                                resultId));
    }

    @Transactional(readOnly = true)
    public List<LaboratoryResultResponse>
    findByRequestItemId(Long requestItemId) {

        return resultRepository
                .findAllByLaboratoryRequestItemIdOrderByRecordedAtDesc(
                        requestItemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LaboratoryResultResponse release(
            Long resultId,
            Long releasedByUserId) {

        LaboratoryResult result =
                resultRepository.findById(resultId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Laboratory result",
                                        resultId));

        if (result.getReleasedAt() != null) {
            throw new BadRequestException(
                    "Laboratory result is already released");
        }

        LaboratoryRequestItem item =
                itemRepository.findById(
                        result.getLaboratoryRequestItemId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Laboratory request item",
                                        result.getLaboratoryRequestItemId()));

        LaboratoryRequest request =
                requestRepository.findById(
                        item.getLaboratoryRequestId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Laboratory request",
                                        item.getLaboratoryRequestId()));

        if (request.getStatus()
                == LaboratoryRequestStatus.CANCELLED) {

            throw new BadRequestException(
                    "Cannot release a result for a cancelled request");
        }

        Instant now = Instant.now();

        result.setReleasedAt(now);
        result.setUpdatedAt(now);

        LaboratoryResult savedResult =
                resultRepository.save(result);

        updateRequestStatus(
                request,
                releasedByUserId,
                savedResult.getReleasedAt());

        return toResponse(savedResult);
    }

    private void validateAmendment(
            CreateLaboratoryResultRequest input,
            LaboratoryRequestItem item) {

        Long amendedFromResultId =
                input.amendedFromResultId();

        if (amendedFromResultId == null) {
            return;
        }

        LaboratoryResult originalResult =
                resultRepository.findById(amendedFromResultId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Original laboratory result",
                                        amendedFromResultId));

        if (!originalResult
                .getLaboratoryRequestItemId()
                .equals(item.getId())) {

            throw new BadRequestException(
                    "Amended result must belong to the same request item");
        }

        if (originalResult.getReleasedAt() == null) {
            throw new BadRequestException(
                    "Only a released laboratory result can be amended");
        }
    }

    private void reopenRequestForAmendment(
            LaboratoryRequest request,
            Long updatedByUserId) {

        request.setStatus(
                LaboratoryRequestStatus.RESULT_READY);

        request.setReleasedAt(null);
        request.setUpdatedAt(Instant.now());
        request.setUpdatedByUserId(updatedByUserId);

        requestRepository.save(request);
    }

    private void updateRequestStatus(
            LaboratoryRequest request,
            Long updatedByUserId,
            Instant releasedAt) {

        List<LaboratoryRequestItem> items =
                itemRepository
                        .findAllByLaboratoryRequestIdOrderBySortOrder(
                                request.getId());

        boolean allItemsHaveResults = true;
        boolean allResultsAreReleased = true;

        for (LaboratoryRequestItem item : items) {
            long totalResults =
                    resultRepository.countByLaboratoryRequestItemId(
                            item.getId());

            long releasedResults =
                    resultRepository
                            .countByLaboratoryRequestItemIdAndReleasedAtIsNotNull(
                                    item.getId());

            if (totalResults == 0) {
                allItemsHaveResults = false;
                allResultsAreReleased = false;
                continue;
            }

            if (totalResults != releasedResults) {
                allResultsAreReleased = false;
            }
        }

        if (allResultsAreReleased && !items.isEmpty()) {
            request.setStatus(
                    LaboratoryRequestStatus.RELEASED);

            request.setReleasedAt(releasedAt);

        } else if (allItemsHaveResults && !items.isEmpty()) {
            request.setStatus(
                    LaboratoryRequestStatus.RESULT_READY);

            request.setReleasedAt(null);

        } else {
            request.setStatus(
                    LaboratoryRequestStatus.IN_PROGRESS);

            request.setReleasedAt(null);
        }

        request.setUpdatedAt(Instant.now());
        request.setUpdatedByUserId(updatedByUserId);

        requestRepository.save(request);
    }

    private void validateValue(
            CreateLaboratoryResultRequest input) {

        boolean numericPresent =
                input.resultValue() != null;

        boolean textPresent =
                input.resultText() != null
                        && !input.resultText().isBlank();

        if (!numericPresent && !textPresent) {
            throw new BadRequestException(
                    "Either resultValue or resultText is required");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private LaboratoryResultResponse toResponse(
            LaboratoryResult result) {

        return new LaboratoryResultResponse(
                result.getId(),
                result.getLaboratoryRequestItemId(),
                result.getResultValue(),
                result.getResultText(),
                result.getUnit(),
                result.getReferenceRange(),
                result.getInterpretation(),
                result.getRecordedByUserId(),
                result.getRecordedAt(),
                result.getReleasedAt(),
                result.getAmendedFromResultId(),
                result.getCreatedAt(),
                result.getUpdatedAt());
    }
}