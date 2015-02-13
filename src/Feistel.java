import java.util.Base64;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.RandomAccessFile;

/**
 * The Feistel class holds all the functions that are used to implement the
 * Feistel network cipher-algorithm and uses cipher block chaining.
 * 
 * Happy cow says: "Muuuuuuu.."
 * 
 * @author Ben Sabah.
 */
public class Feistel {

	private static final int KEY_SIZE = 56;

	/**
	 * This table specifies the input permutation of a 64-bit block. it
	 * re-assign the bits in the order of the table below in the following
	 * fashion: the first bit of the output is taken from the 58th bit of the
	 * input; the second bit from the 50th bit, and so on. This information is
	 * taken from http://en.wikipedia.org/wiki/DES_supplementary_material
	 */
	private static int[] IP = { 58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62,
			54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8, 57, 49, 41, 33, 25, 17, 9, 1,
			59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23,
			15, 7 };

	/**
	 * The final permutation is the inverse of the initial permutation (IP); the
	 * table is interpreted similarly to as the IP. taken from
	 * http://en.wikipedia.org/wiki/DES_supplementary_material
	 */
	private static int[] IPinverse = { 40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63,
			31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52,
			20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9,
			49, 17, 57, 25 };

	/**
	 * The expansion permutation for the f function taken from
	 * http://en.wikipedia.org/wiki/DES_supplementary_material
	 */
	private static int[] E = { 9, 8, 9, 10, 11, 12, 13, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26,
			27, 12, 13, 14, 15, 16, 17, 16, 17, 32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 18, 19, 28, 29,
			28, 29, 30, 31, 32, 1 };

	/**
	 * The P permutation shuffles the bits of a 32-bit half-block. taken from
	 * http://en.wikipedia.org/wiki/DES_supplementary_material
	 */
	private static int[] P = { 16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8,
			24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25 };

	/**
	 * This PC1 permutation selects 56 bits from the 64-bit master key, the
	 * eight remaining bits (8, 16, 24, 32, 40, 48, 56, 64) were specified for
	 * use as parity bits. taken from
	 * http://en.wikipedia.org/wiki/DES_supplementary_material
	 */
	private static int[] PC1 = { 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59,
			51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46,
			38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4 };

	/**
	 * This permutation selects the 48-bit sub-key for each round of the 16
	 * rounds from the 56-bit key-schedule state. taken from
	 * http://en.wikipedia.org/wiki/DES_supplementary_material
	 */
	private static int[] PC2 = { 14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8,
			16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56,
			34, 53, 46, 42, 50, 36, 29, 32 };

	/**
	 * This vector determine how many 'ticks' each half of the sub-key will
	 * rotated left.taken from
	 * http://en.wikipedia.org/wiki/DES_supplementary_material
	 */
	private static int[] keyShift = { 1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1 };

	/**
	 * This array adds a fixed number to every 3 bit that will later be
	 * modulo'ed with 3 in-order to determine which of the 3 bit segment to
	 * remove, and the 2 other bits to will be switched
	 */
	private static int[] additionTable = { 1, 2, 1, 0, 2, 1, 1, 2, 2, 0, 1, 0, 2, 0, 1, 2 };

	static String plainTextPath; // The path to the plain-text file.
	static String cipherTextPath; // The path to the cipher-text file.
	static String keyPath; // The path to the key file.
	static String configPath; // The path to the configuration file.
	static byte[] key = new byte[KEY_SIZE]; // The Master 54-bit key.
	static byte[][] subKeys; // All the sub-keys.

	/**
	 * This method gets a cipher-text and a plain-text and verify that the
	 * deciphered text of the cipher-text equals to the plain-text.
	 * 
	 * @return Whether or not the deciphered text is the same as the original
	 *         text.
	 * 
	 * @throws IllegalArgumentException
	 *             In case of base64 translation error
	 */
	static boolean verify() throws IOException, IllegalArgumentException {
		// Read the cipher-text from the file and decipher it.
		byte[] cipherTextBytes = Files.readAllBytes(Paths.get(cipherTextPath));
		cipherTextBytes = Base64.getDecoder().decode(cipherTextBytes);
		byte[] decipheredTextBytes = decrypt(cipherTextBytes, key);

		// Read the plain-text file.
		byte[] plainTextBytes = Files.readAllBytes(Paths.get(plainTextPath));

		// Check if the sizes are different.
		if (plainTextBytes.length != decipheredTextBytes.length) {
			return false;
		}

		// Check if the deciphered bytes are the same as the plain text bytes.
		for (int i = 0; i < plainTextBytes.length; i++) {
			if (plainTextBytes[i] != decipheredTextBytes[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * The function decipher the cipher-text file and write the result into the
	 * plain-text file.
	 * 
	 * @param plainTextFile
	 *            The stream to the plain-text file
	 * @throws IOException
	 *             In case of error while read / write to file.
	 * @throws IllegalArgumentException
	 *             In case of base64 translation error
	 */
	static void decryptFile(RandomAccessFile plainTextFile) throws IOException,
			IllegalArgumentException {
		// Load the plain text from the files to the memory.
		byte[] cipherTextBytes = Files.readAllBytes(Paths.get(cipherTextPath));

		// Convert the resulting byte to base64 text.
		cipherTextBytes = Base64.getDecoder().decode(cipherTextBytes);

		// Decipher the text.
		byte[] plainTextBytes = decrypt(cipherTextBytes, key);

		// Write the deciphered result.
		plainTextFile.seek(0);
		plainTextFile.write(plainTextBytes);
		plainTextFile.setLength(plainTextBytes.length);
	}

	/**
	 * The function encrypt the plain-text file and write it to the cipher-text
	 * file
	 * 
	 * @param cipherTextFile
	 *            Stream to the cipher-text file.
	 * @throws IOException
	 *             In case of error while read / write to file.
	 * @throws IllegalArgumentException
	 *             In case of base64 translation error
	 */
	static void encryptFile(RandomAccessFile cipherTextFile) throws IOException,
			IllegalArgumentException {
		// Load the plain text from the files to the memory.
		byte[] plainTextBytes = Files.readAllBytes(Paths.get(plainTextPath));

		// Encrypt the text.
		byte[] cipherTextBytes = encrypt(plainTextBytes, key);

		// Convert the resulting byte to base64 text.
		cipherTextBytes = Base64.getEncoder().encode(cipherTextBytes);

		// Write the encrypted result.
		cipherTextFile.seek(0);
		cipherTextFile.write(cipherTextBytes);
		cipherTextFile.setLength(cipherTextBytes.length);
	}

	/**
	 * The function after mixing in the sub-key, the block is divided into
	 * sixteen 3-bit pieces. Each segment is added with a fixed number from the
	 * <code>additionTable</code> then we modulo the number with 3, to determine
	 * which of the 3 bit segment to remove, while we swap (twist) the 2 other
	 * bits the result is a non-linear, 48 to 32 bit narrowed byte array.
	 */
	private static byte[] twistBox(byte[] input) {
		byte[] result = new byte[4];

		// Splitting the array into 16 parts (bytes).
		byte[] threeBitsPieces = CoreUtils.splitBytes(input, 16);

		int whichBitToRemove;
		for (int i = 0; i < 16; i++) {
			// Adding to each byte a set number, each byte has it own number.
			threeBitsPieces[i] += additionTable[i];

			// Modulo the result with 3 and that bit we will remove.
			whichBitToRemove = threeBitsPieces[i] % 3;

			// Now we remove the selected bit an swap the 2 other bits.
			switch (whichBitToRemove) {
			case 0:
				CoreUtils.setBit(result, i * 2, CoreUtils.getBit(threeBitsPieces, 7));
				CoreUtils.setBit(result, i * 2 + 1, CoreUtils.getBit(threeBitsPieces, 6));
				break;

			case 1:
				CoreUtils.setBit(result, i * 2, CoreUtils.getBit(threeBitsPieces, 7));
				CoreUtils.setBit(result, i * 2 + 1, CoreUtils.getBit(threeBitsPieces, 5));
				break;

			case 2:
				CoreUtils.setBit(result, i * 2, CoreUtils.getBit(threeBitsPieces, 6));
				CoreUtils.setBit(result, i * 2 + 1, CoreUtils.getBit(threeBitsPieces, 5));
				break;
			}
		}
		return result;
	}

	/**
	 * Expansion — the 32-bit half-block is expanded into 48 bits using an
	 * expansion permutation, this is achieved by duplicating half of the bits.
	 * The output consists of 48 bits.
	 * 
	 * Key mixing — The result of the expansion is combined with a sub-key using
	 * the XOR operation. This sub-key is one of 16 48-bit sub-keys created for
	 * each of the 16 rounds of the feistel network that are derived from the
	 * main key using the key schedule (which is described bellow).
	 * 
	 * Substitution — after xor'ing the sub-key and expansion, the 48-bit block
	 * splits to 16 3 bits pieces, each of thus get added a random number from
	 * the additionTable and modulu'ed with 3 and the result will be the bit
	 * that will be removed and the 2 other will be swapped. This solution
	 * provide the core of the security and it provide the confusion, without
	 * it, the cipher would be linear, and trivially breakable.
	 * 
	 * Permutation — finally, the 32 outputs from the twistBox are rearranged
	 * according to a fixed permutation, the P-box. than we permuted that result
	 * using the P-box and the expansion provides the "confusion and diffusion"
	 * respectively, this process that been identified by Claude Shannon in the
	 * 1940s as a necessary condition for a secure yet practical cipher.
	 */

	private static byte[] f_Function(byte[] R, byte[] key) {
		byte[] result;
		result = CoreUtils.permutation(R, E);
		result = CoreUtils.xor(result, key);
		result = twistBox(result);
		result = CoreUtils.permutation(result, P);
		return result;
	}

	/**
	 * The function encrypts/deciphers the given array of bytes with the given
	 * sub-keys.
	 * 
	 * @param block
	 *            the 64-bit block that we want to handle
	 * @param subkeys
	 *            the 16, 48-bit, generated sub-keys.
	 * @param isDecrypt
	 *            decide whether we want to encrypt (<code>false</code>) or
	 *            decrypt (<code>true</code>).
	 * @return
	 */
	private static byte[] blockEncryptDecrypt(byte[] block, byte[][] subkeys, boolean isDecrypt) {
		byte[] result = new byte[block.length];
		byte[] R = new byte[block.length / 2];
		byte[] L = new byte[block.length / 2];

		result = CoreUtils.permutation(block, IP);
		L = CoreUtils.getBits(result, 0, IP.length / 2);
		R = CoreUtils.getBits(result, IP.length / 2, IP.length / 2);

		/**
		 * Since this is a symmetric cipher all we need to do is change the
		 * order of the sub-keys to change encryption to decyption.
		 */
		for (int i = 0; i < 16; i++) {
			byte[] tmpR = R;
			R = f_Function(R, (isDecrypt) ? subkeys[15 - i] : subkeys[i]);
			R = CoreUtils.xor(L, R);
			L = tmpR;
		}

		result = CoreUtils.concatenateBits(R, IP.length / 2, L, IP.length / 2);
		result = CoreUtils.permutation(result, IPinverse);
		return result;
	}

	/**
	 * The function encrypts the given vector of bytes using a a fiestel network
	 * and CBC.
	 * 
	 * @param input
	 *            The input byte array to encode.
	 * @param key
	 *            The 56-bit master key (7-byte array) to use while encoding.
	 * @return The encoded text in a byte array.
	 */

	public static byte[] encrypt(byte[] input, byte[] key) {
		// Initializing variables.
		byte[] iv = "UUUUUUUU".getBytes(); // IV to be of the form: 010101....
		int length = 8 - input.length % 8;
		byte[] result = new byte[input.length + length]; // The result array.
		byte[] curBlock = new byte[8]; // The current block
		byte[] padding = new byte[8 - input.length % 8]; // The padding bytes.
		padding[0] = (byte) 0x80;
		int i;
		for (i = 1; i < length; i++) {
			padding[i] = 0;
		}

		// Generating 16 sub-keys from the master 54-bit master key.
		subKeys = CoreUtils.subKeysGenerator(key, PC1, PC2, keyShift);

		// Filling each block.
		int count = 0;
		for (i = 0; i < input.length + length; i++) {
			if (i > 0 && i % 8 == 0) {

				// Xor'ing the current block with the iv.
				curBlock = CoreUtils.xor(curBlock, iv);

				// updating the iv value for the next block in the chain.
				iv = blockEncryptDecrypt(curBlock, subKeys, false);

				// Decipher the block.
				curBlock = blockEncryptDecrypt(curBlock, subKeys, false);
				System.arraycopy(curBlock, 0, result, i - 8, curBlock.length);
			}

			// Construct the block bit at a time.
			if (i < input.length) {
				curBlock[i % 8] = input[i];
			} else {
				curBlock[i % 8] = padding[count % 8];
				count++;
			}
		}

		// Handle the last block.
		curBlock = CoreUtils.xor(curBlock, iv);
		curBlock = blockEncryptDecrypt(curBlock, subKeys, false);
		System.arraycopy(curBlock, 0, result, i - 8, curBlock.length);
		return result;
	}

	/**
	 * The function deciphers the given array of bytes using a feistel network
	 * and CBC.
	 * 
	 * @param input
	 *            The input byte array to decode.
	 * @param key
	 *            The 56-bit master key (7-byte array) to use while decoding.
	 * @return The decoded text in a byte array.
	 */
	public static byte[] decrypt(byte[] input, byte[] key) {
		// Initializing variables.
		byte[] iv = "UUUUUUUU".getBytes(); // initial iv = 010101....
		byte[] result = new byte[input.length];// The result array.
		byte[] cipherTextBlock = new byte[8]; // The current cipher-block
		byte[] curBlock = new byte[8];// The current block
		int i;

		// Generating 16 sub-keys from the master 54-bit master key.
		subKeys = CoreUtils.subKeysGenerator(key, PC1, PC2, keyShift);

		// Filling each block.
		for (i = 0; i < input.length; i++) {
			if (i > 0 && i % 8 == 0) {
				cipherTextBlock = curBlock;

				// Decipher the block.
				curBlock = blockEncryptDecrypt(curBlock, subKeys, true);

				// Xor'ing the current block with the iv.
				curBlock = CoreUtils.xor(curBlock, iv);

				// updating the iv value for the next block in the chain.
				iv = cipherTextBlock;
				System.arraycopy(curBlock, 0, result, i - 8, curBlock.length);
			}

			// Construct the block bit at a time.
			if (i < input.length)
				curBlock[i % 8] = input[i];
		}
		// Handle the last block.
		curBlock = blockEncryptDecrypt(curBlock, subKeys, true);
		curBlock = CoreUtils.xor(curBlock, iv);
		System.arraycopy(curBlock, 0, result, i - 8, curBlock.length);
		result = CoreUtils.removePadding(result);
		return result;
	}
}
