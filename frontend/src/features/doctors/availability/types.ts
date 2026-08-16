import type { PagedApiResponse } from "../../../types/api";

export type DoctorAvailability = {
  id: number;
  doctorId: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  effectiveFrom: string;
  effectiveUntil: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type AvailabilityPage =
  PagedApiResponse<DoctorAvailability>;

export type CreateAvailabilityInput = {
  doctorId: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  effectiveFrom: string;
  effectiveUntil?: string;
};

export type UpdateAvailabilityInput = Partial<
  Omit<CreateAvailabilityInput, "doctorId">
>;
