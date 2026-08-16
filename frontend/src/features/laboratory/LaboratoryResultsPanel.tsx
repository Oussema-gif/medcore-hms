import { useState } from "react";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import axios from "axios";
import {
  createLaboratoryResult,
  getLaboratoryResultsByRequestItem,
  releaseLaboratoryResult,
  type LaboratoryRequest,
} from "./api";

type LaboratoryResultsPanelProps = {
  request: LaboratoryRequest;
};

export function LaboratoryResultsPanel({
  request,
}: LaboratoryResultsPanelProps) {
  return (
    <section aria-label={`Results for laboratory request ${request.id}`}>
      <div className="details-list">
        <div>
          <dt>Status</dt>
          <dd>
            <span className="app-user-pill">{request.status}</span>
          </dd>
        </div>
        <div>
          <dt>Priority</dt>
          <dd>{request.priority}</dd>
        </div>
        {request.clinicalNotes && (
          <div>
            <dt>Clinical notes</dt>
            <dd>{request.clinicalNotes}</dd>
          </div>
        )}
      </div>

      <div className="laboratory-item-list">
        {request.items.map((item) => (
          <LaboratoryItemResult
            key={item.id}
            request={request}
            itemId={item.id}
            testCode={item.testCode}
            testName={item.testName}
          />
        ))}
      </div>
    </section>
  );
}

type LaboratoryItemResultProps = {
  request: LaboratoryRequest;
  itemId: number;
  testCode: string;
  testName: string;
};

function LaboratoryItemResult({
  request,
  itemId,
  testCode,
  testName,
}: LaboratoryItemResultProps) {
  const queryClient = useQueryClient();
  const [resultValue, setResultValue] = useState("");
  const [resultText, setResultText] = useState("");
  const [unit, setUnit] = useState("");
  const [referenceRange, setReferenceRange] = useState("");
  const [interpretation, setInterpretation] = useState("");

  const resultsQuery = useQuery({
    queryKey: ["laboratory-results", itemId],
    queryFn: () => getLaboratoryResultsByRequestItem(itemId),
    enabled: itemId > 0,
  });

  const results = resultsQuery.data?.data ?? [];
  const latestResult = results[0];
  const latestReleasedResult = results.find((result) => result.releasedAt);
  const requestIsReleased = request.status === "RELEASED";
  const requestIsCancelled = request.status === "CANCELLED";

  const saveMutation = useMutation({
    mutationFn: async () => {
      const trimmedText = resultText.trim();
      const trimmedValue = resultValue.trim();
      const numericValue = trimmedValue === "" ? undefined : Number(trimmedValue);

      if (numericValue !== undefined && !Number.isFinite(numericValue)) {
        throw new Error("Result value must be a valid number.");
      }

      if (numericValue === undefined && !trimmedText) {
        throw new Error("Enter a numeric result or text result.");
      }

      return createLaboratoryResult({
        laboratoryRequestItemId: itemId,
        resultValue: numericValue,
        resultText: trimmedText || undefined,
        unit: unit.trim() || undefined,
        referenceRange: referenceRange.trim() || undefined,
        interpretation: interpretation.trim() || undefined,
        amendedFromResultId:
          latestReleasedResult && requestIsReleased
            ? latestReleasedResult.id
            : undefined,
      });
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["laboratory-results", itemId],
        }),
        queryClient.invalidateQueries({
          queryKey: ["laboratory-requests", request.patientId],
        }),
      ]);
    },
  });

  const releaseMutation = useMutation({
    mutationFn: (resultId: number) => releaseLaboratoryResult(resultId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["laboratory-results", itemId],
        }),
        queryClient.invalidateQueries({
          queryKey: ["laboratory-requests", request.patientId],
        }),
      ]);
    },
  });

  const disabled =
    requestIsCancelled ||
    requestIsReleased ||
    saveMutation.isPending ||
    releaseMutation.isPending;

  return (
    <article className="card laboratory-result-card">
      <div className="page-header">
        <div>
          <h4>
            {testCode} — {testName}
          </h4>
          <p className="helper-text">Request item #{itemId}</p>
        </div>
        {latestReleasedResult && (
          <span className="app-user-pill">Released</span>
        )}
      </div>

      {resultsQuery.isLoading && (
        <div className="empty-state">Loading results...</div>
      )}

      {resultsQuery.isError && (
        <div className="alert" role="alert">
          Unable to load results for this test.
        </div>
      )}

      {!resultsQuery.isLoading && latestResult && (
        <div className="record-list">
          <div className="record-list-item">
            <strong>
              {latestResult.resultValue ?? latestResult.resultText ?? "No value"}
            </strong>
            <small>
              {latestResult.unit || "No unit"}
              {latestResult.referenceRange
                ? ` · Reference: ${latestResult.referenceRange}`
                : ""}
            </small>
            <small>
              {latestResult.releasedAt
                ? `Released ${new Date(latestResult.releasedAt).toLocaleString()}`
                : `Recorded ${new Date(latestResult.recordedAt).toLocaleString()}`}
            </small>
          </div>
        </div>
      )}

      {!requestIsCancelled && !requestIsReleased && (
        <form
          className="card-grid"
          onSubmit={(event) => {
            event.preventDefault();
            saveMutation.mutate();
          }}
        >
          <label className="field">
            <span>Numeric result</span>
            <input
              type="number"
              step="any"
              value={resultValue}
              onChange={(event) => setResultValue(event.target.value)}
              placeholder="6.8"
              disabled={disabled}
            />
          </label>

          <label className="field">
            <span>Unit</span>
            <input
              value={unit}
              onChange={(event) => setUnit(event.target.value)}
              placeholder="mg/dL"
              maxLength={64}
              disabled={disabled}
            />
          </label>

          <label className="field field-wide">
            <span>Text result</span>
            <textarea
              value={resultText}
              onChange={(event) => setResultText(event.target.value)}
              placeholder="Normal renal and hepatic markers"
              disabled={disabled}
            />
          </label>

          <label className="field field-wide">
            <span>Reference range</span>
            <input
              value={referenceRange}
              onChange={(event) => setReferenceRange(event.target.value)}
              placeholder="4.0–11.0"
              maxLength={256}
              disabled={disabled}
            />
          </label>

          <label className="field field-wide">
            <span>Interpretation</span>
            <textarea
              value={interpretation}
              onChange={(event) => setInterpretation(event.target.value)}
              placeholder="Within normal range"
              maxLength={1024}
              disabled={disabled}
            />
          </label>

          {saveMutation.isError && (
            <div className="alert field-wide" role="alert">
              {getApiErrorMessage(saveMutation.error)}
            </div>
          )}

          {releaseMutation.isError && (
            <div className="alert field-wide" role="alert">
              {getApiErrorMessage(releaseMutation.error)}
            </div>
          )}

          <div className="form-actions field-wide">
            <button
              className="app-button"
              type="submit"
              disabled={disabled}
            >
              {saveMutation.isPending ? "Saving..." : "Save result"}
            </button>

            {latestResult && !latestResult.releasedAt && (
              <button
                className="app-button"
                type="button"
                disabled={
                  saveMutation.isPending || releaseMutation.isPending
                }
                onClick={() => releaseMutation.mutate(latestResult.id)}
              >
                {releaseMutation.isPending ? "Releasing..." : "Release result"}
              </button>
            )}
          </div>
        </form>
      )}

      {requestIsCancelled && (
        <div className="alert" role="status">
          This laboratory request was cancelled.
        </div>
      )}

      {requestIsReleased && (
        <div className="alert" role="status">
          This request is released. Create an amendment only if a correction is required.
        </div>
      )}
    </article>
  );
}

function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as
      | { message?: unknown; fieldErrors?: Array<{ field?: unknown; message?: unknown }> }
      | undefined;

    if (Array.isArray(data?.fieldErrors) && data.fieldErrors.length > 0) {
      return data.fieldErrors
        .map((fieldError) => {
          const field = typeof fieldError.field === "string" ? fieldError.field : "Field";
          const message =
            typeof fieldError.message === "string"
              ? fieldError.message
              : "Invalid value";
          return `${field}: ${message}`;
        })
        .join("; ");
    }

    if (typeof data?.message === "string") return data.message;
    return `Request failed with status ${error.response?.status ?? "unknown"}.`;
  }

  if (error instanceof Error) return error.message;
  return "An unexpected error occurred.";
}
