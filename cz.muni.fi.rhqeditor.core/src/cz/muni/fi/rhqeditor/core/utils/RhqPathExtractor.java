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
 * This class provides functionality for extracting all files from project directory and all sub directories.
 * @author syche
 *
 */
public class RhqPathExtractor {
	
	/**
	 * list of all files in project. Doesn't contain zip and jar archives, these are saved separately.
	 */
	private List<IPath> 		fAbsolutePathsFiles 		= null;
	
	/**
	 *	concurent hash map contains <String path to archive, list of all files in archive> 
	 */
	private Map<IPath,ArrayList<IPath> >   fArchiveContent = null;
	
	/**
	 * IProject attached to this extractor
	 */
	private  IProject 			fProject					= null;

	/**
	 * serves as semaphore to avoid multiple listing of files in project
	 */
	AtomicBoolean 				fShouldBeJobScheduled		= null;
	
	/**
	 * comparetor used to sort IPath
	 */
	private PathComparator fPathComparator		= null;	
	
	private final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	/**
	 * 
	 * @param resource
	 */
	public RhqPathExtractor(IProject project){
		fProject = project;
		fPathComparator = new PathComparator();
		fAbsolutePathsFiles =  new ArrayList<IPath>();
		fArchiveContent = new ConcurrentHashMap<IPath, ArrayList<IPath>>();
		fShouldBeJobScheduled = new AtomicBoolean(true);
	}
		
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
		List<IPath> paths =  Collections.synchronizedList(new ArrayList<IPath>());
		for(IPath path: fArchiveContent.keySet()){
			paths.add(path);
		}
		Collections.sort(paths, fPathComparator);
		return paths;
	}

//	public List<IPath> getAbsolutePathsDirectories() {
//		Collections.sort(fAbsolutePathsDirectories, fPathComparator);
//		return  Collections.synchronizedList(fAbsolutePathsDirectories);
//	}
	
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
		List <IPath> files = fArchiveContent.get(new Path(archiveName));
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
		List<IPath> paths = fArchiveContent.get(new Path(archive));
		if(paths == null)
			return false;
		
		if(path.toString().contains("*")){
			return validateRelativePath(path.toString(), paths);
		}
		
		return paths.contains(path);
	
	}
	
	public boolean isPathToArchiveValid(IPath absolutePath){
		return fArchiveContent.containsKey(absolutePath);
	}
	
	public List<IPath> getAllFiles(){
		List<IPath> all = getAbsolutePathsArchives();
		all.addAll(getAbsolutePathsFiles());
		Collections.sort(all, fPathComparator);
		return all;
		
	}
	
	public List<IPath> getAllFilesByPrefix(String prefix){
		if(prefix == null || prefix.isEmpty())
			return getAllFiles();
		return getListAccordingToPrefix(getAllFiles(),prefix);
	}
	

	
	
	
	
	/**
	 * Method scans all files included in project and sorts them into categories
	 * @param resource - IResouce corresponding to file deploy.xml
	 * @throws URISyntaxException
	 */
	
	public void listFiles() {
		fAbsolutePathsFiles.clear();
		fArchiveContent.clear();
		
		fShouldBeJobScheduled.compareAndSet(true, false);
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
		}
		
		System.out.println("job for project done "+ fProject.getName());
		fShouldBeJobScheduled.compareAndSet(false, true);			

	}
	
	/**
	 * reloads content of given archive
	 */
	public void reloadArchive(IPath archive) {
		fArchiveContent.remove(archive);
		manageArchive(archive);
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
	}
	
	
	/**
	 * adds to fArchiveContent archive and all it's content. Runs in separated thread.
	 * @param pathToArchive
	 */
	private void manageArchive(final IPath pathToArchive){		
		
		Job listArchiveContent = new Job("listArchiveContent") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {

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
					fArchiveContent.put(pathToArchive, filesOfArchive);
				} catch (IOException e) {
					System.out.println("cannot open " + finalPath);
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				} 
				return Status.OK_STATUS;
			}	
	
		};
		listArchiveContent.schedule();
		
	}
	
	
	
	/**
	 * 
	 * @param expresion relative path from file, i.e. path="**\/*.txt
	 * @param possiblePaths list of path that should be searched
	 * @return true is at least one path from given list is valid
	 */
	private boolean validateRelativePath(String expresion, List<IPath> possiblePaths){
		
		String replacedExpresion = expresion.replaceAll("\\?", ".");
		replacedExpresion = replacedExpresion.replaceAll("\\*\\*",".*" );
		
		StringBuilder regexBuilder = new StringBuilder();
		for (int i = 0; i < replacedExpresion.length(); i++){
			if(replacedExpresion.charAt(i) == '*') {
				if(i > 0 && replacedExpresion.charAt(i-1) == '.') {
					continue;
				} else {
					regexBuilder.append("[^/]*");
				}
			} else {
				regexBuilder.append(replacedExpresion.charAt(i));
			}
		}
	
		
		String currentPath;
		for(IPath path: possiblePaths){
			currentPath = path.toString();
			if(currentPath.matches(replacedExpresion))
				return true;
		}
		
		return false;
	}
	
	
	/**
	 * removes single file from extractor
	 * @param file
	 */
	public void removeFile(IPath file){
		if(file.toString().endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX) ||
				file.toString().endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX)){
			fArchiveContent.remove(file);
		}else{
			fAbsolutePathsFiles.remove(file);
		}
	}
	
	/**
	 * Comparetor of IPath, two paths are equal iff their toString() value is equal. Path1 is greater than Path2 iff it's toString() is alfabetically
	 * @author syche
	 *
	 */
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
	public void updatePaths(IPath formerPath, IPath newPath){
		//update archives
		 ArrayList<IPath> temp;
		 for(Iterator<IPath> i = fArchiveContent.keySet().iterator(); i.hasNext();) {
			 IPath currentPath = i.next();
		     if(formerPath.isPrefixOf(currentPath)){
					temp = fArchiveContent.get(currentPath);
					fArchiveContent.remove(currentPath);
					fArchiveContent.put(newPath.append(currentPath.removeFirstSegments(formerPath.segmentCount())), temp);
				}
		 }
		
		//update files
		int index = 0;
		for(Iterator<IPath> i = fAbsolutePathsFiles.iterator(); i.hasNext();) {
			IPath currentPath = i.next();
			if(formerPath.isPrefixOf(currentPath)) {
				fAbsolutePathsFiles.set(index, newPath.append(currentPath.removeFirstSegments(formerPath.segmentCount())));
			}
			index++;
		}
	}
	
	/**
	 * removes folder and all it's content from extractor
	 * @param folder
	 */
	public void removeFolder(IPath folder){
		
		
		 IPath currentPath;
		 for(Iterator<IPath> i = fAbsolutePathsFiles.iterator(); i.hasNext();) {
		     currentPath = i.next();
		     if(folder.isPrefixOf(currentPath))
		       i.remove();
		 }
		

		 for(Iterator<IPath> i = fArchiveContent.keySet().iterator(); i.hasNext();) {
		     currentPath = i.next();
		     if(folder.isPrefixOf(currentPath))
		       i.remove();
		 }
		
	}
	
	
	/**
	 * adds folder and all it's content into extractor
	 * @param folderName
	 */
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
