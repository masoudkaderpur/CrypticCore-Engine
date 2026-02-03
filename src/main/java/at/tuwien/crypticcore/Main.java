package at.tuwien.crypticcore;


import java.nio.charset.StandardCharsets;

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
            CryptionMode cryptionMode = null;
            if (mode.equals("encrypt") || mode.equals("encryption")) cryptionMode = CryptionMode.ENCRYPTION;
            else if (mode.equals("decrypt") || mode.equals("decryption")) cryptionMode = CryptionMode.DECRYPTION;
            else throw new IllegalArgumentException("The CryptionMode can either be encrypti or decrypt");

            engine.processFile(cryptionMode, input, output, key);
            System.out.println("Data successfully processed");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}