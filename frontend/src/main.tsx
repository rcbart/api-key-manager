import React from "react";
import ReactDOM from "react-dom/client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import App from "./App";
import { AuthProvider } from "./auth/AuthContext";
import { ToastProvider, ToastViewport } from "./components/common/ToastContext";
import { ErrorBoundary } from "./components/common/ErrorBoundary";
import "./index.css";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false },
    mutations: { retry: 0 }
  }
});

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <AuthProvider>
          <ErrorBoundary label="root">
            <App />
          </ErrorBoundary>
        </AuthProvider>
        <ToastViewport />
      </ToastProvider>
    </QueryClientProvider>
  </React.StrictMode>
);
