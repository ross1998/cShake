import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import extras.SHAKE;

public class Shake {
	
	private byte[] b = new byte[200];	// 8-bit bytes
    private int pt;
    private int rsiz;
    private int mdlen;
	private static final int KECCAKF_ROUNDS = 24;

	private boolean ext = false;
	private boolean kmac = false;
	
    private static final byte[] KMAC_N = {(byte)0x4B, (byte)0x4D, (byte)0x41, (byte)0x43}; // "KMAC" in ASCII
    private static final byte[] right_encode_0 = {(byte)0x00, (byte)0x01}; // right_encode(0)
    
	private long[] q = new long[25]; // 64-bit words
	private long[] bc = new long[5];

    private static final long[] keccakf_rndc = {
		0x0000000000000001L, 0x0000000000008082L, 0x800000000000808aL,
		0x8000000080008000L, 0x000000000000808bL, 0x0000000080000001L,
		0x8000000080008081L, 0x8000000000008009L, 0x000000000000008aL,
		0x0000000000000088L, 0x0000000080008009L, 0x000000008000000aL,
		0x000000008000808bL, 0x800000000000008bL, 0x8000000000008089L,
		0x8000000000008003L, 0x8000000000008002L, 0x8000000000000080L,
		0x000000000000800aL, 0x800000008000000aL, 0x8000000080008081L,
		0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
	};
    

    private static final int[] keccakf_rotc = {
		1,  3,  6,  10, 15, 21, 28, 36, 45, 55, 2,  14,
		27, 41, 56, 8,  25, 43, 62, 18, 39, 61, 20, 44
	};

    private static final int[] keccakf_piln = {
		10, 7,  11, 17, 18, 3, 5,  16, 8,  21, 24, 4,
		15, 23, 19, 13, 12, 2, 20, 14, 22, 9,  6,  1
	};
    
	public void Shake() {
		
	}
	
	/*
	1. For all pairs (x, z) such that 0 ≤ x < 5 and 0 ≤ z < w, 
	let  C[x, z] = A[x, 0, z] ⊕ A[x, 1, z] ⊕ A[x, 2, z] ⊕ A[x, 3, z] ⊕ A[x, 4, z]. 
	2. For all pairs (x, z) such that 0 ≤ x < 5 and 0 ≤ z < w let  D[x, z] = C[(x1) mod 5, z] ⊕ C[(x+1) mod 5, (z – 1) mod w]. 
	3. For all triples (x, y, z) such that 0 ≤ x < 5, 0 ≤ y < 5, and 0 ≤ z < w, let  A′[x, y, z] = A[x, y, z] ⊕ D[x, z]. 
	*/
	
	public void theta() {
		
		for (int i = 0; i < 5; i++) {
            bc[i] = q[i] ^ q[i + 5] ^ q[i + 10] ^ q[i + 15] ^ q[i + 20];
        }
		for (int i = 0; i < 5; i++) { 
			long t = bc[(i + 4) % 5] ^ ((bc[(i + 1) % 5] << 1) | (bc[(i + 1) % 5] >>> (64 - 1)));
			for (int j = 0; j < 25; j += 5) {
				q[j + i] ^= t;
			}
		}
	}
	
	public void ropi() {
		
		long t = q[1];
		for (int i = 0; i < 24; i++) {
			int j = keccakf_piln[i];
			bc[0] = q[j];
			q[j] = ((t << keccakf_rotc[i]) | t >>> (64 - keccakf_rotc[i]));
			t = bc[0];
		}
	}
	
	public void chi() {
		
		for (int j = 0; j < 25; j += 5) {
			for (int i = 0; i < 5; i++) {
                bc[i] = q[j + i];
            }
			for (int i = 0; i < 5; i++) {
                q[j + i] ^= (~bc[(i + 1) % 5]) & bc[(i + 2) % 5];
            }
		}
	}
	public void iota(int round) {
		
		q[0] ^= keccakf_rndc[round];
		
	}
	private static void sha3_keccakf(byte[/*200*/] v) {
		long[] q = new long[25]; // 64-bit words
		long[] bc = new long[5];

		// map from bytes (in v[]) to longs (in q[]).
		for (int i = 0, j = 0; i < 25; i++, j += 8) {
			q[i] =  (((long)v[j + 0] & 0xFFL)      ) | (((long)v[j + 1] & 0xFFL) <<  8) |
					(((long)v[j + 2] & 0xFFL) << 16) | (((long)v[j + 3] & 0xFFL) << 24) |
					(((long)v[j + 4] & 0xFFL) << 32) | (((long)v[j + 5] & 0xFFL) << 40) |
					(((long)v[j + 6] & 0xFFL) << 48) | (((long)v[j + 7] & 0xFFL) << 56);
		}

		// actual iteration
		for (int r = 0; r < KECCAKF_ROUNDS; r++) {

			// Theta
			for (int i = 0; i < 5; i++) {
                bc[i] = q[i] ^ q[i + 5] ^ q[i + 10] ^ q[i + 15] ^ q[i + 20];
            }
			for (int i = 0; i < 5; i++) {
				long t = bc[(i + 4) % 5] ^ ((bc[(i + 1) % 5] << 1) | (bc[(i + 1) % 5] >>> (64 - 1)));
				for (int j = 0; j < 25; j += 5) {
					q[j + i] ^= t;
				}
			}

			// Rho Pi
			long t = q[1];
			for (int i = 0; i < 24; i++) {
				int j = keccakf_piln[i];
				bc[0] = q[j];
				q[j] = ((t << keccakf_rotc[i]) | t >>> (64 - keccakf_rotc[i]));
				t = bc[0];
			}

			//  Chi
			for (int j = 0; j < 25; j += 5) {
				for (int i = 0; i < 5; i++) {
                    bc[i] = q[j + i];
                }
				for (int i = 0; i < 5; i++) {
                    q[j + i] ^= (~bc[(i + 1) % 5]) & bc[(i + 2) % 5];
                }
			}

			//  Iota
			q[0] ^= keccakf_rndc[r];
		}

		// map from longs (in q[]) to bytes (in v[]).
		for (int i = 0, j = 0; i < 25; i++, j += 8) {
			long t = q[i];
			v[j + 0] = (byte)((t      ) & 0xFF);
			v[j + 1] = (byte)((t >>  8) & 0xFF);
			v[j + 2] = (byte)((t >> 16) & 0xFF);
			v[j + 3] = (byte)((t >> 24) & 0xFF);
			v[j + 4] = (byte)((t >> 32) & 0xFF);
			v[j + 5] = (byte)((t >> 40) & 0xFF);
			v[j + 6] = (byte)((t >> 48) & 0xFF);
			v[j + 7] = (byte)((t >> 56) & 0xFF);
		}
	}
	
    /**
     * Switch from absorbing to extensible squeezing.
     */
    public void xof() {
    	//here we pad as per NIST specification
        if (kmac) {
            update(right_encode_0, right_encode_0.length); 
        }
        
    	this.b[this.pt] ^= (byte)(this.ext ? 0x04 : 0x1F);
		this.b[this.rsiz - 1] ^= (byte)0x80;
		sha3_keccakf(b);
		this.pt = 0;
	}

    /**
     * Squeeze a chunk of hashed bytes from the sponge.
     * Repeat as many times as needed to extract the total desired number of bytes.
     * @param out   hash value buffer
     * @param len   squeezed byte count
     */
    public void out(byte[] out, int len) {
		int j = pt;
		for (int i = 0; i < len; i++) {
			if (j >= rsiz) {
				sha3_keccakf(b);
				j = 0;
			}
			out[i] = b[j++];
		}
		pt = j;
	}
    public void init256() {
        Arrays.fill(this.b, (byte)0);
        this.mdlen = 32; // fixed for SHAKE256 (for SHA128 it would be 16)
        this.rsiz = 200 - 2*mdlen;
        this.pt = 0;

        this.ext = false;
        this.kmac = false;
    }

    /**
     * Initialize the cSHAKE256 sponge.
     *
     * @param N     function name
     * @param S     customization string
     * @throws IOException 
     */
    public void cinit256(byte[] N, byte[] S) throws IOException {
        // Validity Conditions: len(N) < 2^2040 and len(S) < 2^2040
        init256();
        if ((N != null && N.length != 0) || (S != null && S.length != 0)) {
            this.ext = true; // cSHAKE instead of SHAKE
            byte[] prefix = bytepad(concatenate(encode_string(N), encode_string(S)), 136);
            update(prefix, prefix.length);
        }
    }

    /**
     * Initialize the KMACXOF256 sponge.
     *
     * @param K     MAC key
     * @param S     customization string
     * @return
     * @throws IOException 
     */
    public void kinit256(byte[] K, byte[] S) throws IOException {

        byte[] encodedK = bytepad(encode_string(K), 136);
        
        /*KMAC concatenates a padded version of the key K with the input X and an encoding of the requested output length L. 
         * The result is then passed to cSHAKE, along with the requested output length L, 
         * the name N ="KMAC" = 11010010 10110010 10000010 110000109, 
         * and the optional customization string S. 
         */
        cinit256(KMAC_N, S);
        this.kmac = true;
        
        //way to update
        int j = this.pt;//*
		for (int i = 0; i < encodedK.length; i++) { //*
			this.b[j++] ^= encodedK[i]; // xor = ^ in java
			if (this.rsiz <= j) { //*
				sha3_keccakf(b);//*
				j = 0;//*
			}
		}
		
		this.pt = j;
		
        //update(encodedK, encodedK.length);
    }

    /**
     * Updating the shake function
     */
    public void update(byte[] data, int l) {
		int j = this.pt;
		for (int i = 0; i < l; i++) {
			this.b[j++] ^= data[i]; // si if change possible
			if (j >= this.rsiz) {
				sha3_keccakf(b);
				j = 0;
			}
		}
		this.pt = j;
	}
    static byte[] cSHAKE256(byte[] X, int L, byte[] N, byte[] S) throws IOException {
        // Validity Conditions: len(N) < 2^2040 and len(S) < 2^2040
        if ((L & 7) != 0) {
            throw new RuntimeException("Implementation restriction: output length (in bits) must be a multiple of 8");
        }
        byte[] val = new byte[L >>> 3];
        Shake shake = new Shake();
        shake.cinit256(N, S);
        shake.update(X, X.length);
        shake.xof();
        shake.out(val, L >>> 3);
        return val; // SHAKE256(X, L) or KECCAK512(prefix || X || 00, L)
    }

    /**
     * Compute the streamlined KMACXOF256 with key K on input X, with output bitlength L and customization string S.
     *
     * @param K     MAC key
     * @param X     data to be hashed
     * @param L     desired output length in bits
     * @param S     customization string
     * @return  the desired MAC tag
     * @throws IOException 
     */
    static byte[] KMACXOF256(byte[] K, byte[] X, int L, byte[] S) throws IOException {
        // Validity Conditions: len(K) < 2^2040 and 0 ≤ L and len(S) < 2^2040
        if ((L & 7) != 0) {
            throw new RuntimeException("Implementation restriction: output length (in bits) must be a multiple of 8");
        }
        byte[] val = new byte[L >>> 3];
        Shake shake = new Shake();
        shake.kinit256(K, S);
        shake.update(X, X.length);
        shake.xof();
        shake.out(val, L >>> 3);
        return val; // SHAKE256(X, L) or KECCAK512(prefix || X || 00, L)
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

