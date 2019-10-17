package ru.krivocraft.robinhood;

public class Client {

    private final String userAgent;
    private final String clientSecret;
    private final String clientId;

    public Client() {
        userAgent = "VKAndroidApp/5.23-2978 (Android 4.4.2; SDK 19; x86; unknown Android SDK built for x86; en; 320x240)";
        clientSecret = "hHbZxrka2uZ6jB1inYsH";
        clientId = "2274003";
    }


    public String getUserAgent() {
        return userAgent;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientId() {
        return clientId;
    }
}
