import type { PagedApiResponse } from "../../types/api";

export type PatientSex =
  | "MALE"
  | "FEMALE"
  | "OTHER"
  | "UNKNOWN";

export type Patient = {
  id: number;
  userAccountId: number | null;
  medicalRecordNumber: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  sex: PatientSex;
  email: string | null;
  phoneNumber: string | null;
  addressLine1: string | null;
  addressLine2: string | null;
  city: string | null;
  state: string | null;
  postalCode: string | null;
  country: string | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
  bloodGroup: string | null;
  allergies: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type PatientPage = PagedApiResponse<Patient>;
