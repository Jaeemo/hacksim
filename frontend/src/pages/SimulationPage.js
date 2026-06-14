import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import SimulatedDesktop from './SimulatedDesktop';
import { DEFAULT_SCENARIO_ID, SCENARIO_BY_ID } from '../data/scenarios';
import { SIMULATION_TIMEOUT_SECONDS } from '../config/appConfig';

export default function SimulationPage() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const scenario = state?.scenario || DEFAULT_SCENARIO_ID;
  const scenarioName = SCENARIO_BY_ID[scenario]?.name || scenario;

  const [secondsLeft, setSecondsLeft] = useState(SIMULATION_TIMEOUT_SECONDS);

  useEffect(() => {
    const interval = setInterval(() => {
      setSecondsLeft((prev) => Math.max(prev - 1, 0));
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (secondsLeft === 0) {
      navigate('/warning', { state: { scenario } });
    }
  }, [navigate, scenario, secondsLeft]);

  return (
    <div className="relative w-full h-screen flex flex-col bg-gray-900 text-white">
      <header className="flex items-center justify-between px-6 py-4 bg-gradient-to-r from-indigo-800 to-black shadow-md">
        <h1 className="text-2xl font-bold">{scenarioName} Simulation</h1>
        <button
          onClick={() => navigate(-1)}
          className="bg-white/20 hover:bg-white/30 transition-colors px-4 py-2 rounded-full"
        >
          취소
        </button>
      </header>

      <motion.div
        className="flex-1 flex items-center justify-center p-4"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.9 }}
        transition={{ duration: 0.5 }}
      >
        <div className="w-full max-w-4xl h-3/4 bg-black rounded-2xl shadow-lg border border-white/20">
          <SimulatedDesktop scenario={scenario} />
        </div>
      </motion.div>

      <footer className="px-6 py-4 bg-gradient-to-t from-black to-indigo-800 text-center">
        <p className="text-sm text-gray-300">
          {secondsLeft > 0
            ? `${secondsLeft}초 후 경고 페이지로 이동합니다...`
            : '잠시만 기다려주세요...'}
        </p>
      </footer>
    </div>
  );
}
