/**
 * Utilities class holds all the needed functions to work with bits to apply
 * permutations and remove padding
 * 
 * Happy cow says: "Muuuuuuu.."
 * 
 * @author Ben Sabah.
 */
class CoreUtils {
	/**
	 * This function applies a bitwise-XOR over two byte vectors.
	 * 
	 * @param byteArrA
	 *            The array of bytes to be xor'ed with <code>byteArrB</code>
	 * @param byteArrB
	 *            The array of bytes to be xor'ed with <code>byteArrA</code>
	 * @return The result Xor'ed byte array
	 */

	static byte[] xor(byte[] byteArrA, byte[] byteArrB) {
		byte[] result = new byte[byteArrA.length];

		for (int i = 0; i < byteArrA.length; i++) {
			result[i] = (byte) (byteArrA[i] ^ byteArrB[i]);
		}

		return result;
	}

	/**
	 * This function get a array of bytes, an index of a bit and a value (1 or
	 * 0, the method will change the bit of the given index (as if the bytes
	 * were concatenated) to the given vale.
	 * 
	 * @param byteArr
	 *            The byte array that needed a bit change.
	 * @param index
	 *            The index of the bit to change.
	 * @param val
	 *            The value (0 or 1) of the bit that we want to change to.
	 */
	static void setBit(byte[] byteArr, int index, int val) {
		int whichByte = index / 8;
		int whichBit = index % 8;
		byte byteToModify = byteArr[whichByte];

		byteToModify = (byte) (((0xFF7F >> whichBit) & byteToModify) & 0x00FF);
		byteArr[whichByte] = (byte) ((val << (8 - (whichBit + 1))) | byteToModify);
	}

	/**
	 * This function get array of bytes and an index, and returns the value of
	 * that bit (as if the bytes were concatenated).
	 * 
	 * @param byteArr
	 *            The array of bytes.
	 * @param index
	 *            The index of the bit we want to get.
	 * @return The value of the bit in the given index.
	 */
	static int getBit(byte[] byteArr, int index) {
		int whichByte = index / 8;
		int whichBit = index % 8;

		return byteArr[whichByte] >> (8 - (whichBit + 1)) & 0x0001;
	}

	/**
	 * This function get array of bytes a starting index and the number of bits
	 * to copy and returns array of bytes with the request bits.
	 * 
	 * @param byteArr
	 *            The array of bytes.
	 * @param index
	 *            The index from where to start copying the bits.
	 * @param length
	 *            how many bits to return from the starting <code>index</code>.
	 * @return The requested bits in an array of bytes.
	 */
	static byte[] getBits(byte[] byteArr, int index, int length) {
		int numOfBytes = (length - 1) / 8 + 1;
		byte[] result = new byte[numOfBytes];
		int val;

		for (int i = 0; i < length; i++) {
			val = getBit(byteArr, index + i);
			setBit(result, i, val);
		}

		return result;
	}

	/**
	 * /** This function concatenates the bits of 2 byte arrays.
	 * 
	 * @param byteArrA
	 *            The first byte array to copy from.
	 * @param aLength
	 *            The number of bits to copy from the <code>byteArrA</code>.
	 * @param byteArrB
	 *            The second byte array to copy from.
	 * @param bLength
	 *            The number of bits to copy from the <code>byteArrA</code>.
	 * @return The result byte array of the concatenation.
	 */
	static byte[] concatenateBits(byte[] byteArrA, int aLength, byte[] byteArrB, int bLength) {
		int numOfBytes = (aLength + bLength - 1) / 8 + 1;
		byte[] result = new byte[numOfBytes];
		int j = 0;
		int val;

		for (int i = 0; i < aLength; i++, j++) {
			val = getBit(byteArrA, i);
			setBit(result, j, val);
		}
		for (int i = 0; i < bLength; i++, j++) {
			val = getBit(byteArrB, i);
			setBit(result, j, val);
		}

		return result;
	}

	/**
	 * This function get array of bytes and split it into
	 * <code>howManyParts</code> parts.
	 * 
	 * @param byteArr
	 *            The byte array to copy from.
	 * @param howManyParts
	 *            The number of bytes split the bit to.
	 * @return The split byte array.
	 */
	static byte[] splitBytes(byte[] byteArr, int howManyParts) {
		byte[] result = new byte[howManyParts];
		int whichByte;
		int val;

		for (int i = 0; i < byteArr.length * 8; i++) {
			whichByte = i / 3;
			val = getBit(byteArr, i);
			setBit(result, 5 * (whichByte + 1) + i, val);
		}

		return result;
	}

	/**
	 * This function get array of bytes, the number of relevant bits in it, and
	 * rotate its bits to the left by the given number of times.
	 * 
	 * @param byteArr
	 *            The bytes array to rotate.
	 * @param length
	 *            The number of relevant bits in the byte array.
	 * @param timesToRotate
	 *            how many "ticks" to rotate the bits.
	 * @return
	 */
	static byte[] leftRotation(byte[] byteArr, int length, int timesToRotate) {
		int numberOfBytes = (length - 1) / 8 + 1;
		byte[] result = new byte[numberOfBytes];
		int val;

		for (int i = 0; i < length; i++) {
			val = getBit(byteArr, (i + timesToRotate) % length);
			setBit(result, i, val);
		}

		return result;
	}

	/**
	 * This function applies a permutation on a array of bytes. it uses an
	 * indexing-table in-order to create a permutation. the "new" location of
	 * bits is given in the table.
	 * 
	 * @param byteArr
	 *            The byte array to rearrange.
	 * @param table
	 *            The indexing table that we use to rearrange the
	 *            <code>byteArr</code>.
	 * @return The permutation of the array.
	 */
	static byte[] permutation(byte[] byteArr, int[] table) {
		int tableSize = (table.length - 1) / 8 + 1;
		byte[] result = new byte[tableSize];
		int val;

		for (int i = 0; i < table.length; i++) {
			val = getBit(byteArr, table[i] - 1);
			setBit(result, i, val);
		}

		return result;
	}

	/**
	 * The function counts the size of the padding if the vector of bytes and
	 * creates a new vector of bytes like the original just without the padding.
	 * 
	 * @param byteArr
	 *            The array of bytes of which to remove the padding.
	 * @return The array with its padding removed.
	 */
	static byte[] removePadding(byte[] byteArr) {
		int paddingSize = 0;
		int i = byteArr.length - 1;

		while (i >= 0 && byteArr[i] == 0) {
			paddingSize++;
			i--;
		}
		byte[] result = new byte[byteArr.length - paddingSize - 1];
		System.arraycopy(byteArr, 0, result, 0, result.length);

		return result;
	}

	/**
	 * This function generates 16 sub-keys from a given 64-bit master key.
	 * 
	 * @param key
	 *            The 64-bit master key.
	 * @param PC1
	 *            The 1st permutation table, used on the master key to remove
	 *            and mix some of its bits.
	 * @param PC2
	 *            The 2nd permutation table, used on the generated 54 bits keys
	 *            to add more confusion to the process.
	 * @param keyShift
	 *            The table that says how many 'ticks' to rotate the keys in
	 *            each round.
	 * @return
	 */
	static byte[][] subKeysGenerator(byte[] key, int[] PC1, int[] PC2, int[] keyShift) {
		byte[][] subKeys = new byte[16][];
		byte[] tmp = CoreUtils.permutation(key, PC1);
		byte[] C = CoreUtils.getBits(tmp, 0, PC1.length / 2);
		byte[] D = CoreUtils.getBits(tmp, PC1.length / 2, PC1.length / 2);

		for (int i = 0; i < 16; i++) {
			C = CoreUtils.leftRotation(C, 28, keyShift[i]);
			D = CoreUtils.leftRotation(D, 28, keyShift[i]);
			byte[] CD = CoreUtils.concatenateBits(C, 28, D, 28);
			subKeys[i] = CoreUtils.permutation(CD, PC2);
		}
		return subKeys;
	}

	/**
	 * This method gets a byte and return its string representation, for example
	 * the following <code>byteToString((byte) 5)</code> will return the string
	 * <code>00000101</code> This method is completely for testing purposes.
	 * 
	 * @param a
	 *            The byte to return as a string.
	 * @return The string representing the byte.
	 */
	static String byteToString(byte a) {
		String result = Integer.toBinaryString(Byte.toUnsignedInt(a));

		while (result.length() < 8) {
			result = "0" + result;
		}

		return result;
	}

	/**
	 * This method gets an array of bytes and return their string
	 * representation, for example the following
	 * <code>byteToString(new byte[] {(byte) 4, (byte) 7})</code> will return
	 * the string <code>0000010100000111</code> This method is completely for
	 * testing purposes.
	 * 
	 * @param a
	 *            The byte to return as a string.
	 * @return The string representing the byte array.
	 */
	static String byteArrToString(byte[] a, boolean addSeparator) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < a.length; i++) {
			sb.append(byteToString(a[i]) + '|');
		}

		sb.deleteCharAt(sb.length() - 1);

		return (addSeparator) ? sb.toString() : sb.toString().replace("|", "");
	}
}