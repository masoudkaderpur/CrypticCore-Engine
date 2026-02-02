package at.tuwien.crypticcore;

public interface CipherAlgorithm {
    byte transform (byte data, byte key);
}