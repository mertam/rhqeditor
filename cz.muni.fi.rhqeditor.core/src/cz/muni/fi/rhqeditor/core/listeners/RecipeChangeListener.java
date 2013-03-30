package cz.muni.fi.rhqeditor.core.listeners;

import java.util.ArrayList;
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

import cz.muni.fi.rhqeditor.core.ProjectScanner;
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

public class RecipeChangeListener implements IResourceChangeListener {

	// refactoring purposes
	private ArrayList<IPath> fAddedFolders = new ArrayList<>();
	private ArrayList<IPath> fDeletedFolders = new ArrayList<>();
	private ArrayList<IPath> fAddedFiles = new ArrayList<>();
	private ArrayList<IPath> fDeletedFiles = new ArrayList<>();

	/**
	 * When resource is chan
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		fAddedFiles.clear();
		fAddedFolders.clear();
		fDeletedFiles.clear();
		fDeletedFolders.clear();
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

		finalizeRefactoring();
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
			ProjectScanner scan = new ProjectScanner();
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
			switch (currentDelta.getKind()) {
			case IResourceDelta.ADDED:
				// ignore added files into proj/.bin
				if (currentDelta.getResource().getFullPath()
						.removeFirstSegments(1).toString().startsWith(".bin")) {
					break;
				}
				// for (IResourceDelta d : delta.getAffectedChildren()) {
				// stackDelta.push(d);
				// }

				IResource addedResource = currentDelta.getResource();
				IPath path = currentDelta.getFullPath();
				if (addedResource instanceof IFile) {

					if (addedResource.getName().endsWith(
							RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX)
							|| addedResource.getName().endsWith(
									RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX)) {

						if (!extractor.getAbsolutePathsArchives().contains(
								path.removeFirstSegments(1))) {

							extractor.addArchive(path.removeFirstSegments(1));
						}
					} else {
						extractor.addFile(path.removeFirstSegments(1));
					}
					fAddedFiles.add(path);

				}

				// foldername refactoring
				if (addedResource instanceof IFolder) {
					fAddedFolders.add(addedResource.getFullPath());
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

				if ((currentDelta.getFlags() & IResourceDelta.MOVED_FROM) > 0)
					System.out.println("MOVED");
				break;

			case IResourceDelta.REMOVED:
				if (currentDelta.getResource().getFullPath()
						.removeFirstSegments(1).toString().startsWith(".bin")) {
					break;
				}
				extractor.removeFile(currentDelta.getFullPath()
						.removeFirstSegments(1));
				removedResourcePath = currentDelta.getFullPath();
				removedResourcePath = removedResourcePath
						.removeFirstSegments(1);

				IResource removedResource = currentDelta.getResource();
				if (removedResource instanceof IFile) {
					fDeletedFiles.add(removedResource.getFullPath());
				}

				if (removedResource instanceof IFolder) {

					fDeletedFolders.add(removedResource.getFullPath());
				}

				break;

			}

		}
	}

	private void finalizeRefactoring() {

		// import add files and folders into project
		for (IPath addedFolder : fAddedFolders) {
			IProject project = getProjectFromPath(addedFolder);
			RhqPathExtractor extractor = ExtractorProvider.getInstance().getMap().get(project);
			extractor.addFolder(addedFolder.removeFirstSegments(1));
			for(IPath path :extractor.getAbsolutePathsFilesByPrefix(addedFolder.removeFirstSegments(1).toString())){
				addFileToRecipe(project, path);
			}
		}
		// removes all folders from extractor
		for (IPath deletedFolder : fDeletedFolders) {
			IProject project = getProjectFromPath(deletedFolder);
			ExtractorProvider.getInstance().getMap().get(project)
					.removeFolder(deletedFolder.removeFirstSegments(1));
		}

		// folder was moved with content
		if (fAddedFolders.size() == 1 && fDeletedFolders.size() == 1) {
			IPath newPath = fAddedFolders.get(0);
			IPath oldPath = fDeletedFolders.get(0);

			IProject affectedProject = getProjectFromPath(newPath);

			RhqRecipeContentChange change = new RhqRecipeContentChange(
					"change",
					affectedProject.getFile(RhqConstants.RHQ_RECIPE_FILE));
			change.refactorFileName(oldPath.removeFirstSegments(1).toString(),
					newPath.removeFirstSegments(1).toString());
			RhqPathExtractor extractor = ExtractorProvider.getInstance()
					.getMap().get(affectedProject);
			extractor.updatePaths(oldPath.removeFirstSegments(1).toString(),
					newPath.removeFirstSegments(1).toString());
		}

		// file was moved or renamed
		if (fAddedFiles.size() == 1 && fDeletedFiles.size() == 1) {
			IPath newPath = fAddedFiles.get(0);
			IPath oldPath = fDeletedFiles.get(0);

			IProject affectedProject = getProjectFromPath(newPath);
			IProject unaffectedProject = getProjectFromPath(oldPath);

			// do not change recipe when folder was moved to different project
			if (!affectedProject.equals(unaffectedProject)) {
				return;
			}

			RhqRecipeContentChange change = new RhqRecipeContentChange(
					"change",
					affectedProject.getFile(RhqConstants.RHQ_RECIPE_FILE));
			change.refactorFileName(oldPath.removeFirstSegments(1).toString(),
					newPath.removeFirstSegments(1).toString());
			return;
		}

	}

	
	private void addFileToRecipe(IProject project, IPath path){
		if (path.removeFirstSegments(1).toString()
				.equals(RhqConstants.RHQ_RECIPE_FILE))
			return;

		RhqRecipeContentChange change = new RhqRecipeContentChange(
				"add file to recipe",
				project.getFile(RhqConstants.RHQ_RECIPE_FILE));
		if (path.toString().endsWith(
				RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX)
				|| path.toString().endsWith(
						RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX))
			change.addTaskToRecipe(RhqConstants.RHQ_TYPE_ARCHIVE, path
					.toString());
		else
			change.addTaskToRecipe(RhqConstants.RHQ_TYPE_FILE, path
					.toString());
	}
	
	private IProject getProjectFromPath(IPath path) {
		return ResourcesPlugin
				.getWorkspace()
				.getRoot()
				.getProject(
						path.removeLastSegments(path.segmentCount() - 1)
								.toString());
	}

}
