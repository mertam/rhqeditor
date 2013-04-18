package cz.muni.fi.rhqeditor.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.widgets.Display;

/**
 * class used for refactoring recipe content
 * 
 * @author syche
 * 
 */
public class RhqRecipeContentChange extends TextFileChange {
	
	public RhqRecipeContentChange(String name, IFile file) {
		super(name, file);
	}

	/**
	 * refactors name of file in recipe i.e. rhq:file name="formerName" --->
	 * name="newName"
	 * 
	 * @param formerName
	 *            formerName
	 * @param newName
	 *            new name of file
	 */
	public void refactorFileName(final String formerName, final String newName) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					IDocument document = acquireDocument(null);
					String content = document.get();
					String namespacePrefix = RecipeReader.getRhqNamespacePrefix(content);
					
					//finds all rhq:file or rhq:archive tags with attribute name
					Pattern pattern = 
							Pattern.compile("("+ namespacePrefix + RhqConstants.RHQ_TYPE_FILE + "|"+ 
							namespacePrefix + RhqConstants.RHQ_TYPE_ARCHIVE + ")"+     				
							"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" +			
							"\\s+name\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]+\"" +    			
							"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			
							 "\\s*/?\\s*>", Pattern.DOTALL);		
					setSaveMode(KEEP_SAVE_STATE);
					
					Matcher matcher = pattern.matcher(content);
					while(matcher.find()){
						content = content.replaceFirst("name\\s*=\\s*\"\\s*" + formerName,"name=\"" + newName);
					}
					
					document.set(content);
					performEdits(document);
				} catch (CoreException | BadLocationException e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * adds task to recipe using format namespacePrefix:taskName
	 * name="filename". Only archive, and file should be used
	 * 
	 * @param taskName
	 * @param filename
	 */

	public void addTaskToRecipe(final String taskName, final String filename) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {

					setSaveMode(KEEP_SAVE_STATE);
					IDocument document = acquireDocument(null);
					String content = document.get();
					int position = getPositionForTask(document, taskName);
					if (position == -1)
						return;

					String namespace = RecipeReader
							.getRhqNamespacePrefix(content);
					content = addToTextToPosition(prepareTagToInsert(namespace+taskName, filename), content, position);
					document.set(content);
					performEdits(document);

				} catch (BadLocationException | CoreException e) {
					e.printStackTrace();
				}

			}
		});
	}
	

	/**
	 * find position in recipe to insert task,
	 * 
	 * @param taskName
	 *            name of task wih no prefix (supported file, archive)
	 * @param line
	 *            output -1
	 * @param output
	 *            -1
	 */
	private int getPositionForTask(IDocument doc, String element) {
		int position = -1;
		if (doc == null)
			return position;
		String content = doc.get();
		String namespace = RecipeReader.getRhqNamespacePrefix(content);

		int lastValidOffset = -1;
		// finds closing tag of existing element
		Pattern pattern = Pattern.compile("(<\\s*"+namespace+element + 
				"\\s+(\\s*\\w+=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*+\\s*/>)" +//<rhq:file attt="" />
				"|(<\\s*/\\s*"+namespace+element+"\\s*>)",Pattern.DOTALL); //</rhq:file>
		Matcher matcher = pattern.matcher(content);
		int commentEndPosition, commentStartPosition;
		while (matcher.find()) {
			if ((commentEndPosition = content.substring(matcher.start(),
					content.length()).indexOf("-->")) > -1) {
				commentStartPosition = content.substring(matcher.start(),
						content.length()).indexOf("<!--");
				if(true && commentStartPosition > commentEndPosition){
					// commented, find another
					continue;
				} else  {
					return matcher.start();
				}
			} else {
				lastValidOffset = matcher.start();
			}
		}

		if(lastValidOffset > 0)
			return lastValidOffset;
		// if there's no previous element of this type, insert into
		// deployment-unit
		pattern = Pattern.compile("<\\s*" + namespace
				+ RhqConstants.RHQ_TYPE_DEPLOYMENT_UNIT + ".*>",Pattern.DOTALL);
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			if ((commentEndPosition = content.substring(matcher.start(),content.length()).indexOf("-->")) > -1) {
				commentStartPosition = content.substring(matcher.start(),content.length()).indexOf("<!--");
				if(true && commentStartPosition > commentEndPosition){
					// commented, find another
					continue;
				} else  {
					return matcher.start();
				}
			} else {
				return matcher.start();
			}
		}
		return -1;

	}

	
	/**
	 * inserts given text into recipe behind tang on given position. Keeps line offset. 
	 * 			<rhq:file name=/>   ----> <rhq:file name=... \>
	 *          ^-- position			  insertedText
	 * @param text
	 * @param document
	 * @param position
	 * @return updated String
	 */
	private String addToTextToPosition(String text, String document,
			int position) {

		if (position < 0)
			return null;
		int startIndex = 0;

		startIndex = document.substring(0, position).lastIndexOf("\n");
		String whiteSpace = document.substring(startIndex, position);
		int whiteSpaceCount;
		for (whiteSpaceCount = 0; whiteSpaceCount != whiteSpace.length(); whiteSpaceCount++) {
			if (Character.isWhitespace(whiteSpace.charAt(whiteSpaceCount)))
				continue;
			break;
		}
		whiteSpace = whiteSpace.substring(0, whiteSpaceCount);

		position = document.indexOf(">", position);
		return document.substring(0, position + 1) + whiteSpace + text + "\n"
				+ document.substring(position + 1);

	}

	
	private String prepareTagToInsert(String tagName, String filename){
		String insert;
		//for archive prepare paired tag
		if(tagName.endsWith(RhqConstants.RHQ_TYPE_ARCHIVE)){
			insert = "<" + tagName + " name=\"" + filename + "\"></"+tagName+">";
		} else {
			insert = "<" + tagName+ " name=\"" + filename + "\"/>";
		}
		return insert;
	}
}
