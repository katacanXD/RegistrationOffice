package ru.katacan.registrationoffice.util;

import java.util.Locale;

public final class LoginNormalizer {

    private LoginNormalizer() {
    }

    public static String normalize(String login) {
        return login == null ? null : login.trim().toLowerCase(Locale.ROOT);
    }
}

