import { API_BASE_URL } from '../config/appConfig';

export const startSimulation = async (scenarioId) => {
  const response = await fetch(`${API_BASE_URL}/api/start-simulation/${scenarioId}`, {
    method: 'POST',
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.message || `백엔드 서버 응답 실패 (Status: ${response.status})`);
  }

  return data;
};
