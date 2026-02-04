package at.tuwien.crypticcore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class EncryptionEngineTest {

    @TempDir
    Path tempDir;

    private final byte[] key = "secret".getBytes(StandardCharsets.UTF_8);
    private final EncryptionEngine engine = new EncryptionEngine(new XorCipher());
    private Path inputFile;
    private Path encryptedFile;
    private Path decryptedFile;

    @BeforeEach
    void setUp() throws IOException {
        inputFile = tempDir.resolve("input.bin");
        encryptedFile = tempDir.resolve("encrypted.cce");
        decryptedFile = tempDir.resolve("decrypted.bin");
        Files.write(inputFile, new byte[]{0, -1, 127, -128, 65, 66, 67});
    }

    @Test
    @DisplayName("Integration: End-to-End Cycle")
    void testFullCycle() throws IOException {
        long inputSize = Files.size(inputFile);
        engine.processFile(CrypticMode.ENCRYPTION, inputFile.toString(), encryptedFile.toString(), key, inputSize);

        byte[] encryptedBytes = Files.readAllBytes(encryptedFile);
        byte[] expectedHeader = new byte[]{'C', 'C', 'E', 1};
        for (int i = 0; i < expectedHeader.length; i++) {
            Assertions.assertEquals(expectedHeader[i], encryptedBytes[i], "Header byte mismatch at " + i);
        }

        long encryptedSize = Files.size(encryptedFile);
        engine.processFile(CrypticMode.DECRYPTION, encryptedFile.toString(), decryptedFile.toString(), key, encryptedSize - 4);

        byte[] decryptedBytes = Files.readAllBytes(decryptedFile);
        byte[] originalBytes = Files.readAllBytes(inputFile);
        Assertions.assertArrayEquals(originalBytes, decryptedBytes, "Decrypted data must match original");
    }

    @Test
    @DisplayName("Error Resilience: Truncated File")
    void testTruncation() {
        Assertions.assertThrows(IOException.class, () -> {
            long inputSize = Files.size(inputFile);
            engine.processFile(CrypticMode.ENCRYPTION, inputFile.toString(), encryptedFile.toString(), key, inputSize + 1);
        });
    }

    @Test
    @DisplayName("Error Resilience: Same File Validation")
    void testSameFile() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            long inputSize = Files.size(inputFile);
            engine.processFile(CrypticMode.ENCRYPTION, inputFile.toString(), inputFile.toString(), key, inputSize);
        });
    }

    @Test
    @DisplayName("Integration: Header Validation")
    void testInvalidHeader() throws IOException {
        byte[] badFile = new byte[]{'B', 'A', 'D', 1, 42};
        Files.write(encryptedFile, badFile);
        long sizeForProgress = Files.size(encryptedFile) - 4;
        Assertions.assertThrows(IOException.class, () -> {
            engine.processFile(CrypticMode.DECRYPTION, encryptedFile.toString(), decryptedFile.toString(), key, sizeForProgress);
        });
    }

    @Test
    @DisplayName("Integration: Version Validation")
    void testInvalidVersion() throws IOException {
        byte[] badVersionFile = new byte[]{'C', 'C', 'E', 2, 42};
        Files.write(encryptedFile, badVersionFile);
        long sizeForProgress = Files.size(encryptedFile) - 4;
        Assertions.assertThrows(IOException.class, () -> {
            engine.processFile(CrypticMode.DECRYPTION, encryptedFile.toString(), decryptedFile.toString(), key, sizeForProgress);
        });
    }
}
