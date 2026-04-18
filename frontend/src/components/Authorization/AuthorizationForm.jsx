import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import './AuthorizationForm.css'

const roleEntryPath = {
    patient: '/patient/main',
    doctor: '/doctor/calendar',
    registrar: '/registrar/patients',
    admin: '/admin',
};

export default function AuthorizationForm () {
    const navigate = useNavigate();
    const { login, loading } = useAuth();

    const [data, setData] = useState({
        login: '',
        password: '',
    });

    const [errors, setErrors] = useState({});
    const [generalError, setGeneralError] = useState('');

    const handleChange = (event) => {
        const { name, value } = event.target;
        setData((prevData) => ({ ...prevData, [name]: value }));
        // Очищаем ошибки при вводе
        if (errors[name]) {
            setErrors((prev) => ({ ...prev, [name]: '' }));
        }
    };

    const validateForm = () => {
        let newErrors = {};

        if (!data.login.trim()) {
            newErrors['login'] = 'Логин обязателен';
        }

        if (!data.password.trim()) {
            newErrors['password'] = 'Пароль обязателен';
        }

        return newErrors;
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setGeneralError('');
        setErrors({});

        const validationResult = validateForm();

        if (Object.keys(validationResult).length > 0) {
            setErrors(validationResult);
        } else {
            const result = await login(data.login, data.password);
            if (result.success) {
                const role = (result.user?.role || '').toLowerCase();
                navigate(roleEntryPath[role] || '/patient/main');
            } else {
                setGeneralError(result.error || 'Ошибка входа');
            }
        }
    };

    return (
    <div className='auth-screen'>
        <div className="authorization-container">
            <h1 className='auth-title'>REGISTRATION OFFICE</h1>

            {generalError && <div className="general-error">{generalError}</div>}

            <form onSubmit={handleSubmit}>
            <label htmlFor="login">Логин</label>
            <input
                type="text"
                id="login"
                name="login"
                value={data.login}
                onChange={handleChange}
                className={errors.login ? 'auth-error-input' : ''}
                disabled={loading}
            />
            {errors.login && <p className="field-error">{errors.login}</p>}

            <label htmlFor="password">Пароль</label>
            <input
                type="password"
                id="password"
                name="password"
                value={data.password}
                onChange={handleChange}
                className={errors.password ? 'auth-error-input' : ''}
                disabled={loading}
            />
            {errors.password && <p className="field-error">{errors.password}</p>}

            <button type="submit" disabled={loading}>
                {loading ? 'Загрузка...' : 'Войти'}
            </button>
            </form>

            <Link to="/RegistrationForm" className="no-account-link">Нет аккаунта</Link>

        </div>
    </div>
    );
};
