import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import './SearchU.css'; 
import myLogo from '../Foto/Icon.png';
import myAvatar from '../Foto/Account.png';

export default function SearchU()  {
  const [isOpen, setIsOpen] = useState(false);

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

  const doctors = [
    { id: 1, spec: "Терапевт", name: "Кеминова Екатерина Юрьевна" },
    { id: 2, spec: "Ортадонт", name: "Кеминова Екатерина Юрьевна" },
    { id: 3, spec: "Автальмолог", name: "Кеминова Екатерина Юрьевна" },
    { id: 4, spec: "Акулист", name: "Кеминова Екатерина Юрьевна" },
  ];

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

      <main className="content search-page">
        <div className="search-container">
          
          <div className="top-bar">
            <Link to="/MainU" state={{ userName }} className="back-button">Назад</Link>
          </div>

          <div className="search-input-wrapper">
            <div className="search-icon">🔍</div>
            <input type="text" placeholder="Имя / Специалист" className="search-field" />
          </div>

          <div className="doctors-grid">
            {doctors.map(doc => (
              <Link 
                key={doc.id} 
                to="/CalendarU" 
                state={{ 
                  userName, 
                  selectedDoctor: doc.name, 
                  specialization: doc.spec, 
                  address: "г. Москва ул. Менделеева д. 15с2"
                }}
                className="doctor-card-link"
              >
                <div className="doctor-card">
                  <h3 className="spec-title">{doc.spec}</h3>
                  <p className="doc-name">{doc.name}</p>
                </div>
              </Link>
            ))}
          </div>
          
        </div>
      </main>

      <footer className="main-footer"></footer>
    </div>
   );
}