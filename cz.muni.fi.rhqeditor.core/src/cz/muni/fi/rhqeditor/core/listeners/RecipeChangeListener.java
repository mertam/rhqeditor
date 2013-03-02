package cz.muni.fi.rhqeditor.core.listeners;

import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import utils.ExtractorProvider;
import utils.RhqConstants;
import utils.RhqPathExtractor;
import cz.muni.fi.rhqeditor.core.ProjectScanner;
/**
 * listener used to tracking changes of resources in project
 * @author syche
 *
 */


public class RecipeChangeListener implements IResourceChangeListener{

	
	/**
	 * When resource is chan
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		
		IProject project 					 = null;
		RhqPathExtractor extractor			 = null;
		IResourceDelta rootDelta = event.getDelta();
		
		
		
		//do nothing if project is closing
		System.out.println(event.getType() +" " + IResourceChangeEvent.PRE_CLOSE);
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE)
			return;
		
		
		IResource changedResource = rootDelta.getResource();
		
		if(changedResource.getType() == IResource.ROOT){
			IResourceDelta[] del = rootDelta.getAffectedChildren();
			if(del.length == 1 && del[0].getResource().getType() == IResource.PROJECT)
				project = (IProject) del[0].getResource();
		}
		
		//check out whether is project type of RHQ
		//check out whether is project open (during creating project this listener is notified and can receive change on closed project)
		//terminate on failure
		try {
			if(project == null || !project.isOpen() ||!project.hasNature(RhqConstants.RHQ_NATURE_ID))
				return;
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
		
		ExtractorProvider provider = ExtractorProvider.getInstance();
		extractor = provider.getMap().get(project);
		
		
		//this should happen when project is opened for first time
		if(extractor == null){
			ProjectScanner scan = new ProjectScanner();
			scan.initProject(project);
			//get extractor again
			extractor = provider.getMap().get(project);
		}
			
		//list all files in new job, used for opening projects
		if(extractor.getAllFiles().isEmpty()){
			extractor.listFiles();
		}
		
		IResourceDelta projDelta = rootDelta.findMember(new Path(project.getProject().getName()));
		//stack used for storing delta tree
		Stack<IResourceDelta> stackDelta = new Stack<IResourceDelta>();
		for(IResourceDelta d: projDelta.getAffectedChildren()){
			stackDelta.push(d);
		}
			
		IResourceDelta currentDelta;
		//go through all deltas
		while(!stackDelta.isEmpty()){
			currentDelta = stackDelta.pop();
			switch(currentDelta.getKind()){
			case IResourceDelta.ADDED: 
				
				IResource addedResource = currentDelta.getResource();
				IPath path = currentDelta.getFullPath();
				path = path.removeFirstSegments(1);
				if(addedResource instanceof IFile){
					if(addedResource.getName().endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX) ||
							addedResource.getName().endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX)){
						
						if(!extractor.getAbsolutePathsArchives().contains(path)){

							extractor.addArchive(path);
						}
						System.out.println("archive should be added");
					}else{
						extractor.addFile(path);
					}
				}
				break;
				
			case IResourceDelta.CHANGED:
				System.out.println("changed");
				for(IResourceDelta d: currentDelta.getAffectedChildren()){
					stackDelta.push(d);
				}
				if((currentDelta.getFlags() & IResourceDelta.MOVED_FROM) > 0)
					System.out.println("MOVED");
				break;
			
			case IResourceDelta.REMOVED:  
				System.out.println("removed");
				extractor.removeFile(currentDelta.getFullPath().removeFirstSegments(1));
				;break;
				
//			case IResourceDelta.OPEN:  System.out.println("open");break;
//			case IResourceDelta.CONTENT:  System.out.println("content");break;
//			case IResourceDelta.REPLACED:  System.out.println("replaced");break;
			default: System.out.println("def");
			}
			
		}
	}
	


//	
//	public void setExtractor(RhqPathExtractor ext){
//		fExtractor = ext;
//	}
//	
//	public void setProject(IProject proj){
//		fProject = proj;
//	}
//	
//	public boolean hasProject(){
//		return (fProject == null ? false : true);
//	}
	

}