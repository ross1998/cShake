
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class Main {
	
	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	static Shake shake;
	static KMACXOF256 kmacx;
	public static void main(String[] args) throws IOException {
		
//		String s = "Email Signature";
//		byte[] b = s.getBytes(StandardCharsets.US_ASCII);
//		//System.out.println(b.length);
////		byte[] b = new byte[4] ;
////		b[0] = 00;
////		b[1] = 01;
////		b[2] = 02;
////		b[3] = 03;
//		
//		//byte[] a = left_encode(49);
//		//byte[] c = new byte[5];
//		
//		byte[] c = bytepad(encode_string(b), 136);
//		
//		for(int i = 0; i < c.length; i++) {
//			
//			System.out.println(String.format("0x%02X", c[i]));
//		}
		//shake = new Shake();
		//shake = new Shake();
		kmacx = new KMACXOF256();
		byte[] S; //diversification string
        byte[] K; //key
        byte[] M; //message
        
        S = "D".getBytes(StandardCharsets.US_ASCII); //D is the diversification string for this action.
        K = "".getBytes(StandardCharsets.US_ASCII); //K is the key which we are not using for this so it is empty.

        //JFileChooser myChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        //int retValue = myChooser.showOpenDialog(null);
        //File selectedFile = null;

        //if (retValue == JFileChooser.APPROVE_OPTION) {
            //selectedFile = myChooser.getSelectedFile();
            //Path path = Paths.get(selectedFile.getAbsolutePath());
           // try {
                //M = Files.readAllBytes(path);
        		M = "Hello World".getBytes();
                byte[] message = shake.KMACXOF256(K, M, 512, S);
                System.out.println("SHA3 result: " + generateHexFromByteArray(message));
                //System.out.println("SHA3 result: " + generateHexFromByteArray(message));
            //} catch (Exception e) { e.printStackTrace(); }

	}

    public static String generateHexFromByteArray(byte[] byteArray) {
        final int length = byteArray.length;
        final char[] returnMe = new char[length << 1];

        for (int i = 0, j = 0; i < length; i++) {
            returnMe[j++] = DIGITS[(0xF0 & byteArray[i]) >>> 4];
            returnMe[j++] = DIGITS[0x0F & byteArray[i]];
        }

        return new String(returnMe);
    }

	//}

}
