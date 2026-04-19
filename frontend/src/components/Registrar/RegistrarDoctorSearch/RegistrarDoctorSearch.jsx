import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useApp } from '../../../context/AppContext';
import myLogo from '../../User/Foto/Icon.png';
import myAvatar from '../../User/Foto/Account.png';
import searchIcon from '../../User/Foto/search.svg';
import './RegistrarDoctorSearch.css';

export default function RegistrarDoctorSearch() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const {
    doctors,
    selectedPatient,
    fetchDoctors,
    setSelectedDoctor,
    loading,
    error,
  } = useApp();

  const [isOpen, setIsOpen] = useState(false);
  const [searchText, setSearchText] = useState('');

  const formatName = (fio) => {
    if (!fio) return 'Гость';
    const parts = fio.trim().split(/\s+/);
    if (parts.length === 1) return parts[0];
    const firstName = parts[0];
    const initials = parts.slice(1).map((part) => `${part[0].toUpperCase()}.`).join(' ');
    return `${firstName} ${initials}`;
  };

  useEffect(() => {
    if (!selectedPatient?.id) {
      navigate('/registrar/patients');
      return;
    }

    fetchDoctors('');
  }, [fetchDoctors, navigate, selectedPatient?.id]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const handleSearch = async (value) => {
    setSearchText(value);
    await fetchDoctors(value);
  };

  const handleSelectDoctor = (doctor) => {
    setSelectedDoctor({
      id: doctor.doctor_id ?? doctor.doctorId,
      name: doctor.fio,
      specialty: doctor.speciality ?? doctor.specialty,
      address: doctor.address || doctor.clinic_address || 'г. Москва, ул. Менделеева, д. 15с2',
    });

    navigate('/registrar/calendar');
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

      <main className="content search-page registrar-doctor-search-page">
        <div className="search-container registrar-doctor-search-container">
          <div className="top-bar registrar-search-top">
            <button className="back-button" onClick={() => navigate('/registrar/patient')}>Назад</button>
          </div>

          <div className="selected-patient-card">{selectedPatient?.fio}</div>

          <div className="search-input-wrapper">
            <div className="search-icon" aria-hidden="true">
              <img src={searchIcon} alt="" className="search-icon-image" />
            </div>
            <input
              type="text"
              placeholder="Имя / Специализация"
              className="search-field"
              value={searchText}
              onChange={(event) => handleSearch(event.target.value)}
            />
          </div>

          {error && <p className="small-error">{error}</p>}

          <div className="doctors-grid">
            {loading ? (
              <p className="small-hint">Загрузка врачей...</p>
            ) : doctors.length === 0 ? (
              <p className="small-hint">Врачи не найдены</p>
            ) : (
              doctors.map((doctor) => (
                <button
                  key={doctor.doctor_id ?? doctor.doctorId}
                  className="doctor-card-link"
                  onClick={() => handleSelectDoctor(doctor)}
                >
                  <div className="doctor-card">
                    <h3 className="spec-title">{doctor.speciality ?? doctor.specialty}</h3>
                    <p className="doc-name">{doctor.fio}</p>
                  </div>
                </button>
              ))
            )}
          </div>
        </div>
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}

