import { createContext, useContext, useState, useCallback } from 'react';
import apiClient from '../services/apiClient';

const AppContext = createContext();

export function AppProvider({ children }) {
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [patients, setPatients] = useState([]);
  const [users, setUsers] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [registrarPatientDashboard, setRegistrarPatientDashboard] = useState(null);
  const [doctorSlots, setDoctorSlots] = useState([]);
  const [doctorSchedule, setDoctorSchedule] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [userPolicy, setUserPolicy] = useState(null);
  const [rescheduleAppointmentId, setRescheduleAppointmentId] = useState(null);

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

  // Получить расписание врача на конкретный день (включая занятые слоты)
  const fetchDoctorSchedule = useCallback(async (doctorId, date) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get(`/doctors/${doctorId}/schedule`, {
        params: { date },
      });
      setDoctorSchedule(response.data || null);
      return { success: true, schedule: response.data };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки расписания врача';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Получить детали записи
  const fetchAppointmentDetail = useCallback(async (appointmentId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get(`/appointments/detail/${appointmentId}`);
      return { success: true, detail: response.data };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки деталей записи';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Получить dashboard выбранного пациента для регистратора
  const fetchRegistrarPatientDashboard = useCallback(async (patientId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get(`/patients/${patientId}/dashboard`);
      setRegistrarPatientDashboard(response.data || null);
      return { success: true, dashboard: response.data };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки карточки пациента';
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

  // Создать запись за пациента (регистратор/админ)
  const createRegistrarAppointment = useCallback(async (doctorId, patientId, slotDatetime) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.post('/appointments', {
        doctor_id: doctorId,
        patient_id: patientId,
        slot_datetime: slotDatetime,
      });
      return { success: true, data: response.data };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка при создании записи';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

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

  // Удалить запись без перезагрузки patient/me dashboard (для registrar/admin потоков)
  const cancelAppointmentDirect = useCallback(async (appointmentId) => {
    setLoading(true);
    setError(null);
    try {
      await apiClient.delete(`/appointments/${appointmentId}`);
      return { success: true };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка при удалении записи';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

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

  // Поиск пациентов для регистратора/админа
  const fetchPatients = useCallback(async (search = '', page = 0, size = 8) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get('/patients', {
        params: { search, page, size },
      });
      const result = response.data || {};
      const items = result.patients || [];
      setPatients(items);
      return {
        success: true,
        patients: items,
        totalPages: result.total_pages ?? result.totalPages ?? 0,
        currentPage: result.current_page ?? result.currentPage ?? 0,
        totalFound: result.total_found ?? result.totalFound ?? 0,
      };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки пациентов';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Список пользователей для админ-панели
  const fetchUsers = useCallback(async (search = '', page = 0, size = 10) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get('/users', {
        params: { search, page, size },
      });
      const result = response.data || {};
      const items = result.users || [];
      setUsers(items);
      return {
        success: true,
        users: items,
        totalPages: result.total_pages ?? result.totalPages ?? 0,
        currentPage: result.current_page ?? result.currentPage ?? 0,
        totalFound: result.total_found ?? result.totalFound ?? 0,
      };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка загрузки пользователей';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Обновление роли и специальности пользователя (admin)
  const updateUserRoleAndSpeciality = useCallback(async (userId, payload) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.patch(`/users/${userId}`, payload);
      return { success: true, data: response.data };
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка обновления пользователя';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Импорт CSV для admin
  const importCsv = useCallback(async (entity, file) => {
    setLoading(true);
    setError(null);
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await apiClient.post(`/admins/import/${entity}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return { success: true, data: response.data };
    } catch (err) {
      const message = err.response?.data?.message || err.response?.data?.error || 'Ошибка импорта CSV';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
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
       savedUser.policyNumber = policyNumber;
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

   // Обновить время записи
   const updateRegistrarAppointment = useCallback(async (appointmentId, slotDatetime) => {
     setLoading(true);
     setError(null);
     try {
       const response = await apiClient.patch(`/appointments/${appointmentId}`, {
         slot_datetime: slotDatetime,
       });
       return { success: true, data: response.data };
     } catch (err) {
       const message = err.response?.data?.message || 'Ошибка при обновлении записи';
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
        patients,
        users,
        selectedDoctor,
        setSelectedDoctor,
        selectedPatient,
        setSelectedPatient,
        registrarPatientDashboard,
        doctorSlots,
        doctorSchedule,
        loading,
        error,
        userPolicy,
        rescheduleAppointmentId,
        setRescheduleAppointmentId,
        fetchAppointments,
        fetchDoctors,
        fetchDoctorSlots,
        fetchDoctorCalendar,
        fetchDoctorSchedule,
        fetchAppointmentDetail,
        fetchRegistrarPatientDashboard,
        createAppointment,
        createRegistrarAppointment,
        deleteAppointment,
        cancelAppointmentDirect,
        updatePolicy,
        updateRegistrarAppointment,
        fetchUserPolicy,
        fetchPatients,
        fetchUsers,
        updateUserRoleAndSpeciality,
        importCsv,
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
