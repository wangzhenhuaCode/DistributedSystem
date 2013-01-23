package lab1;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{
	private String filename;
	private FileOutputStream fout;
	private static final long serialVersionUID = 1L;


	
	public TransactionalFileOutputStream(String filename, boolean append) throws IOException{
		super();
		this.filename = filename;
		try {
			fout=new FileOutputStream(filename,append);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			throw e;
		}
	}
	@Override
	public void write(int arg0) throws IOException {
		// TODO Auto-generated method stub
		fout.write(arg0);
	}
	public TransactionalFileOutputStream() throws IOException{
		super();
		try {
		fout=new FileOutputStream(filename,false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

}
