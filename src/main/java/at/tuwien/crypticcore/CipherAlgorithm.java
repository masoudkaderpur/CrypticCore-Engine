package at.tuwien.crypticcore;

public interface CipherAlgorithm {
    public byte transform (byte data, byte key);
}