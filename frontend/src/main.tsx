import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import "./styles/design-tokens.css";
import "./styles/dashboard-premium-styles.css";
import "./styles/auth-experience.css";
import { initializeTheme } from "./theme";

initializeTheme();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);