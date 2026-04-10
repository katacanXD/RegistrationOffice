import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import './RegistrationForm.css';

export default function RegistrationForm () {
  const navigate = useNavigate(); // Получаем возможность управлять навигацией

  const [data, setData] = useState({
    lastName: '',
    firstName: '',
    middleName: '',
    password: '',
    confirmPassword: ''
  });

  const [errors, setErrors] = useState({});

  const handleChange = (event) => {
    const { name, value } = event.target;
    setData((prevData) => ({ ...prevData, [name]: value }));
  };

  const validateForm = () => {
    let newErrors = {};

    ['lastName', 'firstName', 'middleName'].forEach(field => {
      if (!data[field].trim()) {
        newErrors[field] = `${field} обязательное поле`;
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

  const handleSubmit = (event) => {
    event.preventDefault(); // Предотвращаем стандартное поведение отправки формы

    const validationResult = validateForm();

    if (Object.keys(validationResult).length > 0) {
      setErrors(validationResult); 
    } else {
      alert('Регистрация прошла успешно!');
      navigate('/'); 
    }
  };

  return (
    <div className='screen'>
      <Link to="/" className="back-link">Назад</Link>

      <div className="registration-container">
        <h1 className='Reg'>REGISTRATION OFFICE</h1>
        
        <form onSubmit={handleSubmit}>
        <label htmlFor="lastName">Фамилия</label>
        <input
          type="text"
          id="lastName"
          name="lastName"
          value={data.lastName}
          onChange={handleChange}
          className={errors.lastName ? 'error-input' : ''}
        />

        <label htmlFor="firstName">Имя</label>
        <input
          type="text"
          id="firstName"
          name="firstName"              
          value={data.firstName}        
          onChange={handleChange}       
          className={errors.firstName ? 'error-input' : ''}
        />

        <label htmlFor="middleName">Отчество</label>
        <input
          type="text"
          id="middleName"
          name="middleName"
          value={data.middleName}
          onChange={handleChange}
          className={errors.middleName ? 'error-input' : ''}
        />

        <label htmlFor="password">Пароль</label>
        <input
          type="password"
          id="password"
          name="password"
          value={data.password}
          onChange={handleChange}
          className={errors.password ? 'error-input' : ''}
        />
        
        <label htmlFor="confirmPassword">Повтор пароля</label>
        <input
          type="password"
          id="confirmPassword"
          name="confirmPassword"
          value={data.confirmPassword}
          onChange={handleChange}
          className={errors.confirmPassword ? 'error-input' : ''}
        />

        <button type="submit">Зарегистрироваться</button>
        </form> 
      </div>
    </div>
  );
};
