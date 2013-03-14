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
import cz.muni.fi.rhqeditor.core.launch.LaunchConfigurationsManager;

/**
 * listener used to tracking changes of resources in project
 * 
 * @author syche
 * 
 */

public class RecipeChangeListener implements IResourceChangeListener {

	/**
	 * When resource is chan
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		IProject project = null;
		RhqPathExtractor extractor = null;
		IResourceDelta rootDelta = event.getDelta();

		// do nothing if project is closing
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
			return;
		
		//deleting whole project
		if( event.getType() == IResourceChangeEvent.PRE_DELETE){
			IProject proj = (IProject) event.getResource();
			LaunchConfigurationsManager.removeConfigurationsOfProject(proj);
			return;
		}
		
		IResource changedResource = rootDelta.getResource();

		if (changedResource.getType() == IResource.ROOT) {
			IResourceDelta[] del = rootDelta.getAffectedChildren();
			if (del.length == 1
					&& del[0].getResource().getType() == IResource.PROJECT)
				project = (IProject) del[0].getResource();
		}

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
		extractor = provider.getMap().get(project);

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

		IResourceDelta projDelta = rootDelta.findMember(new Path(project
				.getProject().getName()));
		// stack used for storing delta tree
		Stack<IResourceDelta> stackDelta = new Stack<IResourceDelta>();
		for (IResourceDelta d : projDelta.getAffectedChildren()) {
			stackDelta.push(d);
		}

		// indicates whether some resource was removed or added in this change
		// event, used for renaming files in recipe
		// if both are not null, then refactoring occured
		IPath removedResourcePath = null;
		IPath addedResourcePath = null;
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
				IResource addedResource = currentDelta.getResource();
				IPath path = currentDelta.getFullPath();

				path = path.removeFirstSegments(1);
				if (addedResource instanceof IFile) {

					if (addedResource.getName().endsWith(
							RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX)
							|| addedResource.getName().endsWith(
									RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX)) {

						if (!extractor.getAbsolutePathsArchives()
								.contains(path)) {

							extractor.addArchive(path);
						}
						System.out.println("archive should be added");
					} else {
						extractor.addFile(path);
					}
					// handle refactoring file
					addedResourcePath = path;
					if (removedResourcePath != null) {
						// renameFileInRecipe(addedResourcePath,
						// removedResourcePath, project);
					}
				}
				break;

			case IResourceDelta.CHANGED:
				System.out.println("changed");
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
				System.out.println("removed");
				if(currentDelta.getResource().getFullPath().removeFirstSegments(1).toString().startsWith(".bin")){
					break;
				}
				extractor.removeFile(currentDelta.getFullPath()
						.removeFirstSegments(1));
				removedResourcePath = currentDelta.getFullPath();
				removedResourcePath = removedResourcePath
						.removeFirstSegments(1);
				if (addedResourcePath != null) {
					// renameFileInRecipe(addedResourcePath,
					// removedResourcePath, project);
				}

				break;

			default:
				System.out.println("def");
			}

		}
	}
	


	/**
	 * 
	 * @param addedResourcePath
	 * @param removedResourcePath
	 */
	// private void renameFileInRecipe( IPath addedResourcePath, IPath
	// removedResourcePath, IProject proj){
	// StringBuilder sb = RecipeReader.readRecipe(proj);
	//
	// //some error occured
	// if(sb == null)
	// return;
	//
	//
	//
	// Pattern pattern =
	// Pattern.compile("file2.txt");
	// System.out.println(pattern.toString());
	// Matcher matcher =
	// pattern.matcher(sb.toString());
	//
	// boolean found = false;
	// while (matcher.find()) {
	// System.out.println(matcher.start());
	// System.out.println("found");
	// }
	// if(!found){
	// System.out.println("not found");
	// }
	//
	// String content = sb.toString();
	// content.replaceAll(removedResourcePath.toString(),
	// addedResourcePath.toString());
	// RecipeReader.setRecipeContent(proj, content);
	// }
	//

}
