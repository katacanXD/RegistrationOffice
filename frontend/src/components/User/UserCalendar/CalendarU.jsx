import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { 
  format, addMonths, subMonths, startOfMonth, endOfMonth, 
  startOfWeek, endOfWeek, addDays, isSameMonth, isSameDay 
} from 'date-fns';
import { ru } from 'date-fns/locale';
import './CalendarU.css';
import myLogo from '../Foto/Icon.png';
import myAvatar from '../Foto/Account.png';

export default function CalendarU() {
  const [isOpen, setIsOpen] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  const userName = location.state?.userName || "Гость";
  const doctorName = location.state?.selectedDoctor || "Врач не выбран";
  const specialization = location.state?.specialization || "Специалист";
  const address = location.state?.address || "Адрес не указан";
  
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedTime, setSelectedTime] = useState(null);

  const timesBeforeLunch = ["9:00", "10:00", "11:00", "12:00"];
  const timesAfterLunch = ["15:00", "16:00", "17:00", "18:00"];

  const formatName = (name) => {
    if (!name) return "Гость";
    const parts = name.trim().split(/\s+/); 
    if (parts.length === 1) return parts[0]; 
    const firstName = parts[0];
    const initials = parts.slice(1).map(part => `${part[0].toUpperCase()}.`).join(' '); 
    return `${firstName} ${initials}`;
  };

  const handleConfirm = () => {
    const newAppointment = {
      id: Date.now(), 
      spec: specialization,
      doctor: doctorName,
      address: address,
      date: format(selectedDate, 'd MMMM, ee', { locale: ru }),
      time: selectedTime
    };

    // 1. Достаем старые записи из памяти
    const existingAppointments = JSON.parse(localStorage.getItem('my_appointments') || '[]');
    
    // 2. Добавляем новую к старым
    const updatedAppointments = [newAppointment, ...existingAppointments];
    
    // 3. Сохраняем обратно в память
    localStorage.setItem('my_appointments', JSON.stringify(updatedAppointments));

    // 4. Возвращаемся на главную
    navigate('/MainU', { state: { userName } });
  };

  const handleTimeClick = (time) => {
  setSelectedTime(time);
  setShowModal(true); 
  };

  const renderHeader = () => (
    <div className="calendar-header-nav">
      <button onClick={() => setCurrentMonth(subMonths(currentMonth, 1))} className="nav-btn">←</button>
      <div className="current-month-label">
        {format(currentMonth, 'LLLL', { locale: ru }).toUpperCase()}
      </div>
      <button onClick={() => setCurrentMonth(addMonths(currentMonth, 1))} className="nav-btn">→</button>
    </div>
  );

  const renderDays = () => {
    const days = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ', 'ВС'];
    return (
      <div className="days-row">
        {days.map((d, i) => <div key={i} className="day-name">{d}</div>)}
      </div>
    );
  };

  const renderCells = () => {
    const monthStart = startOfMonth(currentMonth);
    const monthEnd = endOfMonth(monthStart);
    const startDate = startOfWeek(monthStart, { weekStartsOn: 1 });
    const endDate = endOfWeek(monthEnd, { weekStartsOn: 1 });

    const rows = [];
    let days = [];
    let day = startDate;

    while (day <= endDate) {
      for (let i = 0; i < 7; i++) {
        const formattedDate = format(day, 'd');
        const cloneDay = day;
        days.push(
          <div
            key={day}
            className={`cell ${!isSameMonth(day, monthStart) ? 'disabled' : isSameDay(day, selectedDate) ? 'selected' : ''}`}
            onClick={() => setSelectedDate(cloneDay)}
          >
            <span>{formattedDate}</span>
          </div>
        );
        day = addDays(day, 1);
      }
      rows.push(<div className="row" key={day}>{days}</div>);
      days = [];
    }
    return <div className="calendar-body">{rows}</div>;
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
      </header>

      <main className="content calendar-page">
        <div className="calendar-container">
          <div className="top-bar">
            <Link to="/SearchU" state={{ userName }} className="back-button">Назад</Link>
          </div>

          <div className="calendar-layout">
            <div className="calendar-box">
              {renderHeader()}
              {renderDays()}
              {renderCells()}
            </div>

            <div className="time-box">
              <div className="time-header">{format(selectedDate, 'd MMMM', { locale: ru })}</div>
              
              <div className="time-grid">
                {timesBeforeLunch.map((t, i) => (
                  <button 
                    key={i} 
                    className={`time-item ${selectedTime === t ? 'active' : ''}`} 
                    onClick={() => handleTimeClick(t)} 
                  >
                    {t}
                  </button>
                ))}
              </div>

              <div className="divider-label"><span>Перерыв</span></div>

              <div className="time-grid">
                {timesAfterLunch.map((t, i) => (
                  <button 
                    key={i} 
                    className={`time-item ${selectedTime === t ? 'active' : ''}`} 
                    onClick={() => handleTimeClick(t)} 
                  >
                    {t}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {showModal && (
            <div className="modal-overlay">
              <div className="confirm-modal">
                <div className="modal-header-row">
                  <h2 className="modal-title">{specialization}</h2>
                  <div className="modal-date-time">
                    <p>{format(selectedDate, 'd MMMM', { locale: ru })}</p>
                    <span className="modal-time-big">{selectedTime}</span>
                  </div>
                </div>
                
                <div className="modal-body">
                  <div className="info-block">
                    <span className="info-label">Врач</span>
                    <p className="info-value">{doctorName}</p>
                  </div>
                  <div className="info-block">
                    <span className="info-label">Адрес</span>
                    <p className="info-value">{address}</p>
                  </div>
                </div>

                <div className="modal-footer">
                  <button className="btn-cancel" onClick={() => setShowModal(false)}>Отмена</button>
                  <button className="btn-confirm" onClick={handleConfirm}>Подтвердить</button>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}