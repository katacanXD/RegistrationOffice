import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useApp } from '../../../context/AppContext';
import myLogo from '../../User/Foto/Icon.png';
import myAvatar from '../../User/Foto/Account.png';
import './RegistrarPatientOverview.css';

export default function RegistrarPatientOverview() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const {
    selectedPatient,
    registrarPatientDashboard,
    fetchRegistrarPatientDashboard,
    cancelAppointmentDirect,
    loading,
    error,
    setSelectedDoctor,
    setRescheduleAppointmentId,
  } = useApp();

  const [isOpen, setIsOpen] = useState(false);
  const [expandedIndex, setExpandedIndex] = useState(null);

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

    fetchRegistrarPatientDashboard(selectedPatient.id);
  }, [fetchRegistrarPatientDashboard, navigate, selectedPatient?.id]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const handleContinue = () => {
    setSelectedDoctor(null);
    navigate('/registrar/doctors');
  };

  const handleDeleteAppointment = async (appointmentId) => {
    if (!selectedPatient?.id) return;
    if (!window.confirm('Вы уверены, что хотите отменить запись?')) return;

    const result = await cancelAppointmentDirect(appointmentId);
    if (result.success) {
      await fetchRegistrarPatientDashboard(selectedPatient.id);
      setExpandedIndex(null);
    }
  };

   const handleRescheduleAppointment = (appointmentId, doctorId, doctorFio, specialty, address) => {
     setSelectedDoctor({
       id: doctorId,
       name: doctorFio,
       specialty: specialty,
       address: address || 'г. Москва, ул. Менделеева, д. 15с2',
     });
     setRescheduleAppointmentId(appointmentId);
     navigate('/registrar/calendar');
   };

  const dashboard = registrarPatientDashboard;
  const patientUser = dashboard?.user;
  const appointments = dashboard?.appointments || [];

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

      <main className="content registrar-overview-page">
        <div className="registrar-overview-container">
          <div className="top-bar registrar-overview-back-row">
            <button className="back-button" onClick={() => navigate('/registrar/patients')}>Назад</button>
          </div>

          {error && <p className="small-error">{error}</p>}
          {loading && !dashboard && <p className="small-hint">Загрузка карточки пациента...</p>}

          <section className="policy-section registrar-overview-summary">
            <div className="policy-panel patient-info-panel">
              <div className="patient-info-row">
                <label className="field-label">ФИО пациента</label>
                <div className="policy-display">
                  <div className="policy-value">{patientUser?.fio || selectedPatient?.fio}</div>
                </div>
              </div>

              <div className="patient-info-row">
                <label className="field-label">Номер полиса ОМС</label>
                <div className="policy-display">
                  <div className="policy-value">{patientUser?.policyNumber || selectedPatient?.policyNumber || 'Без полиса'}</div>
                </div>
              </div>
            </div>

            <button className="btn-action-primary btn-book-cta registrar-overview-book-btn" onClick={handleContinue}>
              Записать
            </button>
          </section>

           <section className="registrar-appointments-section">
             <div className="section-header" style={{ display: 'none' }}>
               <h3>Записи пациента</h3>
               <span>{appointments.length}</span>
             </div>

            {appointments.length === 0 ? (
              <p className="small-hint">У пациента нет активных записей</p>
            ) : (
              appointments.map((app, index) => (
                <div key={app.appointmentId ?? app.appointment_id} className="appointment-card">
                  <div className="appointment-main" onClick={() => setExpandedIndex(expandedIndex === index ? null : index)}>
                    <div className="app-info-left">
                      <h3 className="app-spec-title">{app.speciality}</h3>
                    </div>
                    <div className="app-info-right">
                      <span className="app-date-text">{new Date(app.datetime).toLocaleDateString('ru-RU')}</span>
                      <span className="app-time-text">{new Date(app.datetime).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}</span>
                    </div>
                    <div className={`arrow-icon ${expandedIndex === index ? 'up' : 'down'}`}></div>
                  </div>

                  {expandedIndex === index && (
                    <div className="appointment-details">
                      <div className="detail-row">
                        <span className="detail-label">Врач</span>
                        <p className="detail-value">{app.doctorFio ?? app.doctor_fio}</p>
                      </div>
                      <div className="detail-row">
                        <span className="detail-label">Адрес</span>
                        <p className="detail-value">{app.address}</p>
                      </div>
                       <div className="detail-buttons">
                         <button
                           className="btn-app-action btn-app-delete"
                           onClick={() => handleDeleteAppointment(app.appointmentId ?? app.appointment_id)}
                           disabled={loading}
                         >
                           Удалить
                         </button>
                         <button
                           className="btn-app-action"
                           onClick={() => handleRescheduleAppointment(
                             app.appointmentId ?? app.appointment_id,
                             app.doctorId ?? app.doctor_id,
                             app.doctorFio ?? app.doctor_fio,
                             app.speciality ?? app.specialty,
                             app.address
                           )}
                         >
                           Перенести
                         </button>
                       </div>
                    </div>
                  )}
                </div>
              ))
            )}
          </section>
        </div>
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}

