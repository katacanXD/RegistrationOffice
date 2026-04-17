package ru.katacan.registrationoffice.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.katacan.registrationoffice.entity.*;
import ru.katacan.registrationoffice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final int TOTAL_TEST_USERS = 100;
    private static final int TEST_DOCTORS_COUNT = 10;
    private static final int TEST_REGISTRARS_COUNT = 1;
    private static final int TEST_ADMINS_COUNT = 1;

    private final DictStatusRepository statusRepository;
    private final DictRoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WorkSlotRepository workSlotRepository;
    private final AppointmentRepository appointmentRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String @NonNull... args) {
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

            String[] specialities = {
                    "Терапевт",
                    "Отоларинголог",
                    "Офтальмолог",
                    "Кардиолог",
                    "Невролог"
            };

            for (int i = 1; i <= TEST_DOCTORS_COUNT; i++) {
                createUser(
                        String.format("doctor_%03d", i),
                        String.format("Врач Тестовый %03d", i),
                        "doc123",
                        null,
                        doctorRole
                ).setSpeciality(specialities[(i - 1) % specialities.length]);
            }

            for (int i = 1; i <= TEST_REGISTRARS_COUNT; i++) {
                createUser(
                        String.format("registrar_%03d", i),
                        String.format("Регистратор Тестовый %03d", i),
                        "reg123",
                        null,
                        registrarRole
                );
            }

            for (int i = 1; i <= TEST_ADMINS_COUNT; i++) {
                createUser(
                        String.format("admin_%03d", i),
                        String.format("Администратор Тестовый %03d", i),
                        "admin123",
                        null,
                        adminRole
                );
            }

            int patientsCount = TOTAL_TEST_USERS - TEST_DOCTORS_COUNT - TEST_REGISTRARS_COUNT - TEST_ADMINS_COUNT;
            for (int i = 1; i <= patientsCount; i++) {
                createUser(
                        String.format("patient_%03d", i),
                        String.format("Пациент Тестовый %03d", i),
                        "pass123",
                        String.format("%016d", 1_000_000_000_000_000L + i),
                        patientRole
                );
            }

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
            List<User> doctors = findAllDoctors();
            LocalDate today = LocalDate.now();

            for (User doctor : doctors) {
                // Создаем слоты на сегодня и ближайшие дни
                for (int i = 0; i < 30; i++) {
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

            List<User> doctors = findAllDoctors();
            List<User> patients = findAllPatients();
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

    private List<User> findAllDoctors() {
        return fetchAllUsersByPage(page -> userRepository.searchDoctors("", page));
    }

    private List<User> findAllPatients() {
        return fetchAllUsersByPage(page -> userRepository.searchPatients("", page));
    }

    private List<User> fetchAllUsersByPage(java.util.function.Function<PageRequest, Page<User>> pageFetcher) {
        List<User> result = new java.util.ArrayList<>();
        int pageNumber = 0;
        int pageSize = 50;

        while (true) {
            Page<User> page = pageFetcher.apply(PageRequest.of(pageNumber, pageSize));
            result.addAll(page.getContent());

            if (!page.hasNext()) {
                break;
            }

            pageNumber++;
        }

        return result;
    }
}

