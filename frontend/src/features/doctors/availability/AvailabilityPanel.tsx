import { zodResolver } from "@hookform/resolvers/zod";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  CalendarClock,
  CheckCircle2,
  Clock3,
  Plus,
  ShieldCheck,
  Trash2,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";

import {
  createDoctorAvailability,
  deleteDoctorAvailability,
  getDoctorAvailability,
} from "./api";

import type { DoctorAvailability } from "./types";

const availabilitySchema = z
  .object({
    dayOfWeek: z.coerce.number().int().min(1).max(7),

    startTime: z
      .string()
      .min(1, "Start time is required."),

    endTime: z
      .string()
      .min(1, "End time is required."),

    effectiveFrom: z
      .string()
      .min(1, "Effective-from date is required."),

    effectiveUntil: z.string(),
  })
  .refine(
    (values) => values.startTime < values.endTime,
    {
      message: "End time must be after start time.",
      path: ["endTime"],
    },
  )
  .refine(
    (values) =>
      !values.effectiveUntil ||
      values.effectiveUntil >= values.effectiveFrom,
    {
      message:
        "Effective-until date must be on or after effective-from.",
      path: ["effectiveUntil"],
    },
  );

type AvailabilityFormInput =
  z.input<typeof availabilitySchema>;

type AvailabilityFormValues =
  z.output<typeof availabilitySchema>;

const days = [
  { value: 1, label: "Monday" },
  { value: 2, label: "Tuesday" },
  { value: 3, label: "Wednesday" },
  { value: 4, label: "Thursday" },
  { value: 5, label: "Friday" },
  { value: 6, label: "Saturday" },
  { value: 7, label: "Sunday" },
];

function today() {
  return new Date().toISOString().slice(0, 10);
}

const defaultValues: AvailabilityFormInput = {
  dayOfWeek: 1,
  startTime: "08:00",
  endTime: "16:00",
  effectiveFrom: today(),
  effectiveUntil: "",
};

export function AvailabilityPanel({
  doctorId,
}: {
  doctorId: number;
}) {
  const queryClient = useQueryClient();

  const availabilityQuery = useQuery({
    queryKey: ["doctor-availability", doctorId],
    queryFn: () => getDoctorAvailability(doctorId),
    enabled: doctorId > 0,
  });

  const form = useForm<
    AvailabilityFormInput,
    unknown,
    AvailabilityFormValues
  >({
    resolver: zodResolver(availabilitySchema),
    defaultValues,
  });

  const createMutation = useMutation({
    mutationFn: (values: AvailabilityFormValues) =>
      createDoctorAvailability({
        doctorId,
        dayOfWeek: values.dayOfWeek,
        startTime: values.startTime,
        endTime: values.endTime,
        effectiveFrom: values.effectiveFrom,
        effectiveUntil:
          values.effectiveUntil || undefined,
      }),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: [
          "doctor-availability",
          doctorId,
        ],
      });

      form.reset({
        ...defaultValues,
        effectiveFrom: today(),
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (availabilityId: number) =>
      deleteDoctorAvailability(availabilityId),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: [
          "doctor-availability",
          doctorId,
        ],
      });
    },
  });

  const availability =
    availabilityQuery.data?.data ?? [];

  function dayLabel(dayOfWeek: number) {
    return (
      days.find(
        (day) => day.value === dayOfWeek,
      )?.label ?? `Day ${dayOfWeek}`
    );
  }

  function handleDelete(
    entry: DoctorAvailability,
  ) {
    const confirmed = window.confirm(
      `Deactivate ${dayLabel(
        entry.dayOfWeek,
      )} availability?`,
    );

    if (confirmed) {
      deleteMutation.mutate(entry.id);
    }
  }

  return (
    <div className="availability-workspace">
      <div className="availability-columns">
        {/* =====================================================
            ADD AVAILABILITY
            ===================================================== */}

        <article className="availability-add-card">
          <header className="availability-card-header">
            <div className="availability-card-heading">
              <span className="availability-eyebrow">
                Schedule configuration
              </span>

              <h3>Add availability</h3>

              <p>
                Create a new working window for
                this physician.
              </p>
            </div>

            <div className="availability-card-icon availability-card-icon-cyan">
              <Plus size={18} />
            </div>
          </header>

          <form
            className="availability-form"
            onSubmit={form.handleSubmit(
              (values) =>
                createMutation.mutate(values),
            )}
          >
            <div className="availability-form-grid">
              <div className="availability-field">
                <label htmlFor="availability-day">
                  Day
                </label>

                <select
                  id="availability-day"
                  {...form.register("dayOfWeek")}
                  disabled={
                    createMutation.isPending
                  }
                >
                  {days.map((day) => (
                    <option
                      key={day.value}
                      value={day.value}
                    >
                      {day.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="availability-field">
                <label htmlFor="availability-start">
                  Start time
                </label>

                <div className="availability-input-with-icon">
                  <Clock3 size={14} />

                  <input
                    id="availability-start"
                    type="time"
                    {...form.register("startTime")}
                    disabled={
                      createMutation.isPending
                    }
                  />
                </div>

                {form.formState.errors
                  .startTime && (
                  <span className="availability-error-text">
                    {
                      form.formState.errors
                        .startTime.message
                    }
                  </span>
                )}
              </div>

              <div className="availability-field">
                <label htmlFor="availability-end">
                  End time
                </label>

                <div className="availability-input-with-icon">
                  <Clock3 size={14} />

                  <input
                    id="availability-end"
                    type="time"
                    {...form.register("endTime")}
                    disabled={
                      createMutation.isPending
                    }
                  />
                </div>

                {form.formState.errors
                  .endTime && (
                  <span className="availability-error-text">
                    {
                      form.formState.errors
                        .endTime.message
                    }
                  </span>
                )}
              </div>

              <div className="availability-field">
                <label htmlFor="availability-from">
                  Effective from
                </label>

                <input
                  id="availability-from"
                  type="date"
                  {...form.register(
                    "effectiveFrom",
                  )}
                  disabled={
                    createMutation.isPending
                  }
                />
              </div>

              <div className="availability-field">
                <label htmlFor="availability-until">
                  Effective until
                </label>

                <input
                  id="availability-until"
                  type="date"
                  {...form.register(
                    "effectiveUntil",
                  )}
                  disabled={
                    createMutation.isPending
                  }
                />

                {form.formState.errors
                  .effectiveUntil && (
                  <span className="availability-error-text">
                    {
                      form.formState.errors
                        .effectiveUntil.message
                    }
                  </span>
                )}
              </div>
            </div>

            {createMutation.isError && (
              <div
                className="availability-error"
                role="alert"
              >
                Unable to create availability.
                Check the doctor ID and the
                selected time/date range.
              </div>
            )}

            <div className="availability-form-footer">
              <div className="availability-validation-note">
                <ShieldCheck size={14} />

                <span>
                  Time and date ranges are
                  validated automatically.
                </span>
              </div>

              <button
                className="app-button availability-submit"
                type="submit"
                disabled={
                  createMutation.isPending
                }
              >
                <Plus size={15} />

                {createMutation.isPending
                  ? "Adding..."
                  : "Add availability"}
              </button>
            </div>
          </form>
        </article>

        {/* =====================================================
            WEEKLY AVAILABILITY
            ===================================================== */}

        <article className="availability-weekly-card">
          <header className="availability-card-header">
            <div className="availability-card-heading">
              <span className="availability-eyebrow availability-eyebrow-blue">
                Weekly schedule
              </span>

              <h3>Weekly availability</h3>

              <p>
                Current working windows configured
                for this physician.
              </p>
            </div>

            <div className="availability-count-badge">
              <span className="availability-count-dot" />

              {availability.length > 0
                ? `${availability.length} ${
                    availability.length === 1
                      ? "window"
                      : "windows"
                  }`
                : "No schedule"}
            </div>
          </header>

          <div className="availability-weekly-content">
            {availabilityQuery.isLoading && (
              <div className="availability-empty-state">
                <div className="availability-empty-icon">
                  <CalendarClock
                    size={17}
                  />
                </div>

                <strong>
                  Loading availability
                </strong>

                <span>
                  Retrieving the current
                  physician schedule...
                </span>
              </div>
            )}

            {availabilityQuery.isError && (
              <div
                className="availability-error"
                role="alert"
              >
                Unable to load doctor
                availability.
              </div>
            )}

            {!availabilityQuery.isLoading &&
              !availabilityQuery.isError &&
              availability.length === 0 && (
                <div className="availability-empty-state">
                  <div className="availability-empty-icon">
                    <CalendarClock
                      size={17}
                    />
                  </div>

                  <strong>
                    No availability configured
                  </strong>

                  <span>
                    Add the physician&apos;s first
                    working window to start
                    building the weekly schedule.
                  </span>
                </div>
              )}

            {availability.length > 0 && (
              <div className="availability-weekly-list">
                {availability.map(
                  (entry) => (
                    <div
                      className="availability-day-row"
                      key={entry.id}
                    >
                      <div className="availability-day-main">
                        <div className="availability-day-name">
                          {dayLabel(
                            entry.dayOfWeek,
                          )}
                        </div>

                        <div className="availability-day-time">
                          <Clock3
                            size={13}
                          />

                          <strong>
                            {entry.startTime.slice(
                              0,
                              5,
                            )}
                            {" – "}
                            {entry.endTime.slice(
                              0,
                              5,
                            )}
                          </strong>
                        </div>

                        <div className="availability-day-date">
                          {entry.effectiveUntil
                            ? `${entry.effectiveFrom} → ${entry.effectiveUntil}`
                            : `From ${entry.effectiveFrom} · ongoing`}
                        </div>
                      </div>

                      <div className="availability-day-actions">
                        <span className="availability-day-status">
                          <CheckCircle2
                            size={12}
                          />
                          Active
                        </span>

                        <button
                          className="availability-deactivate-button"
                          type="button"
                          disabled={
                            deleteMutation.isPending
                          }
                          onClick={() =>
                            handleDelete(
                              entry,
                            )
                          }
                        >
                          <Trash2 size={13} />

                          {deleteMutation.isPending
                            ? "..."
                            : "Deactivate"}
                        </button>
                      </div>
                    </div>
                  ),
                )}
              </div>
            )}
          </div>
        </article>
      </div>
    </div>
  );
}