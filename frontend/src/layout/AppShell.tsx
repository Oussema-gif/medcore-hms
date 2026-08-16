import { useEffect, useState } from "react";
import {
  Activity,
  Bell,
  CalendarDays,
  ClipboardList,
  Command,
  FlaskConical,
  HeartPulse,
  LayoutDashboard,
  LogOut,
  Menu,
  Moon,
  Package,
  Pill,
  Receipt,
  Search,
  Stethoscope,
  Sun,
  UsersRound,
  X,
} from "lucide-react";
import { AnimatePresence } from "framer-motion";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { clearSession, getCurrentUser } from "../features/auth/session";
import { getNavigationForUser } from "../navigation/navigation";
import { PageTransition } from "../components/PageTransition";
import { LogoutTransition } from "../features/auth/LogoutTransition";
import { getStoredTheme, setTheme, type Theme } from "../theme";

const iconMap: Record<string, typeof Activity> = {
  dashboard: LayoutDashboard,
  patients: UsersRound,
  appointments: CalendarDays,
  doctors: Stethoscope,
  "medical-records": ClipboardList,
  laboratory: FlaskConical,
  prescriptions: Pill,
  medicines: Package,
  inventory: Activity,
  invoices: Receipt,
  notifications: Bell,
};

export function AppShell() {
  const navigate = useNavigate();
  const location = useLocation();
  const user = getCurrentUser();
  const navigation = getNavigationForUser(user);
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [loggingOut, setLoggingOut] = useState(false);
  const [theme, setThemeState] = useState<Theme>(() => getStoredTheme());

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      const isCommand = event.metaKey || event.ctrlKey;
      if (isCommand && event.key.toLowerCase() === "k") {
        event.preventDefault();
        setPaletteOpen((open) => !open);
      }
      if (event.key === "Escape") {
        setPaletteOpen(false);
        setMobileOpen(false);
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  function toggleTheme() {
    const nextTheme: Theme =
      theme === "dark" ? "light" : "dark";

    document.documentElement.classList.add("theme-changing");

    setThemeState(nextTheme);
    setTheme(nextTheme);

    window.setTimeout(() => {
      document.documentElement.classList.remove("theme-changing");
    }, 360);
  }
  function openPalette() {
    setQuery("");
    setPaletteOpen(true);
  }

  function go(path: string) {
    setPaletteOpen(false);
    setMobileOpen(false);
    setQuery("");
    navigate(path);
  }

  function handleLogout() {
    if (loggingOut) {
      return;
    }
    setMobileOpen(false);
    setLoggingOut(true);
  }

  function finishLogout() {
    clearSession();
    navigate("/login", { replace: true });
  }

  const normalizedQuery = query.trim().toLowerCase();
  const filteredNavigation = normalizedQuery
    ? navigation.filter((item) => item.label.toLowerCase().includes(normalizedQuery))
    : navigation;

  const currentItem = navigation.find((item) =>
    item.path === "/"
      ? location.pathname === "/"
      : location.pathname === item.path || location.pathname.startsWith(`${item.path}/`),
  );

  return (
    <>
      <div className="app-shell">
        <header className="app-command-bar">
          <div className="app-command-brand" onClick={() => go("/")} role="button" tabIndex={0}>
            <div className="brand-mark" aria-hidden="true">
              <HeartPulse size={17} strokeWidth={2.6} />
            </div>
            <div>
              <strong>MEDCORE</strong>
              <span>clinical operating system</span>
            </div>
          </div>

          <button className="global-search" type="button" onClick={openPalette}>
            <Search size={17} />
            <span>Search patients, doctors, workflows...</span>
            <kbd>⌘ K</kbd>
          </button>

          <div className="command-actions">
            <button className="icon-button" type="button" aria-label="Search" onClick={openPalette}>
              <Command size={18} />
            </button>
            <button
              className="icon-button theme-toggle"
              type="button"
              aria-label={
                theme === "dark"
                  ? "Switch to light mode"
                  : "Switch to dark mode"
              }
              title={
                theme === "dark"
                  ? "Switch to light mode"
                  : "Switch to dark mode"
              }
              onClick={toggleTheme}
            >
              {theme === "dark" ? (
                <Sun size={18} />
              ) : (
                <Moon size={18} />
              )}
            </button>
            <button className="icon-button" type="button" aria-label="Notifications" onClick={() => go("/notifications")}>
              <Bell size={18} />
              <span className="notification-dot" />
            </button>
            <div className="user-chip">
              <span className="avatar avatar-small">{(user?.displayName ?? user?.username ?? "U").slice(0, 1).toUpperCase()}</span>
              <span className="user-chip-name">{user?.displayName ?? user?.username ?? "User"}</span>
            </div>
            <button className="icon-button mobile-menu-button" type="button" aria-label="Open navigation" onClick={() => setMobileOpen(true)}>
              <Menu size={19} />
            </button>
          </div>
        </header>

        <div className="app-shell-body">
          <aside className="app-rail" aria-label="Primary navigation">
            <div className="rail-section-label">Workspace</div>
            <nav className="app-nav">
              {navigation.map((item) => {
                const Icon = iconMap[item.id] ?? Activity;
                return (
                  <NavLink
                    key={item.id}
                    to={item.path}
                    end={item.path === "/"}
                    className={({ isActive }) => `rail-link${isActive ? " active" : ""}`}
                    title={item.label}
                  >
                    <Icon size={18} />
                    <span>{item.label}</span>
                  </NavLink>
                );
              })}
            </nav>
            <div className="rail-footer">
              <button className="rail-link rail-button" type="button" onClick={handleLogout}>
                <LogOut size={18} />
                <span>Sign out</span>
              </button>
            </div>
          </aside>

          <main className="app-main">
            <div className="workspace-context">
              <div>
                <span className="context-kicker">Active workspace</span>
                <strong>{currentItem?.label ?? "MedCore"}</strong>
              </div>
              <div className="context-meta">
                <span className="status-pulse"><i /> Live clinical system</span>
                <span>{user?.roles?.[0] ?? "USER"}</span>
              </div>
            </div>
            <div className="app-content">
              <AnimatePresence mode="wait" initial={false}>
                <PageTransition key={location.pathname}>
                  <Outlet />
                </PageTransition>
              </AnimatePresence>
            </div>
          </main>
        </div>

        {paletteOpen && (
          <div className="command-overlay" role="presentation" onMouseDown={() => setPaletteOpen(false)}>
            <section className="command-palette" role="dialog" aria-modal="true" aria-label="Command palette" onMouseDown={(event) => event.stopPropagation()}>
              <div className="command-palette-head">
                <Search size={18} />
                <input autoFocus value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Jump to a workspace..." />
                <button className="icon-button" type="button" onClick={() => setPaletteOpen(false)} aria-label="Close">
                  <X size={17} />
                </button>
              </div>
              <div className="command-palette-list">
                {filteredNavigation.map((item) => {
                  const Icon = iconMap[item.id] ?? Activity;
                  return (
                    <button key={item.id} type="button" className="command-item" onClick={() => go(item.path)}>
                      <span className="command-item-icon"><Icon size={17} /></span>
                      <span><strong>{item.label}</strong><small>{item.path === "/" ? "Command center" : "Open workspace"}</small></span>
                      <span className="command-arrow">↵</span>
                    </button>
                  );
                })}
                {filteredNavigation.length === 0 && <div className="command-empty">No workspace matches “{query}”.</div>}
              </div>
              <div className="command-palette-footer">
                <span><kbd>↑</kbd><kbd>↓</kbd> navigate</span>
                <span><kbd>Enter</kbd> open</span>
                <span><kbd>Esc</kbd> close</span>
              </div>
            </section>
          </div>
        )}

        {mobileOpen && (
          <div className="mobile-nav-overlay" onMouseDown={() => setMobileOpen(false)}>
            <aside className="mobile-nav" onMouseDown={(event) => event.stopPropagation()}>
              <div className="mobile-nav-head">
                <div className="app-command-brand">
                  <div className="brand-mark"><HeartPulse size={17} /></div>
                  <div><strong>MEDCORE</strong><span>clinical operating system</span></div>
                </div>
                <button className="icon-button" type="button" onClick={() => setMobileOpen(false)}><X size={18} /></button>
              </div>
              <nav className="mobile-nav-list">
                {navigation.map((item) => {
                  const Icon = iconMap[item.id] ?? Activity;
                  return <NavLink key={item.id} to={item.path} onClick={() => setMobileOpen(false)} className="mobile-nav-link"><Icon size={18} /><span>{item.label}</span></NavLink>;
                })}
              </nav>
              <button className="mobile-signout" type="button" onClick={handleLogout}><LogOut size={17} /> Sign out</button>
            </aside>
          </div>
        )}
      </div>

      <AnimatePresence>
        {loggingOut && (
          <LogoutTransition onComplete={finishLogout} />
        )}
      </AnimatePresence>
    </>
  );
}