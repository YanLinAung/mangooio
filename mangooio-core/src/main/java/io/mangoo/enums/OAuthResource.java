package io.mangoo.enums;

/**
 *
 * @author skubiak
 *
 */
public enum OAuthResource {
    TWITTER("https://api.twitter.com/1.1/account/verify_credentials.json"),
    GOOGLE("https://www.googleapis.com/oauth2/v2/userinfo?alt=json"),
    FACEBOOK("https://graph.facebook.com/me");

    private final String value;

    OAuthResource (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
