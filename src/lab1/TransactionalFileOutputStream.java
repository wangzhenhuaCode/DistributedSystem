package lab1;


import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{
	private String filename;
	private transient RandomAccessFile fileAcess;
	private static final long serialVersionUID = 1L;
	private long readed;


	
	public TransactionalFileOutputStream(String filename, boolean append) throws IOException{
		super();
		this.filename = filename;
		readed=0;
		try {
			fileAcess=new RandomAccessFile(filename,"rw");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			throw e;
		}
	}
	@Override
	public void write(int arg0) throws IOException {
		// TODO Auto-generated method stub
		if(fileAcess==null){
			fileAcess=new RandomAccessFile(filename,"rw");
			fileAcess.seek(readed);
		}
		fileAcess.write(arg0);
		readed++;
	}
	@Override
	public void close() throws IOException{
		
			super.close();
			if(fileAcess!=null)
				fileAcess.close();
		
	}
	

}
