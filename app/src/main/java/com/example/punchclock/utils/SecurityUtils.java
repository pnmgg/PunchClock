package com.example.punchclock.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import android.util.Base64;

public class SecurityUtils {

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.decode(salt, Base64.NO_WRAP));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.encodeToString(hashedPassword, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean verifyPassword(String password, String salt, String hash) {
        String calculatedHash = hashPassword(password, salt);
        return calculatedHash != null && calculatedHash.equals(hash);
    }
}
