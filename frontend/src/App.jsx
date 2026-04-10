import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './styles/colors.css';
import { AuthProvider } from './context/AuthContext';
import { AppProvider } from './context/AppContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import AuthorizationForm from './components/Authorization/AuthorizationForm';
import RegistrationForm from './components/Registration/RegistrationForm';
import MainU from './components/User/UserMain/MainU';
import SearchU from './components/User/UserSearch/SearchU';
import CalendarU from './components/User/UserCalendar/CalendarU';

export default function App() {
  return (
    <AuthProvider>
      <AppProvider>
        <Router>
          <Routes>
            <Route path="/" element={<AuthorizationForm />} />
            <Route path="/RegistrationForm" element={<RegistrationForm />} />
            <Route
              path="/MainU"
              element={
                <ProtectedRoute>
                  <MainU />
                </ProtectedRoute>
              }
            />
            <Route
              path="/SearchU"
              element={
                <ProtectedRoute>
                  <SearchU />
                </ProtectedRoute>
              }
            />
            <Route
              path="/CalendarU"
              element={
                <ProtectedRoute>
                  <CalendarU />
                </ProtectedRoute>
              }
            />
          </Routes>
        </Router>
      </AppProvider>
    </AuthProvider>
  );
}