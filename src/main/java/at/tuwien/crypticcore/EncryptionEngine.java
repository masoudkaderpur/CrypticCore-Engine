package at.tuwien.crypticcore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class EncryptionEngine {
    private final CipherAlgorithm algorithm;
    private static final byte[] MAGIC_NUMBER = "CCE".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte VERSION = 1;

    public EncryptionEngine(CipherAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void processFile(CrypticMode mode, String inputPath, String outputPath, byte[] key) throws IOException {
        if (mode == null) throw new IllegalArgumentException("Mode must not be null");

        Path inPath = Paths.get(inputPath);
        Path outPath = Paths.get(outputPath);

        if (!Files.exists(inPath)) {
            throw new FileNotFoundException("Input file does not exist: " + inputPath);
        }

        if (Files.exists(outPath) && Files.isSameFile(inPath, outPath)) {
            throw new IllegalArgumentException("Input and output paths must not be the same!");
        }

        if (key == null || key.length == 0) throw new IllegalArgumentException("Key must not be null or empty");

        byte[] buffer = new byte[8192];
        int bytesRead, keyPointer = 0;

        long totalBytesProcessed = 0;
        int lastPercentage = -1;

        try (FileInputStream in = new FileInputStream(inputPath); FileOutputStream out = new FileOutputStream(outputPath)) {
            if (mode == CrypticMode.ENCRYPTION) {
                out.write(MAGIC_NUMBER);
                out.write(VERSION);
            } else if (mode == CrypticMode.DECRYPTION) {
                byte[] fileMagic = new byte[3];
                if (in.read(fileMagic) != 3 || !Arrays.equals(fileMagic, MAGIC_NUMBER)) {
                    throw new IOException("Incorrect CrypticCore-Engine Data!");
                }
                int fileVersion = in.read();
                if (fileVersion != VERSION) {
                    throw new IOException("Incompatible version: " + fileVersion);
                }
            } else return;

            totalBytesProcessed += 4;

            while ((bytesRead = in.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (keyPointer == key.length) keyPointer = 0;
                    buffer[i] = algorithm.transform(buffer[i], key[keyPointer++]);
                }
                out.write(buffer, 0, bytesRead);

                totalBytesProcessed += bytesRead;
                long fileSize = Files.size(Path.of(inputPath));
                int currentPercentage = (int) ((totalBytesProcessed * 100) / fileSize);

                if (currentPercentage > lastPercentage) {
                    printProgress(currentPercentage);
                    lastPercentage = currentPercentage;
                }
            }
        }
    }

    private void printProgress(int percentage) {
        System.out.print("\rProgress: [");
        int bars = percentage / 2;
        for (int i = 0; i < 50; i++) {
            if (i < bars) System.out.print("=");
            else System.out.print(" ");
        }
        System.out.print("] " + percentage + "%");
    }
}