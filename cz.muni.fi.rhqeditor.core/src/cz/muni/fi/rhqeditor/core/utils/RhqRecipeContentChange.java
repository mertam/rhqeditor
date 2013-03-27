package cz.muni.fi.rhqeditor.core.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.widgets.Display;


/**
 * class used for refactoring recipe content
 * @author syche
 *
 */
public class RhqRecipeContentChange extends TextFileChange{

	public RhqRecipeContentChange(String name, IFile file) {
		super(name, file);
	}
	
	/**
	 * refactors name of file in recipe i.e. rhq:file name="formerName" ---> name="newName"
	 * @param formerName formerName
	 * @param newName new name of file
	 */
	public void refactorFileName(final String formerName, final String newName){
		Display.getDefault().syncExec(new Runnable() {
		    public void run()  {
		    	try{
		    		System.out.println("from " + formerName);
		    		System.out.println("to " +newName);
		    		setSaveMode(KEEP_SAVE_STATE);
		    		IDocument document = acquireDocument(null);
					String content = document.get();
					
					content = content.replaceAll("name=\""+ formerName, "name=\""+ newName);
//					content.replaceAll(formerName, newName);
					document.set(content);
					performEdits(document);
//					updateExtractor(formerName, newName);
		    } catch (CoreException | BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    }
		});
		
	}
	

//	private void updateExt
	
	/**
	 * updates RhqPathExtractor references attached to project which file belongs to
	 */
	private void updateExtractor(String formerName, String newName){
		ExtractorProvider provider = ExtractorProvider.getInstance();
		RhqPathExtractor extractor = provider.getMap().get(super.getFile().getProject());
		extractor.removeFile(new Path(formerName));
//		if(newName.endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX) ||newName.endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX)){
//			extractor.addArchive(new Path(newName));
//		} else {
//			extractor.addFile(new Path(newName));
//		}
			
		
		
		
		
	}
	
	
	public void addFileToRecipe(String filename){
		
	}
	
	public void addArchiveToRecipe(String archiveName){
		
	}
	

}
