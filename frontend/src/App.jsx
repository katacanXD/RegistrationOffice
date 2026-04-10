// import { useState } from 'react'
// import './App.css'
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import AuthorizationForm from './components/Authorization/AuthorizationForm';
import RegistrationForm from './components/Registration/RegistrationForm';
import MainU from './components/User/UserMain/MainU';
import SearchU from './components/User/UserSearch/SearchU';
import CalendarU from './components/User/UserCalendar/CalendarU';

export default function App() {
  return (
   <Router>
      <Routes>
        <Route path="/" element={<AuthorizationForm />} />
        <Route path="/RegistrationForm" element={<RegistrationForm />} />
        <Route path="/MainU" element={<MainU />} />
        <Route path="/SearchU" element={<SearchU />} />
        <Route path="/CalendarU" element={<CalendarU />} />
      </Routes>
    </Router>
  )
}