import { useState } from "react";
import { Activity, ArrowRight, LoaderCircle, ShieldCheck, Sparkles } from "lucide-react";
import { motion } from "framer-motion";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { login } from "./api";

type LoginFormValues = {
  usernameOrEmail: string;
  password: string;
};

export function LoginPage() {
  const [serverError, setServerError] = useState<string | null>(null);
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    defaultValues: { usernameOrEmail: "", password: "" },
  });

  async function onSubmit(values: LoginFormValues) {
    setServerError(null);
    setIsLoggingIn(true);
    try {
      await login(values.usernameOrEmail, values.password);
      navigate("/", { replace: true });
    } catch {
      setIsLoggingIn(false);
      setServerError("Unable to sign in. Check your credentials and try again.");
    }
  }

  return (
    <main className="auth-screen">
      <div className="auth-ambient auth-ambient-one" />
      <div className="auth-ambient auth-ambient-two" />
      <div className="auth-grid" />

      <div className="auth-layout">
        <motion.section
          className="auth-story"
          initial={{ opacity: 0, x: -28 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.7, ease: "easeOut" }}
        >
          <div className="auth-brand-mark">
            <Activity size={22} strokeWidth={2.4} />
          </div>

          <div className="auth-kicker">
            <span className="auth-status-dot" />
            Clinical operations platform
          </div>

          <h1>
            Care, coordinated
            <span> with clarity.</span>
          </h1>

          <p className="auth-story-copy">
            MedCore brings patient care, clinical workflows, scheduling,
            laboratory, pharmacy and hospital operations into one focused
            workspace.
          </p>

          <div className="auth-story-meta">
            <div>
              <ShieldCheck size={16} />
              <span>Secure hospital access</span>
            </div>
            <div>
              <Sparkles size={16} />
              <span>Workspace-first experience</span>
            </div>
          </div>
        </motion.section>

        <motion.section
          className="auth-card"
          initial={{ opacity: 0, y: 26, scale: 0.985 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ delay: 0.08, duration: 0.65, ease: "easeOut" }}
        >
          <div className="auth-card-topline" />

          <div className="auth-card-heading">
            <div>
              <p className="eyebrow">MedCore HMS</p>
              <h2>Welcome back</h2>
              <p>Enter your credentials to continue into your workspace.</p>
            </div>
            <div className="auth-card-orb" aria-hidden="true">
              <Activity size={20} />
            </div>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
            <div className="auth-field">
              <label htmlFor="usernameOrEmail">Username or email</label>
              <input
                id="usernameOrEmail"
                autoComplete="username"
                placeholder="you@example.com"
                {...register("usernameOrEmail", {
                  required: "Username or email is required",
                })}
              />
              {errors.usernameOrEmail && (
                <span className="auth-field-error">
                  {errors.usernameOrEmail.message}
                </span>
              )}
            </div>

            <div className="auth-field">
              <div className="auth-label-row">
                <label htmlFor="password">Password</label>
                <span>Protected access</span>
              </div>
              <input
                id="password"
                type="password"
                autoComplete="current-password"
                placeholder="Enter your password"
                {...register("password", {
                  required: "Password is required",
                })}
              />
              {errors.password && (
                <span className="auth-field-error">{errors.password.message}</span>
              )}
            </div>

            {serverError && (
              <div className="auth-error" role="alert">
                <span className="auth-error-dot" />
                {serverError}
              </div>
            )}

            <button
              className="auth-submit"
              type="submit"
              disabled={isSubmitting || isLoggingIn}
            >
              <span>{isLoggingIn ? "Opening workspace" : "Enter MedCore"}</span>
              {isLoggingIn ? (
                <LoaderCircle size={18} className="auth-spin" />
              ) : (
                <ArrowRight size={18} />
              )}
            </button>
          </form>

          <div className="auth-card-footer">
            <ShieldCheck size={15} />
            <span>Authorized hospital personnel only</span>
          </div>
        </motion.section>
      </div>

      {isLoggingIn && (
        <motion.div
          className="auth-loading-screen"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.25 }}
        >
          <div className="auth-loading-core">
            <div className="auth-loading-ring auth-loading-ring-one" />
            <div className="auth-loading-ring auth-loading-ring-two" />
            <div className="auth-loading-icon">
              <Activity size={24} />
            </div>
          </div>
          <p className="eyebrow">MedCore</p>
          <h2>Opening your workspace</h2>
          <p>Authenticating your hospital session…</p>
        </motion.div>
      )}
    </main>
  );
}