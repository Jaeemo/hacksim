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

export const fetchRuns = async () => {
  const response = await fetch(`${API_BASE_URL}/api/runs`);

  if (!response.ok) {
    throw new Error(`감사 로그 조회 실패 (Status: ${response.status})`);
  }

  return response.json();
};

export const fetchReport = async (runId) => {
  const response = await fetch(`${API_BASE_URL}/api/runs/${runId}/report`);
  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.message || `분석 리포트 조회 실패 (Status: ${response.status})`);
  }

  return data;
};

export const fetchPostureReports = async () => {
  const response = await fetch(`${API_BASE_URL}/api/posture`);

  if (!response.ok) {
    throw new Error(`자가진단 결과 조회 실패 (Status: ${response.status})`);
  }

  return response.json();
};
