package lab1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fileName;
	private Integer readSize;
	private FileInputStream fin;
	public TransactionalFileInputStream(String fileName,Integer readSize) {
		super();
		this.fileName = fileName;
		//readSize=0;
		this.readSize=readSize;
		try {
			fin=new FileInputStream(fileName);
			fin.skip(readSize);
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		readSize++;
		System.out.println(readSize);
		return fin.read();
	}

}
