package com.brokerplatform.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String[] passwords = args.length > 0 ? args : new String[]{"Admin@1234", "Broker@1234"};
        for (String password : passwords) {
            System.out.println(password + " -> " + encoder.encode(password));
        }
    }
}
