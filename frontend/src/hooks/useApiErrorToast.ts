import { useEffect } from "react";
import { ApiError } from "../api/client";
import { useToast } from "../components/common/ToastContext";

export function useApiErrorToast(error: unknown) {
  const { showToast } = useToast();

  useEffect(() => {
    if (!error) return;
    const message = error instanceof ApiError ? error.message : "Something went wrong. Please try again.";
    showToast(message, "error");
  }, [error, showToast]);
}
