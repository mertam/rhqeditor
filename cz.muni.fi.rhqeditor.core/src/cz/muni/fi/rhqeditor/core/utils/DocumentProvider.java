package cz.muni.fi.rhqeditor.core.utils;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jface.text.IDocument;


/**
 * Singleton class, holds information about open Document of each project. 
 * Used for searching for properties in recipe unsaved recipe
 * @author syche
 *
 */
public class DocumentProvider {

	
	private static final DocumentProvider instance 	= new DocumentProvider();
	private Map<String, IDocument> map 				= new WeakHashMap<>();
	
	public static DocumentProvider getInstance(){
		return instance;
	}
	
	public void attachDocumentToProject(String projName, IDocument doc){
		if(projName == null || doc == null)
			return;
		map.put(projName, doc);
	}
	
	public IDocument getDocument(String projName){
		return map.get(projName);
	}
	
}
