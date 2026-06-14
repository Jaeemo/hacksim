import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import SimulationPage from './pages/SimulationPage';
import WarningPage from './pages/WarningPage';
import ResultPage from './pages/ResultPage';
import NotFoundPage from './pages/NotFoundPage';
import SelfDiagnosisPage from './pages/SelfDiagnosisPage';
import DashboardPage from './pages/DashboardPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/simulation" element={<SimulationPage />} />
        <Route path="/warning" element={<WarningPage />} />
        <Route path="/result" element={<ResultPage />} />
        <Route path="/self" element={<SelfDiagnosisPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Router>
  );
}

export default App;
