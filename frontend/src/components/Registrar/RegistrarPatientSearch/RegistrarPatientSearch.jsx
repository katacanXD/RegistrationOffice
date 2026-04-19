import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useApp } from '../../../context/AppContext';
import myLogo from '../../User/Foto/Icon.png';
import myAvatar from '../../User/Foto/Account.png';
import searchIcon from '../../User/Foto/search.svg';
import './RegistrarPatientSearch.css';

export default function RegistrarPatientSearch() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { patients, fetchPatients, setSelectedPatient, setSelectedDoctor, loading, error } = useApp();

  const [isOpen, setIsOpen] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const formatName = (fio) => {
    if (!fio) return 'Гость';
    const parts = fio.trim().split(/\s+/);
    if (parts.length === 1) return parts[0];
    const firstName = parts[0];
    const initials = parts.slice(1).map((part) => `${part[0].toUpperCase()}.`).join(' ');
    return `${firstName} ${initials}`;
  };

  useEffect(() => {
    const load = async () => {
      const result = await fetchPatients(searchText, page, 8);
      if (result.success) {
        setTotalPages(result.totalPages || 0);
      }
    };
    load();
  }, [fetchPatients, page, searchText]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const handleSelectPatient = (patient) => {
    setSelectedPatient({
      id: patient.user_id ?? patient.userId,
      fio: patient.fio,
      policyNumber: patient.policy_number ?? patient.policyNumber,
    });
    setSelectedDoctor(null);
    navigate('/registrar/patient');
  };

  return (
    <div className="app-wrapper">
      <header className="main-header">
        <div className="header-left">
          <Link to="/registrar/patients" className="header-link">
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
              <div className="dropdown-item user-info">
                <div className="small-avatar-circle">
                  <img src={myAvatar} alt="User" className="user-photo" />
                </div>
                <span className="user-name">{formatName(user?.fio)}</span>
              </div>

              <div className="dropdown-divider"></div>

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

      <main className="content registrar-page">
        <div className="registrar-container">
          <div className="search-input-wrapper">
            <div className="search-icon" aria-hidden="true">
              <img src={searchIcon} alt="" className="search-icon-image" />
            </div>
            <input
              type="text"
              placeholder="ФИО / Полис ОМС"
              className="search-field"
              value={searchText}
              onChange={(event) => {
                setSearchText(event.target.value);
                setPage(0);
              }}
            />
          </div>

          {error && <p className="small-error">{error}</p>}

          <div className="patients-list">
            {loading ? (
              <p className="small-hint">Загрузка пациентов...</p>
            ) : patients.length === 0 ? (
              <p className="small-hint">Пациенты не найдены</p>
            ) : (
              patients.map((patient) => (
                <button
                  key={patient.user_id ?? patient.userId}
                  className="patient-card"
                  onClick={() => handleSelectPatient(patient)}
                >
                  <p className="patient-name">{patient.fio}</p>
                  <p className="patient-policy">{(patient.policy_number ?? patient.policyNumber) || 'Без полиса'}</p>
                </button>
              ))
            )}
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
        </div>
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}


