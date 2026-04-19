import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  addDays,
  addMonths,
  endOfMonth,
  endOfWeek,
  format,
  isSameDay,
  isSameMonth,
  isPast,
  startOfDay,
  startOfMonth,
  startOfWeek,
  subMonths,
} from 'date-fns';
import { ru } from 'date-fns/locale';
import { useAuth } from '../../../context/AuthContext';
import { useApp } from '../../../context/AppContext';
import myLogo from '../../User/Foto/Icon.png';
import myAvatar from '../../User/Foto/Account.png';
import './RegistrarCalendar.css';

export default function RegistrarCalendar() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const {
    selectedPatient,
    selectedDoctor,
    fetchDoctorSlots,
    createRegistrarAppointment,
    updateRegistrarAppointment,
    rescheduleAppointmentId,
    setRescheduleAppointmentId,
    loading,
    error,
  } = useApp();

  const [isOpen, setIsOpen] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedTime, setSelectedTime] = useState(null);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [creating, setCreating] = useState(false);

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
    if (!selectedDoctor?.id) {
      navigate('/registrar/patient');
    }
  }, [navigate, selectedDoctor?.id, selectedPatient?.id]);

  const loadSlots = useCallback(async () => {
    if (!selectedDoctor?.id) return;

    setSlotsLoading(true);
    const result = await fetchDoctorSlots(selectedDoctor.id, format(selectedDate, 'yyyy-MM-dd'));
    if (!result.success) {
      setAvailableSlots([]);
      setSlotsLoading(false);
      return;
    }

    const free = (result.slots || []).filter((slot) => {
      if (typeof slot === 'string') return true;
      return slot.is_available === true || slot.isAvailable === true;
    });

    setAvailableSlots(free);
    setSelectedTime(null);
    setSlotsLoading(false);
  }, [fetchDoctorSlots, selectedDate, selectedDoctor?.id]);

  useEffect(() => {
    loadSlots();
  }, [loadSlots]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

   const handleConfirm = async () => {
     if (!selectedTime || !selectedDoctor?.id || !selectedPatient?.id) return;

     setCreating(true);
     const slotDatetime = `${format(selectedDate, 'yyyy-MM-dd')}T${selectedTime}`;

     let result;
     if (rescheduleAppointmentId) {
       // Режим переноса
       result = await updateRegistrarAppointment(rescheduleAppointmentId, slotDatetime);
     } else {
       // Режим создания
       result = await createRegistrarAppointment(selectedDoctor.id, selectedPatient.id, slotDatetime);
     }
     setCreating(false);

     if (result.success) {
       setShowModal(false);
       setRescheduleAppointmentId(null);
       navigate('/registrar/patient');
     }
   };

  const groupedSlots = useMemo(() => {
    const grouped = { before: [], after: [] };

    availableSlots.forEach((slot) => {
      const time = slot.time || slot;
      const [hours] = String(time).split(':');
      const period = Number(hours) < 13 ? 'before' : 'after';
      grouped[period].push(time);
    });

    return grouped;
  }, [availableSlots]);

  const renderCells = () => {
    const monthStart = startOfMonth(currentMonth);
    const monthEnd = endOfMonth(monthStart);
    const startDate = startOfWeek(monthStart, { weekStartsOn: 1 });
    const endDate = endOfWeek(monthEnd, { weekStartsOn: 1 });

    const rows = [];
    let days = [];
    let day = startDate;

    while (day <= endDate) {
      for (let i = 0; i < 7; i += 1) {
        const cloneDay = day;
        const isCurrentMonth = isSameMonth(day, monthStart);
        const isPastDay = isPast(startOfDay(day)) && !isSameDay(day, new Date());
        const isSelected = isSameDay(day, selectedDate);

        let cellClass = 'cell';
        if (!isCurrentMonth) {
          cellClass += ' disabled';
        } else if (isPastDay) {
          cellClass += ' past-day';
        }
        if (isSelected) {
          cellClass += ' selected';
        }

        days.push(
          <div
            key={format(day, 'yyyy-MM-dd')}
            className={cellClass}
            onClick={() => !isPastDay && isCurrentMonth && setSelectedDate(cloneDay)}
          >
            <span>{format(day, 'd')}</span>
          </div>
        );

        day = addDays(day, 1);
      }

      rows.push(
        <div className="row" key={format(day, 'yyyy-MM-dd')}>
          {days}
        </div>
      );
      days = [];
    }

    return <div className="calendar-body">{rows}</div>;
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

      <main className="content calendar-page">
        <div className="calendar-container">
          <div className="top-bar registrar-top-row">
            <button className="back-button" onClick={() => navigate('/registrar/doctors')}>Назад</button>
            <span className="selected-patient-tag">{selectedPatient?.fio}</span>
          </div>

          <div className="calendar-layout">
            <div className="calendar-box">
              <div className="calendar-header-nav">
                <button onClick={() => setCurrentMonth(subMonths(currentMonth, 1))} className="nav-btn">←</button>
                <div className="current-month-label">{format(currentMonth, 'LLLL', { locale: ru }).toUpperCase()}</div>
                <button onClick={() => setCurrentMonth(addMonths(currentMonth, 1))} className="nav-btn">→</button>
              </div>

              <div className="days-row">
                {['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ', 'ВС'].map((d) => (
                  <div key={d} className="day-name">{d}</div>
                ))}
              </div>

              {renderCells()}
            </div>

            <div className="time-box">
              <div className="time-header">{format(selectedDate, 'd MMMM', { locale: ru })}</div>

              {error && <p className="small-error">{error}</p>}
              {slotsLoading && <p className="small-hint">Загрузка слотов...</p>}

              <div className="time-grid">
                {groupedSlots.before.map((time) => (
                  <button
                    key={`before-${time}`}
                    className={`time-item ${selectedTime === time ? 'active' : ''}`}
                    onClick={() => {
                      setSelectedTime(time);
                      setShowModal(true);
                    }}
                    disabled={slotsLoading}
                  >
                    {time}
                  </button>
                ))}
              </div>

              <div className="divider-label"><span>Перерыв</span></div>

              <div className="time-grid">
                {groupedSlots.after.map((time) => (
                  <button
                    key={`after-${time}`}
                    className={`time-item ${selectedTime === time ? 'active' : ''}`}
                    onClick={() => {
                      setSelectedTime(time);
                      setShowModal(true);
                    }}
                    disabled={slotsLoading}
                  >
                    {time}
                  </button>
                ))}
              </div>

              {!slotsLoading && groupedSlots.before.length === 0 && groupedSlots.after.length === 0 && (
                <p className="small-hint">Нет свободных слотов</p>
              )}
            </div>
          </div>
        </div>

        {showModal && (
          <div className="modal-overlay" onClick={() => setShowModal(false)}>
            <div className="confirm-modal" onClick={(event) => event.stopPropagation()}>
              <div className="modal-header-row">
                <h2 className="modal-title">{selectedDoctor?.specialty}</h2>
                <div className="modal-date-time">
                  <p className="modal-date-label">{format(selectedDate, 'd MMMM', { locale: ru })}</p>
                  <span className="modal-time-big">{selectedTime}</span>
                </div>
              </div>

              <div className="modal-body">
                <div className="info-block">
                  <span className="info-label">Врач</span>
                  <p className="info-value">{selectedDoctor?.name}</p>
                </div>
                <div className="info-block">
                  <span className="info-label">Пациент</span>
                  <p className="info-value">{selectedPatient?.fio}</p>
                </div>
                <div className="info-block">
                  <span className="info-label">Полис ОМС</span>
                  <p className="info-value">{selectedPatient?.policyNumber || '-'}</p>
                </div>
                <div className="info-block">
                  <span className="info-label">Адрес</span>
                  <p className="info-value">{selectedDoctor?.address}</p>
                </div>
              </div>

              <div className="modal-footer">
                <button className="btn-cancel" onClick={() => setShowModal(false)} disabled={creating}>Отмена</button>
                <button className="btn-confirm" onClick={handleConfirm} disabled={creating || loading}>
                  {creating ? 'Создание...' : 'Подтвердить'}
                </button>
              </div>
            </div>
          </div>
        )}
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}


