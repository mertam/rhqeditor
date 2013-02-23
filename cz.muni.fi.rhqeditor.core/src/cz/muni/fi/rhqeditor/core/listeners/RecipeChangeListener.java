package cz.muni.fi.rhqeditor.core.listeners;

import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import cz.muni.fi.rhqeditor.core.RhqConstants;
import cz.muni.fi.rhqeditor.core.RhqPathExtractor;
/**
 * class implemets listener used for trackting changes of file recipe.xml
 * @author syche
 *
 */


public class RecipeChangeListener implements IResourceChangeListener{

	private IProject fProject = null;
	private RhqPathExtractor fExtractor = null;
	private long fErrorMarkerId = 0;
	
	
	/**
	 * When resource is changed, listener calls listFiles() on given RhqPathExtractor 
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		
		IResourceDelta rootDelta = event.getDelta();

		IResourceDelta projDelta = rootDelta.findMember(new Path(fProject.getProject().getName()));
		
		//detects whether this event has influence on project
		if(projDelta != null){
			
			if(fExtractor.shouldBeListed())
				fExtractor.listFiles();
			
			
//			IResourceDelta[] deltas = projDelta.getAffectedChildren();
			Stack<IResourceDelta> deltas = new Stack<IResourceDelta>();
			for(IResourceDelta d: projDelta.getAffectedChildren()){
				deltas.push(d);
			}
			
			IResourceDelta currentDelta;
			//walks through all deltas
			while(!deltas.isEmpty()){
				currentDelta = deltas.pop();
				switch(currentDelta.getKind()){
				case IResourceDelta.ADDED: {
					IResource addedResource = currentDelta.getResource();
					IPath path = currentDelta.getFullPath();
					path = path.removeFirstSegments(1);
					if(addedResource instanceof IFile){
						if(addedResource.getName().endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX) ||
								addedResource.getName().endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX)){
							
							if(!fExtractor.getAbsolutePathsArchives().contains(path)){

								fExtractor.addArchive(path);
							}
							System.out.println("archive should be added");
						}else{
							fExtractor.addFile(path);
						}
					}
					
				};break;
				case IResourceDelta.CHANGED:
					System.out.println("changed");
					for(IResourceDelta d: currentDelta.getAffectedChildren()){
						deltas.push(d);
					}
					break;
				case IResourceDelta.LOCAL_CHANGED:  System.out.println("localchanged");break;
				case IResourceDelta.REMOVED:  
					System.out.println("removed");
					fExtractor.removeFile(currentDelta.getFullPath().removeFirstSegments(1));
					;break;
				case IResourceDelta.OPEN:  System.out.println("open");break;
				case IResourceDelta.CONTENT:  System.out.println("content");break;
				case IResourceDelta.REPLACED:  System.out.println("replaced");break;
				default: System.out.println("def");
				}
				
			}
		}
		

	}
	
	public void setExtractor(RhqPathExtractor ext){
		fExtractor = ext;
	}
	
	public void setProject(IProject proj){
		fProject = proj;
	}
	
	public boolean hasProject(){
		return (fProject == null ? false : true);
	}
	

}