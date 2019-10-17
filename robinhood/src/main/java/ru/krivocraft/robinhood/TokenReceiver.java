package ru.krivocraft.robinhood;

import java.util.Random;

public class TokenReceiver {
    private final String login;
    private final String password;
    private final Client client;

    public TokenReceiver(String login, String password) {
        this.login = login;
        this.password = password;
        this.client = new Client();

    }

    private Token getToken() {
        //TODO: implementation
        return null;
    }

    private Token getNonRefreshed() {
        //TODO: implementation
        return null;
    }

    private Token refreshToken() {
        //TODO: implementation
        return null;
    }

    private String randomString(int length, String characters) {
        final int charactersLength = characters.length();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(charactersLength - 1)));
        }
        return stringBuilder.toString();
    }

}
