package cz.muni.fi.rhqeditor.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cz.muni.fi.rhqeditor.core.utils.RhqConstants;

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
		}
		return false;

	}

	public static void unzipFile(String pathToArchive, String target)  throws IOException{
	
		ZipFile zipFile = new ZipFile(pathToArchive);

		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();

			if (entry.isDirectory()) {
				(new File(target + RhqConstants.FILE_SEPARATOR
						+ entry.getName())).mkdir();
				continue;
			}

			OutputStream out = new FileOutputStream(target
					+ RhqConstants.FILE_SEPARATOR + entry.getName());
			copyInputStream(zipFile.getInputStream(entry), out);
			out.close();
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
