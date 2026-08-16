import { apiClient } from "../../api/client";
import type { ApiResponse, PagedApiResponse } from "../../types/api";

export type InvoiceStatus =
  | "DRAFT"
  | "ISSUED"
  | "PARTIALLYPAID"
  | "PAID"
  | "VOID"
  | "OVERDUE";

export type InvoiceLine = {
  id: number;
  lineNumber: number;
  itemType: string;
  description: string;
  quantity: number;
  unitPrice: number;
  discountAmount: number;
  taxAmount: number;
  lineTotalAmount: number;
};

export type Invoice = {
  id: number;
  invoiceNumber: string;
  patientId: number;
  appointmentId: number | null;
  status: InvoiceStatus;
  currencyCode: string;
  issueDate: string;
  dueDate: string | null;
  subtotalAmount: number;
  discountAmount: number;
  taxAmount: number;
  totalAmount: number;
  paidAmount: number;
  balanceAmount: number;
  issuedAt: string | null;
  voidedAt: string | null;
  voidReason: string | null;
  lines: InvoiceLine[];
};

export type CreateInvoiceInput = {
  patientId: number;
  appointmentId?: number;
  currencyCode: string;
  dueDate?: string;
  lines: Array<{
    lineNumber: number;
    itemType: string;
    description: string;
    quantity: number;
    unitPrice: number;
    discountAmount?: number;
    taxAmount?: number;
  }>;
};

export type PaymentMethod = "CASH" | "CARD" | "BANKTRANSFER" | "INSURANCE" | "OTHER";
export type PaymentStatus = "RECORDED" | "VOIDED";

export type Payment = {
  id: number;
  paymentNumber: string;
  invoiceId: number;
  patientId: number;
  amount: number;
  currencyCode: string;
  paymentMethod: PaymentMethod;
  paymentReference: string | null;
  paidAt: string;
  recordedByUserId: number;
  status: PaymentStatus;
  voidedAt: string | null;
  voidReason: string | null;
};

export type CreatePaymentInput = {
  invoiceId: number;
  amount: number;
  paymentMethod: PaymentMethod;
  paymentReference?: string;
};

export type VoidPaymentInput = {
  reason: string;
};

export async function getPatientInvoices(
  patientId: number,
): Promise<PagedApiResponse<Invoice>> {
  const response = await apiClient.get<PagedApiResponse<Invoice>>(
    `/invoices/patient/${patientId}`,
    { params: { page: 0, size: 50, sort: "issueDate,desc" } },
  );
  return response.data;
}

export async function getInvoice(
  invoiceId: number,
): Promise<ApiResponse<Invoice>> {
  const response = await apiClient.get<ApiResponse<Invoice>>(
    `/invoices/${invoiceId}`,
  );
  return response.data;
}

export async function createInvoice(
  input: CreateInvoiceInput,
): Promise<ApiResponse<Invoice>> {
  const response = await apiClient.post<ApiResponse<Invoice>>(
    "/invoices",
    input,
  );
  return response.data;
}

export async function issueInvoice(
  invoiceId: number,
): Promise<ApiResponse<Invoice>> {
  const response = await apiClient.post<ApiResponse<Invoice>>(
    `/invoices/${invoiceId}/issue`,
  );
  return response.data;
}

export async function recordPayment(
  input: CreatePaymentInput,
): Promise<ApiResponse<Payment>> {
  const response = await apiClient.post<ApiResponse<Payment>>(
    "/payments",
    input,
  );
  return response.data;
}

export async function getPaymentsByInvoice(
  invoiceId: number,
): Promise<ApiResponse<Payment[]>> {
  const response = await apiClient.get<ApiResponse<Payment[]>>(
    `/payments/invoice/${invoiceId}`,
  );
  return response.data;
}

export async function voidPayment(
  paymentId: number,
  input: VoidPaymentInput,
): Promise<ApiResponse<Payment>> {
  const response = await apiClient.post<ApiResponse<Payment>>(
    `/payments/${paymentId}/void`,
    input,
  );
  return response.data;
}
