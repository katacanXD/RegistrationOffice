package ru.katacan.registrationoffice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.katacan.registrationoffice.entity.Appointment;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.repository.AppointmentRepository;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import java.util.Optional;

@Component("appointmentAccess")
@RequiredArgsConstructor
public class AppointmentAccess {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public boolean isPatientOwnerOfAppointment(Long appointmentId, Authentication authentication) {
        if (appointmentId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String normalizedLogin = LoginNormalizer.normalize(authentication.getName());
        Optional<User> currentUser = userRepository.findByLoginIgnoreCase(normalizedLogin);
        if (currentUser.isEmpty()) {
            return false;
        }

        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        if (appointment.isEmpty() || appointment.get().getPatient() == null) {
            return false;
        }

        return appointment.get().getPatient().getId().equals(currentUser.get().getId());
    }
}