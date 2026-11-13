import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useApp } from '../../../context/AppContext';
import myLogo from '../../User/Foto/Icon.png';
import myAvatar from '../../User/Foto/Account.png';
import './AdminPanel.css';

const importTargets = [
  { key: 'doctors', label: 'Врачи CSV' },
  { key: 'patients', label: 'Пациенты CSV' },
  { key: 'appointments', label: 'Записи CSV' },
  { key: 'work-slots', label: 'Слоты CSV' },
];

const toIsoDate = (date) => date.toISOString().slice(0, 10);
const defaultReportFrom = toIsoDate(new Date());
const defaultReportTo = toIsoDate(new Date(Date.now() + 6 * 24 * 60 * 60 * 1000));

export default function AdminPanel() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const {
    users,
    loading,
    error,
    fetchUsers,
    updateUserRoleAndSpeciality,
    importCsv,
    fetchUtilizationReport,
    exportUtilizationCsv,
  } = useApp();

  const [isOpen, setIsOpen] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [importResult, setImportResult] = useState('');
  const [reportFrom, setReportFrom] = useState(defaultReportFrom);
  const [reportTo, setReportTo] = useState(defaultReportTo);
  const [reportSpeciality, setReportSpeciality] = useState('');
  const [reportRows, setReportRows] = useState([]);
  const [reportTotals, setReportTotals] = useState(null);
  const [reportMessage, setReportMessage] = useState('');

  useEffect(() => {
    const load = async () => {
      const result = await fetchUsers(searchText, page, 10);
      if (result.success) {
        setTotalPages(result.totalPages || 0);
      }
    };
    load();
  }, [fetchUsers, page, searchText]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const handleImport = async (target, file) => {
    if (!file) return;

    const result = await importCsv(target, file);
    if (result.success) {
      const status = result.data?.status || 'SUCCESS';
      const processed = result.data?.rows_processed ?? result.data?.rowsProcessed ?? 0;
      const success = result.data?.rows_successful ?? result.data?.rowsSuccessful ?? 0;
      setImportResult(`${target}: ${status}, обработано ${processed}, успешно ${success}`);
      return;
    }

    setImportResult(`${target}: ${result.error}`);
  };

  const handleUpdateUser = async (user) => {
    const nextRole = prompt('Новая роль (patient/doctor/registrar/admin):', user.role || 'patient');
    if (!nextRole) return;

    const nextSpeciality = prompt('Специальность (только для doctor):', user.specialty ?? user.speciality ?? '');

    const payload = {
      role: nextRole.trim().toLowerCase(),
      speciality: nextSpeciality || null,
    };

    const result = await updateUserRoleAndSpeciality(user.user_id ?? user.userId, payload);
    if (result.success) {
      await fetchUsers(searchText, page, 10);
    }
  };

  const reportParams = () => ({
    from: reportFrom,
    to: reportTo,
    speciality: reportSpeciality.trim() || undefined,
  });

  const handleBuildReport = async () => {
    const result = await fetchUtilizationReport(reportParams());
    if (!result.success) {
      setReportRows([]);
      setReportTotals(null);
      setReportMessage(result.error);
      return;
    }

    const report = result.report;
    setReportRows(report.rows || []);
    setReportTotals({
      totalSlots: report.total_slots,
      totalBooked: report.total_booked,
      totalUtilization: report.total_utilization,
    });
    setReportMessage(`Период: ${report.from} — ${report.to}. Строк: ${(report.rows || []).length}`);
  };

  const handleExportReport = async () => {
    const result = await exportUtilizationCsv(reportParams());
    if (!result.success) {
      setReportMessage(result.error);
    }
  };

  return (
    <div className="app-wrapper">
      <header className="main-header">
        <div className="header-left">
          <Link to="/admin" className="header-link">
            <img src={myLogo} alt="Logo" className="header-logo-img" />
            <span className="brand-name">REGISTRATION OFFICE</span>
          </Link>
        </div>

        <div className="user-menu-container">
          <div className="user-avatar" onClick={() => setIsOpen(!isOpen)}>
            <img src={myAvatar} alt="User" className="user-photo" />
          </div>
          {isOpen && (
            <div className="dropdown-menu">
              <button className="dropdown-item logout-link" onClick={handleLogout}>
                <div className="logout-icon-circle">
                  <span className="exit-arrow">→</span>
                </div>
                <span className="logout-text">Sign out</span>
              </button>
            </div>
          )}
        </div>
      </header>

      <main className="content admin-page">
        <div className="admin-container">
          <h2 className="panel-title">Панель администратора</h2>

          <section className="admin-card">
            <h3>Импорт CSV</h3>
            <div className="import-grid">
              {importTargets.map((target) => (
                <label key={target.key} className="import-item">
                  <span>{target.label}</span>
                  <input
                    type="file"
                    accept=".csv,text/csv"
                    onChange={(event) => handleImport(target.key, event.target.files?.[0])}
                  />
                </label>
              ))}
            </div>
            {importResult && <p className="small-hint">{importResult}</p>}
          </section>

          <section className="admin-card">
            <h3>Загрузка расписания</h3>
            <div className="report-filters">
              <label>
                С
                <input type="date" value={reportFrom} onChange={(event) => setReportFrom(event.target.value)} />
              </label>
              <label>
                По
                <input type="date" value={reportTo} onChange={(event) => setReportTo(event.target.value)} />
              </label>
              <label>
                Специальность
                <input
                  type="text"
                  placeholder="например, Терапевт"
                  value={reportSpeciality}
                  onChange={(event) => setReportSpeciality(event.target.value)}
                />
              </label>
              <button className="btn-action-primary" onClick={handleBuildReport}>Построить</button>
              <button className="btn-action-primary" onClick={handleExportReport}>Экспорт CSV</button>
            </div>

            {reportMessage && <p className="small-hint">{reportMessage}</p>}

            {reportRows.length > 0 && (
              <div className="report-table">
                <div className="report-row report-head">
                  <span>Врач</span>
                  <span>Дата</span>
                  <span>Слотов</span>
                  <span>Занято</span>
                  <span>Утилизация</span>
                </div>
                {reportRows.map((row) => (
                  <div className="report-row" key={`${row.doctor_id}-${row.date}`}>
                    <span>
                      {row.doctor_fio}
                      <small>{row.speciality}</small>
                    </span>
                    <span>{row.date}</span>
                    <span>{row.slots_total}</span>
                    <span>{row.slots_booked}</span>
                    <span>{Math.round((row.utilization || 0) * 100)}%</span>
                  </div>
                ))}
                {reportTotals && (
                  <div className="report-row report-total">
                    <span>Итого</span>
                    <span>—</span>
                    <span>{reportTotals.totalSlots}</span>
                    <span>{reportTotals.totalBooked}</span>
                    <span>{Math.round((reportTotals.totalUtilization || 0) * 100)}%</span>
                  </div>
                )}
              </div>
            )}
          </section>

          <section className="admin-card">
            <h3>Пользователи: роли и специальности</h3>
            <input
              className="admin-search-field"
              placeholder="Поиск пользователя"
              value={searchText}
              onChange={(event) => {
                setSearchText(event.target.value);
                setPage(0);
              }}
            />

            {error && <p className="small-error">{error}</p>}
            {loading && <p className="small-hint">Загрузка...</p>}

            <div className="users-table">
              {users.map((user) => (
                <div className="user-row" key={user.user_id ?? user.userId}>
                  <div>
                    <p className="user-fio">{user.fio}</p>
                    <p className="user-meta">role: {user.role || '-'}</p>
                    <p className="user-meta">speciality: {user.specialty ?? user.speciality ?? '-'}</p>
                  </div>
                  <button className="btn-action-primary" onClick={() => handleUpdateUser(user)}>
                    Изменить
                  </button>
                </div>
              ))}
            </div>

            <div className="pager-row">
              <button className="nav-btn" disabled={page <= 0} onClick={() => setPage((prev) => prev - 1)}>←</button>
              <span>{page + 1} / {Math.max(totalPages, 1)}</span>
              <button
                className="nav-btn"
                disabled={totalPages === 0 || page >= totalPages - 1}
                onClick={() => setPage((prev) => prev + 1)}
              >
                →
              </button>
            </div>
          </section>
        </div>
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}

