package lab1;

import java.io.IOException;
import java.io.InputStream;

public class TransactionalFileInputStream extends InputStream {
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
