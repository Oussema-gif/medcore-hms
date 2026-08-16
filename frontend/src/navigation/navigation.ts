import type { UserSummary } from "../features/auth/types";

export type NavigationItem = {
  id: string;
  label: string;
  path: string;
  roles: string[];
};

export const navigationItems: NavigationItem[] = [
  {
    id: "dashboard",
    label: "Dashboard",
    path: "/",
    roles: ["ADMINISTRATOR", "DOCTOR", "RECEPTIONIST", "PATIENT"],
  },
  {
    id: "patients",
    label: "Patients",
    path: "/patients",
    roles: ["ADMINISTRATOR", "DOCTOR", "RECEPTIONIST"],
  },
  {
    id: "appointments",
    label: "Appointments",
    path: "/appointments",
    roles: ["ADMINISTRATOR", "DOCTOR", "RECEPTIONIST", "PATIENT"],
  },
  {
    id: "doctors",
    label: "Doctors",
    path: "/doctors",
    roles: ["ADMINISTRATOR", "DOCTOR", "RECEPTIONIST"],
  },
  {
    id: "medical-records",
    label: "Medical records",
    path: "/patients",
    roles: ["ADMINISTRATOR", "DOCTOR", "PATIENT"],
  },
  {
    id: "laboratory",
    label: "Laboratory",
    path: "/patients",
    roles: ["ADMINISTRATOR", "DOCTOR", "NURSE"],
  },
  {
    id: "prescriptions",
    label: "Prescriptions",
    path: "/patients",
    roles: ["ADMINISTRATOR", "DOCTOR", "PATIENT"],
  },
  {
    id: "medicines",
    label: "Medicines",
    path: "/medicines",
    roles: ["ADMINISTRATOR", "DOCTOR", "RECEPTIONIST"],
  },
  {
    id: "inventory",
    label: "Inventory",
    path: "/inventory",
    roles: ["ADMINISTRATOR", "RECEPTIONIST"],
  },
  {
    id: "invoices",
    label: "Invoices",
    path: "/invoices",
    roles: ["ADMINISTRATOR", "RECEPTIONIST", "PATIENT"],
  },
  {
    id: "notifications",
    label: "Notifications",
    path: "/notifications",
    roles: ["ADMINISTRATOR", "DOCTOR", "RECEPTIONIST", "PATIENT"],
  },
];

export function getNavigationForUser(
  user: UserSummary | null,
): NavigationItem[] {
  if (!user) return [];

  return navigationItems.filter((item) =>
    item.roles.some((role) => user.roles.includes(role)),
  );
}
