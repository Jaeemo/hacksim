import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { DEFAULT_SCENARIO_ID, SCENARIO_BY_ID } from '../data/scenarios';

const SAFE_FILES = [
  { id: 1, name: '중요문서.docx', icon: '📄', type: 'safe' },
  { id: 2, name: '가족사진.zip', icon: '📁', type: 'safe' },
  { id: 4, name: '업무계획.xlsx', icon: '📊', type: 'safe' },
];

const buildInitialFiles = (scenarioId) => {
  const scenario = SCENARIO_BY_ID[scenarioId] || SCENARIO_BY_ID[DEFAULT_SCENARIO_ID];

  return [
    ...SAFE_FILES,
    {
      id: 3,
      type: 'bait',
      infected: false,
      ...scenario.baitFile,
    },
  ];
};

const FileIcon = ({ file, onFileClick }) => (
  <motion.button
    type="button"
    className="flex w-28 flex-col items-center rounded-lg p-4 text-center text-white hover:bg-white/10"
    onClick={() => onFileClick(file)}
    whileHover={{ scale: 1.1 }}
    whileTap={{ scale: 0.9 }}
  >
    <span className="text-5xl mb-2">{file.infected ? '🔒' : file.icon}</span>
    <p
      className={`text-sm break-all ${
        file.infected ? 'text-red-400 line-through' : ''
      }`}
    >
      {file.infected ? `${file.name}.locked` : file.name}
    </p>
  </motion.button>
);

const RansomNote = ({ onClose }) => (
  <motion.div
    initial={{ opacity: 0, scale: 0.7 }}
    animate={{ opacity: 1, scale: 1 }}
    exit={{ opacity: 0, scale: 0.7 }}
    drag
    dragMomentum={false}
    className="absolute top-1/4 left-1/4 w-96 h-auto bg-white text-black rounded-lg shadow-2xl flex flex-col cursor-grab"
    style={{ fontFamily: `'Courier New', Courier, monospace` }}
  >
    <div className="bg-gray-200 p-2 flex justify-between items-center rounded-t-lg">
      <h3 className="font-bold">Ransom_Note.txt</h3>
      <button
        onClick={onClose}
        className="bg-red-500 hover:bg-red-700 text-white font-bold w-6 h-6 rounded-full"
      >
        X
      </button>
    </div>
    <div className="p-4 text-sm whitespace-pre-wrap">
      <h2 className="text-lg font-bold mb-2 text-red-600">!!! YOUR FILES HAVE BEEN ENCRYPTED !!!</h2>
      <p>
        당신의 모든 중요한 파일(사진, 문서, 데이터베이스)이 강력한 암호화로 잠겼습니다.
      </p>
      <p className="mt-2">
        파일을 복구할 유일한 방법은 복호화 키를 구매하는 것입니다.
      </p>
      <p className="mt-4 font-bold">
        복구를 시도하다가는 파일을 영원히 잃게 될 것입니다.
      </p>
    </div>
  </motion.div>
);

const DataLeakWindow = () => (
  <motion.div className="absolute bottom-4 right-4 w-72 bg-gray-800 border border-red-500 rounded-lg shadow-2xl p-4 text-white">
    <div className="flex items-center mb-2">
      <span className="text-red-500 mr-2">⚠️</span>
      <h4 className="font-bold">보안 경고</h4>
    </div>
    <p className="text-sm">개인 데이터가 외부 서버로 전송되고 있습니다...</p>
    <div className="w-full bg-gray-600 rounded-full h-2.5 mt-2">
      <motion.div
        className="bg-red-500 h-2.5 rounded-full"
        initial={{ width: '0%' }}
        animate={{ width: '100%' }}
        transition={{ duration: 4, ease: 'linear' }}
      />
    </div>
  </motion.div>
);

export default function SimulatedDesktop({ scenario = DEFAULT_SCENARIO_ID }) {
  const navigate = useNavigate();

  const [files, setFiles] = useState(() => buildInitialFiles(scenario));
  const [showRansomNote, setShowRansomNote] = useState(false);
  const [showDataLeak, setShowDataLeak] = useState(false);
  const [isInfected, setIsInfected] = useState(false);

  useEffect(() => {
    setFiles(buildInitialFiles(scenario));
    setShowRansomNote(false);
    setShowDataLeak(false);
    setIsInfected(false);
  }, [scenario]);

  useEffect(() => {
    if (!isInfected || scenario !== 'worm') {
      return undefined;
    }

    const interval = setInterval(() => {
      setFiles((currentFiles) => {
        if (currentFiles.length > 50) {
          clearInterval(interval);
          return currentFiles;
        }

        return [
          ...currentFiles,
          {
            id: Date.now(),
            name: 'worm_clone.exe',
            icon: '🐛',
            infected: true,
            type: 'clone',
          },
        ];
      });
    }, 500);

    return () => clearInterval(interval);
  }, [isInfected, scenario]);

  const navigateToResult = (delay) => {
    setTimeout(() => navigate('/result', { state: { scenario } }), delay);
  };

  const handleFileClick = (clickedFile) => {
    if (isInfected || clickedFile.type !== 'bait') {
      alert(isInfected ? '시스템이 감염되어 파일을 열 수 없습니다.' : `${clickedFile.name} 파일은 안전합니다.`);
      return;
    }

    setIsInfected(true);

    switch (scenario) {
      case 'ransomware':
        setTimeout(() => setFiles((currentFiles) => currentFiles.map((file) => ({ ...file, infected: true }))), 500);
        setTimeout(() => setShowRansomNote(true), 1500);
        navigateToResult(3000);
        break;
      case 'trojan':
        setTimeout(() => alert('오류: 이 프로그램은 현재 시스템과 호환되지 않습니다.'), 1000);
        navigateToResult(2000);
        break;
      case 'spyware':
        setShowDataLeak(true);
        navigateToResult(4000);
        break;
      case 'worm':
        setFiles((currentFiles) => (
          currentFiles.map((file) => (
            file.id === clickedFile.id ? { ...file, icon: '🐛', infected: true } : file
          ))
        ));
        navigateToResult(5000);
        break;
      default:
        break;
    }
  };

  return (
    <div className="relative w-full h-full bg-indigo-900 overflow-hidden flex flex-wrap content-start p-4">
      {files.map((file) => (
        <FileIcon
          key={file.id}
          file={file}
          onFileClick={handleFileClick}
        />
      ))}

      <AnimatePresence>
        {showRansomNote && <RansomNote onClose={() => setShowRansomNote(false)} />}
        {showDataLeak && <DataLeakWindow />}
      </AnimatePresence>
    </div>
  );
}
