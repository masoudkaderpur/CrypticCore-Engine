package at.tuwien.crypticcore;

public enum CrypticMode {
    ENCRYPTION, DECRYPTION;

    public static CrypticMode fromString(String text) {
        for (CrypticMode mode : CrypticMode.values()) {
            if (mode.name().equalsIgnoreCase(text) ||
                    (mode == ENCRYPTION && text.equalsIgnoreCase("encrypt")) ||
                    (mode == DECRYPTION && text.equalsIgnoreCase("decrypt"))) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown mode: " + text);
    }
}
