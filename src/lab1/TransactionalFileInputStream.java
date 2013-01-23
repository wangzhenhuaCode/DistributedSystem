package lab1;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7779242789718779893L;
	private String fileName;
	public TransactionalFileInputStream(String fileName) {
		super();
		this.fileName = fileName;
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		
		return 0;
	}

}
