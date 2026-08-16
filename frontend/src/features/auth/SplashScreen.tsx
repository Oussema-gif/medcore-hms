import { motion } from "framer-motion";
import { Activity, ArrowUpRight, ShieldCheck } from "lucide-react";

type SplashScreenProps = {
  onComplete: () => void;
};

export function SplashScreen({ onComplete }: SplashScreenProps) {
  return (
    <motion.main
      className="boot-screen"
      initial={{ opacity: 1 }}
      animate={{ opacity: 0 }}
      transition={{ delay: 1.55, duration: 0.55, ease: "easeInOut" }}
      onAnimationComplete={onComplete}
    >
      <div className="boot-noise" aria-hidden="true" />
      <div className="boot-grid" aria-hidden="true" />
      <div className="boot-glow boot-glow-one" aria-hidden="true" />
      <div className="boot-glow boot-glow-two" aria-hidden="true" />

      <motion.div
        className="boot-content"
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.65, ease: "easeOut" }}
      >
        <motion.div
          className="boot-symbol"
          initial={{ scale: 0.72, rotate: -12, opacity: 0 }}
          animate={{ scale: 1, rotate: 0, opacity: 1 }}
          transition={{ delay: 0.12, duration: 0.65, type: "spring", stiffness: 180, damping: 16 }}
        >
          <div className="boot-symbol-core">
            <Activity size={28} strokeWidth={2.2} />
          </div>
          <span className="boot-symbol-pulse" />
        </motion.div>

        <motion.div
          className="boot-kicker"
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.45, duration: 0.45 }}
        >
          <span className="boot-live-dot" />
          Clinical operating environment
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.52, duration: 0.5 }}
        >
          MedCore
          <span>HMS</span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.78, duration: 0.5 }}
        >
          One workspace for modern hospital operations.
        </motion.p>

        <motion.div
          className="boot-progress"
          initial={{ opacity: 0, scaleX: 0.25 }}
          animate={{ opacity: 1, scaleX: 1 }}
          transition={{ delay: 0.92, duration: 0.7, ease: "easeOut" }}
        >
          <span />
        </motion.div>

        <motion.div
          className="boot-meta"
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 1.02, duration: 0.45 }}
        >
          <span>
            <ShieldCheck size={14} /> Secure environment
          </span>
          <span>
            <ArrowUpRight size={14} /> Ready
          </span>
        </motion.div>
      </motion.div>

      <div className="boot-corner boot-corner-left">MEDCORE / 01</div>
      <div className="boot-corner boot-corner-right">SYSTEM ONLINE</div>
    </motion.main>
  );
}