import type { PagedApiResponse } from "../../types/api";

export type Doctor = {
  id: number;
  userAccountId: number;
  departmentId: number;
  professionalRegistrationNumber: string;
  firstName: string;
  lastName: string;
  specialty: string | null;
  phoneNumber: string | null;
  email: string | null;
  consultationDurationMinutes: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type DoctorPage = PagedApiResponse<Doctor>;

export type CreateDoctorInput = {
  userAccountId: number;
  departmentId: number;
  professionalRegistrationNumber: string;
  firstName: string;
  lastName: string;
  specialty?: string;
  phoneNumber?: string;
  email?: string;
  consultationDurationMinutes: number;
};

export type UpdateDoctorInput = Partial<CreateDoctorInput>;
