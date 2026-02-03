# CrypticCore Engine

CrypticCore is a high-performance Java-based encryption engine designed for memory-efficient file transformation. It
implements a decoupled architecture that separates the cryptographic logic from the data streaming process.

---

## 1. Theoretical Foundation

### 1.1 The Transformation (XOR Logic)

The engine utilizes the bitwise **Exclusive OR (XOR)** operation. Given that XOR is an involution, the transformation is
self-inverse, allowing for identical encryption and decryption logic.

The operation is defined as:
$$P \oplus K = C$$
$$C \oplus K = P$$

**Variable Definitions:**

* $P \in \{0, 1\}^8$: Plaintext byte
* $K \in \{0, 1\}^8$: Key byte
* $C \in \{0, 1\}^8$: Ciphertext byte
* $\oplus$: Bitwise XOR operator

### 1.2 Key Streaming (Modular Arithmetic)

To handle data streams where the length of the plaintext exceeds the key length, a cyclic key schedule is implemented
using modular arithmetic.

The index mapping for the keystream is defined as:
$$i_{key} = i_{file} \pmod{L_{key}}$$

**Variable Definitions:**

* $i_{key} \in \mathbb{N}_0$: Resulting index within the key array ($0 \le i_{key} < L_{key}$)
* $i_{file} \in \mathbb{N}_0$: Current byte offset within the source file
* $L_{key} \in \mathbb{N}^+$: Total length of the key in bytes

---

## 2. Implementation Details

### 2.1 Java Type Handling (Sign Extension Mitigation)

In the Java Virtual Machine, bitwise operations on `byte` types involve an implicit promotion to a 32-bit `int`. This
promotion performs a sign extension, where the sign bit (MSB) is propagated across the upper 24 bits.

To preserve the integrity of the 8-bit data and prevent unintended sign extension, a bitmask of $0xFF$ is applied:
$$B_{safe} = (B_{raw} \ \& \ 0xFF)$$

This ensures bit-level consistency across all byte values ($0$ to $255$):
$$Result = (byte) ((P \ \& \ 0xFF) \oplus (K \ \& \ 0xFF))$$

### 2.2 Memory Efficiency (Buffering Strategy)

The engine is designed for $O(1)$ space complexity. Instead of loading the entire file into RAM, data is processed in
discrete chunks.

* **Buffer Size**: Fixed at 8192 bytes (8 KB).
* **Processing Logic**: The engine explicitly handles the final buffer iteration by using the actual number of bytes
  read ($n$), ensuring that no padding bytes are appended to the output file.
* **Stream Integrity**: During decryption, the file pointer is advanced by 4 bytes (header size) before the
  transformation loop begins.

---

## 3. File Format Specification

To ensure file integrity and prevent the processing of incompatible data, the engine prepends a metadata header to every
encrypted file.

### 3.1 Custom File Header

The engine validates this header during the decryption phase. If the magic number is missing or the version is
incompatible, an `IOException` is raised.

| Offset | Length  | Description        | Value (Hex / ASCII) |
|:-------|:--------|:-------------------|:--------------------|
| 0x00   | 3 Bytes | Magic Number (CCE) | `0x43 0x43 0x45`    |
| 0x03   | 1 Byte  | Format Version     | `0x01`              |

---

## 4. Project Structure

The project follows the standard Maven directory layout:

* **`src/main/java`**: Core logic, `CipherAlgorithm` interface, and `XorCipher` implementation.
* **`src/test/java`**: Automated JUnit 5 unit tests for cryptographic integrity.
* **`pom.xml`**: Build configuration and metadata.

---

## 5. Usage

### 5.1 Running the Application

The application requires four mandatory command-line arguments:

```bash
java -jar CrypticCore.jar <mode> <input> <output> <key>