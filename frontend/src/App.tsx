import { lazy, Suspense, useEffect, useState } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";
import { LoginPage } from "./features/auth/LoginPage";
import { SplashScreen } from "./features/auth/SplashScreen";
import { AppShell } from "./layout/AppShell";
import { ProtectedRoute } from "./router/ProtectedRoute";
import {
  getRefreshToken,
  setCurrentUser,
} from "./features/auth/session";
import {
  refreshSession,
  getAuthenticatedUser,
} from "./features/auth/api";

const DashboardPage = lazy(
  () => import("./features/dashboard/DashboardPage")
    .then((module) => ({ default: module.DashboardPage })),
);

const NotificationsPage = lazy(
  () => import("./features/notifications/NotificationsPage")
    .then((module) => ({ default: module.NotificationsPage })),
);

const PatientsPage = lazy(
  () => import("./features/patients/PatientsPage")
    .then((module) => ({ default: module.PatientsPage })),
);

const PatientForm = lazy(
  () => import("./features/patients/PatientForm")
    .then((module) => ({ default: module.PatientForm })),
);

const PatientDetailPage = lazy(
  () => import("./features/patients/PatientDetailPage")
    .then((module) => ({ default: module.PatientDetailPage })),
);

const PatientEditPage = lazy(
  () => import("./features/patients/PatientEditPage")
    .then((module) => ({ default: module.PatientEditPage })),
);

const PatientClinicalPage = lazy(
  () => import("./features/patients/PatientClinicalPage")
    .then((module) => ({ default: module.PatientClinicalPage })),
);

const DoctorsPage = lazy(
  () => import("./features/doctors/DoctorsPage")
    .then((module) => ({ default: module.DoctorsPage })),
);

const DoctorForm = lazy(
  () => import("./features/doctors/DoctorForm")
    .then((module) => ({ default: module.DoctorForm })),
);

const DoctorDetailPage = lazy(
  () => import("./features/doctors/DoctorDetailPage")
    .then((module) => ({ default: module.DoctorDetailPage })),
);

const DoctorEditPage = lazy(
  () => import("./features/doctors/DoctorEditPage")
    .then((module) => ({ default: module.DoctorEditPage })),
);

const AppointmentsPage = lazy(
  () => import("./features/appointments/AppointmentsPage")
    .then((module) => ({ default: module.AppointmentsPage })),
);

const AppointmentForm = lazy(
  () => import("./features/appointments/AppointmentForm")
    .then((module) => ({ default: module.AppointmentForm })),
);

const AppointmentDetailPage = lazy(
  () => import("./features/appointments/AppointmentDetailPage")
    .then((module) => ({ default: module.AppointmentDetailPage })),
);

const MedicinesPage = lazy(
  () => import("./features/medicines/MedicinesPage")
    .then((module) => ({ default: module.MedicinesPage })),
);

const InventoryPage = lazy(
  () => import("./features/inventory/InventoryPage")
    .then((module) => ({ default: module.InventoryPage })),
);

const InvoicesPage = lazy(
  () => import("./features/invoices/InvoicesPage")
    .then((module) => ({ default: module.InvoicesPage })),
);

const queryClient = new QueryClient();

function RouteLoading() {
  return (
    <div
      className="route-loading"
      role="status"
      aria-live="polite"
      aria-label="Loading workspace"
    >
      <div className="route-loading-orbit" />
      <div>
        <strong>MedCore</strong>
        <span>Loading workspace...</span>
      </div>
    </div>
  );
}

function AuthBootstrap({
  children,
}: {
  children: React.ReactNode;
}) {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      const refreshToken = getRefreshToken();

      if (!refreshToken) {
        if (!cancelled) {
          setReady(true);
        }
        return;
      }

      try {
        await refreshSession();

        const user = await getAuthenticatedUser();

        if (!cancelled) {
          setCurrentUser(user);
        }
      } catch {
        localStorage.removeItem("medcore.refreshToken");
        localStorage.removeItem("medcore.user");
      } finally {
        if (!cancelled) {
          setReady(true);
        }
      }
    }

    void bootstrap();

    return () => {
      cancelled = true;
    };
  }, []);

  if (!ready) {
    return <SplashScreen onComplete={() => undefined} />;
  }

  return <>{children}</>;
}

function App() {
  const [showSplash, setShowSplash] = useState(true);

  if (showSplash) {
    return <SplashScreen onComplete={() => setShowSplash(false)} />;
  }

  return (
    <AuthBootstrap>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Suspense fallback={<RouteLoading />}>
            <Routes>
              <Route path="/login" element={<LoginPage />} />

              <Route element={<ProtectedRoute />}>
                <Route element={<AppShell />}>
                  <Route path="/" element={<DashboardPage />} />
                  <Route
                    path="/notifications"
                    element={<NotificationsPage />}
                  />
                  <Route path="/patients" element={<PatientsPage />} />
                  <Route path="/patients/new" element={<PatientForm />} />
                  <Route
                    path="/patients/:patientId"
                    element={<PatientDetailPage />}
                  />
                  <Route
                    path="/patients/:patientId/edit"
                    element={<PatientEditPage />}
                  />
                  <Route
                    path="/patients/:patientId/clinical"
                    element={<PatientClinicalPage />}
                  />
                  <Route path="/doctors/new" element={<DoctorForm />} />
                  <Route path="/doctors/:doctorId/edit" element={<DoctorEditPage />} />
                  <Route path="/doctors/:doctorId" element={<DoctorDetailPage />} />
                  <Route path="/doctors" element={<DoctorsPage />} />
                  <Route path="/appointments/new" element={<AppointmentForm />} />
                  <Route
                    path="/appointments/:appointmentId"
                    element={<AppointmentDetailPage />}
                  />
                  <Route path="/appointments" element={<AppointmentsPage />} />
                  <Route
                    path="/medical-records"
                    element={<Navigate to="/patients" replace />}
                  />
                  <Route
                    path="/laboratory"
                    element={<Navigate to="/patients" replace />}
                  />
                  <Route path="/medicines" element={<MedicinesPage />} />
                  <Route path="/inventory" element={<InventoryPage />} />
                  <Route path="/invoices" element={<InvoicesPage />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Route>
              </Route>

              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </QueryClientProvider>
    </AuthBootstrap>
  );
}

export default App;