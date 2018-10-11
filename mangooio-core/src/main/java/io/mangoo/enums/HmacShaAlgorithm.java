package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum HmacShaAlgorithm {
    HMAC_SHA_1("HmacSHA1"),
    HMAC_SHA_256("HmacSHA256"),
    HMAC_SHA_512("HmacSHA512");

    public static HmacShaAlgorithm from(String algorithm) {
        for (HmacShaAlgorithm alg : values()) {
            if (alg.algorithm.equals(algorithm)) {
                return alg;
            }
        }
        throw new IllegalArgumentException("No matching HmacShaAlgorithm constant for [" + algorithm + "]");
    }

    private final String algorithm;

    HmacShaAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String toString() {
        return getAlgorithm();
    }
}