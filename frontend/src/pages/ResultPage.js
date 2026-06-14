import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { NOVNC_URL } from '../config/appConfig';
import { SCENARIO_BY_ID } from '../data/scenarios';
import { startSimulation } from '../services/api';

const ResultPage = () => {
  const location = useLocation();
  const scenarioId = location.state?.scenario;
  const scenario = SCENARIO_BY_ID[scenarioId];
  const [isStarting, setIsStarting] = useState(false);

  const getErrorMessage = (error) => (
    error.response?.data?.message || error.message || '알 수 없는 오류가 발생했습니다.'
  );

  const handleStartSimulation = async () => {
    if (isStarting) return;

    if (!scenario) {
      alert('오류: 시나리오가 선택되지 않았습니다. 메인 페이지에서 다시 시도해주세요.');
      return;
    }

    setIsStarting(true);

    try {
      const data = await startSimulation(scenario.id);

      if (data.status !== 'success') {
        alert(`시뮬레이션 시작 실패: ${data.message}`);
        setIsStarting(false);
        return;
      }

      window.location.href = NOVNC_URL;
    } catch (error) {
      console.error('시뮬레이션 API 호출 오류:', error);
      alert(`서버에 연결할 수 없습니다: ${getErrorMessage(error)}\n백엔드 서버와 VM 상태를 확인하세요.`);
      setIsStarting(false);
    }
  };

  return (
    <div className="flex h-screen flex-col items-center justify-center bg-gray-50 px-4 text-center font-sans">
      <h1 className="mb-4 text-3xl font-bold text-gray-900">가상 시뮬레이션 종료 ✅</h1>
      <p className="mb-6 text-lg text-gray-600">가상 시나리오가 종료되었습니다. 이제 실제 VM 환경에서 실행합니다.</p>
      <p className="mb-8 text-lg text-gray-600">
        선택된 시나리오: <strong>{scenario?.name || '선택 안 됨'}</strong>
      </p>

      <button
        onClick={handleStartSimulation}
        className={`rounded-lg px-6 py-3 text-base font-semibold text-white transition-colors ${
          isStarting ? 'cursor-wait bg-gray-500' : 'bg-blue-600 hover:bg-blue-500'
        }`}
        disabled={isStarting}
      >
        {isStarting ? 'VM에 명령 전송 중...' : '실제 시뮬레이션 환경으로 이동'}
      </button>
    </div>
  );
};

export default ResultPage;
