import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { DEFAULT_SCENARIO_ID } from '../data/scenarios';

export default function WarningPage() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const scenario = state?.scenario || DEFAULT_SCENARIO_ID;

  const handleConfirm = () => {
    navigate('/result', { state: { scenario } });
  };

  return (
    <div className="relative w-full h-screen flex flex-col bg-gradient-to-br from-red-900 via-black to-red-800 text-white">
      <motion.div
        className="flex-1 flex flex-col items-center justify-center px-6"
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.8 }}
        transition={{ duration: 0.6 }}
      >
        <motion.h1
          className="text-5xl font-extrabold mb-4"
          initial={{ y: -20 }}
          animate={{ y: 0 }}
          transition={{ delay: 0.2, duration: 0.5 }}
        >
          ⚠️ 경고!
        </motion.h1>
        <motion.p
          className="text-lg text-red-200 mb-8"
          initial={{ y: 20 }}
          animate={{ y: 0 }}
          transition={{ delay: 0.4, duration: 0.5 }}
        >
          악성코드 감염이 감지되었습니다.
        </motion.p>
        <motion.button
          onClick={handleConfirm}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          className="bg-red-600 hover:bg-red-500 transition-colors px-6 py-3 rounded-full font-semibold"
        >
          실제체험으로 이동합니다
        </motion.button>
      </motion.div>

      <footer className="py-4 text-center bg-black/50">
        <p className="text-sm text-gray-400">시뮬레이션이 중단되었습니다. 결과 페이지로 이동해주세요.</p>
      </footer>
    </div>
  );
}
