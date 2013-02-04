package lab1;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileProcess implements MigratableProcess {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	private String unzippedFile;
	private String zippedFile;
	private volatile boolean suspending;

	public ZipFileProcess(String unzipFilename, String zippedFilename) throws Exception {
		
		unzippedFile = unzipFilename;
		zippedFile = zippedFilename;
		inFile = new TransactionalFileInputStream(unzippedFile);
		outFile = new TransactionalFileOutputStream(zippedFile, false);
		suspending = false;
	}

	@Override
	public void run() {

		try {
			ZipOutputStream zos = new ZipOutputStream(outFile);
			zos.putNextEntry(new ZipEntry(unzippedFile));
			int size;
			byte[] buffer = new byte[1024];
			while (!suspending) {

				if ((size = inFile.read(buffer, 0, buffer.length)) > 0) {
					zos.write(buffer, 0, size);
				} else {
					break;
				}

			}

			zos.closeEntry();
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
