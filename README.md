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

### 1.2 Key Streaming (Modular Arithmetic)

To handle data streams where the length of the plaintext exceeds the key length, a cyclic key schedule is implemented:
$$i_{key} = i_{file} \pmod{L_{key}}$$

---

## 2. Implementation Details

### 2.1 Java Type Handling (Sign Extension Mitigation)

To prevent unintended sign extension during the implicit promotion from `byte` to 32-bit `int`, a bitmask of $0xFF$ is
applied to maintain 8-bit integrity:
$$Result = (byte) ((P \ \& \ 0xFF) \oplus (K \ \& \ 0xFF))$$

### 2.2 Memory Efficiency & Performance

* **O(1) Space Complexity**: Processes data in discrete **8 KB buffers**, allowing files of arbitrary size (tested up to
  5 GB) to be processed with minimal RAM footprint.
* **Throughput**: Optimized for high-speed I/O, achieving over **400 MB/s** on standard hardware.
* **Real-time Telemetry**: Integrated progress bar and performance statistics (MB/s, latency).

### 2.3 Robustness & Safety (Bombenfest)

* **Atomic Writes**: Utilizes a `.tmp` file staging strategy. The final output is only created via an atomic `move`
  operation upon successful completion, preventing data corruption during crashes or power failures.
* **Memory Sanitation**: The encryption key is explicitly overwritten in the JVM heap using `Arrays.fill()` immediately
  after use to mitigate memory dump exploits.
* **Header Validation**: Strict magic number and version checking prevents the processing of incompatible or corrupted
  files.

---

## 3. File Format Specification

Every encrypted file starts with a 4-byte metadata header.

| Offset | Length  | Description        | Value (Hex / ASCII) |
|:-------|:--------|:-------------------|:--------------------|
| 0x00   | 3 Bytes | Magic Number (CCE) | `0x43 0x43 0x45`    |
| 0x03   | 1 Byte  | Format Version     | `0x01`              |

---

## 4. Usage

```bash
java -jar CrypticCore.jar <mode> <input> <output> <key>
```

**Parameters:**

* **mode:** `ENCRYPTION` or `DECRYPTION` (Case-insensitive).
* **input:** Path to the source file.
* **output:** Final destination path for the transformed file.
* **key:** Secret key for transformation.

## 5. Quality Assurance

The project follows a rigorous testing strategy to ensure data integrity and system stability:

### 6.1 Unit Testing (Cryptographic Core)

* **Involution Property:** Verified that $E_k(D_k(P)) = P$.
* **Edge Cases:** Tested with byte boundaries ($0x00, 0xFF, 0x7F, 0x80$) to prevent sign-extension issues.

### 6.2 Integration Testing (Engine Pipeline)

* **End-to-End Cycle:** Successful encryption and decryption of real file streams.
* **Atomic Integrity:** Verification of the `.tmp` staging and atomic move strategy.
* **Error Resilience:** * Detection of truncated files (Expected vs. Actual size check).
    * Prevention of in-place corruption (Same-file validation).
    * Robust header and version validation.

## 6. Project Structure

* `src/main/java`: Core engine logic and CLI handler.
* `src/test/java`: JUnit 5 tests for cryptographic integrity.
* `pom.xml`: Maven build configuration.
