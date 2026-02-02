package at.tuwien.crypticcore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class EncryptionEngine {
    private final CipherAlgorithm algorithm;

    public EncryptionEngine (CipherAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void processFile (String inputPath, String outputPath, byte[] key) throws IOException {
        if (key == null || key.length == 0) throw new IllegalArgumentException ("Key must not be null or empty");

        byte[] buffer = new byte[8192];
        int bytesRead, keyPointer = 0;

        try (FileInputStream in = new FileInputStream (inputPath); FileOutputStream out =
                new FileOutputStream (outputPath)) {
            while ((bytesRead = in.read (buffer)) != - 1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (keyPointer == key.length) keyPointer = 0;
                    buffer[i] = algorithm.transform (buffer[i], key[keyPointer++]);
                }
                out.write (buffer, 0, bytesRead);
            }
        }
    }
}