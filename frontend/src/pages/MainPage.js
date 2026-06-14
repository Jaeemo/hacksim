import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { SCENARIOS } from '../data/scenarios';

const pageVariants = {
  enter: (direction) => ({
    x: direction > 0 ? 1000 : -1000,
    opacity: 0,
  }),
  center: { zIndex: 1, x: 0, opacity: 1 },
  exit: (direction) => ({
    zIndex: 0,
    x: direction < 0 ? 1000 : -1000,
    opacity: 0,
  }),
};

const malwareDescriptions = [
  '웜 : 사용자의 개입 없이 네트워크를 통해 스스로 복제하고 확산하는 악성 프로그램(멀웨어)',
  '트로이목마 : 컴퓨터의 프로그램 내에 사용자는 알 수 없도록 프로그래머가 고의로 포함시킨, 자기 자신을 복사하지 않는 명령어들의 조합',
  '스파이웨어 : 스파이(Spy)와 소프트웨어(Software)의 합성어로 개인이나 조직의 정보를 몰래 수집하거나 해당 데이터를 소비자의 동의없이 수집하는 악성코드',
  '랜섬웨어 : 몸값(Ransom)과 소프트웨어(Software)의 합성어로 사용자의 컴퓨터를 해킹하거나 데이터를 암호화한 뒤,정상적인 사용을 위해 필요한 복호화 키를 조건으로 금전을 요구하는 악성코드',
];

export default function MainPage() {
  const navigate = useNavigate();
  const [[page, direction], setPage] = useState([0, 0]);

  const pages = [
    {
      title: "당신의 PC는 안전하십니까?",
      content: (
        <div className="max-w-2xl mx-auto p-10 bg-white/10 backdrop-blur-lg rounded-3xl shadow-xl border border-white/20">
          <h2 className="text-3xl font-bold text-green-400 mb-4">악성코드의 위험성</h2>
          <p className="text-gray-200 mb-6 leading-7">
            악성코드는 컴퓨터 시스템에 해를 끼치는 모든 종류의 소프트웨어를 의미합니다.
          </p>
          <ul className="space-y-3">
            {malwareDescriptions.map((item) => (
              <li
                key={item}
                className="flex items-start bg-white/20 rounded-xl p-4 border-l-4 border-green-400 hover:border-green-300 transition-all duration-200"
              >
                <span className="mr-3 text-green-300">•</span>
                <span className="text-gray-100 leading-snug">{item}</span>
              </li>
            ))}
          </ul>
        </div>
      ),
    },
    {
      title: "악성코드 침투 과정",
      content: (
        <div className="flex justify-center items-center h-full">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 p-6">
            {SCENARIOS.map(({ id, stageTitle, stageDescription }) => (
              <motion.div
                key={id}
                whileHover={{ y: -8 }}
                className="relative p-6 bg-white/10 rounded-2xl shadow-lg hover:bg-white/20 transition-all duration-200"
              >
                <div className="text-xl font-semibold text-gray-100">{stageTitle}</div>
                <p className="mt-2 text-sm text-gray-300 leading-relaxed">{stageDescription}</p>
              </motion.div>
            ))}
          </div>
        </div>
      ),
    },
    {
      title: "악성코드 시뮬레이션",
      content: (
        <div className="flex justify-center items-center h-full">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 p-6">
            {SCENARIOS.map(({ id, icon, name }) => (
              <motion.button
                key={id}
                onClick={() => navigate('/simulation', { state: { scenario: id } })}
                whileHover={{ scale: 1.20, rotate: 5 }}
                className="flex flex-col items-center text-5xl p-6 bg-white/10 rounded-2xl shadow-2xl hover:bg-white/20 transition-all duration-200"
              >
                <span>{icon}</span>
                <span className="mt-2 text-sm text-gray-200 font-medium">{name}</span>
              </motion.button>
            ))}
          </div>
        </div>
      ),
    }
  ];

  const paginate = (newDirection) => {
    const newPage = page + newDirection;

    if (newPage >= 0 && newPage < pages.length) {
      setPage([newPage, newDirection]);
    }
  };

  return (
    <div className="relative w-full h-screen bg-gradient-to-br from-gray-900 via-indigo-900 to-black text-white overflow-hidden">
      <button
        onClick={() => navigate('/dashboard')}
        className="absolute top-4 right-4 z-20 rounded-full bg-white/10 px-4 py-2 text-sm font-medium backdrop-blur transition-colors hover:bg-white/20"
      >
        분석 대시보드
      </button>

      <AnimatePresence initial={false} custom={direction}>
        <motion.div
          key={page}
          custom={direction}
          variants={pageVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{
            x: { type: 'spring', stiffness: 300, damping: 30 },
            opacity: { duration: 0.2 }
          }}
          className="absolute inset-0 flex flex-col justify-center items-center px-4"
        >
          <h1 className="text-5xl md:text-6xl font-extrabold mb-8 relative">
            {pages[page].title}
            <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-32 h-1 bg-gradient-to-r from-green-400 to-indigo-400 rounded-full" />
          </h1>
          <div className="w-full max-w-4xl mx-auto">{pages[page].content}</div>
        </motion.div>
      </AnimatePresence>

      <button
        onClick={() => paginate(-1)}
        disabled={page === 0}
        className="absolute left-4 top-1/2 transform -translate-y-1/2 bg-white/20 p-3 rounded-full hover:bg-white/30 transition-all disabled:opacity-30 disabled:cursor-not-allowed z-10"
      >
        ←
      </button>
      <button
        onClick={() => paginate(1)}
        disabled={page === pages.length - 1}
        className="absolute right-4 top-1/2 transform -translate-y-1/2 bg-white/20 p-3 rounded-full hover:bg-white/30 transition-all disabled:opacity-30 disabled:cursor-not-allowed z-10"
      >
        →
      </button>

      <div className="absolute bottom-8 left-1/2 transform -translate-x-1/2 flex space-x-2 z-10">
        {pages.map((_, idx) => (
          <span
            key={idx}
            className={`w-3 h-3 rounded-full transition-colors ${idx === page ? 'bg-green-400' : 'bg-white/30'}`}
          />
        ))}
      </div>
    </div>
  );
}
