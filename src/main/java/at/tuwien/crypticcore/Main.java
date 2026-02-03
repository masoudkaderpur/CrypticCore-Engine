package at.tuwien.crypticcore;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("correct syntax: java -jar CrypticCore.jar <mode> <input> <output> <key>");
            System.exit(1);
            return;
        }

        String mode = args[0], input = args[1], output = args[2], password = args[3];

        CipherAlgorithm xor = new XorCipher();
        EncryptionEngine engine = new EncryptionEngine(xor);

        byte[] key = password.getBytes(StandardCharsets.UTF_8);

        try {
            CrypticMode crypticMode = CrypticMode.fromString(mode);

            long start = System.nanoTime();
            engine.processFile(crypticMode, input, output, key);
            long end = System.nanoTime();

            Arrays.fill(key, (byte) 0);

            long durationNs = end - start;
            double seconds = durationNs / 1_000_000_000.0;
            long bytes = Files.size(Paths.get(input));
            double megabytes = bytes / (1024.0 * 1024.0);
            double throughput = megabytes / seconds;
            double durationMs = durationNs / 1_000_000.0;


            System.out.println("\n----- Performance Statistics -----");
            System.out.println();
            System.out.printf("  Action:      %s%n", crypticMode);
            System.out.printf("  File Size:   %.2f MB%n", megabytes);
            System.out.printf("  Time taken:  %.2f ms%n", durationMs);
            System.out.printf("  Throughput:  %.2f MB/s%n", throughput);
            System.out.println();
            System.out.println("------------------------------------");


        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
    }
}