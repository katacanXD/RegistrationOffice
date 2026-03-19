package ru.katacan.registrationoffice.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.katacan.registrationoffice.entity.*;
import ru.katacan.registrationoffice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DictStatusRepository statusRepository;
    private final DictRoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WorkSlotRepository workSlotRepository;
    private final AppointmentRepository appointmentRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Инициализация словарей, если их нет
        initDicts();

        // Создание тестовых пользователей, если их нет
        initTestUsers();

        // Создание тестовых рабочих слотов postgres
        initTestWorkSlots();

        // Создание тестовых записей
        initTestAppointments();
    }

    private void initDicts() {
        // Статусы записей
        if (statusRepository.count() == 0) {
            createStatus("booked");
            createStatus("canceled");
            createStatus("completed");
        }

        // Роли пользователей
        if (roleRepository.count() == 0) {
            createRole("patient");
            createRole("doctor");
            createRole("registrar");
            createRole("admin");
        }
    }

    private void createStatus(String name) {
        DictStatus status = new DictStatus();
        status.setName(name);
        statusRepository.save(status);
    }

    private void createRole(String name) {
        DictRole role = new DictRole();
        role.setName(name);
        roleRepository.save(role);
    }

    private void initTestUsers() {
        if (userRepository.count() == 0) {
            DictRole patientRole = roleRepository.findByName("patient").orElseThrow();
            DictRole doctorRole = roleRepository.findByName("doctor").orElseThrow();
            DictRole registrarRole = roleRepository.findByName("registrar").orElseThrow();
            DictRole adminRole = roleRepository.findByName("admin").orElseThrow();

            // Пациенты
            createUser("Иванов Иван Иванович", "pass123", null, patientRole);
            createUser("Петров Петр Петрович", "pass123", "1234567890123456", patientRole);
            createUser("Сидорова Анна Сергеевна", "pass123", null, patientRole);
            createUser("Козлов Дмитрий Николаевич", "pass123", "9876543210987654", patientRole);

            // Врачи
            createUser("Кеминова Екатерина Юрьевна", "doc123", null, doctorRole)
                    .setSpeciality("Терапевт");
            createUser("Иванов Иван Иванович", "doc123", null, doctorRole)
                    .setSpeciality("Отоларинголог");
            createUser("Ахметов Демеург Евгеньевич", "doc123", null, doctorRole)
                    .setSpeciality("Терапевт");
            createUser("Смирнова Ольга Викторовна", "doc123", null, doctorRole)
                    .setSpeciality("Офтальмолог");

            // Регистратор
            createUser("Регистратор Регистратор Регистраторович", "reg123", null, registrarRole);

            // Админ
            createUser("Администратор Админ Админович", "admin123", null, adminRole);

            userRepository.flush();
        }
    }

    private User createUser(String fullName, String password, String policyNumber, DictRole role) {
        User user = new User();
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(password));
        user.setPolicyNumber(policyNumber);
        user.setRole(role);
        return userRepository.save(user);
    }

    private void initTestWorkSlots() {
        if (workSlotRepository.count() == 0) {
            // Заменяем вызов на фильтрацию по роли вручную
            List<User> doctors = userRepository.findAll().stream()
                    .filter(u -> "doctor".equals(u.getRole().getName()))
                    .toList();
            LocalDate today = LocalDate.now();

            for (User doctor : doctors) {
                // Создаем слоты на сегодня и ближайшие дни
                for (int i = 0; i < 7; i++) {
                    LocalDate date = today.plusDays(i);

                    WorkSlot morningSlot = new WorkSlot();
                    morningSlot.setDoctor(doctor);
                    morningSlot.setDate(date);
                    morningSlot.setStartTime(LocalTime.of(9, 0));
                    morningSlot.setEndTime(LocalTime.of(13, 0));
                    morningSlot.setSlotMinutes(15);
                    morningSlot.setBreakStart(LocalTime.of(11, 0));
                    morningSlot.setBreakEnd(LocalTime.of(11, 15));
                    workSlotRepository.save(morningSlot);

                    WorkSlot eveningSlot = new WorkSlot();
                    eveningSlot.setDoctor(doctor);
                    eveningSlot.setDate(date);
                    eveningSlot.setStartTime(LocalTime.of(14, 0));
                    eveningSlot.setEndTime(LocalTime.of(18, 0));
                    eveningSlot.setSlotMinutes(15);
                    eveningSlot.setBreakStart(LocalTime.of(16, 0));
                    eveningSlot.setBreakEnd(LocalTime.of(16, 15));
                    workSlotRepository.save(eveningSlot);
                }
            }
            workSlotRepository.flush();
        }
    }

    private void initTestAppointments() {
        if (appointmentRepository.count() == 0) {
            DictStatus bookedStatus = statusRepository.findByName("booked").orElseThrow();

// 1. Получаем ВООБЩЕ ВСЕХ пользователей из базы одним списком
            List<User> allUsers = userRepository.findAll();

            // 2. Фильтруем этот список в памяти, чтобы достать врачей
            List<User> doctors = allUsers.stream()
                    .filter(u -> u.getRole() != null && "doctor".equals(u.getRole().getName()))
                    .toList();

            // 3. Фильтруем этот же список, чтобы достать пациентов
            List<User> patients = allUsers.stream()
                    .filter(u -> u.getRole() != null && "patient".equals(u.getRole().getName()))
                    .toList();
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            if (!doctors.isEmpty() && !patients.isEmpty()) {
                // Создаем несколько тестовых записей
                for (int i = 0; i < 5; i++) {
                    User doctor = doctors.get(i % doctors.size());
                    User patient = patients.get(i % patients.size());

                    LocalDateTime slotTime = LocalDateTime.of(tomorrow, LocalTime.of(9, 15).plusMinutes(i * 15));

                    Appointment appointment = new Appointment();
                    appointment.setDoctor(doctor);
                    appointment.setPatient(patient);
                    appointment.setSlotDateTime(slotTime);
                    appointment.setStatus(bookedStatus);

                    appointmentRepository.save(appointment);
                }
            }
            appointmentRepository.flush();
        }
    }
}