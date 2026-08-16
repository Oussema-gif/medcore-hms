import { motion } from "framer-motion";
import {
  Activity,
  ArrowUpRight,
  CalendarDays,
  ChevronRight,
  FlaskConical,
  HeartPulse,
  Plus,
  ShieldCheck,
  Users,
} from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";

import { getCurrentUser } from "../auth/session";
import { getDashboardSummary } from "./api";

type MetricTone = "cyan" | "blue" | "violet" | "green";

type MetricConfig = {
  key: string;
  label: string;
  detail: string;
  icon: typeof Users;
  tone: MetricTone;
};

const metricConfigs: MetricConfig[] = [
  {
    key: "patients",
    label: "Active patients",
    detail: "Patients currently registered",
    icon: Users,
    tone: "blue",
  },
  {
    key: "appointments",
    label: "Today's appointments",
    detail: "Scheduled consultations",
    icon: CalendarDays,
    tone: "cyan",
  },
  {
    key: "laboratory",
    label: "Pending laboratory",
    detail: "Awaiting clinical action",
    icon: FlaskConical,
    tone: "violet",
  },
  {
    key: "system",
    label: "System status",
    detail: "Core services operational",
    icon: ShieldCheck,
    tone: "green",
  },
];

const quickActions = [
  {
    label: "Register patient",
    description: "Create a new patient record",
    icon: Plus,
    path: "/patients/new",
  },
  {
    label: "Appointments",
    description: "Review today's schedule",
    icon: CalendarDays,
    path: "/appointments",
  },
  {
    label: "Clinical workspace",
    description: "Open patient care workflows",
    icon: HeartPulse,
    path: "/patients",
  },
  {
    label: "Laboratory",
    description: "Review pending clinical work",
    icon: FlaskConical,
    path: "/patients",
  },
];

export function DashboardPage() {
  const user = getCurrentUser();
  const displayName =
    user?.displayName?.trim() ||
    user?.username ||
    "Administrator";

  const navigate = useNavigate();

  const dashboardQuery = useQuery({
    queryKey: ["dashboard", "summary"],
    queryFn: getDashboardSummary,
  });

  const summary = dashboardQuery.data;

  function formatMetricValue(key: string): string | number {
    if (dashboardQuery.isError || !summary) {
      return "—";
    }

    switch (key) {
      case "patients":
        return summary.activePatients;

      case "appointments":
        return summary.todayAppointments;

      case "laboratory":
        return summary.pendingLaboratoryWork;

      case "system":
        return summary.systemStatus;

      default:
        return "—";
    }
  }

  const firstName = displayName.split(" ")[0];

  return (
    <section className="dashboard-page">
      {/* HERO */}
      <motion.header
        className="dashboard-hero dashboard-hero-command"
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{
          duration: 0.45,
          ease: [0.22, 1, 0.36, 1],
        }}
      >
        <div className="dashboard-hero-copy">
          <div className="dashboard-status-line">
            <span className="dashboard-status-dot" />
            MEDCORE COMMAND CENTER
          </div>

          <h2>
            Good morning,{" "}
            <span>{firstName}</span>
          </h2>

          <p>
            Your hospital operations are ready.
            Monitor activity, patient care, and today's
            clinical workflow from one workspace.
          </p>
        </div>

        <div className="dashboard-hero-meta">
          <div className="dashboard-date-card">
            <span>Today</span>
            <strong>
              {new Intl.DateTimeFormat("en-US", {
                weekday: "short",
                day: "2-digit",
                month: "short",
              }).format(new Date())}
            </strong>
          </div>

          <div className="dashboard-role">
            <ShieldCheck size={14} />
            {user?.roles?.[0] ?? "USER"}
          </div>
        </div>
      </motion.header>

      {/* METRICS */}
      <div className="dashboard-metrics dashboard-metrics-command">
        {metricConfigs.map((metric, index) => {
          const Icon = metric.icon;
          const value = formatMetricValue(metric.key);

          return (
            <motion.article
              className={`dashboard-metric dashboard-metric-command metric-${metric.tone}`}
              key={metric.key}
              initial={{
                opacity: 0,
                y: 16,
              }}
              animate={{
                opacity: 1,
                y: 0,
              }}
              transition={{
                delay: 0.1 + index * 0.07,
                duration: 0.38,
                ease: [0.22, 1, 0.36, 1],
              }}
              whileHover={{
                y: -3,
                transition: {
                  duration: 0.18,
                },
              }}
            >
              <div className="metric-command-top">
                <div className="metric-icon">
                  <Icon size={18} />
                </div>

                <span className="metric-command-index">
                  0{index + 1}
                </span>
              </div>

              <div className="metric-command-value">
                {dashboardQuery.isLoading &&
                metric.key !== "system"
                  ? "…"
                  : value}
              </div>

              <div className="metric-command-label">
                {metric.label}
              </div>

              <div className="metric-command-detail">
                {metric.detail}
              </div>
            </motion.article>
          );
        })}
      </div>

      {/* MAIN COMMAND GRID */}
      <div className="dashboard-command-grid">
        {/* WORKFLOW */}
        <motion.article
          className="dashboard-panel dashboard-workflow-command"
          initial={{
            opacity: 0,
            y: 18,
          }}
          animate={{
            opacity: 1,
            y: 0,
          }}
          transition={{
            delay: 0.38,
            duration: 0.45,
            ease: [0.22, 1, 0.36, 1],
          }}
        >
          <div className="command-panel-header">
            <div>
              <span className="eyebrow">
                Operational stream
              </span>

              <h3>Today's workflow</h3>

              <p>
                Real-time hospital activity
              </p>
            </div>

            <div className="command-live">
              <span />
              LIVE
            </div>
          </div>

          <div className="workflow-command-list">
            <WorkflowItem
              icon={CalendarDays}
              title="Appointments"
              description="Today's scheduled consultations"
              value={summary?.todayAppointments ?? 0}
              suffix="scheduled"
              onClick={() => navigate("/appointments")}
            />

            <WorkflowItem
              icon={FlaskConical}
              title="Laboratory"
              description="Requests awaiting clinical action"
              value={summary?.pendingLaboratoryWork ?? 0}
              suffix="pending"
              onClick={() => navigate("/patients")}
            />

            <WorkflowItem
              icon={Users}
              title="Patient registry"
              description="Active patient population"
              value={summary?.activePatients ?? 0}
              suffix="active"
              onClick={() => navigate("/patients")}
            />
          </div>

          <button
            type="button"
            className="command-panel-footer"
            onClick={() => navigate("/appointments")}
          >
            <span>Open operational workspace</span>
            <ChevronRight size={16} />
          </button>
        </motion.article>

        {/* QUICK ACTIONS */}
        <motion.article
          className="dashboard-panel dashboard-actions-command"
          initial={{
            opacity: 0,
            y: 18,
          }}
          animate={{
            opacity: 1,
            y: 0,
          }}
          transition={{
            delay: 0.46,
            duration: 0.45,
            ease: [0.22, 1, 0.36, 1],
          }}
        >
          <div className="command-panel-header">
            <div>
              <span className="eyebrow">
                Workspace
              </span>

              <h3>Quick actions</h3>

              <p>
                Common hospital operations
              </p>
            </div>

            <Activity size={18} />
          </div>

          <div className="dashboard-action-list">
            {quickActions.map((action) => {
              const Icon = action.icon;

              return (
                <button
                  type="button"
                  className="dashboard-action"
                  key={action.label}
                  onClick={() => navigate(action.path)}
                >
                  <span className="dashboard-action-icon">
                    <Icon size={17} />
                  </span>

                  <span className="dashboard-action-copy">
                    <strong>{action.label}</strong>
                    <span>{action.description}</span>
                  </span>

                  <ArrowUpRight size={16} />
                </button>
              );
            })}
          </div>
        </motion.article>

        {/* SYSTEM STRIP */}
        <motion.article
          className="dashboard-system-strip"
          initial={{
            opacity: 0,
            scale: 0.985,
          }}
          animate={{
            opacity: 1,
            scale: 1,
          }}
          transition={{
            delay: 0.54,
            duration: 0.4,
          }}
        >
          <div className="system-strip-icon">
            <Activity size={19} />
          </div>

          <div className="system-strip-copy">
            <strong>MedCore services operational</strong>

            <span>
              Authentication, clinical records, appointments,
              laboratory and billing are available.
            </span>
          </div>

          <div className="system-strip-status">
            <span />
            {summary?.systemStatus ?? "UP"}
          </div>
        </motion.article>
      </div>
    </section>
  );
}

type WorkflowItemProps = {
  icon: typeof CalendarDays;
  title: string;
  description: string;
  value: number;
  suffix: string;
  onClick: () => void;
};

function WorkflowItem({
  icon: Icon,
  title,
  description,
  value,
  suffix,
  onClick,
}: WorkflowItemProps) {
  return (
    <button
      type="button"
      className="workflow-command-item"
      onClick={onClick}
    >
      <span className="workflow-command-icon">
        <Icon size={17} />
      </span>

      <span className="workflow-command-copy">
        <strong>{title}</strong>
        <span>{description}</span>
      </span>

      <span className="workflow-command-value">
        <strong>{value}</strong>
        <small>{suffix}</small>
      </span>

      <ChevronRight size={16} />
    </button>
  );
}