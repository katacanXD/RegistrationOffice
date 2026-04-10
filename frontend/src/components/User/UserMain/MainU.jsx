import './MainU.css'
import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useApp } from '../../../context/AppContext';
import myLogo from '../Foto/Icon.png';
import myAvatar from '../Foto/Account.png';

export default function MainU () {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { appointments, loading, error, userPolicy, fetchAppointments, fetchUserPolicy, deleteAppointment, updatePolicy } = useApp();
  const [isOpen, setIsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(null);
  const [policyInput, setPolicyInput] = useState('');
  const [policyLoading, setPolicyLoading] = useState(false);
  const [policyLoaded, setPolicyLoaded] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      await fetchAppointments();
      await fetchUserPolicy();
      setPolicyLoaded(true);
    };
    loadData();
  }, []);

  const formatName = (fio) => {
    if (!fio) return "Гость";
    const parts = fio.trim().split(/\s+/);
    if (parts.length === 1) return parts[0];
    const firstName = parts[0];
    const initials = parts.slice(1).map(part => `${part[0].toUpperCase()}.`).join(' ');
    return `${firstName} ${initials}`;
  };

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const handleDeleteAppointment = async (appointmentId) => {
    if (window.confirm('Вы уверены, что хотите отменить запись?')) {
      const result = await deleteAppointment(appointmentId);
      if (!result.success) {
        alert(result.error || 'Ошибка при удалении записи');
      }
    }
  };

  const handleUpdatePolicy = async (policyValue = policyInput) => {
    if (!policyValue.trim()) {
      alert('Введите номер полиса');
      return;
    }
    setPolicyLoading(true);
    const result = await updatePolicy(policyValue);
    setPolicyLoading(false);
    if (result.success) {
      alert('Полис успешно добавлен');
      setPolicyInput('');
    } else {
      alert(result.error || 'Ошибка при обновлении полиса');
    }
  };

  const handleSavePolicy = async () => {
    if (policyInput.trim()) {
      await handleUpdatePolicy(policyInput);
    }
  };

  const handleGoToSearch = () => {
    if (!userPolicy) {
      alert('Пожалуйста, сначала добавьте полис ОМС');
      return;
    }
    navigate('/SearchU');
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

                <button
                  onClick={handleLogout}
                  className="dropdown-item logout-link"
                  style={{ background: 'none', border: 'none', cursor: 'pointer', width: '100%', textAlign: 'left' }}
                >
                  <div className="logout-icon-circle">
                    <span className="exit-arrow">→</span>
                  </div>
                  <span className="logout-text">Sign out</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      <main className="content">
        <div className="container">

          <div className="policy-section">
            <label className="field-label">Номер полиса ОМС</label>
            {!policyLoaded ? (
              <p style={{ color: '#A5B4FC', textAlign: 'center' }}>Загрузка полиса...</p>
            ) : userPolicy ? (
              <div className="policy-display">
                <div className="policy-value">{userPolicy}</div>
                <button
                  className="btn-action-primary"
                  onClick={handleGoToSearch}
                  disabled={loading}
                >
                  Записаться
                </button>
              </div>
            ) : (
              <div className="policy-input-group">
                <input
                  type="text"
                  id="oms"
                  className="soft-input"
                  value={policyInput}
                  onChange={(e) => setPolicyInput(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && policyInput.trim() && handleSavePolicy()}
                  disabled={policyLoading}
                />
                <button
                  className="btn-action-primary"
                  onClick={handleSavePolicy}
                  disabled={policyLoading || !policyInput.trim()}
                >
                  {policyLoading ? '...' : 'Сохранить'}
                </button>
              </div>
            )}
          </div>

          {error && <p style={{ color: 'red', textAlign: 'center', marginTop: '20px' }}>{error}</p>}

          <div className="appointments-section">
            {appointments.length > 0 && (
              appointments.map((app, index) => (
                <div key={app.appointment_id} className="appointment-card">
                  <div
                    className="appointment-main"
                    onClick={() => setActiveIndex(activeIndex === index ? null : index)}
                  >
                    <div className="app-info-left">
                      <h3 className="app-spec-title">{app.speciality}</h3>
                    </div>
                    <div className="app-info-right">
                      <span className="app-date-text">{new Date(app.datetime).toLocaleDateString('ru-RU')}</span>
                      <span className="app-time-text">{new Date(app.datetime).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}</span>
                    </div>
                    <div className={`arrow-icon ${activeIndex === index ? 'up' : 'down'}`}></div>
                  </div>

                  {activeIndex === index && (
                    <div className="appointment-details">
                      <div className="detail-row">
                        <span className="detail-label">Врач</span>
                        <p className="detail-value">{app.doctor_fio}</p>
                      </div>
                      <div className="detail-row">
                        <span className="detail-label">Адрес</span>
                        <p className="detail-value">{app.address}</p>
                      </div>
                      <div className="detail-buttons">
                        <button
                          className="btn-app-action"
                          onClick={() => handleDeleteAppointment(app.appointment_id)}
                          disabled={loading}
                        >
                          Удалить
                        </button>
                        <button className="btn-app-action" disabled>Перенести (в разработке)</button>
                      </div>
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}