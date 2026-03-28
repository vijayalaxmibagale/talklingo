const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function parseResponse(response) {
  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof body === "string" ? body : body.message || "Request failed.";
    throw new Error(message);
  }

  return body;
}

export async function apiRequest(path, options = {}) {
  const token = localStorage.getItem("talklingo-token");
  const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;
  const headers = {
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(!isFormData ? { "Content-Type": "application/json" } : {}),
    ...(options.headers || {})
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers
  });

  return parseResponse(response);
}

export { API_BASE_URL };
