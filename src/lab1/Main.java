package lab1;

import java.io.DataInputStream;
import java.io.IOException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TransactionalFileInputStream in=new TransactionalFileInputStream("c:\\test.txt",4);
		DataInputStream is = new DataInputStream(in);
		try {
			int b=is.read();
			while(b!=-1){
				System.out.print(b+" ");
				b=is.read();
			};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
