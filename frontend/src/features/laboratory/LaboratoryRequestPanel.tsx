import {
  useState,
  type FormEvent,
} from "react";
import {
  Plus,
  FlaskConical,
  Trash2,
} from "lucide-react";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";

import {
  createLaboratoryRequest,
  getPatientLaboratoryRequests,
  type CreateLaboratoryRequestInput,
  type LaboratoryPriority,
} from "./api";

import { LaboratoryResultsPanel } from "./LaboratoryResultsPanel";

type LaboratoryRequestPanelProps = {
  patientId: number;
  doctorId?: number;
  appointmentId?: number;
  medicalRecordId?: number;
};

type TestDraft = {
  testCode: string;
  testName: string;
};

const emptyTest: TestDraft = {
  testCode: "",
  testName: "",
};

export function LaboratoryRequestPanel({
  patientId,
  doctorId,
  appointmentId,
  medicalRecordId,
}: LaboratoryRequestPanelProps) {
  const queryClient =
    useQueryClient();

  const [
    priority,
    setPriority,
  ] =
    useState<LaboratoryPriority>(
      "ROUTINE",
    );

  const [
    clinicalNotes,
    setClinicalNotes,
  ] = useState("");

  const [tests, setTests] =
    useState<TestDraft[]>([
      { ...emptyTest },
    ]);

  const [
    selectedRequestId,
    setSelectedRequestId,
  ] = useState<number | null>(
    null,
  );

  const requestsQuery = useQuery({
    queryKey: [
      "laboratory-requests",
      patientId,
    ],

    queryFn: () =>
      getPatientLaboratoryRequests(
        patientId,
      ),

    enabled: patientId > 0,
  });

  const createMutation = useMutation({
    mutationFn: (
      input: CreateLaboratoryRequestInput,
    ) =>
      createLaboratoryRequest(
        input,
      ),

    onSuccess: async (
      response,
    ) => {
      await queryClient.invalidateQueries(
        {
          queryKey: [
            "laboratory-requests",
            patientId,
          ],
        },
      );

      setPriority("ROUTINE");
      setClinicalNotes("");
      setTests([{ ...emptyTest }]);

      if (response?.data?.id) {
        setSelectedRequestId(
          response.data.id,
        );
      }
    },
  });

  function updateTest(
    index: number,
    field: keyof TestDraft,
    value: string,
  ) {
    setTests((current) =>
      current.map(
        (test, testIndex) =>
          testIndex === index
            ? {
              ...test,
              [field]: value,
            }
            : test,
      ),
    );
  }

  function removeTest(
    index: number,
  ) {
    setTests((current) =>
      current.length === 1
        ? current
        : current.filter(
          (_, testIndex) =>
            testIndex !==
            index,
        ),
    );
  }

  function addTest() {
    setTests((current) => [
      ...current,
      { ...emptyTest },
    ]);
  }

  function submit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    if (!doctorId) {
      return;
    }

    const validTests = tests
      .map((test) => ({
        testCode:
          test.testCode.trim(),

        testName:
          test.testName.trim(),
      }))
      .filter(
        (test) =>
          test.testCode &&
          test.testName,
      );

    if (validTests.length === 0) {
      return;
    }

    createMutation.mutate({
      patientId,
      orderingDoctorId:
        doctorId,
      appointmentId,
      medicalRecordId,
      priority,
      clinicalNotes:
        clinicalNotes.trim() ||
        undefined,
      items: validTests.map(
        (test, index) => ({
          ...test,
          sortOrder: index + 1,
        }),
      ),
    });
  }

  const requests =
    requestsQuery.data?.data ?? [];

  const selectedRequest =
    requests.find(
      (request) =>
        request.id ===
        selectedRequestId,
    );

  return (
    <div className="clinical-panel-grid laboratory-panel-grid">
      <article className="clinical-module-card clinical-lab-create">
        <div className="clinical-module-header">
          <div>
            <span>
              Diagnostics
            </span>

            <h3>
              Create laboratory request
            </h3>

            <p>
              Order laboratory tests and
              attach them to the current
              encounter.
            </p>
          </div>

          <div className="clinical-module-icon clinical-icon-violet">
            <FlaskConical
              size={18}
            />
          </div>
        </div>

        {!doctorId && (
          <div
            className="alert"
            role="alert"
          >
            A doctor is required before
            creating a laboratory request.
          </div>
        )}

        <form
          className="card-grid"
          onSubmit={submit}
        >
          <label className="field">
            <span>
              Priority
            </span>

            <select
              value={priority}
              onChange={(event) =>
                setPriority(
                  event.target
                    .value as LaboratoryPriority,
                )
              }
              disabled={
                !doctorId ||
                createMutation.isPending
              }
            >
              <option value="ROUTINE">
                Routine
              </option>

              <option value="URGENT">
                Urgent
              </option>

              <option value="STAT">
                STAT
              </option>
            </select>
          </label>

          <div className="field">
            <span>
              Linked context
            </span>

            <div className="clinical-context-tags">
              <span>
                Patient #{patientId}
              </span>

              {doctorId && (
                <span>
                  Doctor #{doctorId}
                </span>
              )}

              {appointmentId && (
                <span>
                  Appointment #
                  {appointmentId}
                </span>
              )}

              {medicalRecordId && (
                <span>
                  Record #
                  {medicalRecordId}
                </span>
              )}
            </div>
          </div>

          <label className="field field-wide">
            <span>
              Clinical notes
            </span>

            <textarea
              value={clinicalNotes}
              onChange={(event) =>
                setClinicalNotes(
                  event.target.value,
                )
              }
              maxLength={2048}
              disabled={
                !doctorId ||
                createMutation.isPending
              }
              placeholder="Optional context for the laboratory team..."
            />
          </label>

          <div className="field-wide">
            <div className="clinical-section-label">
              Ordered tests
            </div>
          </div>

          {tests.map(
            (test, index) => (
              <div
                className="clinical-item-block field-wide"
                key={index}
              >
                <label className="field">
                  <span>
                    Test code
                  </span>

                  <input
                    value={
                      test.testCode
                    }
                    onChange={(
                      event,
                    ) =>
                      updateTest(
                        index,
                        "testCode",
                        event.target
                          .value,
                      )
                    }
                    placeholder="CBC"
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>
                    Test name
                  </span>

                  <input
                    value={
                      test.testName
                    }
                    onChange={(
                      event,
                    ) =>
                      updateTest(
                        index,
                        "testName",
                        event.target
                          .value,
                      )
                    }
                    placeholder="Complete blood count"
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                  />
                </label>

                <div className="clinical-item-actions">
                  <button
                    className="app-button app-button-danger"
                    type="button"
                    onClick={() =>
                      removeTest(
                        index,
                      )
                    }
                    disabled={
                      tests.length === 1 ||
                      createMutation.isPending
                    }
                  >
                    <Trash2
                      size={14}
                    />
                    Remove
                  </button>
                </div>
              </div>
            ),
          )}

          <div className="field-wide clinical-form-actions">
            <button
              className="app-button"
              type="button"
              onClick={addTest}
              disabled={
                !doctorId ||
                createMutation.isPending
              }
            >
              <Plus size={15} />
              Add test
            </button>

            <button
              className="app-button primary"
              type="submit"
              disabled={
                !doctorId ||
                createMutation.isPending
              }
            >
              {createMutation.isPending
                ? "Creating..."
                : "Create request"}
            </button>
          </div>

          {createMutation.isError && (
            <div
              className="alert field-wide"
              role="alert"
            >
              Unable to create the
              laboratory request. Check
              the doctor, record, and
              test values.
            </div>
          )}
        </form>
      </article>

      <article className="clinical-module-card clinical-lab-requests">
        <div className="clinical-module-header">
          <div>
            <span>
              Laboratory history
            </span>

            <h3>
              Requests
            </h3>

            <p>
              Track requests, results,
              and release status.
            </p>
          </div>

          <span className="clinical-count">
            {requests.length}
          </span>
        </div>

        {requestsQuery.isLoading && (
          <div className="state-card clinical-inner-state">
            <div className="state-card-icon">
              <FlaskConical
                size={22}
              />
            </div>

            <h3>
              Loading requests
            </h3>

            <p>
              Retrieving laboratory
              history...
            </p>
          </div>
        )}

        {requestsQuery.isError && (
          <div
            className="alert"
            role="alert"
          >
            Unable to load
            laboratory requests.
          </div>
        )}

        {!requestsQuery.isLoading &&
          !requestsQuery.isError &&
          requests.length === 0 && (
            <div className="state-card clinical-inner-state">
              <div className="state-card-icon">
                <FlaskConical
                  size={22}
                />
              </div>

              <h3>
                No laboratory requests
              </h3>

              <p>
                Create the first request
                for this patient.
              </p>
            </div>
          )}

        {requests.length > 0 && (
          <div className="record-list">
            {requests.map(
              (request) => {
                const selected =
                  selectedRequest?.id ===
                  request.id;

                return (
                  <button
                    key={request.id}
                    className={`record-list-item ${selected
                        ? "is-selected"
                        : ""
                      }`}
                    type="button"
                    onClick={() =>
                      setSelectedRequestId(
                        request.id,
                      )
                    }
                    aria-pressed={
                      selected
                    }
                  >
                    <span className="record-list-main">
                      <strong>
                        Request #
                        {request.id}
                      </strong>

                      <small>
                        {request.priority}
                        {" · "}
                        {request.status}
                      </small>

                      <small>
                        {
                          request.items
                            .length
                        }{" "}
                        test(s)
                      </small>
                    </span>

                    <span className="record-list-meta">
                      {request.status}
                    </span>
                  </button>
                );
              },
            )}
          </div>
        )}

        {selectedRequest && (
          <div className="clinical-result-container">
            <LaboratoryResultsPanel
              request={selectedRequest}
            />
          </div>
        )}
      </article>
    </div>
  );
}