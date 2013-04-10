package cz.muni.fi.rhqeditor.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

import cz.muni.fi.rhqeditor.core.utils.RhqConstants;


public class RhqAnnotationModel extends ResourceMarkerAnnotationModel{

	
	
	//temporary marker storage
	List<IMarker> fMarkers	= null;

	
	public RhqAnnotationModel(IResource resource) {
		super(resource);
		fMarkers = new ArrayList<IMarker>();
	}
	
	public void addSampleMarker(){
		try {
			IMarker marker = getResource().createMarker(RhqConstants.RHQ_MARKER_TYPE);
//			System.out.println(marker.getType());
//			System.out.println(marker.isSubtypeOf(IMarker.PROBLEM));
			marker.setAttribute(IMarker.MESSAGE, "new marker");
			marker.setAttribute(IMarker.LINE_NUMBER, 5);
			marker.setAttribute(IMarker.SEVERITY,IMarker.SEVERITY_WARNING);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			System.err.println("sample marker failed");
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Removes all markers of type RHQ_MARKER_TYPE from recipe
	 * @throws CoreException
	 */
	public void removeMarkers() {
		try{
		List<IMarker> markers = new ArrayList<IMarker>();
		for(IMarker m: retrieveMarkers()){
			if(m.getType().equals(RhqConstants.RHQ_MARKER_TYPE))
				markers.add(m);
		}
		IMarker[] field = markers.toArray(new IMarker[markers.size()]);
		deleteMarkers(field);
		}catch(CoreException e){
			e.printStackTrace();
		}
	}
	
	public void addMarker(int line, String message, int type){
		try {
			IMarker marker = getResource().createMarker(RhqConstants.RHQ_MARKER_TYPE);
			
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.LINE_NUMBER, line);
			if(type != IMarker.SEVERITY_ERROR && type != IMarker.SEVERITY_WARNING)
				type = IMarker.SEVERITY_WARNING;
			marker.setAttribute(IMarker.SEVERITY,IMarker.SEVERITY_WARNING);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
}
