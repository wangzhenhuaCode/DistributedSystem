package lab1;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileProcess implements MigratableProcess{
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String unzippedFile;
	private String zippedFile;
	private volatile boolean suspending;
	
	public ZipFileProcess(String[] args) throws Exception{
		if (args.length != 2) {
			System.out.println("usage: ZipProcessProcess <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		unzippedFile = args[0];
		zippedFile = args[1];
		inFile = new TransactionalFileInputStream(unzippedFile);
		outFile = new TransactionalFileOutputStream(zippedFile, false);
	}
	@Override
	public void run() {		
		
		Class name = ZipFileProcess.class;
		inFile = (TransactionalFileInputStream) name.getResourceAsStream(unzippedFile);
		
		try{
			while(!suspending){
				ZipOutputStream zos = new ZipOutputStream(outFile);
	        	
	        	zos.putNextEntry(new ZipEntry(unzippedFile));
	        	int size;
	        	byte[] buffer = new byte[1024];
	        	
	        	while((size = inFile.read(buffer, 0, buffer.length)) > 0){
	        		zos.write(buffer, 0, size);
	        	}
	        	
	        	zos.closeEntry();
	        	inFile.close();
	        	zos.close();
			}
			
			
        }catch(IOException e){
        	System.out.println ("ZipFileProcess: Error: " + e);
        }
		
	}

	@Override
	public void suspend() {
		suspending = true;
		while (suspending);
		
	}

}
