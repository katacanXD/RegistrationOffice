package ru.katacan.registrationoffice.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.katacan.registrationoffice.entity.*;
import ru.katacan.registrationoffice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Component
@Profile("!test")
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
    public void run(String... args) {
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
            createUser("patient_ivanov", "Иванов Иван Иванович", "pass123", null, patientRole);
            createUser("patient_petrov", "Петров Петр Петрович", "pass123", "1234567890123456", patientRole);
            createUser("patient_sidorova", "Сидорова Анна Сергеевна", "pass123", null, patientRole);
            createUser("patient_kozlov", "Козлов Дмитрий Николаевич", "pass123", "9876543210987654", patientRole);

            // Врачи
            createUser("doctor_keminova", "Кеминова Екатерина Юрьевна", "doc123", null, doctorRole)
                    .setSpeciality("Терапевт");
            createUser("doctor_ivanov", "Иванов Иван Иванович", "doc123", null, doctorRole)
                    .setSpeciality("Отоларинголог");
            createUser("doctor_ahmetov", "Ахметов Демеург Евгеньевич", "doc123", null, doctorRole)
                    .setSpeciality("Терапевт");
            createUser("doctor_smirnova", "Смирнова Ольга Викторовна", "doc123", null, doctorRole)
                    .setSpeciality("Офтальмолог");

            // Регистратор
            createUser("registrar_main", "Регистратор Регистратор Регистраторович", "reg123", null, registrarRole);

            // Админ
            createUser("admin_main", "Администратор Админ Админович", "admin123", null, adminRole);

            userRepository.flush();
        }
    }

    private User createUser(String login, String fullName, String password, String policyNumber, DictRole role) {
        User user = new User();
        user.setLogin(LoginNormalizer.normalize(login));
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
                    morningSlot.setSlotMinutes(60);
                    morningSlot.setBreakStart(LocalTime.of(13, 0));
                    morningSlot.setBreakEnd(LocalTime.of(14, 0));
                    workSlotRepository.save(morningSlot);

                    WorkSlot eveningSlot = new WorkSlot();
                    eveningSlot.setDoctor(doctor);
                    eveningSlot.setDate(date);
                    eveningSlot.setStartTime(LocalTime.of(14, 0));
                    eveningSlot.setEndTime(LocalTime.of(18, 0));
                    eveningSlot.setSlotMinutes(60);
                    eveningSlot.setBreakStart(LocalTime.of(13, 0));
                    eveningSlot.setBreakEnd(LocalTime.of(14, 0));
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
                LocalTime[] demoTimes = {
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0),
                        LocalTime.of(14, 0),
                        LocalTime.of(15, 0)
                };

                // Создаем несколько тестовых записей
                for (int i = 0; i < 5; i++) {
                    User doctor = doctors.get(i % doctors.size());
                    User patient = patients.get(i % patients.size());

                    LocalDateTime slotTime = LocalDateTime.of(tomorrow, demoTimes[i]);

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