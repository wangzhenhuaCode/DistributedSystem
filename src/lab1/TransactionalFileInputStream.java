package lab1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable{
	
	
	private String fileName;
	private Integer readSize;
	private transient FileInputStream fin;
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
		if(fin==null){
			fin=new FileInputStream(fileName);
			fin.skip(readSize);
		}
		readSize++;
		return fin.read();
	}
	@Override
	public void close() throws IOException{
		super.close();
		fin.close();
	}


}
