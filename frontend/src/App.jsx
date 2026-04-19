import { BrowserRouter as Router, Navigate, Route, Routes } from 'react-router-dom';
import './styles/colors.css';
import { AuthProvider } from './context/AuthContext';
import { AppProvider } from './context/AppContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import AuthorizationForm from './components/Authorization/AuthorizationForm';
import RegistrationForm from './components/Registration/RegistrationForm';
import MainU from './components/User/UserMain/MainU';
import SearchU from './components/User/UserSearch/SearchU';
import CalendarU from './components/User/UserCalendar/CalendarU';
import DoctorCalendar from './components/Doctor/DoctorCalendar/DoctorCalendar';
import RegistrarPatientSearch from './components/Registrar/RegistrarPatientSearch/RegistrarPatientSearch';
import RegistrarPatientOverview from './components/Registrar/RegistrarPatientOverview/RegistrarPatientOverview';
import RegistrarDoctorSearch from './components/Registrar/RegistrarDoctorSearch/RegistrarDoctorSearch';
import RegistrarCalendar from './components/Registrar/RegistrarCalendar/RegistrarCalendar';
import AdminPanel from './components/Admin/AdminPanel/AdminPanel';

export default function App() {
  return (
    <AuthProvider>
      <AppProvider>
        <Router>
          <Routes>
            <Route path="/" element={<AuthorizationForm />} />
            <Route path="/RegistrationForm" element={<RegistrationForm />} />

            <Route
              path="/patient/main"
              element={
                <ProtectedRoute allowedRoles={['patient']}>
                  <MainU />
                </ProtectedRoute>
              }
            />
            <Route
              path="/MainU"
              element={
                <ProtectedRoute allowedRoles={['patient']}>
                  <Navigate to="/patient/main" replace />
                </ProtectedRoute>
              }
            />

            <Route
              path="/patient/search"
              element={
                <ProtectedRoute allowedRoles={['patient']}>
                  <SearchU />
                </ProtectedRoute>
              }
            />
            <Route
              path="/SearchU"
              element={
                <ProtectedRoute allowedRoles={['patient']}>
                  <Navigate to="/patient/search" replace />
                </ProtectedRoute>
              }
            />

            <Route
              path="/patient/calendar"
              element={
                <ProtectedRoute allowedRoles={['patient']}>
                  <CalendarU />
                </ProtectedRoute>
              }
            />
            <Route
              path="/CalendarU"
              element={
                <ProtectedRoute allowedRoles={['patient']}>
                  <Navigate to="/patient/calendar" replace />
                </ProtectedRoute>
              }
            />

            <Route
              path="/doctor/calendar"
              element={
                <ProtectedRoute allowedRoles={['doctor']}>
                  <DoctorCalendar />
                </ProtectedRoute>
              }
            />

            <Route
              path="/registrar/patients"
              element={
                <ProtectedRoute allowedRoles={['registrar']}>
                  <RegistrarPatientSearch />
                </ProtectedRoute>
              }
            />
            <Route
              path="/registrar/patient"
              element={
                <ProtectedRoute allowedRoles={['registrar']}>
                  <RegistrarPatientOverview />
                </ProtectedRoute>
              }
            />
            <Route
              path="/registrar/doctors"
              element={
                <ProtectedRoute allowedRoles={['registrar']}>
                  <RegistrarDoctorSearch />
                </ProtectedRoute>
              }
            />
            <Route
              path="/registrar/calendar"
              element={
                <ProtectedRoute allowedRoles={['registrar']}>
                  <RegistrarCalendar />
                </ProtectedRoute>
              }
            />

            <Route
              path="/admin"
              element={
                <ProtectedRoute allowedRoles={['admin']}>
                  <AdminPanel />
                </ProtectedRoute>
              }
            />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </AppProvider>
    </AuthProvider>
  );
}