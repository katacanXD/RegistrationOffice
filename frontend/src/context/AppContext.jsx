import { createContext, useContext, useState, useCallback } from 'react';
import apiClient from '../services/apiClient';

const AppContext = createContext();

export function AppProvider({ children }) {
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [doctorSlots, setDoctorSlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [userPolicy, setUserPolicy] = useState(null);

  // Получить список записей пациента
  const fetchAppointments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get('/patients/me/dashboard');
      setAppointments(response.data.appointments || []);
      return { success: true, appointments: response.data.appointments };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки записей';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Получить список врачей
  const fetchDoctors = useCallback(async (search = '', specialty = '') => {
    setLoading(true);
    setError(null);
    try {
      const params = {};
      if (search) params.search = search;
      if (specialty) params.specialty = specialty;

      const response = await apiClient.get('/doctors', { params });
      setDoctors(response.data.doctors || []);
      return { success: true, doctors: response.data.doctors };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки врачей';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Получить слоты врача на определенную дату
  const fetchDoctorSlots = useCallback(async (doctorId, date) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get(`/doctors/${doctorId}/slots`, {
        params: { date },
      });
      setDoctorSlots(response.data.slots || []);
      return { success: true, slots: response.data.slots };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки слотов';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Получить календарь врача
  const fetchDoctorCalendar = useCallback(async (doctorId, month, year) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get(`/doctors/${doctorId}/calendar`, {
        params: { month, year },
      });
      return { success: true, calendar: response.data.work_days };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки календаря';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Создать запись
  const createAppointment = useCallback(async (doctorId, slotDatetime) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.post('/appointments/me', {
        doctor_id: doctorId,
        slot_datetime: slotDatetime,
      });
      // Обновляем список записей
      await fetchAppointments();
      return { success: true, data: response.data };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка при создании записи';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, [fetchAppointments]);

  // Удалить запись
  const deleteAppointment = useCallback(async (appointmentId) => {
    setLoading(true);
    setError(null);
    try {
      await apiClient.delete(`/appointments/${appointmentId}`);
      // Обновляем список записей
      await fetchAppointments();
      return { success: true };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка при удалении записи';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, [fetchAppointments]);

  // Получить информацию о полисе ОМС пользователя
  const fetchUserPolicy = useCallback(async () => {
    setError(null);
    try {
      const response = await apiClient.get('/patients/me/dashboard');
      const policy = response.data.user?.policy_number || null;
      setUserPolicy(policy);
      return { success: true, policy };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки полиса';
      setError(message);
      return { success: false, error: message, policy: null };
    }
  }, []);

  // Обновить номер полиса ОМС
  const updatePolicy = useCallback(async (policyNumber) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.patch('/patients/me/policy', {
        policy_number: policyNumber,
      });
      setUserPolicy(policyNumber);

      // Также обновляем user в localStorage
      const savedUser = JSON.parse(localStorage.getItem('user') || '{}');
      savedUser.policy_number = policyNumber;
      localStorage.setItem('user', JSON.stringify(savedUser));

      return { success: true, data: response.data };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка при обновлении полиса';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  return (
    <AppContext.Provider
      value={{
        appointments,
        doctors,
        selectedDoctor,
        setSelectedDoctor,
        doctorSlots,
        loading,
        error,
        userPolicy,
        fetchAppointments,
        fetchDoctors,
        fetchDoctorSlots,
        fetchDoctorCalendar,
        createAppointment,
        deleteAppointment,
        updatePolicy,
        fetchUserPolicy,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
}
