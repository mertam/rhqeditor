package cz.muni.fi.rhqeditor.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.part.FileEditorInput;

import cz.muni.fi.rhqeditor.core.utils.RhqConstants;

/**
 * Editor matching strategy used by RhqEditor
 * @author syche
 *
 */
public class RhqBundleEditorMatchingStrategy implements IEditorMatchingStrategy{

	/**
	 * @return true if project has RHQ nature and file name is deploy.xml
	 */
	@Override
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		if(input instanceof FileEditorInput){
			FileEditorInput fei = (FileEditorInput) input;
			IFile file = fei.getFile();
			
			try {
				System.out.println("strategy: " +  (file.getProject().hasNature(RhqConstants.RHQ_NATURE_ID) && file.getName().equals(RhqConstants.RHQ_RECIPE_FILE)));
				return file.getProject().hasNature(RhqConstants.RHQ_NATURE_ID) && file.getName().equals(RhqConstants.RHQ_RECIPE_FILE);
			} catch (CoreException e) {
				//return false if exception occurs
			}
		}
		System.out.println("strategy: FALSE");
		return false;
	}


}
