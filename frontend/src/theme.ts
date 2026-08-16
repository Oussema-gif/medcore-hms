export type Theme = "dark" | "light";

const STORAGE_KEY = "medcore.theme";

export function getStoredTheme(): Theme {
  const stored = localStorage.getItem(STORAGE_KEY);

  if (stored === "light" || stored === "dark") {
    return stored;
  }

  return "dark";
}

export function applyTheme(theme: Theme): void {
  document.documentElement.dataset.theme = theme;
}

export function setTheme(theme: Theme): void {
  localStorage.setItem(STORAGE_KEY, theme);
  applyTheme(theme);
}

export function initializeTheme(): Theme {
  const theme = getStoredTheme();
  applyTheme(theme);
  return theme;
}
