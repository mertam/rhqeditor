package cz.muni.fi.rhqeditor.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import cz.muni.fi.rhqeditor.core.Activator;


public class ArchiveReader {

	private String fArchivePath;

	public ArchiveReader(String archiveName) {
		fArchivePath = archiveName;
	}

	private String getFileContent(String pathToArchive, String fileName)
			throws IOException {
		ZipFile archive = new ZipFile(pathToArchive);
		ZipEntry entry = archive.getEntry(fileName);
		if (entry == null) {
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

	}

	/**
	 * Checks whether is archive on given path RHQ bundle.
	 * 
	 * @param file
	 * @return true if contains deploy.xml
	 */
	public static boolean isBundle(File file) {
		
		try (ZipFile archive = new ZipFile(file, ZipFile.OPEN_READ);) {
			ZipEntry entry = archive.getEntry(RhqConstants.RHQ_RECIPE_FILE);
			return entry != null;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(file);
			Activator.getLog().log(new Status(IStatus.WARNING,RhqConstants.PLUGIN_CORE_ID,"ERROR while opening file +" +file+" " + e.getMessage()));
		}
		return false;

	}

	
	
	/**
	 * unzips given file to target location
	 * @param pathToArchive
	 * @param target
	 * @param temporary if true, file is marked as deleteOnExit
	 * @throws IOException
	 */
	public static void unzipArchive(String pathToArchive, String target, boolean temporary)  throws IOException{
	
		ZipFile zipFile = new ZipFile(pathToArchive);

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();

			if (entry.isDirectory()) {
				File directory = new File(target + RhqConstants.FILE_SEPARATOR+ entry.getName());
				directory.mkdir();
				if(temporary) {
					directory.deleteOnExit();
				}
				continue;
			}

			File newFile = new File(target
					+ RhqConstants.FILE_SEPARATOR + entry.getName());
			OutputStream out = new FileOutputStream(newFile);
			copyInputStream(zipFile.getInputStream(entry), out);
			out.close();
			if(temporary) {
				newFile.deleteOnExit();
			}
		}

		zipFile.close();

		return;
		
	}

	public static void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte buf[] = new byte[1024];
		int read;

		while ((read = in.read(buf)) > -1) {
			out.write(buf, 0, read);
		}
	}

	public String getRecipe() throws IOException {
		return getFileContent(fArchivePath, RhqConstants.RHQ_RECIPE_FILE);
	}
}
