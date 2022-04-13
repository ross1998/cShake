import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementation of the KMACXOF256 Algorithm for encryption.
 * @author Ross Martsenyak
 *
 */

public class KMACXOF256 {
	
	static int ROUNDS = 24;
	
	public static final long[] keccakf_rndc = {
	        0x0000000000000001L, 0x0000000000008082L, 0x800000000000808aL,
	        0x8000000080008000L, 0x000000000000808bL, 0x0000000080000001L,
	        0x8000000080008081L, 0x8000000000008009L, 0x000000000000008aL,
	        0x0000000000000088L, 0x0000000080008009L, 0x000000008000000aL,
	        0x000000008000808bL, 0x800000000000008bL, 0x8000000000008089L,
	        0x8000000000008003L, 0x8000000000008002L, 0x8000000000000080L,
	        0x000000000000800aL, 0x800000008000000aL, 0x8000000080008081L,
	        0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
	    };
	
	public static final int keccakf_rotc[] = {
	    1,  3,  6,  10, 15, 21, 28, 36, 45, 55, 2,  14,
	    27, 41, 56, 8,  25, 43, 62, 18, 39, 61, 20, 44
	};
	
	public static final int keccakf_piln[] = {
	     10, 7,  11, 17, 18, 3, 5,  16, 8,  21, 24, 4,
	     15, 23, 19, 13, 12, 2, 20, 14, 22, 9,  6,  1
	};

	
	
	public KMACXOF256() {
		
		
	}
	
	/**
	 * K is a key bit string of any length7, including zero. 
	 * • X is the main input bit string. It may be of any length, including zero. 
	 * • L is an integer representing the requested output length8 in bits. 
	 * • S is an optional customization bit string of any length, including zero. 
	 * If no customization is desired, S is set to the empty string
	 * @return
	 * @throws IOException 
	 */
	public byte[] k256 (byte[] K, byte[] X,int L, byte[] S) throws IOException {
		
		 //newX = bytepad(encode_string(K), 136) || X || right_encode(0). 
		//2. return cSHAKE256(newX, L, “KMAC”, S). 
		
		byte[] newX = bytepad(encode_string(K), 136);
		newX = concatenate(newX, X);
		//byte[] right = new byte[] {(byte)0x00, (byte)0x01};
		//newX = concatenate(newX, right);
		
		
		for(int i = 0; i < newX.length; i++) {
			
			System.out.println(String.format("0x%02X", newX[i]));
		}
		System.out.println(newX);
		return null;
		
	}
	
public static byte[] bytepad(byte[] X, int w) throws IOException {
		
		
		//1. z = left_encode(w) || X. 
		byte[] temp = left_encode(w);
		byte[] z = new byte[136];
		
		int length = concatenate(temp, X).length;
		
		for(int i = 0; i < length; i++) {
			z[i] = concatenate(temp, X)[i];
		}
		
		//z = concatenate(temp, X);
		
		//System.arraycopy(temp, 0, z, 0, temp.length);
        //System.arraycopy(X, 0, z, temp.length, X.length);
		
		

		for (int i = temp.length + X.length; i < z.length; i++) {
            z[i] = (byte)0;
        }
		
		return z;
	}
	
	public static byte[] right_encode(int x) {

		//Validity Conditions: 0 ≤ x < 22040 
				//1. Let n be the smallest positive integer for which 2^(8*n) > x. 
				if(x >= 22040) {
					
					throw new RuntimeException("Validity Condition not satisfied" + x);
				}
				
				int n = 1;
				int temp;
				
				while (n < x) {
					
					temp = (int) Math.pow(2, (8 * (n)));
					if(temp > x) {
						break;
					} 
					n++;
					
				}
				
				
				
				//2. Let x1, x2, …, xn be the base-256 encoding of x satisfying: x = ∑ 28(n-i)xi, for i = 1 to n.
				
				
				//3. Let Oi = enc8(xi), for i = 1 to n. 
				//TO DO CHANGE 
				byte[] O = new byte[n + 1];
		        for (int i = n; i > 0; i--) {
		            O[i] = (byte)(x & 0xFF);
		            x = x >>> 8;
		        }
				
				//4. Let O(n+1) = enc8(n). 
				 O[n + 1] = (byte) n;
				 
				 
				
				//5. Return O = O0 || O1 || … || On−1 || On. 
				 
				 return O;
	}

	public static byte[] left_encode(int x) {
		
		//Validity Conditions: 0 ≤ x < 22040 
		//1. Let n be the smallest positive integer for which 2^(8*n) > x. 
		if(x >= 22040) {
			
			throw new RuntimeException("Validity Condition not satisfied" + x);
		}
		
		int n = 1;
		int temp;
		
		while (n < x) {
			
			temp = (int) Math.pow(2, (8 * (n)));
			if(temp > x) {
				break;
			} 
			
			n++;
			
		}
		
		//System.out.println(n);
		
		
		
		//2. Let x1, x2, …, xn be the base-256 encoding of x satisfying: x = ∑ 28(n-i)xi, for i = 1 to n.
		
		
		//3. Let Oi = enc8(xi), for i = 1 to n. 
		
		byte[] O = new byte[n + 1];
        for (int i = n; i > 0; i--) {
            O[i] = (byte)(x & 0xFF);
            x = x >>> 8;
        }
		
		//4. Let O0 = enc8(n). 
		 O[0] = (byte) n;
		 
		 
		
		//5. Return O = O0 || O1 || … || On−1 || On. 
		 
		 return O;
		
	}
	public static byte[] encode_string(byte[] S) throws IOException {
		
		byte[] len1 = left_encode(S.length << 3);
		byte[] len2 = new byte[len1.length + S.length];
		
		 System.arraycopy(len1, 0, len2, 0, len1.length);
		 System.arraycopy(S, 0, len2, len1.length, S.length);

	      return len2;
		
	}
	
	public static byte[] concatenate(byte[] arrayOne, byte[] arrayTwo) throws IOException {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write( arrayOne );
		outputStream.write( arrayTwo );
		
		int size = arrayOne.length + arrayTwo.length;
		
		byte temp[] = outputStream.toByteArray();
		
		byte result[] = new byte[size];
		
		for(int i = 0; i < temp.length; i ++) {
			result[i] = temp[i];
		}
		
		return result;
	}
}
