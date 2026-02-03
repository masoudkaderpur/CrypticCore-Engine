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
    private static final byte[] MAGIC = "CCE".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte VERSION = 1;

    public EncryptionEngine(CipherAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void processFile(CrypticMode mode, String inputPath, String outputPath, byte[] key, long fileSize) throws IOException {
        int bytesRead, keyPointer = 0, lastPercentage = -1;
        long totalBytesProcessed = 0;
        byte[] buffer = new byte[8192];

        validateParameters(mode, key, Paths.get(inputPath), Paths.get(outputPath), fileSize);

        try (FileInputStream in = new FileInputStream(inputPath); FileOutputStream out = new FileOutputStream(outputPath)) {
            if (mode == CrypticMode.ENCRYPTION) {
                writeHeader(out);
            } else {
                checkHeader(in);
            }

            while ((bytesRead = in.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (keyPointer == key.length) keyPointer = 0;
                    buffer[i] = algorithm.transform(buffer[i], key[keyPointer++]);
                }
                out.write(buffer, 0, bytesRead);

                totalBytesProcessed += bytesRead;

                int currentPercentage = (int) ((totalBytesProcessed * 100L) / fileSize);

                if (currentPercentage > lastPercentage) {
                    printProgress(currentPercentage);
                    lastPercentage = currentPercentage;
                }
            }
            if (totalBytesProcessed != fileSize) {
                throw new IOException("Data truncation detected! Expected " + fileSize + " bytes but processed " + totalBytesProcessed);
            }
            System.out.println();
        }
    }

    private static void checkHeader(FileInputStream in) throws IOException {
        byte[] fileMagic = new byte[3];
        if (in.read(fileMagic) != 3 || !Arrays.equals(fileMagic, MAGIC)) {
            throw new IOException("Incorrect CrypticCore-Engine Data!");
        }
        int fileVersion = in.read();
        if (fileVersion != VERSION) {
            throw new IOException("Incompatible version: " + fileVersion);
        }
    }

    private static void writeHeader(FileOutputStream out) throws IOException {
        out.write(MAGIC);
        out.write(VERSION);
    }

    private static void validateParameters(CrypticMode mode, byte[] key, Path inPath, Path outPath, long fileSize) throws IOException {
        if (mode == null) throw new IllegalArgumentException("Mode must not be null");

        if (!Files.exists(inPath)) {
            throw new FileNotFoundException("Input file does not exist: " + inPath);
        }

        if (Files.exists(outPath) && Files.isSameFile(inPath, outPath)) {
            throw new IllegalArgumentException("Input and output paths must not be the same!");
        }

        if (key == null || key.length == 0) throw new IllegalArgumentException("Key must not be null or empty");

        if (fileSize == 0) throw new IllegalArgumentException("File size must be >0");
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