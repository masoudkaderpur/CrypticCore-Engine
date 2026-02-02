package at.tuwien.crypticcore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XorCipherTest {
    @Test
    public void testXor () {
        CipherAlgorithm algorithm = new XorCipher ();
        byte original = 122;
        byte key = 3;
        byte encrypted = algorithm.transform (original, key);
        byte decrypted = algorithm.transform (encrypted, key);
        assertEquals (original, decrypted, "decrypted byte should be equal to original byte");
    }
}