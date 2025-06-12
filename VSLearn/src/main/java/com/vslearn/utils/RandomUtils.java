package com.vslearn.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomUtils {

    public String getRandomActiveCodeString(Long length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return getStringRandom(length, characters);
    }

    private String getStringRandom(Long length, String characters) {
        StringBuilder randomString = new StringBuilder();
        Random random = new Random();
        for (int i = 0;
             i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }

    public String getRandomActiveCodeNumber(Long length) {
        String characters = "0123456789";
        return getStringRandom(length, characters);
    }
}
