package lab1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable{
	
	
	private String fileName;
	private Integer readSize;
	private FileInputStream fin;
	public TransactionalFileInputStream(String fileName) {
		super();
		this.fileName = fileName;
		readSize=0;
		
		try {
			fin=new FileInputStream(fileName);
			
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		readSize++;
		return fin.read();
	}

	public TransactionalFileInputStream() {
		super();
		try {
			fin=new FileInputStream(fileName);
			fin.skip(readSize);
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
