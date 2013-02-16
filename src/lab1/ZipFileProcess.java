package lab1;

import java.io.IOException;

import java.util.zip.GZIPOutputStream;

public class ZipFileProcess implements MigratableProcess {
	/*
	 * This class is a migratable process for zipping file.
	 */
	private static final long serialVersionUID = 1L;
	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	private String unzippedFile;
	private String zippedFile;
	private volatile boolean suspending;

	public ZipFileProcess(String[] args)throws Exception {
		
		if(args.length!=2){
			throw new Exception("Invalid Arguments, please input two argument <unzipfilename> <zipfilename>");

		}
		unzippedFile = args[0];
		zippedFile = args[1];
		inFile = new TransactionalFileInputStream(unzippedFile);
		outFile = new TransactionalFileOutputStream(zippedFile, false);
		suspending = false;
	}

	@Override
	public void run() {

		try {
			GZIPOutputStream zos = new GZIPOutputStream(outFile);
			
			int size;
			
			while (!suspending) {
				byte[] buffer = new byte[1024];
				if ((size = inFile.read(buffer, 0, buffer.length)) > 0) {
					zos.write(buffer, 0, size);
				} else {
					break;
				}
				zos.flush();
				
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			

			
			zos.close();
			inFile.close();
			outFile.close();
			suspending = false;
		} catch (IOException e) {
			System.out.println("ZipFileProcess: Error: " + e);
		}

	}

	@Override
	public void suspend() {
		suspending = true;

	}

}
