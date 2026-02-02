package at.tuwien.crypticcore;

public class XorCipher implements CipherAlgorithm {
    public byte transform (byte data, byte key) {
        return (byte) ((data & 0xFF) ^ (key & 0xFF));
    }
}