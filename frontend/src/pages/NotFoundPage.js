import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

export default function NotFoundPage() {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate('/');
  };

  return (
    <div className="flex flex-col items-center justify-center w-full h-screen bg-gradient-to-br from-gray-900 via-black to-indigo-900 text-white">
      <motion.div
        className="text-center p-8"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <h1 className="text-9xl font-black text-transparent bg-clip-text bg-gradient-to-r from-yellow-400 to-orange-500">
          404
        </h1>

        <p className="mt-4 text-2xl font-bold text-gray-300">
          페이지를 찾을 수 없습니다.
        </p>
        
        <p className="mt-2 text-lg text-gray-500">
          요청하신 페이지가 사라졌거나, 주소가 잘못 입력되었을 수 있습니다.
        </p>

        <motion.button
          onClick={handleGoHome}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          className="mt-8 px-6 py-3 bg-indigo-600 hover:bg-indigo-500 rounded-full font-semibold transition-colors duration-300"
        >
          🏠 홈으로 돌아가기
        </motion.button>
      </motion.div>
    </div>
  );
}
