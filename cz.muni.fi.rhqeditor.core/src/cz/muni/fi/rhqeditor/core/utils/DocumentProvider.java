package cz.muni.fi.rhqeditor.core.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;


/**
 * Singleton class, holds information about open Document of each project. 
 * Used for searching for properties in recipe unsaved recipe
 * @author syche
 *
 */
public enum DocumentProvider {

	INSTANCE;

	private Map<IProject, WeakReference<IDocument> > map = new HashMap<>();
	
	/**
	 * attaches document to given project, overwrites existing
	 * @param project
	 * @param doc
	 */
	public void attachDocumentToProject(IProject project, IDocument doc){
		if(project == null || doc == null)
			return;
		
		map.put(project, new WeakReference<IDocument>(doc));
	}
	
	
	/**
	 * returns document from map, if exists. 
	 * @param project
	 * @return
	 */
	public IDocument getDocument(IProject project){
		clearMap();
		WeakReference<IDocument> ref;
		if((ref = map.get(project)) == null)
			return null;
		
		return ref.get();
	}
	
	/**
	 * remove null references from map
	 */
	private void clearMap(){
		List<IProject> toRemove = new ArrayList<>();
		WeakReference<IDocument> ref;
		for(IProject proj: map.keySet()){
			ref = map.get(proj);
			if(ref != null && ref.get() == null)
				toRemove.add(proj);
		}
		for(IProject proj: toRemove){
			map.remove(proj);
		}
	}
	
}
