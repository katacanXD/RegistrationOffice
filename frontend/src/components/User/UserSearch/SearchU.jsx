import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useApp } from '../../../context/AppContext';
import './SearchU.css';
import myLogo from '../Foto/Icon.png';
import myAvatar from '../Foto/Account.png';
import searchIcon from '../Foto/search.svg';

export default function SearchU() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { doctors, loading, error, userPolicy, fetchDoctors, fetchUserPolicy, setSelectedDoctor } = useApp();
  const [isOpen, setIsOpen] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [selectedSpecialty, setSelectedSpecialty] = useState('');
  const [policyChecked, setPolicyChecked] = useState(false);

  useEffect(() => {
    const loadInitialData = async () => {
      // Загружаем полис если его нет
      if (!policyChecked) {
        await fetchUserPolicy();
        setPolicyChecked(true);
      }
    };
    loadInitialData();
  }, []);

  useEffect(() => {
    if (policyChecked) {
      if (!userPolicy) {
        navigate('/MainU');
      } else {
        // Загружаем список всех врачей
        fetchDoctors();
      }
    }
  }, [policyChecked]);

  const formatName = (fio) => {
    if (!fio) return "Гость";
    const parts = fio.trim().split(/\s+/);
    if (parts.length === 1) return parts[0];
    const firstName = parts[0];
    const initials = parts.slice(1).map(part => `${part[0].toUpperCase()}.`).join(' ');
    return `${firstName} ${initials}`;
  };

  const handleSearch = async (text) => {
    setSearchText(text);
    await fetchDoctors(text, selectedSpecialty);
  };

  const handleSpecialtyFilter = async (specialty) => {
    setSelectedSpecialty(specialty);
    await fetchDoctors(searchText, specialty);
  };

  const handleSelectDoctor = (doctor) => {
    setSelectedDoctor({
      id: doctor.doctor_id,
      name: doctor.fio,
      specialty: doctor.speciality,
      address: doctor.address
    });
  };

  return (
    <div className="app-wrapper">
      <header className="main-header">
        <div className="header-left">
          <Link to="/MainU" className="header-link">
            <img src={myLogo} alt="Logo" className="header-logo-img" />
            <span className="brand-name">REGISTRATION OFFICE</span>
          </Link>
        </div>
        <div className="header-right">
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

                <Link to="/" className="dropdown-item logout-link">
                  <div className="logout-icon-circle">
                    <span className="exit-arrow">→</span>
                  </div>
                  <span className="logout-text">Sign out</span>
                </Link>
              </div>
            )}
          </div>
        </div>
      </header>

      <main className="content search-page">
        <div className="search-container">

          <div className="top-bar">
            <Link to="/MainU" className="back-button">Назад</Link>
          </div>

          <div className="search-input-wrapper">
            <div className="search-icon" aria-hidden="true">
              <img src={searchIcon} alt="" className="search-icon-image" />
            </div>
            <input
              type="text"
              placeholder="Имя / Специалист"
              className="search-field"
              value={searchText}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </div>

          {error && <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>}

          <div className="doctors-grid">
            {loading ? (
              <p style={{ color: '#A5B4FC', textAlign: 'center', gridColumn: '1/-1' }}>Загрузка врачей...</p>
            ) : doctors.length === 0 ? (
              <p style={{ color: '#A5B4FC', textAlign: 'center', gridColumn: '1/-1' }}>Врачей не найдено</p>
            ) : (
              doctors.map(doc => (
                <Link
                  key={doc.doctor_id}
                  to="/CalendarU"
                  onClick={() => handleSelectDoctor(doc)}
                  className="doctor-card-link"
                >
                  <div className="doctor-card">
                    <h3 className="spec-title">{doc.speciality}</h3>
                    <p className="doc-name">{doc.fio}</p>
                  </div>
                </Link>
              ))
            )}
          </div>

        </div>
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}