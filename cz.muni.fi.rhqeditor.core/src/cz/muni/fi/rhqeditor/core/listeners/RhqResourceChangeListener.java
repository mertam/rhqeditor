package cz.muni.fi.rhqeditor.core.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import cz.muni.fi.rhqeditor.core.ProjectInitializer;
import cz.muni.fi.rhqeditor.core.launch.LaunchConfigurationsManager;
import cz.muni.fi.rhqeditor.core.utils.ExtractorProvider;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;
import cz.muni.fi.rhqeditor.core.utils.RhqPathExtractor;
import cz.muni.fi.rhqeditor.core.utils.RhqRecipeContentChange;

/**
 * listener used to tracking changes of resources in project
 * 
 * @author syche
 * 
 */

public class RhqResourceChangeListener implements IResourceChangeListener {

	// refactoring purposes
	private ArrayList<IPath> fAddedFolders = new ArrayList<>();
	private ArrayList<IPath> fDeletedFolders = new ArrayList<>();
	private ArrayList<IPath> fAddedFiles = new ArrayList<>();
	private ArrayList<IPath> fDeletedFiles = new ArrayList<>();
	private Map<IPath, RefactoredPair> fRefactoredMap = new HashMap<IPath,RefactoredPair>();
	

	/**
	 * When resource is chan
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		fAddedFiles.clear();
		fAddedFolders.clear();
		fDeletedFiles.clear();
		fDeletedFolders.clear();
		fRefactoredMap.clear();
		fRefactoredMap.clear();
		IProject project = null;
		IResourceDelta rootDelta = event.getDelta();

		// do nothing if project is closing
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
			return;

		// deleting whole project
		if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
			IProject proj = (IProject) event.getResource();
			LaunchConfigurationsManager.removeConfigurationsOfProject(proj);
			return;
		}

		IResource changedResource = rootDelta.getResource();

		if (changedResource.getType() == IResource.ROOT) {
			IResourceDelta[] del = rootDelta.getAffectedChildren();
			if (del.length == 1
					&& del[0].getResource().getType() == IResource.PROJECT) {
				project = (IProject) del[0].getResource();
				handleProjectChange(project, rootDelta);
			}

			if (del.length > 1)
				refactorOverMultipleProjects(del);

		}

		finalizeChanges();
	}

	private void refactorOverMultipleProjects(IResourceDelta[] deltas) {
		// is there a chance that more than two projects are affected during
		// refactoring?
		if (deltas.length > 2)
			return;
		for (IResourceDelta delta : deltas) {
			handleProjectChange((IProject) delta.getResource(), delta);
		}

	}

	/**
	 * method handles changes in one project and sets global lists of files, if
	 * changes occures
	 * 
	 * @param project
	 * @param delta
	 */
	private void handleProjectChange(IProject project, IResourceDelta delta) {
		// check out whether is project type of RHQ
		// check out whether is project open (during creating project this
		// listener is notified and can receive change on closed project)
		// terminate on failure
		try {
 			if (project == null || !project.isOpen()
					|| !project.hasNature(RhqConstants.RHQ_NATURE_ID))
				return;
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}

		ExtractorProvider provider = ExtractorProvider.getInstance();
		RhqPathExtractor extractor = provider.getMap().get(project);

		// this should happen when project is opened for first time
		if (extractor == null) {
			ProjectInitializer scan = new ProjectInitializer();
			scan.initProject(project);
			// get extractor again
			extractor = provider.getMap().get(project);
		}

		// list all files in new job, used for opening projects
		if (extractor.getAllFiles().isEmpty()) {
			extractor.listFiles();
		}

		// ------------

		// stack used for storing delta tree
		Stack<IResourceDelta> stackDelta = new Stack<IResourceDelta>();
		for (IResourceDelta d : delta.getAffectedChildren()) {
			stackDelta.push(d);
		}

		IPath removedResourcePath = null;
		IResourceDelta currentDelta;

		// go through all deltas
		while (!stackDelta.isEmpty()) {
			currentDelta = stackDelta.pop();
			
			
			IResource currentResource = currentDelta.getResource();
			RefactoredPair pair;
			IPath key =  findMatchInRefactoredFiles(currentResource.getFullPath());
			if(currentDelta.getMovedFromPath() != null){
				if(key == null){
					pair = new RefactoredPair();
					pair.setFrom(currentDelta.getMovedFromPath());
					key = currentResource.getFullPath();
					fRefactoredMap.put(key,pair);
				} else {
					pair = fRefactoredMap.get(key);
					pair.setFrom(currentDelta.getMovedFromPath());
				} 
				
				continue;
			}
			
			if(currentDelta.getMovedToPath() != null){
				if(key == null){
					pair = new RefactoredPair();
					pair.setTo(currentDelta.getMovedToPath());
					key = currentResource.getFullPath();
					fRefactoredMap.put(key,pair);
				} else {
					pair = fRefactoredMap.get(key);
					pair.setTo(currentDelta.getMovedToPath());
				}
				continue;
			}
			
			
			switch (currentDelta.getKind()) {
			case IResourceDelta.ADDED:
				// ignore added files into proj/.bin of proj/build
				if (currentResource.getFullPath()
						.removeFirstSegments(1).toString().startsWith(RhqConstants.RHQ_DEFAULT_BUILD_DIR) ||
					currentResource.getFullPath()
						.removeFirstSegments(1).toString().startsWith(RhqConstants.RHQ_DEFAULT_BUILD_DIR))
						{
					break;
				}

				IPath path = currentDelta.getFullPath();
				if (currentResource instanceof IFile) {

					fAddedFiles.add(path);

				}

				// foldername refactoring
				if (currentResource instanceof IFolder) {
					fAddedFolders.add(currentResource.getFullPath());
				}

				break;

			case IResourceDelta.CHANGED:
				// ingnore changes in proect/.bin
				if (currentDelta.getResource().getFullPath()
						.removeFirstSegments(1).toString().startsWith(".bin")) {
					break;
				}

				// add affected children to stack
				for (IResourceDelta d : currentDelta.getAffectedChildren()) {
					stackDelta.push(d);
				}

				break;

			case IResourceDelta.REMOVED:
				if (currentResource.getFullPath()
						.removeFirstSegments(1).toString().startsWith(RhqConstants.RHQ_DEFAULT_BUILD_DIR) ||
					currentResource.getFullPath()
						.removeFirstSegments(1).toString().startsWith(RhqConstants.RHQ_DEFAULT_BUILD_DIR))
						{
					break;
				}
				System.out.println("REMOVE: "+ currentDelta.getFullPath());
				extractor.removeFile(currentDelta.getFullPath()
						.removeFirstSegments(1));
				removedResourcePath = currentDelta.getFullPath();

				if (currentResource instanceof IFile) {
					fDeletedFiles.add(removedResourcePath);
				}

				if (currentResource instanceof IFolder) {
					fDeletedFolders.add(removedResourcePath)	;
				}

				break;

			}

		}
	}

	private void finalizeChanges() {
		
		//refactoring (renaming or moving files)
		for(RefactoredPair pair: fRefactoredMap.values()){
			System.out.println("FROM :" +pair.getFrom() + " TO: "+pair.getTo());
		}
		if(!fRefactoredMap.isEmpty()){
			refactorPairs();
		}
		
		// removes all folders from extractor
		for (IPath deletedFolder : fDeletedFolders) {
			IProject project = getProjectFromPath(deletedFolder);
			ExtractorProvider.getInstance().getMap().get(project)
					.removeFolder(deletedFolder.removeFirstSegments(1));
		}
		for(IPath deletedFile : fDeletedFiles){
			IProject project = getProjectFromPath(deletedFile);
			ExtractorProvider.getInstance().getMap().get(project)
			.removeFolder(deletedFile.removeFirstSegments(1));
		}
		
		for(IPath addedFile: fAddedFiles){
			IProject project = getProjectFromPath(addedFile);
			ExtractorProvider.getInstance().getMap().get(project)
				.addFile(addedFile.removeFirstSegments(1));
		}
		
		for(IPath addedFolder: fAddedFolders){
		    IProject project = getProjectFromPath(addedFolder);
			ExtractorProvider.getInstance().getMap().get(project)
				.addFolder(addedFolder.removeFirstSegments(1));
		}
		

	}

	
	
	/**
	 * handles refactoring of content of fRefactoredMap
	 */
	private void refactorPairs() {
		IPath from,to;
		for(RefactoredPair pair: fRefactoredMap.values()) {
			from = pair.getFrom();
			to = pair.getTo();
			//filter non-refactoring related changes
			if(from == null || to == null)
				continue;
			
			IProject affectedProject = getProjectFromPath(to);
			IProject unaffectedProject = getProjectFromPath(from);
			
			if(affectedProject.equals(unaffectedProject)) {
				RhqPathExtractor extractor = ExtractorProvider.getInstance().getMap().get(affectedProject);
				extractor.updatePaths(from.removeFirstSegments(1), to.removeFirstSegments(1));
				
				RhqRecipeContentChange change = new RhqRecipeContentChange(
						"change",
						affectedProject.getFile(RhqConstants.RHQ_RECIPE_FILE));
				change.refactorFileName(from.removeFirstSegments(1).toString(),
						to.removeFirstSegments(1).toString());
			} else {
				RhqPathExtractor fromExtractor = ExtractorProvider.getInstance().getMap().get(unaffectedProject);
				
				IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFolder(to);
				if(resource != null && resource.exists()){
					fromExtractor.removeFolder(from.removeFirstSegments(1));
					fAddedFolders.add(to);
				} else {
					fromExtractor.removeFile(from.removeFirstSegments(1));
					fAddedFiles.add(to);
				}
				System.out.println("sss");
			}	
		}
		
	}
	

	
	private IProject getProjectFromPath(IPath path) {
		return ResourcesPlugin
				.getWorkspace()
				.getRoot()
				.getProject(
						path.removeLastSegments(path.segmentCount() - 1)
								.toString());
	}
	
	/**
	 * object used to holding information about refactoring
	 * @author syche
	 *
	 */
	private class RefactoredPair {
		IPath from;
		IPath to;
		
		public IPath getFrom() {
			return from;
		}
		public void setFrom(IPath from) {
			this.from = from;
		}
		public IPath getTo() {
			return to;
		}
		public void setTo(IPath to) {
			this.to = to;
		}
		
		@Override
		public String toString(){
			return "from: " +from + " to: " + to;
		}
		
	}
	
	/**
	 * return key from fRefactoredMap of matching value. Value can match on key of one of RefactorePair values
	 * @param path
	 * @return
	 */
	private IPath findMatchInRefactoredFiles(IPath path) {
		RefactoredPair pair;
		for(IPath key : fRefactoredMap.keySet()){
			if(key.equals(path))
				return key;
			pair = fRefactoredMap.get(key);
			if(pair.getFrom() != null && pair.getFrom().equals(path))
				return key;
			if(pair.getTo() != null && pair.getTo().equals(path))
				return key;
		}
		return null;
	}
}
