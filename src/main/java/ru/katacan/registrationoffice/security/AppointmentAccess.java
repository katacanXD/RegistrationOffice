package ru.katacan.registrationoffice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.repository.UserRepository;

import java.util.Optional;

@Component("appointmentAccess")
@RequiredArgsConstructor
public class AppointmentAccess {

    private final UserRepository userRepository;

    public boolean isSelf(Long patientId, Authentication authentication) {
        if (patientId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Optional<User> currentUser = userRepository.findByFullName(authentication.getName());
        if (currentUser.isEmpty()) {
            return false;
        }

        return patientId.equals(currentUser.get().getId());
    }
}