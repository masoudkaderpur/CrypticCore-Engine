package at.tuwien.crypticcore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main (String[] args) {
        String originalFile = "test.txt";
        String encryptedFile = "test.enc";
        String decryptedFile = "test_back.txt";

        try {
            Files.writeString (Paths.get (originalFile), "This is a secret message!");

            CipherAlgorithm xor = new XorCipher ();
            EncryptionEngine engine = new EncryptionEngine (xor);

            byte[] key = "TUWien2026".getBytes (StandardCharsets.UTF_8);

            System.out.println ("encrypting...");
            engine.processFile (originalFile, encryptedFile, key);

            System.out.println ("decrypting...");
            engine.processFile (encryptedFile, decryptedFile, key);

            System.out.println ("comparing...");

        } catch (IOException e) {
            System.err.println ("Fehler beim Verarbeiten der Dateien: " + e.getMessage ());
        }
    }
}