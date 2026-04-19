import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  addDays,
  addMonths,
  endOfMonth,
  endOfWeek,
  format,
  isPast,
  isSameDay,
  isSameMonth,
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
import './DoctorCalendar.css';

function normalizeSlots(rawSlots) {
  return (rawSlots || []).map((slot) => ({
    time: slot.time,
    type: slot.type || 'open',
    appointmentId: slot.appointmentId ?? slot.appointment_id,
  }));
}

export default function DoctorCalendar() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const {
    fetchDoctorCalendar,
    fetchDoctorSchedule,
    fetchAppointmentDetail,
    loading,
    error,
  } = useApp();

  const doctorId = user?.userId ?? user?.user_id;

  const [isOpen, setIsOpen] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [workDaysMap, setWorkDaysMap] = useState({});
  const [scheduleSlots, setScheduleSlots] = useState([]);
  const [detail, setDetail] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);

  const formatName = (fio) => {
    if (!fio) return 'Гость';
    const parts = fio.trim().split(/\s+/);
    if (parts.length === 1) return parts[0];
    const firstName = parts[0];
    const initials = parts.slice(1).map((part) => `${part[0].toUpperCase()}.`).join(' ');
    return `${firstName} ${initials}`;
  };

  const loadCalendar = useCallback(async () => {
    if (!doctorId) return;

    const month = Number(format(currentMonth, 'M'));
    const year = Number(format(currentMonth, 'yyyy'));
    const result = await fetchDoctorCalendar(doctorId, month, year);
    if (!result.success) return;

    const map = {};
    (result.calendar || []).forEach((item) => {
      const dateKey = item.date;
      map[dateKey] = {
        hasAppointments: item.has_appointments ?? item.hasAppointments,
        totalSlots: item.total_slots ?? item.totalSlots,
        closedSlots: item.closed_slots ?? item.closedSlots,
      };
    });

    setWorkDaysMap(map);
  }, [currentMonth, doctorId, fetchDoctorCalendar]);

  const loadSchedule = useCallback(async () => {
    if (!doctorId || !selectedDate) return;

    const date = format(selectedDate, 'yyyy-MM-dd');
    const result = await fetchDoctorSchedule(doctorId, date);
    if (!result.success) {
      setScheduleSlots([]);
      return;
    }

    setScheduleSlots(normalizeSlots(result.schedule?.slots));
  }, [doctorId, fetchDoctorSchedule, selectedDate]);

  useEffect(() => {
    loadCalendar();
  }, [loadCalendar]);

  useEffect(() => {
    loadSchedule();
  }, [loadSchedule]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const handleSlotClick = async (slot) => {
    if (slot.type !== 'closed' || !slot.appointmentId) return;

    setDetailLoading(true);
    const result = await fetchAppointmentDetail(slot.appointmentId);
    setDetailLoading(false);

    if (!result.success) return;

    setDetail(result.detail);
    setShowModal(true);
  };

  const groupedSlots = useMemo(() => {
    const groups = { before: [], after: [] };

    scheduleSlots.forEach((slot) => {
      const [hours] = String(slot.time || '').split(':');
      const bucket = Number(hours) < 13 ? 'before' : 'after';
      groups[bucket].push(slot);
    });

    return groups;
  }, [scheduleSlots]);

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
        const key = format(day, 'yyyy-MM-dd');
        const isCurrentMonth = isSameMonth(day, monthStart);
        const isSelected = isSameDay(day, selectedDate);
        const isPastDay = isPast(startOfDay(day)) && !isSameDay(day, new Date());
        const dayInfo = workDaysMap[key];

        let cellClass = 'cell';
        if (!isCurrentMonth) {
          cellClass += ' disabled';
        } else if (isPastDay) {
          cellClass += ' past-day';
        } else if (dayInfo) {
          cellClass += dayInfo.hasAppointments ? ' has-bookings' : ' work-day';
        } else {
          cellClass += ' no-work-day';
        }
        if (isSelected) {
          cellClass += ' selected';
        }

        days.push(
          <div
            key={key}
            className={cellClass}
            onClick={() => isCurrentMonth && !isPastDay && setSelectedDate(cloneDay)}
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

  const dateLabel = format(selectedDate, 'd MMMM', { locale: ru });

  return (
    <div className="app-wrapper">
      <header className="main-header">
        <div className="header-left">
          <Link to="/doctor/calendar" className="header-link">
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

      <main className="content doctor-page">
        <div className="doctor-container">
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
              <div className="time-header">{dateLabel}</div>

              {error && <p className="small-error">{error}</p>}
              {loading && <p className="small-hint">Загрузка...</p>}

              <div className="time-grid">
                {groupedSlots.before.map((slot) => (
                  <button
                    key={`before-${slot.time}`}
                    className={`time-item ${slot.type === 'closed' ? 'booked' : ''}`}
                    onClick={() => handleSlotClick(slot)}
                    disabled={slot.type !== 'closed'}
                  >
                    {slot.time}
                  </button>
                ))}
              </div>

              <div className="divider-label"><span>Перерыв</span></div>

              <div className="time-grid">
                {groupedSlots.after.map((slot) => (
                  <button
                    key={`after-${slot.time}`}
                    className={`time-item ${slot.type === 'closed' ? 'booked' : ''}`}
                    onClick={() => handleSlotClick(slot)}
                    disabled={slot.type !== 'closed'}
                  >
                    {slot.time}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {showModal && (
          <div className="modal-overlay" onClick={() => setShowModal(false)}>
            <div className="confirm-modal" onClick={(event) => event.stopPropagation()}>
              {detailLoading ? (
                <p className="small-hint">Загрузка талона...</p>
              ) : (
                <>
                  <div className="modal-header-row">
                    <h2 className="modal-title">Талон №{detail?.appointment_id ?? detail?.appointmentId}</h2>
                    <span className="modal-time-big">
                      {format(new Date(detail?.datetime), 'HH:mm')}
                    </span>
                  </div>

                  <div className="modal-body">
                    <div className="info-block">
                      <span className="info-label">Врач</span>
                      <p className="info-value">{detail?.doctor?.fio || '-'}</p>
                    </div>
                    <div className="info-block">
                      <span className="info-label">Пациент</span>
                      <p className="info-value">{detail?.patient?.fio || '-'}</p>
                    </div>
                    <div className="info-block">
                      <span className="info-label">Полис ОМС</span>
                      <p className="info-value">{detail?.patient?.policy_number ?? detail?.patient?.policyNumber ?? '-'}</p>
                    </div>
                    <div className="info-block">
                      <span className="info-label">Адрес</span>
                      <p className="info-value">{detail?.address || '-'}</p>
                    </div>
                  </div>

                  <div className="modal-footer">
                    <button className="btn-cancel" onClick={() => setShowModal(false)}>Закрыть</button>
                  </div>
                </>
              )}
            </div>
          </div>
        )}
      </main>

      <footer className="main-footer"></footer>
    </div>
  );
}

