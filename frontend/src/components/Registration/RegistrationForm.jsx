import { useNavigate, Link } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import './RegistrationForm.css';

export default function RegistrationForm () {
  const navigate = useNavigate();
  const { register, loading, error: authError } = useAuth();

  const [data, setData] = useState({
    login: '',
    lastName: '',
    firstName: '',
    middleName: '',
    password: '',
    confirmPassword: ''
  });

  const [errors, setErrors] = useState({});
  const [successMessage, setSuccessMessage] = useState('');

  const handleChange = (event) => {
    const { name, value } = event.target;
    setData((prevData) => ({ ...prevData, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    let newErrors = {};

    if (!data.login.trim()) {
      newErrors['login'] = 'Логин обязателен';
    }

    ['lastName', 'firstName', 'middleName'].forEach(field => {
      if (!data[field].trim()) {
        newErrors[field] = `${field === 'lastName' ? 'Фамилия' : field === 'firstName' ? 'Имя' : 'Отчество'} обязательное поле`;
      }
    });

    if (!data.password.trim()) {
      newErrors['password'] = 'Пароль обязателен';
    }

    if (data.confirmPassword !== data.password) {
      newErrors['confirmPassword'] = 'Пароли не совпадают';
    }

    return newErrors;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const validationResult = validateForm();

    if (Object.keys(validationResult).length > 0) {
      setErrors(validationResult);
    } else {
      const fio = `${data.lastName} ${data.firstName} ${data.middleName}`;
      const result = await register(data.login, fio, data.password);

      if (result.success) {
        setSuccessMessage('Регистрация прошла успешно! Переходим на вход...');
        setTimeout(() => {
          navigate('/');
        }, 2000);
      } else {
        setErrors({ general: result.error });
      }
    }
  };

  return (
    <div className='screen'>
      <Link to="/" className="back-link">Назад</Link>
      <div className="registration-container">
        <h1 className='Reg'>REGISTRATION OFFICE</h1>

        {successMessage && <div style={{ color: 'green', marginBottom: '10px' }}>{successMessage}</div>}
        {authError && <div style={{ color: 'red', marginBottom: '10px' }}>{authError}</div>}
        {errors.general && <div style={{ color: 'red', marginBottom: '10px' }}>{errors.general}</div>}

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

        <label htmlFor="lastName">Фамилия</label>
        <input
          type="text"
          id="lastName"
          name="lastName"
          value={data.lastName}
          onChange={handleChange}
          className={errors.lastName ? 'error-input' : ''}
          disabled={loading}
        />

        <label htmlFor="firstName">Имя</label>
        <input
          type="text"
          id="firstName"
          name="firstName"
          value={data.firstName}
          onChange={handleChange}
          className={errors.firstName ? 'error-input' : ''}
          disabled={loading}
        />

        <label htmlFor="middleName">Отчество</label>
        <input
          type="text"
          id="middleName"
          name="middleName"
          value={data.middleName}
          onChange={handleChange}
          className={errors.middleName ? 'error-input' : ''}
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

        <label htmlFor="confirmPassword">Повтор пароля</label>
        <input
          type="password"
          id="confirmPassword"
          name="confirmPassword"
          value={data.confirmPassword}
          onChange={handleChange}
          className={errors.confirmPassword ? 'error-input' : ''}
          disabled={loading}
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Загрузка...' : 'Зарегистрироваться'}
        </button>
        </form>
      </div>
    </div>
  );
};
