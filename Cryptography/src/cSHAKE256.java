/**
 * Implementation of the SHA-3 Algorithm for encryption.
 * @author Ross Martsenyak
 *
 */
/**
 * 
 * X is the main input bit string. It may be of any length3
, including zero.
• L is an integer representing the requested output length4 in bits.
• N is a function-name bit string, used by NIST to define functions based on cSHAKE.
When no function other than cSHAKE is desired, N is set to the empty string.
• S is a customization bit string. The user selects this string to define a variant of the
function. When no customization is desired, S is set to the empty string5
 *
 */


public class cSHAKE256 {
	
	
	public cSHAKE256() {
		
		
		
	}
	
//	void bytePad(X, W){
//		
//	}
	
	/**
right_encode(x):
Validity Conditions: 0 ≤ x < 22040
1. Let n be the smallest positive integer for which 28n > x.
2. Let x1, x2,…, xn be the base-256 encoding of x satisfying:
x = ∑ 28(n-i)
xi, for i = 1 to n.
3. Let Oi = enc8(xi), for i = 1 to n.
4. Let On+1 = enc8(n).
5. Return O = O1 || O2 || … || On || On+1.
	 */
	
	/**
left_encode(x):
Validity Conditions: 0 ≤ x < 22040
1. Let n be the smallest positive integer for which 28n > x.
2. Let x1, x2, …, xn be the base-256 encoding of x satisfying:
x = ∑ 28(n-i)
xi, for i = 1 to n.
3. Let Oi = enc8(xi), for i = 1 to n.
4. Let O0 = enc8(n).
5. Return O = O0 || O1 || … || On−1 || On.
As an example, right_encode(0) will yield 00000000 10000000, and left_encode(0) will
yield 10000000 00000000.
	 */
}
