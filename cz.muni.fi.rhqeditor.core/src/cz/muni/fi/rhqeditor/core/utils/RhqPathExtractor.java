package cz.muni.fi.rhqeditor.core.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


/**
 * This class provides functionality for extracting all files from project directory and all sub directories
 * @author syche
 *
 */
public class RhqPathExtractor {
	
	private List<IPath> 		fAbsolutePathsFiles 		= null;
//	private ArrayList<IPath> 	fAbsolutePathsArchives 		= null;
	private ArrayList<IPath> 	fAbsolutePathsDirectories 	= null;
	//resource 
	private IProject 			fProject					= null;
	private PathComparator 		fPathComparator				= null;
	private Map<String,ArrayList<IPath> >   fArchiveContent = null;
	
	AtomicBoolean 				fShouldBeJobScheduled		= null;
	
	
	
	private String FILE_SEPARATOR = System.getProperty("file.separator");
	
	/**
	 * 
	 * @param resource
	 */
	public RhqPathExtractor(IProject project){
		fProject = project;
		fPathComparator = new PathComparator();
		fAbsolutePathsFiles =  new ArrayList<IPath>();
		fAbsolutePathsDirectories = new ArrayList<IPath>();
//		fAbsolutePathsArchives = new ArrayList<IPath>();
		fArchiveContent = new ConcurrentHashMap<String, ArrayList<IPath>>();
		fShouldBeJobScheduled = new AtomicBoolean(true);
	}
	
//	public void setResource(IResource resource){
//		fRecipeResource = resource;
//	}
	
	
	
	public boolean hasResource(){
		return (fProject != null ? true : false);
	}
	
	public IProject getProject(){
		return fProject;
	}
	/**
	 * returns sorted list of Paths to all no-archives files in project
	 * @return
	 */
	public List<IPath> getAbsolutePathsFiles() {
		Collections.sort(fAbsolutePathsFiles, fPathComparator);
		return  Collections.synchronizedList(fAbsolutePathsFiles);
	}
	/**
	 * returns sorted list of Path to all archives in project
	 * @return
	 */
	public List<IPath> getAbsolutePathsArchives() {
		List<IPath> paths=  Collections.synchronizedList(new ArrayList<IPath>());
		for(String s: fArchiveContent.keySet()){
			paths.add(new Path(s));
		}
		Collections.sort(paths, fPathComparator);
		return paths;
	}

	public List<IPath> getAbsolutePathsDirectories() {
		Collections.sort(fAbsolutePathsDirectories, fPathComparator);
		return  Collections.synchronizedList(fAbsolutePathsDirectories);
	}
	
	public List<IPath> getAbsolutePathsFilesByPrefix(String prefix){
		if(prefix == null || prefix.isEmpty())
			return getAbsolutePathsFiles();
		return getListAccordingToPrefix(getAbsolutePathsFiles(),prefix);
	}
	
	public List<IPath> getAbsolutePathsArchivesByPrefix(String prefix){
		if(prefix == null || prefix.isEmpty())
			return getAbsolutePathsArchives();
		return getListAccordingToPrefix(getAbsolutePathsArchives(),prefix);
	}
	
	public List<IPath> getContentOfArchiveByPrefix(String archiveName, String prefix){
		if(prefix == null || prefix.isEmpty())
			return getContentOfArchive(archiveName);
		
		List<IPath> files = fArchiveContent.get(archiveName);
		if(files == null)
			return Collections.emptyList();
		return getListAccordingToPrefix(files, prefix);
	}
	
	public List<IPath> getContentOfArchive(String archiveName){
		List <IPath> files = fArchiveContent.get(archiveName);
		if(files == null)
			return Collections.emptyList();
		return  Collections.synchronizedList(files);
	}
	
	/**
	 * checks whether should be listed all project
	 * @return
	 */
	public boolean shouldBeListed(){
		return fAbsolutePathsFiles.isEmpty() && fArchiveContent.isEmpty();
		//		return fAbsolutePathsFiles.isEmpty() && fAbsolutePathsArchives.isEmpty();
	}
	
	/**
	 * returns list containing all values matching prefix
	 * @param paths
	 * @param prefix
	 * @return
	 */
	private List<IPath> getListAccordingToPrefix(List<IPath> paths, String prefix){
		
		int startIndex = -1;
		int endIndex = -1;
				
		for(int i = 0; i!= paths.size(); i++){
			if(startIndex < 0 && paths.get(i).toString().startsWith(prefix)){
				startIndex = i;
				endIndex = i;
				break;
			}
		}
		
		//no matching files found
		if(startIndex < 0)
			return Collections.emptyList();
		
		
		for(int i = startIndex; i!=paths.size(); i++){
			if(paths.get(i).toString().startsWith(prefix))
				endIndex++;
			else
				break;
		}		
		return  Collections.synchronizedList(paths.subList(startIndex, endIndex));
		
	}
	
	public void addFile(IPath path){
		fAbsolutePathsFiles.add(path);
	}
	
	public void addArchive(IPath path){
//		fAbsolutePathsArchives.add(path);
		manageArchive(path);
	}
	public boolean isPathToFileValid(IPath abslutePath){
		if(abslutePath == null)
			return false;
		return fAbsolutePathsFiles.contains(abslutePath);
	}
	
	public boolean isPathToArchiveFileValid(IPath path, String archive){
		if(path == null || archive == null)
			return false;
		List<IPath> paths = fArchiveContent.get(archive);
		if(paths == null)
			return false;
		
		if(path.toString().contains("*")){
			return validateRelativePath(path.toString(), paths);
		}
		
		return paths.contains(path);
	
	}
	
	public boolean isPathToArchiveValid(IPath absolutePath){
		return fArchiveContent.containsKey(absolutePath.toString());
	}
	
	public List<IPath> getAllFiles(){
		List<IPath> all = getAbsolutePathsArchives();
		all.addAll(getAbsolutePathsFiles());
		Collections.sort(all, fPathComparator);
		return all;
		
	}
	

	
	
	
	
	/**
	 * Method scans all files included in project and sorts them into categories
	 * @param resource - IResouce corresponding to file deploy.xml
	 * @throws URISyntaxException
	 */
	
	public void listFiles() {
//		fAbsolutePathsArchives.clear();
		fAbsolutePathsFiles.clear();
		fAbsolutePathsDirectories.clear();
		fArchiveContent.clear();
		
		Job listingJob = new Job("listing"){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				System.out.println("job for project starts "+ fProject.getName());
				try{
					IResource[] res = fProject.getProject().members();
					
					Stack<IFolder> folders = new Stack<IFolder>();
					IFolder temp = null;
					
		//iterates over files in project directory
					for(IResource forResource : res){
						if(forResource instanceof IFolder){
						//ignore content of /.bin/ and /build/
							if(!forResource.getName().equals(".bin") && !forResource.getName().equals("build"))
								folders.push((IFolder)forResource);
						}
					else
						manageResource(forResource);
					}
		//iterates over directories inside parent project
					while(!folders.isEmpty()){
						temp = folders.pop();
						for(IResource forResource : temp.members()){
							if(forResource instanceof IFolder)
								folders.push((IFolder)forResource);
							else
								manageResource(forResource);
						}
					}
				
				} catch (CoreException e) {
					fShouldBeJobScheduled.compareAndSet(false, true);
					return Status.CANCEL_STATUS;
//					e.printStackTrace();
				}
				
				
//				for(IPath p: fAbsolutePathsArchives){
//					manageArchive(p);
//				}
				
				System.out.println("job for project done "+ fProject.getName());
				fShouldBeJobScheduled.compareAndSet(false, true);
				return Status.OK_STATUS;
				}
			};
		
			listingJob.setPriority(Job.LONG);
			//job is scheduled only if no ohter job is running
			if(fShouldBeJobScheduled.compareAndSet(true, false))
				listingJob.schedule();
			

	}
	
	
	/**
	 * Method places resource into corresponding set
	 * @param resource
	 * @throws URISyntaxException 
	 */
	private void manageResource(IResource resource){
		IPath path = resource.getFullPath().removeFirstSegments(1);
		if(path == null || path.isEmpty())
			return;
		
		if(resource instanceof IFile) {
			if(path.toString().endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX) ||
				path.toString().endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX))
			{
				manageArchive(path);
			}else{
				fAbsolutePathsFiles.add(path);
			}
			return;
		}
		fAbsolutePathsDirectories.add(path);
	}
	
	
	
	private void manageArchive(IPath pathToArchive){		
		IFile localFile = fProject.getFile(pathToArchive);
		String finalPath = localFile.getLocation().toString();

		File file = new File(finalPath);
		ZipEntry ze;
		ArrayList<IPath> filesOfArchive = fArchiveContent.get(pathToArchive);
		
		if(filesOfArchive == null)
			filesOfArchive = new ArrayList<IPath>();
		else
			filesOfArchive.clear();
		
		try(
			ZipFile archive = new ZipFile(file, ZipFile.OPEN_READ);
			) {
			Enumeration<? extends ZipEntry> entries = archive.entries();
			
			while(entries.hasMoreElements()){
				ze = (ZipEntry)entries.nextElement();
				filesOfArchive.add(new Path(ze.getName()));
			}
			
			Collections.sort(filesOfArchive,fPathComparator);
			fArchiveContent.put(pathToArchive.toString(), filesOfArchive);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * 
	 * @param expresion relative path from file, i.e. path="**\/*.txt
	 * @param possiblePaths list of path that should be searched
	 * @return true is at least one path from given list is valid
	 */
	private boolean validateRelativePath(String expresion, List<IPath> possiblePaths){
		
		StringBuilder buf = new StringBuilder();
		String [] splitted = expresion.split(FILE_SEPARATOR);
		for(String s: splitted){
			if(s.equals("**")){
				buf.append(".*");
				buf.append(FILE_SEPARATOR);
				continue;
			}
			if(s.equals("*")){
				buf.append("\\w*");
				buf.append(FILE_SEPARATOR);
				continue;
			}
			
			if(s.matches("\\w*")){
				buf.append(s);
				buf.append(FILE_SEPARATOR);
				continue;
			}
			
			if(s.matches("[[\\w\\.]\\*]+|[\\*]\\w\\.]]+")){
				for(String s2: s.split("\\*")){
					if(!s2.isEmpty()){
						s2 = s2.replaceAll("\\.", "\\\\.");
						buf.append(s2);
					}
					buf.append(".*");
				}
				buf = new StringBuilder(buf.subSequence(0, buf.length() - ".*".length()));
				buf.append(FILE_SEPARATOR);
				continue;
			}
		}
		//cheks whether there is additonal FILE_SEPARATOR
		if(buf.length() > 0)
			buf = new StringBuilder(buf.subSequence(0, buf.length() - FILE_SEPARATOR.length()));
		
		
		String currentPath;
		for(IPath path: possiblePaths){
			currentPath = path.toString();
			if(currentPath.matches(buf.toString()))
				return true;
		}
		
		return false;
	}
	
	public void removeFile(IPath file){
		if(file.toString().endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX) ||
				file.toString().endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX)){
			fArchiveContent.remove(file.toString());
//			fAbsolutePathsArchives.remove(file);
		}else{
			fAbsolutePathsFiles.remove(file);
		}
	}
	
	private class PathComparator implements Comparator<IPath>{

		@Override
		public int compare(IPath o1, IPath o2) {
			return o1.toString().compareTo(o2.toString());
		}
		
	}
	
	
	/**
	 * updates all paths starting with formerPath/... to newPath/...
	 * @param formerPath
	 * @param newPath
	 */
	public void updatePaths(String formerPath, String newPath){
		//update archives
		 ArrayList<IPath> temp;
		 for(Iterator<String> i = fArchiveContent.keySet().iterator(); i.hasNext();) {
			 String name = i.next();
			 String newName = name.replaceFirst(formerPath, newPath);
		     if(name.startsWith(formerPath)){
					temp = fArchiveContent.get(name);
					fArchiveContent.remove(name);
					fArchiveContent.put(newName, temp);
				}
		 }
		
		//update files
		int index = 0;
		for(Iterator<IPath> i = fAbsolutePathsFiles.iterator(); i.hasNext();) {
			String currentPath = i.next().toString();
			if(currentPath.startsWith(formerPath)){
				String newName = currentPath.toString().replaceFirst(formerPath, newPath);
				fAbsolutePathsFiles.set(index, new Path(newName));
			}
			index++;
		}
	}
	
	/**
	 * removes folder and all content from extractor
	 * @param folder
	 */
	public void removeFolder(IPath folder){
		
		
		 IPath currentPath;
		 for(Iterator<IPath> i = fAbsolutePathsFiles.iterator(); i.hasNext();) {
		     currentPath = i.next();
		     if(folder.isPrefixOf(currentPath))
		       i.remove();
		 }
		

		 for(Iterator<String> i = fArchiveContent.keySet().iterator(); i.hasNext();) {
		     currentPath = new Path(i.next());
		     if(folder.isPrefixOf(currentPath))
		       i.remove();
		 }
		
	}
	
	public void addFolder(IPath folderName){
		IFolder folder = fProject.getFolder(folderName);
		if(folder == null)
			return;
		try{
		Stack<IFolder> stack = new Stack<>();
		stack.push(folder);
		
		IFolder temp;
		while(!stack.empty()){
			temp = stack.pop();
			
			for(IResource resource: temp.members()){
				if(resource instanceof IFolder)
					stack.push((IFolder) resource);
				if(resource instanceof IFile)
					manageResource(resource);
			}
		}
		} catch (CoreException ex){
			ex.printStackTrace();
		}
	}
	
	
		
	
}
