import type { PagedApiResponse } from "../../types/api";

export type AppointmentStatus =
  | "SCHEDULED"
  | "CHECKED_IN"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELLED"
  | "NO_SHOW";

export type Appointment = {
  id: number;
  patientId: number;
  doctorId: number;
  departmentId: number;
  scheduledStartAt: string;
  scheduledEndAt: string;
  status: AppointmentStatus;
  reason: string;
  schedulingNotes: string | null;
  checkInAt: string | null;
  queuePosition: number | null;
  cancelledAt: string | null;
  cancellationReason: string | null;
  createdAt: string;
  updatedAt: string;
};

export type AppointmentPage =
  PagedApiResponse<Appointment>;

export type CreateAppointmentInput = {
  patientId: number;
  doctorId: number;
  departmentId: number;
  scheduledStartAt: string;
  scheduledEndAt: string;
  reason: string;
  schedulingNotes?: string;
};
