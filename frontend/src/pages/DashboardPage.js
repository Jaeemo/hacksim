import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Activity, AlertTriangle, ArrowLeft, Crosshair, RefreshCw } from 'lucide-react';
import { fetchReport, fetchRuns } from '../services/api';

const STATUS_STYLES = {
  TRIGGERED: 'bg-green-500/20 text-green-300 border-green-500/40',
  FAILED: 'bg-red-500/20 text-red-300 border-red-500/40',
  REQUESTED: 'bg-yellow-500/20 text-yellow-200 border-yellow-500/40',
};

const formatTime = (iso) => (iso ? new Date(iso).toLocaleString('ko-KR') : '-');

const StatusBadge = ({ status }) => (
  <span className={`rounded-full border px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[status] || 'bg-white/10 text-gray-300 border-white/20'}`}>
    {status}
  </span>
);

const Section = ({ icon, title, children }) => (
  <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
    <h3 className="mb-3 flex items-center gap-2 text-lg font-semibold text-green-300">
      {icon}
      {title}
    </h3>
    {children}
  </div>
);

const IocList = ({ label, items }) => (
  <div>
    <p className="mb-1 text-sm font-medium text-gray-400">{label} ({items.length})</p>
    {items.length === 0 ? (
      <p className="text-sm text-gray-600">없음</p>
    ) : (
      <ul className="space-y-1">
        {items.map((item) => (
          <li key={item} className="break-all rounded bg-black/30 px-2 py-1 font-mono text-xs text-gray-200">
            {item}
          </li>
        ))}
      </ul>
    )}
  </div>
);

export default function DashboardPage() {
  const navigate = useNavigate();
  const [runs, setRuns] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [report, setReport] = useState(null);
  const [loadingRuns, setLoadingRuns] = useState(true);
  const [loadingReport, setLoadingReport] = useState(false);
  const [error, setError] = useState(null);

  const loadRuns = useCallback(async () => {
    setLoadingRuns(true);
    setError(null);
    try {
      setRuns(await fetchRuns());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoadingRuns(false);
    }
  }, []);

  useEffect(() => {
    loadRuns();
  }, [loadRuns]);

  const selectRun = useCallback(async (runId) => {
    setSelectedId(runId);
    setReport(null);
    setLoadingReport(true);
    setError(null);
    try {
      setReport(await fetchReport(runId));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoadingReport(false);
    }
  }, []);

  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-gray-900 via-indigo-900 to-black p-6 text-white">
      <header className="mb-6 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/')}
            className="rounded-full bg-white/10 p-2 transition-colors hover:bg-white/20"
            aria-label="메인으로"
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
          <h1 className="text-2xl font-bold">디토네이션 분석 대시보드</h1>
        </div>
        <button
          onClick={loadRuns}
          className="flex items-center gap-2 rounded-full bg-white/10 px-4 py-2 text-sm transition-colors hover:bg-white/20"
        >
          <RefreshCw className={`h-4 w-4 ${loadingRuns ? 'animate-spin' : ''}`} />
          새로고침
        </button>
      </header>

      {error && (
        <div className="mb-4 flex items-center gap-2 rounded-xl border border-red-500/40 bg-red-500/10 px-4 py-3 text-red-200">
          <AlertTriangle className="h-5 w-5" />
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[360px_1fr]">
        <aside className="rounded-2xl border border-white/10 bg-white/5 p-4">
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-400">감사 로그 (최근 실행)</h2>
          {loadingRuns ? (
            <p className="text-gray-400">불러오는 중...</p>
          ) : runs.length === 0 ? (
            <p className="text-gray-500">기록된 실행이 없습니다.</p>
          ) : (
            <ul className="space-y-2">
              {runs.map((run) => (
                <li key={run.id}>
                  <button
                    onClick={() => selectRun(run.id)}
                    className={`w-full rounded-xl border px-3 py-2 text-left transition-colors ${
                      selectedId === run.id
                        ? 'border-green-400/60 bg-green-400/10'
                        : 'border-white/10 bg-black/20 hover:bg-white/10'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-medium">#{run.id} · {run.scenarioId}</span>
                      <StatusBadge status={run.status} />
                    </div>
                    <div className="mt-1 text-xs text-gray-400">{formatTime(run.requestedAt)}</div>
                    <div className="text-xs text-gray-500">{run.clientIp}</div>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </aside>

        <main>
          {!selectedId ? (
            <div className="flex h-64 items-center justify-center rounded-2xl border border-dashed border-white/15 text-gray-500">
              왼쪽에서 실행 기록을 선택하면 분석 리포트가 표시됩니다.
            </div>
          ) : loadingReport ? (
            <div className="flex h-64 items-center justify-center text-gray-400">리포트를 분석하는 중...</div>
          ) : report ? (
            <motion.div
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              className="space-y-6"
            >
              <div className="flex flex-wrap items-center gap-4 rounded-2xl border border-white/10 bg-white/5 p-5">
                <div>
                  <p className="text-sm text-gray-400">시나리오</p>
                  <p className="text-xl font-bold">{report.scenarioId}</p>
                </div>
                <StatusBadge status={report.status} />
                <div className="ml-auto text-right">
                  <p className="text-sm text-gray-400">수집 이벤트</p>
                  <p className="text-xl font-bold">{report.eventCount}</p>
                </div>
              </div>

              <Section icon={<Crosshair className="h-5 w-5" />} title={`MITRE ATT&CK 기법 (${report.attackTechniques.length})`}>
                {report.attackTechniques.length === 0 ? (
                  <p className="text-sm text-gray-500">매핑된 기법이 없습니다.</p>
                ) : (
                  <div className="flex flex-wrap gap-2">
                    {report.attackTechniques.map((t) => (
                      <span key={t.id} className="rounded-lg border border-indigo-400/40 bg-indigo-400/10 px-3 py-1.5 text-sm">
                        <span className="font-mono font-semibold text-indigo-200">{t.id}</span>
                        <span className="text-gray-300"> · {t.name}</span>
                        <span className="ml-2 rounded-full bg-white/10 px-2 text-xs text-gray-300">×{t.count}</span>
                      </span>
                    ))}
                  </div>
                )}
              </Section>

              <Section icon={<AlertTriangle className="h-5 w-5" />} title="IOC (침해 지표)">
                <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                  <IocList label="네트워크/도메인" items={report.iocs.hosts} />
                  <IocList label="파일" items={report.iocs.files} />
                  <IocList label="레지스트리 키" items={report.iocs.registryKeys} />
                </div>
              </Section>

              <Section icon={<Activity className="h-5 w-5" />} title={`이벤트 타임라인 (${report.events.length})`}>
                {report.events.length === 0 ? (
                  <p className="text-sm text-gray-500">수집된 텔레메트리가 없습니다. 게스트에서 ship_telemetry.ps1 을 실행하세요.</p>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm">
                      <thead className="text-xs uppercase text-gray-500">
                        <tr>
                          <th className="px-2 py-2">시각</th>
                          <th className="px-2 py-2">유형</th>
                          <th className="px-2 py-2">행위 주체</th>
                          <th className="px-2 py-2">대상</th>
                          <th className="px-2 py-2">ATT&CK</th>
                        </tr>
                      </thead>
                      <tbody>
                        {report.events.map((ev, idx) => (
                          <tr key={idx} className="border-t border-white/5 align-top">
                            <td className="whitespace-nowrap px-2 py-2 text-gray-400">{formatTime(ev.occurredAt)}</td>
                            <td className="px-2 py-2 font-mono text-xs text-gray-300">{ev.eventType}</td>
                            <td className="break-all px-2 py-2 font-mono text-xs text-gray-400">{ev.actor}</td>
                            <td className="break-all px-2 py-2 font-mono text-xs text-gray-400">{ev.target}</td>
                            <td className="whitespace-nowrap px-2 py-2 font-mono text-xs text-indigo-300">{ev.attackTechniqueId || '-'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </Section>
            </motion.div>
          ) : null}
        </main>
      </div>
    </div>
  );
}
