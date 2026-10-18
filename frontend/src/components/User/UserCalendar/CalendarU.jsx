import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  format, addMonths, subMonths, startOfMonth, endOfMonth,
  startOfWeek, endOfWeek, addDays, isSameMonth, isSameDay, isPast, startOfDay
} from 'date-fns';
import { ru } from 'date-fns/locale';
import { useAuth } from '../../../context/AuthContext';
import { useApp } from '../../../context/AppContext';
import '../UserMain/MainU.css';
import './CalendarU.css';
import myLogo from '../Foto/Icon.png';
import myAvatar from '../Foto/Account.png';

export default function CalendarU() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { selectedDoctor, loading, error, userPolicy, fetchDoctorSlots, fetchUserPolicy, createAppointment, rescheduleAppointmentId, setRescheduleAppointmentId, updateRegistrarAppointment } = useApp();

  const [isOpen, setIsOpen] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedTime, setSelectedTime] = useState(null);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [policyChecked, setPolicyChecked] = useState(false);
  const autoSelectedDateRef = useRef(false);

  const fetchSlotsForDate = useCallback(async (date) => {
    if (!selectedDoctor?.id) return [];

    const dateStr = format(date, 'yyyy-MM-dd');
    const result = await fetchDoctorSlots(selectedDoctor.id, dateStr);
    if (!result.success) {
      return [];
    }

    return (result.slots || []).filter((slot) => {
      if (typeof slot === 'string') {
        return true;
      }
      return slot?.is_available === true;
    });
  }, [fetchDoctorSlots, selectedDoctor?.id]);

  const loadSlots = useCallback(async () => {
    if (!selectedDoctor?.id) return;

    setSlotsLoading(true);

    const currentSlots = await fetchSlotsForDate(selectedDate);
    if (currentSlots.length > 0 || autoSelectedDateRef.current) {
      setAvailableSlots(currentSlots);
      setSelectedTime(null);
      setSlotsLoading(false);
      return;
    }

    // Если на выбранную дату ничего нет — подбираем ближайший день со слотами.
    for (let offset = 1; offset <= 14; offset += 1) {
      const candidateDate = addDays(selectedDate, offset);
      const candidateSlots = await fetchSlotsForDate(candidateDate);
      if (candidateSlots.length > 0) {
        autoSelectedDateRef.current = true;
        setSelectedDate(candidateDate);
        setAvailableSlots(candidateSlots);
        setSelectedTime(null);
        setSlotsLoading(false);
        return;
      }
    }

    setAvailableSlots([]);
    setSelectedTime(null);
    setSlotsLoading(false);
  }, [fetchSlotsForDate, selectedDate, selectedDoctor?.id]);

  useEffect(() => {
    autoSelectedDateRef.current = false;
  }, [selectedDoctor?.id]);

  useEffect(() => {
    const checkPolicy = async () => {
      if (policyChecked) return;

      if (!userPolicy) {
        await fetchUserPolicy();
      }

      setPolicyChecked(true);
    };
    checkPolicy();
  }, [fetchUserPolicy, policyChecked, userPolicy]);

   useEffect(() => {
     if (policyChecked) {
       if (!userPolicy) {
         navigate('/patient/main');
         return;
       }
       if (!selectedDoctor?.id) {
         navigate('/patient/main');
         return;
       }
       const timerId = setTimeout(() => {
         loadSlots();
       }, 0);

       return () => clearTimeout(timerId);
     }
     return undefined;
   }, [loadSlots, navigate, policyChecked, selectedDoctor?.id, selectedDate, userPolicy]);

  const formatName = (fio) => {
    if (!fio) return "Гость";
    const parts = fio.trim().split(/\s+/);
    if (parts.length === 1) return parts[0];
    const firstName = parts[0];
    const initials = parts.slice(1).map(part => `${part[0].toUpperCase()}.`).join(' ');
    return `${firstName} ${initials}`;
  };

   const handleConfirm = async () => {
     if (!userPolicy) {
       navigate('/patient/main');
       return;
     }

     if (!selectedTime) {
       return;
     }

     setCreating(true);
     const slotDatetime = format(selectedDate, 'yyyy-MM-dd') + 'T' + selectedTime;

     let result;
     if (rescheduleAppointmentId) {
       // Режим переноса
       result = await updateRegistrarAppointment(rescheduleAppointmentId, slotDatetime);
     } else {
       // Режим создания
       result = await createAppointment(selectedDoctor.id, slotDatetime);
     }
     setCreating(false);

     if (result.success) {
       setShowModal(false);
       setRescheduleAppointmentId(null);
       navigate('/patient/main');
     }
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
        const isPastDay = isPast(startOfDay(day)) && !isSameDay(day, new Date());
        const isCurrentMonth = isSameMonth(day, monthStart);
        const isSelected = isSameDay(day, selectedDate);

        let cellClass = 'cell';
        if (!isCurrentMonth) {
          cellClass += ' disabled';
        } else if (isPastDay) {
          cellClass += ' past-day';
        } else if (isSelected) {
          cellClass += ' selected';
        }

        days.push(
            <div
                key={day}
                className={cellClass}
                onClick={() => !isPastDay && isCurrentMonth && setSelectedDate(cloneDay)}
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

  if (!selectedDoctor) {
    return <div>Загрузка...</div>;
  }

  const clinicAddress = selectedDoctor.address || 'г. Москва, ул. Менделеева, д. 15с2';
  const weekdayShort = format(selectedDate, 'EEE', { locale: ru }).replace('.', '');
  const weekdayCapitalized = weekdayShort.charAt(0).toUpperCase() + weekdayShort.slice(1);
  const modalDateLabel = `${format(selectedDate, 'd MMMM', { locale: ru })}, ${weekdayCapitalized}`;

  const groupedSlots = {};
  const now = new Date();

  availableSlots.forEach(slot => {
    const time = slot.time || slot;
    const timeObj = new Date(`2000-01-01T${time}`);
    const hour = timeObj.getHours();

    // Проверяем, не прошло ли это время
    const slotDateTime = new Date(selectedDate);
    slotDateTime.setHours(timeObj.getHours(), timeObj.getMinutes(), 0, 0);

    // Не добавляем, если время прошло
    if (slotDateTime <= now) {
      return;
    }

    const period = hour <= 12 ? 'before' : 'after';
    if (!groupedSlots[period]) groupedSlots[period] = [];
    groupedSlots[period].push(time);
  });

  return (
      <div className="app-wrapper">
        <header className="main-header">
          <div className="header-left">
            <Link to="/patient/main" className="header-link">
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
              <Link to="/patient/search" className="back-button">Назад</Link>
            </div>

            <div className="calendar-layout">
              <div className="calendar-box">
                {renderHeader()}
                {renderDays()}
                {renderCells()}
              </div>

              <div className="time-box">
                <div className="time-header">{format(selectedDate, 'd MMMM', { locale: ru })}</div>

                {error && <p className="small-error calendar-status">{error}</p>}
                {slotsLoading && <p className="small-hint calendar-status">Загрузка слотов...</p>}

                {groupedSlots.before && groupedSlots.before.length > 0 && (
                    <div className="time-grid">
                      {groupedSlots.before.map((t, i) => (
                          <button
                              key={i}
                              className={`time-item ${selectedTime === t ? 'active' : ''}`}
                              onClick={() => handleTimeClick(t)}
                              disabled={slotsLoading}
                          >
                            {t}
                          </button>
                      ))}
                    </div>
                )}

                {(groupedSlots.before || groupedSlots.after) && (
                    <div className="divider-label"><span>Перерыв</span></div>
                )}

                {groupedSlots.after && groupedSlots.after.length > 0 && (
                    <div className="time-grid">
                      {groupedSlots.after.map((t, i) => (
                          <button
                              key={i}
                              className={`time-item ${selectedTime === t ? 'active' : ''}`}
                              onClick={() => handleTimeClick(t)}
                              disabled={slotsLoading}
                          >
                            {t}
                          </button>
                      ))}
                    </div>
                )}

                {(!groupedSlots.before && !groupedSlots.after) && !slotsLoading && (
                    <p className="small-hint calendar-status">Нет свободных слотов</p>
                )}
              </div>
            </div>

            {showModal && (
                <div className="modal-overlay">
                  <div className="confirm-modal">
                    <div className="modal-header-row">
                      <h2 className="modal-title">{selectedDoctor.specialty}</h2>
                      <div className="modal-date-time">
                        <p className="modal-date-label">{modalDateLabel}</p>
                        <span className="modal-time-big">{selectedTime}</span>
                      </div>
                    </div>

                    <div className="modal-body">
                      <div className="info-block">
                        <span className="info-label">Врач</span>
                        <p className="info-value">{selectedDoctor.name}</p>
                      </div>
                      <div className="info-block">
                        <span className="info-label">Адрес клиники</span>
                        <p className="info-value">{clinicAddress}</p>
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
          </div>
        </main>

        <footer className="main-footer"></footer>
      </div>
  );
}