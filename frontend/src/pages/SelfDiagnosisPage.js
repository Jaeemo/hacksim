import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Download, ShieldCheck } from 'lucide-react';
import { fetchPostureReports } from '../services/api';

const pageVariants = {
  initial: { opacity: 0, y: 20 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.5 } },
  exit: { opacity: 0, y: -20, transition: { duration: 0.3 } },
};

const STATUS_STYLES = {
  PASS: 'bg-green-500/20 text-green-300 border-green-500/40',
  WARN: 'bg-yellow-500/20 text-yellow-200 border-yellow-500/40',
  FAIL: 'bg-red-500/20 text-red-300 border-red-500/40',
};

export default function SelfDiagnosisPage() {
  const [latest, setLatest] = useState(null);

  useEffect(() => {
    fetchPostureReports()
      .then((reports) => setLatest(reports[0] || null))
      .catch(() => setLatest(null));
  }, []);

  const handleDownload = () => {
    alert("실제 환경에서는 여기서 'Hacksim PC 자가진단.exe' 파일 다운로드가 시작됩니다.");
  };

  return (
    <div className="relative w-full h-screen bg-gradient-to-br from-gray-900 via-indigo-900 to-black text-white overflow-auto flex justify-center items-center p-4">
      <motion.div
        key="self-diagnosis-page"
        variants={pageVariants}
        initial="initial"
        animate="animate"
        exit="exit"
        className="w-full max-w-4xl mx-auto"
      >
        <h1 className="text-4xl md:text-5xl font-extrabold mb-8 text-center relative">
          PC 보안 자가진단 시스템
          <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-32 h-1 bg-gradient-to-r from-green-400 to-indigo-400 rounded-full mt-2" />
        </h1>

        <div className="max-w-3xl mx-auto p-8 md:p-10 bg-white/10 backdrop-blur-lg rounded-3xl shadow-xl border border-white/20">
          <h2 className="text-2xl font-bold text-green-400 mb-4">이제 당신의 PC를 점검할 차례입니다!</h2>
          <p className="text-gray-200 mb-6 leading-relaxed">
            Hacksim 시뮬레이션을 통해 악성코드의 위험성을 체험하셨다면, 이제 배운 내용을 바탕으로 실제 사용 중인 PC의 보안 상태를 점검하고 강화해야 합니다. 저희가 개발한 자가진단 시스템은 전문가가 아니어도 누구나 쉽게 PC의 취약점을 확인하고 조치할 수 있도록 돕습니다.
          </p>

          <h3 className="text-xl font-semibold text-green-300 mb-3">주요 기능:</h3>
          <ul className="space-y-3 mb-8 list-disc list-inside text-gray-100">
            <li><span className="font-bold">8가지 핵심 보안 항목 점검</span>: 방화벽, 백신, Guest 계정, 열린 포트, 의심 파일, UAC, 업데이트, Hosts 파일 변조 여부를 자동으로 확인합니다.</li>
            <li><span className="font-bold">직관적인 결과</span>: 진단 결과를 '안전(녹색)', '취약(빨간색)'으로 명확하게 보여주고, 각 항목별 상세 설명과 위험성을 안내합니다.</li>
            <li><span className="font-bold">원클릭 자동 해결</span>: 발견된 일부 취약점(예: Guest 계정 활성화)은 버튼 클릭 한 번으로 즉시 해결할 수 있습니다.</li>
            <li><span className="font-bold">안전한 실행</span>: WMI 의존성을 제거하여 다양한 Windows 환경에서 안정적으로 작동하며, 관리자 권한으로 실행 시 정확한 진단이 가능합니다.</li>
          </ul>

          <div className="bg-white/20 rounded-xl p-6 border border-green-400/50 text-center">
            <h3 className="text-xl font-semibold text-white mb-3">자가진단 프로그램 다운로드</h3>
            <p className="text-gray-300 text-sm mb-4">
              아래 버튼을 클릭하여 'Hacksim PC 자가진단.exe' 파일을 다운로드하세요.<br/>
              <span className="font-semibold text-yellow-300">※ 정확한 진단을 위해 관리자 권한으로 실행해야 합니다.</span>
            </p>
            <motion.button
              onClick={handleDownload}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="inline-flex items-center bg-green-500 hover:bg-green-600 text-white font-bold py-3 px-6 rounded-lg shadow-lg transition-colors duration-200"
            >
              <Download className="mr-2 h-5 w-5" />
              지금 다운로드 (.exe)
            </motion.button>
          </div>
        </div>

        {latest && (
          <div className="mx-auto mt-6 max-w-3xl rounded-3xl border border-white/20 bg-white/10 p-8 backdrop-blur-lg">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="flex items-center gap-2 text-2xl font-bold text-green-400">
                <ShieldCheck className="h-6 w-6" /> 최근 자가진단 결과
              </h2>
              <span className="text-3xl font-extrabold text-white">
                {latest.score}
                <span className="text-base text-gray-400">/100</span>
              </span>
            </div>
            <p className="mb-4 text-sm text-gray-400">
              {latest.hostId} · {latest.os} · {new Date(latest.reportedAt).toLocaleString('ko-KR')}
            </p>
            <ul className="space-y-2">
              {latest.checks.map((check) => (
                <li key={check.checkId} className="flex items-center justify-between rounded-lg bg-black/20 px-4 py-2">
                  <span className="text-gray-100">{check.name}</span>
                  <span className={`rounded-full border px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[check.status] || 'border-white/20 text-gray-300'}`}>
                    {check.status}
                  </span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </motion.div>
    </div>
  );
}
