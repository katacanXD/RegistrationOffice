package ru.katacan.registrationoffice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.katacan.registrationoffice.entity.User;
import ru.katacan.registrationoffice.repository.UserRepository;
import ru.katacan.registrationoffice.util.LoginNormalizer;

import java.util.Optional;

@Component("doctorAccess")
@RequiredArgsConstructor
public class DoctorAccess {

    private final UserRepository userRepository;

    public boolean isCurrentDoctor(Long doctorId, Authentication authentication) {
        if (doctorId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String normalizedLogin = LoginNormalizer.normalize(authentication.getName());
        Optional<User> currentUser = userRepository.findByLoginIgnoreCase(normalizedLogin);
        if (currentUser.isEmpty()) {
            return false;
        }

        User user = currentUser.get();
        return user.getRole() != null
                && "doctor".equals(user.getRole().getName())
                && doctorId.equals(user.getId());
    }
}

