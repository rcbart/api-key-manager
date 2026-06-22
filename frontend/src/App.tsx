import { useAuth } from "./auth/AuthContext";
import { ErrorBoundary } from "./components/common/ErrorBoundary";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";

export default function App() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="app-shell">
      <ErrorBoundary label="app">{isAuthenticated ? <DashboardPage /> : <LoginPage />}</ErrorBoundary>
    </div>
  );
}
