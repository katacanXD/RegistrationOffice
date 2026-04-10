import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import './AuthorizationForm.css'

export default function AuthorizationForm () {
    const navigate = useNavigate();
    const { login, loading, error: authError } = useAuth();

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

        const validationResult = validateForm();

        if (Object.keys(validationResult).length > 0) {
            setErrors(validationResult);
        } else {
            const result = await login(data.login, data.password);
            if (result.success) {
                navigate('/MainU');
            } else {
                setGeneralError(result.error || 'Ошибка входа');
            }
        }
    };

    return (
    <div className='screen'>
        <div className="authorization-container">
            <h1 className='Reg'>REGISTRATION OFFICE</h1>

            {generalError && <div style={{ color: 'red', marginBottom: '10px' }}>{generalError}</div>}
            {authError && <div style={{ color: 'red', marginBottom: '10px' }}>{authError}</div>}

            <form onSubmit={handleSubmit}>
            <label htmlFor="login">Логин</label>
            <input
                type="text"
                id="login"
                name="login"
                value={data.login}
                onChange={handleChange}
                className={errors.login ? 'error-input' : ''}
                disabled={loading}
            />

            <label htmlFor="password">Пароль</label>
            <input
                type="password"
                id="password"
                name="password"
                value={data.password}
                onChange={handleChange}
                className={errors.password ? 'error-input' : ''}
                disabled={loading}
            />

            <button type="submit" disabled={loading}>
                {loading ? 'Загрузка...' : 'Войти'}
            </button>
            </form>

            <Link to="/RegistrationForm" className="no-account-link">Нет аккаунта</Link>

        </div>
    </div>
    );
};
   