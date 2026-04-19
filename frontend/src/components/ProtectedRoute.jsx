import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function getRoleHomePath(role) {
  const roleName = (role || '').toLowerCase();
  if (roleName === 'doctor') return '/doctor/calendar';
  if (roleName === 'registrar') return '/registrar/patients';
  if (roleName === 'admin') return '/admin';
  return '/patient/main';
}

export function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, loading, user } = useAuth();

  if (loading) {
    return <div>Загрузка...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  if (allowedRoles?.length > 0) {
    const role = (user?.role || '').toLowerCase();
    const allowed = allowedRoles.map((item) => item.toLowerCase());
    if (!allowed.includes(role)) {
      return <Navigate to={getRoleHomePath(role)} replace />;
    }
  }

  return children;
}
