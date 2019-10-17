package ru.krivocraft.robinhood;

public class Token {

    private final String token;
    private final String secret;
    private final String deviceId;

    public Token(String token, String secret, String deviceId) {
        this.token = token;
        this.secret = secret;
        this.deviceId = deviceId;
    }

    public String getToken() {
        return token;
    }

    public String getSecret() {
        return secret;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
