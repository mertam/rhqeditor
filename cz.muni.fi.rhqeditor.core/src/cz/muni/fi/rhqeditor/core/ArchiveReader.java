package cz.muni.fi.rhqeditor.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveReader {
	
	private String getFileContent(String pathToArchive, String fileName){
		try {
			ZipFile archive = new ZipFile(pathToArchive);
			ZipEntry entry = archive.getEntry(fileName);
			if(entry == null){
				archive.close();
				return null;	
			}
			InputStream inputStream = archive.getInputStream(entry);
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			copyInputStream(inputStream, bOut);
			String content = bOut.toString();
			inputStream.close();
			bOut.close();
			archive.close();
			return content;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	 private void copyInputStream(InputStream in, OutputStream out) throws IOException{
	    	byte buf[] = new byte[1024];
			int read;	
	    	
	    	while((read = in.read(buf)) > -1){
				out.write(buf,0,read);
			}
	    }
}
