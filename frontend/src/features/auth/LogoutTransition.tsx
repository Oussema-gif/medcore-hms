import { useEffect } from "react";
import { motion } from "framer-motion";
import { Activity, ShieldCheck } from "lucide-react";

type LogoutTransitionProps = {
  onComplete: () => void;
};

export function LogoutTransition({
  onComplete,
}: LogoutTransitionProps) {
  useEffect(() => {
    const timer = window.setTimeout(onComplete, 1900);

    return () => window.clearTimeout(timer);
  }, [onComplete]);

  return (
    <motion.main
      className="logout-transition"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.35 }}
      role="status"
      aria-live="polite"
    >
      <div className="logout-transition-atmosphere" />
      <div className="logout-transition-grid" />

      <div className="logout-transition-center">
        <motion.div
          className="logout-transition-core"
          initial={{ scale: 0.82, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{
            duration: 0.5,
            ease: [0.22, 1, 0.36, 1],
          }}
        >
          <div className="logout-transition-ring logout-ring-one" />
          <div className="logout-transition-ring logout-ring-two" />

          <motion.div
            className="logout-transition-logo"
            initial={{ scale: 0.75, rotate: -8 }}
            animate={{ scale: 1, rotate: 0 }}
            transition={{
              delay: 0.08,
              duration: 0.5,
              ease: [0.22, 1, 0.36, 1],
            }}
          >
            <Activity size={30} strokeWidth={2} />
          </motion.div>

          <motion.div
            className="logout-transition-pulse"
            initial={{ scale: 0.7, opacity: 0 }}
            animate={{
              scale: [0.7, 1.08, 0.88],
              opacity: [0, 0.55, 0],
            }}
            transition={{
              duration: 1.5,
              ease: "easeOut",
              repeat: Infinity,
            }}
          />
        </motion.div>

        <motion.div
          className="logout-transition-copy"
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{
            delay: 0.18,
            duration: 0.42,
            ease: [0.22, 1, 0.36, 1],
          }}
        >
          <span className="logout-transition-brand">
            MEDCORE
          </span>

          <h1>Session secured</h1>

          <p>
            Your workspace is being safely closed.
          </p>

          <div className="logout-transition-line">
            <motion.span
              initial={{ scaleX: 0 }}
              animate={{ scaleX: 1 }}
              transition={{
                duration: 1.35,
                delay: 0.22,
                ease: [0.76, 0, 0.24, 1],
              }}
            />
          </div>

          <div className="logout-transition-status">
            <ShieldCheck size={14} />
            <span>Secure sign out</span>
          </div>
        </motion.div>
      </div>

      <div className="logout-transition-footer">
        <span>Hospital operations platform</span>
        <span>SECURE SESSION</span>
      </div>
    </motion.main>
  );
}