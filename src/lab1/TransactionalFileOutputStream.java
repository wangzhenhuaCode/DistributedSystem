package lab1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{
	private String filename;
	private boolean result;
	
	

	
	public TransactionalFileOutputStream(String filename, boolean result){
		super();
		this.filename = filename;
		this.result = result;
	}
	@Override
	public void write(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
