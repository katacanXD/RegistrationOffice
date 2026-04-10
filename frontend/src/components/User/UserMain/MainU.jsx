import './MainU.css'
import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import myLogo from '../Foto/Icon.png';
import myAvatar from '../Foto/Account.png';

export default function MainU () {
  const [isOpen, setIsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(null);
  const location = useLocation();
  const userName = location.state?.userName || "Гость";

  const formatName = (name) => {
    if (!name) return "Гость";
    
    const parts = name.trim().split(/\s+/); 
    
    if (parts.length === 1) return parts[0]; 

    const firstName = parts[0];
    const initials = parts
      .slice(1) 
      .map(part => `${part[0].toUpperCase()}.`) 
      .join(' '); 

    return `${firstName} ${initials}`;
  };

  const [appointments, setAppointments] = useState(() => {
    const saved = localStorage.getItem('my_appointments');
    return saved ? JSON.parse(saved) : [];
  });

  const deleteAppointment = (id) => {
    const updated = appointments.filter(app => app.id !== id);
    setAppointments(updated);
    localStorage.setItem('my_appointments', JSON.stringify(updated));
  };

  return (
    <div className="app-wrapper">
      <header className="main-header">
        <div className="header-left">
          <Link to="/MainU" className="header-link" state={{ userName }}>
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
                  <span className="user-name">{formatName(userName)}</span>
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

      <main className="content">
        <div className="container">

          <div className="card input-card">
            <label htmlFor="oms" className="field-label">Введите номер полиса ОМС</label>
            <input type="text" id="oms" className="soft-input" />
          </div>

          <Link to="/SearchU" state={{ userName }}>
            <div className="card button-card" style={{ textDecoration: 'none' }}>
              <button className="action-button">ЗАПИСАТЬСЯ</button>
            </div>
          </Link>

          <div className="appointments-section">
            {appointments.length === 0 ? (
              <p style={{ color: '#A5B4FC', textAlign: 'center' }}>Записей пока нет</p>
            ) : (
              appointments.map((app, index) => (
                <div key={app.id} className="appointment-card"> 
                  <div 
                    className="appointment-main" 
                    onClick={() => setActiveIndex(activeIndex === index ? null : index)}
                  >
                    <div className="app-info-left">
                      <h3 className="app-spec-title">{app.spec}</h3>
                    </div>
                    <div className="app-info-right">
                      <span className="app-date-text">{app.date}</span>
                      <span className="app-time-text">{app.time}</span>
                    </div>
                    <div className={`arrow-icon ${activeIndex === index ? 'up' : 'down'}`}></div>
                  </div>

                  {activeIndex === index && (
                    <div className="appointment-details">
                      <div className="detail-row">
                        <span className="detail-label">Врач</span>
                        <p className="detail-value">{app.doctor}</p>
                      </div>
                      <div className="detail-row">
                        <span className="detail-label">Адрес</span>
                        <p className="detail-value">{app.address}</p>
                      </div>
                      <div className="detail-buttons">
                        <button className="btn-app-action" onClick={() => deleteAppointment(app.id)}>
                          Удалить
                        </button>
                        <button className="btn-app-action">Перенести</button>
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