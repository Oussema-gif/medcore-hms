import { useState } from "react";
import {
  CreditCard,
  Plus,
  Receipt,
  Trash2,
} from "lucide-react";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import axios from "axios";

import {
  createInvoice,
  getPatientInvoices,
  getPaymentsByInvoice,
  issueInvoice,
  recordPayment,
  voidPayment,
  type CreateInvoiceInput,
  type PaymentMethod,
} from "./api";

type InvoicesPanelProps = {
  patientId: number;
  appointmentId?: number;
};

type LineDraft = {
  itemType: string;
  description: string;
  quantity: string;
  unitPrice: string;
  discountAmount: string;
  taxAmount: string;
};

const emptyLine: LineDraft = {
  itemType: "",
  description: "",
  quantity: "1",
  unitPrice: "",
  discountAmount: "0",
  taxAmount: "0",
};

const paymentMethods: PaymentMethod[] = [
  "CASH",
  "CARD",
  "BANKTRANSFER",
  "INSURANCE",
  "OTHER",
];

export function InvoicesPanel({
  patientId,
  appointmentId,
}: InvoicesPanelProps) {
  const queryClient = useQueryClient();

  const [lines, setLines] = useState<LineDraft[]>([
    { ...emptyLine },
  ]);

  const [currencyCode, setCurrencyCode] = useState("USD");

  const [dueDate, setDueDate] = useState("");

  const [
    selectedInvoiceId,
    setSelectedInvoiceId,
  ] = useState<number | null>(null);

  const [paymentAmount, setPaymentAmount] = useState("");

  const [paymentMethod, setPaymentMethod] =
    useState<PaymentMethod>("CASH");

  const [
    paymentReference,
    setPaymentReference,
  ] = useState("");

  const invoicesQuery = useQuery({
    queryKey: [
      "invoices",
      patientId,
    ],

    queryFn: () =>
      getPatientInvoices(
        patientId,
      ),

    enabled: patientId > 0,
  });

  const paymentsQuery = useQuery({
    queryKey: [
      "payments",
      selectedInvoiceId,
    ],

    queryFn: () =>
      getPaymentsByInvoice(
        selectedInvoiceId as number,
      ),

    enabled:
      selectedInvoiceId !== null,
  });

  const invoices =
    invoicesQuery.data?.data ?? [];

  const selectedInvoice =
    invoices.find(
      (invoice) =>
        invoice.id ===
        selectedInvoiceId,
    );

  const payments =
    paymentsQuery.data?.data ?? [];

  const createMutation = useMutation({
    mutationFn: () => {
      const parsedLines = lines
        .filter(
          (line) =>
            line.itemType &&
            line.description &&
            line.unitPrice,
        )
        .map(
          (line, index) => ({
            lineNumber: index + 1,

            itemType:
              line.itemType.trim(),

            description:
              line.description.trim(),

            quantity:
              Number(
                line.quantity,
              ) || 1,

            unitPrice:
              Number(
                line.unitPrice,
              ) || 0,

            discountAmount:
              Number(
                line.discountAmount,
              ) || 0,

            taxAmount:
              Number(
                line.taxAmount,
              ) || 0,
          }),
        );

      if (
        parsedLines.length === 0
      ) {
        throw new Error(
          "At least one complete invoice line is required.",
        );
      }

      const payload: CreateInvoiceInput = {
        patientId,
        appointmentId,

        currencyCode:
          currencyCode
            .trim()
            .toUpperCase(),

        dueDate:
          dueDate || undefined,

        lines: parsedLines,
      };

      return createInvoice(
        payload,
      );
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries(
        {
          queryKey: [
            "invoices",
            patientId,
          ],
        },
      );

      setLines([
        { ...emptyLine },
      ]);

      setDueDate("");
    },
  });

  const issueMutation = useMutation({
    mutationFn: (
      invoiceId: number,
    ) =>
      issueInvoice(
        invoiceId,
      ),

    onSuccess: async () => {
      await queryClient.invalidateQueries(
        {
          queryKey: [
            "invoices",
            patientId,
          ],
        },
      );
    },
  });

  const paymentMutation = useMutation({
    mutationFn: () => {
      if (
        !selectedInvoiceId
      ) {
        throw new Error(
          "Select an invoice first.",
        );
      }

      return recordPayment({
        invoiceId:
          selectedInvoiceId,

        amount:
          Number(
            paymentAmount,
          ),

        paymentMethod,

        paymentReference:
          paymentReference.trim() ||
          undefined,
      });
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries(
        {
          queryKey: [
            "invoices",
            patientId,
          ],
        },
      );

      await queryClient.invalidateQueries(
        {
          queryKey: [
            "payments",
            selectedInvoiceId,
          ],
        },
      );

      setPaymentAmount("");
      setPaymentReference("");
    },
  });

  const voidMutation = useMutation({
    mutationFn: ({
      id,
      reason,
    }: {
      id: number;
      reason: string;
    }) =>
      voidPayment(id, {
        reason,
      }),

    onSuccess: async () => {
      await queryClient.invalidateQueries(
        {
          queryKey: [
            "invoices",
            patientId,
          ],
        },
      );

      await queryClient.invalidateQueries(
        {
          queryKey: [
            "payments",
            selectedInvoiceId,
          ],
        },
      );
    },
  });

  function updateLine(
    index: number,
    field: keyof LineDraft,
    value: string,
  ) {
    setLines((current) =>
      current.map(
        (line, lineIndex) =>
          lineIndex === index
            ? {
                ...line,
                [field]: value,
              }
            : line,
      ),
    );
  }

  function removeLine(
    index: number,
  ) {
    setLines((current) =>
      current.length === 1
        ? current
        : current.filter(
            (_, lineIndex) =>
              lineIndex !== index,
          ),
    );
  }

  function addLine() {
    setLines((current) => [
      ...current,
      { ...emptyLine },
    ]);
  }

  return (
    <div className="clinical-panel-grid billing-panel-grid">
      <article className="clinical-module-card clinical-billing-create">
        <div className="clinical-module-header">
          <div>
            <span>Patient billing</span>

            <h3>Create invoice</h3>

            <p>
              Add charge lines and prepare
              the patient's invoice.
            </p>
          </div>

          <div className="clinical-module-icon clinical-icon-green">
            <Receipt size={18} />
          </div>
        </div>

        <form
          className="card-grid"
          onSubmit={(event) => {
            event.preventDefault();
            createMutation.mutate();
          }}
        >
          <label className="field">
            <span>Currency code</span>

            <input
              value={currencyCode}
              onChange={(event) =>
                setCurrencyCode(
                  event.target.value,
                )
              }
              maxLength={3}
              required
            />
          </label>

          <label className="field">
            <span>Due date</span>

            <input
              type="date"
              value={dueDate}
              onChange={(event) =>
                setDueDate(
                  event.target.value,
                )
              }
            />
          </label>

          <div className="field-wide">
            <div className="clinical-section-label">
              Invoice lines
            </div>
          </div>

          {lines.map(
            (line, index) => (
              <div
                className="clinical-item-block field-wide"
                key={index}
              >
                <label className="field">
                  <span>Item type</span>

                  <input
                    value={
                      line.itemType
                    }
                    onChange={(
                      event,
                    ) =>
                      updateLine(
                        index,
                        "itemType",
                        event.target
                          .value,
                      )
                    }
                    placeholder="CONSULTATION"
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field field-wide">
                  <span>Description</span>

                  <input
                    value={
                      line.description
                    }
                    onChange={(
                      event,
                    ) =>
                      updateLine(
                        index,
                        "description",
                        event.target
                          .value,
                      )
                    }
                    placeholder="Consultation fee"
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>Quantity</span>

                  <input
                    type="number"
                    min={0.0001}
                    step="any"
                    value={
                      line.quantity
                    }
                    onChange={(
                      event,
                    ) =>
                      updateLine(
                        index,
                        "quantity",
                        event.target
                          .value,
                      )
                    }
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>Unit price</span>

                  <input
                    type="number"
                    min={0}
                    step="any"
                    value={
                      line.unitPrice
                    }
                    onChange={(
                      event,
                    ) =>
                      updateLine(
                        index,
                        "unitPrice",
                        event.target
                          .value,
                      )
                    }
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>Discount</span>

                  <input
                    type="number"
                    min={0}
                    step="any"
                    value={
                      line.discountAmount
                    }
                    onChange={(
                      event,
                    ) =>
                      updateLine(
                        index,
                        "discountAmount",
                        event.target
                          .value,
                      )
                    }
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>Tax</span>

                  <input
                    type="number"
                    min={0}
                    step="any"
                    value={
                      line.taxAmount
                    }
                    onChange={(
                      event,
                    ) =>
                      updateLine(
                        index,
                        "taxAmount",
                        event.target
                          .value,
                      )
                    }
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <div className="clinical-item-actions">
                  <button
                    className="app-button app-button-danger"
                    type="button"
                    onClick={() =>
                      removeLine(
                        index,
                      )
                    }
                    disabled={
                      lines.length ===
                        1 ||
                      createMutation.isPending
                    }
                  >
                    <Trash2
                      size={14}
                    />

                    Remove line
                  </button>
                </div>
              </div>
            ),
          )}

          <div className="field-wide clinical-form-actions">
            <button
              className="app-button"
              type="button"
              onClick={addLine}
              disabled={
                createMutation.isPending
              }
            >
              <Plus size={15} />
              Add line
            </button>

            <button
              className="app-button primary"
              type="submit"
              disabled={
                createMutation.isPending
              }
            >
              {createMutation.isPending
                ? "Creating..."
                : "Create invoice"}
            </button>
          </div>

          {createMutation.isError && (
            <div
              className="alert field-wide"
              role="alert"
            >
              {getApiErrorMessage(
                createMutation.error,
              )}
            </div>
          )}
        </form>
      </article>

      <article className="clinical-module-card clinical-billing-history">
        <div className="clinical-module-header">
          <div>
            <span>Financial history</span>

            <h3>Invoices</h3>

            <p>
              Review balances, issue invoices,
              and record payments.
            </p>
          </div>

          <span className="clinical-count">
            {invoices.length}
          </span>
        </div>

        {invoicesQuery.isLoading && (
          <div className="state-card clinical-inner-state">
            <div className="state-card-icon">
              <Receipt size={22} />
            </div>

            <h3>Loading invoices</h3>

            <p>
              Retrieving financial history...
            </p>
          </div>
        )}

        {invoicesQuery.isError && (
          <div
            className="alert"
            role="alert"
          >
            Unable to load invoices.
          </div>
        )}

        {!invoicesQuery.isLoading &&
          !invoicesQuery.isError &&
          invoices.length === 0 && (
            <div className="state-card clinical-inner-state">
              <div className="state-card-icon">
                <Receipt size={22} />
              </div>

              <h3>No invoices</h3>

              <p>
                No billing records have been
                created for this patient.
              </p>
            </div>
          )}

        {invoices.length > 0 && (
          <div className="record-list">
            {invoices.map(
              (invoice) => (
                <button
                  key={invoice.id}
                  className={`record-list-item ${
                    selectedInvoiceId ===
                    invoice.id
                      ? "is-selected"
                      : ""
                  }`}
                  type="button"
                  onClick={() =>
                    setSelectedInvoiceId(
                      invoice.id,
                    )
                  }
                  aria-pressed={
                    selectedInvoiceId ===
                    invoice.id
                  }
                >
                  <span className="record-list-main">
                    <strong>
                      {
                        invoice.invoiceNumber
                      }
                    </strong>

                    <small>
                      {invoice.status}
                      {" · Total "}
                      {
                        invoice.totalAmount
                      }{" "}
                      {
                        invoice.currencyCode
                      }
                    </small>

                    <small>
                      Balance:{" "}
                      {
                        invoice.balanceAmount
                      }{" "}
                      {
                        invoice.currencyCode
                      }
                    </small>
                  </span>

                  <span className="record-list-meta">
                    {invoice.status}
                  </span>
                </button>
              ),
            )}
          </div>
        )}

        {invoices.length > 0 &&
          !selectedInvoice && (
            <div className="state-card clinical-inner-state">
              <div className="state-card-icon">
                <Receipt size={22} />
              </div>

              <h3>Select an invoice</h3>

              <p>
                Choose an invoice to
                inspect its details and
                payments.
              </p>
            </div>
          )}

        {selectedInvoice && (
          <div className="clinical-selected-detail">
            <div className="billing-summary">
              <div>
                <span>Status</span>

                <strong>
                  <span className="app-user-pill">
                    {
                      selectedInvoice.status
                    }
                  </span>
                </strong>
              </div>

              <div>
                <span>Total</span>

                <strong>
                  {
                    selectedInvoice.totalAmount
                  }{" "}
                  {
                    selectedInvoice.currencyCode
                  }
                </strong>
              </div>

              <div>
                <span>Balance</span>

                <strong className="billing-balance">
                  {
                    selectedInvoice.balanceAmount
                  }{" "}
                  {
                    selectedInvoice.currencyCode
                  }
                </strong>
              </div>
            </div>

            <div className="clinical-section-label">
              Invoice lines
            </div>

            <div className="record-list">
              {selectedInvoice.lines.map(
                (line) => (
                  <div
                    className="record-list-item clinical-static-item"
                    key={line.id}
                  >
                    <span className="record-list-main">
                      <strong>
                        {
                          line.description
                        }
                      </strong>

                      <small>
                        {
                          line.itemType
                        }{" "}
                        · Qty{" "}
                        {
                          line.quantity
                        }{" "}
                        ×{" "}
                        {
                          line.unitPrice
                        }
                      </small>

                      <small>
                        Line total:{" "}
                        {
                          line.lineTotalAmount
                        }
                      </small>
                    </span>
                  </div>
                ),
              )}
            </div>

            {issueMutation.isError && (
              <div
                className="alert"
                role="alert"
              >
                {getApiErrorMessage(
                  issueMutation.error,
                )}
              </div>
            )}

            {selectedInvoice.status ===
              "DRAFT" && (
              <div className="clinical-form-actions clinical-detail-actions">
                <button
                  className="app-button primary"
                  type="button"
                  disabled={
                    issueMutation.isPending
                  }
                  onClick={() =>
                    issueMutation.mutate(
                      selectedInvoice.id,
                    )
                  }
                >
                  {
                    issueMutation.isPending
                      ? "Issuing..."
                      : "Issue invoice"
                  }
                </button>
              </div>
            )}

            {[
              "ISSUED",
              "PARTIALLYPAID",
            ].includes(
              selectedInvoice.status,
            ) && (
              <form
                className="clinical-payment-form"
                onSubmit={(event) => {
                  event.preventDefault();
                  paymentMutation.mutate();
                }}
              >
                <div className="clinical-section-label">
                  Record payment
                </div>

                <div className="card-grid">
                  <label className="field">
                    <span>Amount</span>

                    <input
                      type="number"
                      min={0.0001}
                      step="any"
                      max={
                        selectedInvoice.balanceAmount
                      }
                      value={
                        paymentAmount
                      }
                      onChange={(
                        event,
                      ) =>
                        setPaymentAmount(
                          event.target
                            .value,
                        )
                      }
                      required
                    />
                  </label>

                  <label className="field">
                    <span>Method</span>

                    <select
                      value={
                        paymentMethod
                      }
                      onChange={(
                        event,
                      ) =>
                        setPaymentMethod(
                          event.target
                            .value as PaymentMethod,
                        )
                      }
                    >
                      {paymentMethods.map(
                        (method) => (
                          <option
                            key={
                              method
                            }
                            value={
                              method
                            }
                          >
                            {method}
                          </option>
                        ),
                      )}
                    </select>
                  </label>

                  <label className="field field-wide">
                    <span>Reference</span>

                    <input
                      value={
                        paymentReference
                      }
                      onChange={(
                        event,
                      ) =>
                        setPaymentReference(
                          event.target
                            .value,
                        )
                      }
                      maxLength={256}
                      placeholder="Optional payment reference"
                    />
                  </label>

                  {paymentMutation.isError && (
                    <div
                      className="alert field-wide"
                      role="alert"
                    >
                      {getApiErrorMessage(
                        paymentMutation.error,
                      )}
                    </div>
                  )}

                  <div className="field-wide">
                    <button
                      className="app-button primary"
                      type="submit"
                      disabled={
                        paymentMutation.isPending
                      }
                    >
                      <CreditCard
                        size={15}
                      />

                      {
                        paymentMutation.isPending
                          ? "Recording..."
                          : "Record payment"
                      }
                    </button>
                  </div>
                </div>
              </form>
            )}

            <div className="clinical-section-label clinical-payments-label">
              Payments
            </div>

            {paymentsQuery.isLoading && (
              <div className="state-card clinical-inner-state">
                <div className="state-card-icon">
                  <CreditCard size={22} />
                </div>

                <h3>Loading payments</h3>

                <p>
                  Retrieving payment history...
                </p>
              </div>
            )}

            {!paymentsQuery.isLoading &&
              payments.length === 0 && (
                <div className="state-card clinical-inner-state compact">
                  <p>
                    No payments recorded for
                    this invoice.
                  </p>
                </div>
              )}

            {payments.length > 0 && (
              <div className="record-list">
                {payments.map(
                  (payment) => (
                    <div
                      className="record-list-item clinical-static-item"
                      key={payment.id}
                    >
                      <span className="record-list-main">
                        <strong>
                          {
                            payment.paymentNumber
                          }
                        </strong>

                        <small>
                          {
                            payment.amount
                          }{" "}
                          {
                            payment.currencyCode
                          }{" "}
                          ·{" "}
                          {
                            payment.paymentMethod
                          }
                        </small>

                        <small>
                          {
                            payment.status
                          }
                        </small>
                      </span>

                      {payment.status ===
                        "RECORDED" && (
                        <button
                          className="app-button app-button-danger"
                          type="button"
                          disabled={
                            voidMutation.isPending
                          }
                          onClick={() => {
                            const reason =
                              window.prompt(
                                "Void reason:",
                              );

                            if (
                              reason &&
                              reason.trim()
                            ) {
                              voidMutation.mutate(
                                {
                                  id: payment.id,
                                  reason:
                                    reason.trim(),
                                },
                              );
                            }
                          }}
                        >
                          Void
                        </button>
                      )}
                    </div>
                  ),
                )}
              </div>
            )}
          </div>
        )}
      </article>
    </div>
  );
}

function getApiErrorMessage(
  error: unknown,
): string {
  if (axios.isAxiosError(error)) {
    const data =
      error.response?.data as {
        message?: unknown;
      } | undefined;

    if (
      typeof data?.message ===
      "string"
    ) {
      return data.message;
    }

    return `Request failed with status ${
      error.response?.status ??
      "unknown"
    }.`;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "An unexpected error occurred.";
}