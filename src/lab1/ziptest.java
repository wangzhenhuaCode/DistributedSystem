package lab1;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ziptest {
	 public static void main(String[] args) {
	        String source = "data.txt";
	        String target = "data.zip";
	 
	        try {
	            ZipOutputStream zos = new ZipOutputStream(
	                    new FileOutputStream(target));
	 
	            //
	            // Create input stream to read file from resource folder.
	            //
	            Class clazz = ziptest.class;
	            InputStream is = clazz.getResourceAsStream("/" + source);
	 
	            //
	            // Put a new ZipEntry in the ZipOutputStream
	            //
	            zos.putNextEntry(new ZipEntry(source));
	 
	            int size;
	            byte[] buffer = new byte[1024];
	 
	            //
	            // Read data to the end of the source file and write it
	            // to the zip output stream.
	            //
	            while ((size = is.read(buffer, 1, 1)) > 0) {
	                zos.write(buffer, 1, 1);
	            }
	 
	            zos.closeEntry();
	            is.close();
	 
	            //
	            // Finish zip process
	            //
	            zos.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
}
