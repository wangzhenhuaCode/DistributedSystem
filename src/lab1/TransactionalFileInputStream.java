package lab1;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fileName;
	private Integer readSize;
	private transient RandomAccessFile fin;
	public TransactionalFileInputStream(String fileName) {
		super();
		this.fileName = fileName;
		readSize=0;
		
		try {
			fin=new RandomAccessFile(fileName,"r");
			
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		if(fin==null){
			fin=new RandomAccessFile(fileName,"r");
			fin.seek(readSize);
		}
		readSize++;
		return fin.read();
	}
	@Override
	public void close() throws IOException{
		super.close();
		if(fin!=null)
			fin.close();
	}


}
