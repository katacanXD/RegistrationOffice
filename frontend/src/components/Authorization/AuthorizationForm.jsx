import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import './AuthorizationForm.css'

export default function AuthorizationForm () {
    const navigate = useNavigate(); // Получаем возможность управлять навигацией
    
    const [data, setData] = useState({
        fullName: '',
        password: '',
    });
    
    const [errors, setErrors] = useState({});
    
    const handleChange = (event) => {
        const { name, value } = event.target;
        setData((prevData) => ({ ...prevData, [name]: value }));
    };
    
    const validateForm = () => {
        let newErrors = {};
    
        if (!data.fullName.trim()) {
        newErrors['fullName'] = 'ФИО обязателен';
        }

        if (!data.password.trim()) {
        newErrors['password'] = 'Пароль обязателен';
        }
 
        return newErrors;
    };
    
    const handleSubmit = (event) => {
        event.preventDefault(); // Предотвращаем стандартное поведение отправки формы
    
        const validationResult = validateForm();
    
        if (Object.keys(validationResult).length > 0) {
        setErrors(validationResult); 
        } else {
        alert('Добро пожаловать!');
        navigate('/MainU', { state: { userName: data.fullName } });
        }
    };

    return (
    <div className='screen'>
        <div className="authorization-container">
            <h1 className='Reg'>REGISTRATION OFFICE</h1>

            <form onSubmit={handleSubmit}>
            <label htmlFor="fullName">ФИО</label>
            <input
                type="text"
                id="fullName"
                name="fullName"              
                value={data.fullName}        
                onChange={handleChange}       
                className={errors.fullName ? 'error-input' : ''}
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

            <button type="submit">Войти</button>
            </form>

            <Link to="./RegistrationForm" className="no-account-link">Нет аккаунта</Link>

        </div>
    </div>
    );
};
   