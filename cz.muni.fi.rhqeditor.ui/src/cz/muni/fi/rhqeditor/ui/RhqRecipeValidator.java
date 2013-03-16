package cz.muni.fi.rhqeditor.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import utils.RhqConstants;
import utils.RhqPathExtractor;
import cz.muni.fi.rhqeditor.ui.rhqmodel.RhqAttribute;
import cz.muni.fi.rhqeditor.ui.rhqmodel.RhqModelReader;
import cz.muni.fi.rhqeditor.ui.rhqmodel.RhqTask;


/**
 * Class extends uses SAX parser to parse recipe and uses RhqAnnotation model to manage markers.
 * @author syche
 *
 */
public class RhqRecipeValidator extends DefaultHandler2 {
	
	
	private RhqPathExtractor	fRhqPathExtractor	= null;
	private RhqAnnotationModel	fRhqAnnotationModel = null;
	private SAXParserFactory 	fParserFactory 		= null;
	private SAXParser		 	fParser				= null;

	private IDocument			fDocument 			= null;
	
	private String				fOpenArchiveName 	= null;
	private HashMap<String, Integer > fExistingTargets	= null;
	private HashMap<String,	Integer > fRequiredTargets  = null;
	
	private RhqModelReader fRhqModelReader = null;
	
	//contains all rhq tasks and required atts
//	private static HashMap<String, ArrayList<String> >   fRequiredAtts = null;
	
	private static final String EMPTY_STRING = "";
	private Stack<String> openElements;
	Locator locator;
	
	
	
	/**
	 * 
	 * @param recipe	- IResource corresponding to recipe ("deploy.xml")
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public RhqRecipeValidator() {
		openElements = new Stack<>();
		fRequiredTargets = new HashMap<String, Integer>();
		fExistingTargets = new HashMap<String, Integer>();
		try {			
			fParserFactory = SAXParserFactory.newInstance();
			fParser = fParserFactory.newSAXParser();
		} catch (SAXException e) {
			e.printStackTrace();	
		} catch(ParserConfigurationException e){
			e.printStackTrace();
		}
	}
	
	public void setAnnotationModel(RhqAnnotationModel model){
		fRhqAnnotationModel = model;
	}
	
	public void setExtractor(RhqPathExtractor ext){
		fRhqPathExtractor = ext;
	}
	
	/**
	 * crates parsable byte array from given IDocument
	 * @param idoc
	 */
	public void setInputDocument(IDocument idoc){
		fDocument = idoc;
	}
	
	private RhqModelReader getReader(){
		if(fRhqModelReader != null)
    		return fRhqModelReader;
    	fRhqModelReader = new RhqModelReader(fRhqPathExtractor.getProject(), 0);
    	return fRhqModelReader;
	}



	public void validateRecipe(){
		
		try {
			String text = fDocument.get();
			ByteArrayInputStream bs = new ByteArrayInputStream(text.getBytes());
			InputSource input = new InputSource(bs);
			
			if(input != null){
				fRhqAnnotationModel.removeMarkers();
				fParser.parse(input, this);
			}
			
		} catch (SAXException e) {
			//do nothing if document isn't well formed
		} catch (IOException e) {
			System.out.println(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void setDocumentLocator(Locator loc){
		locator = loc;
	}
	
	@Override
	public void startDocument() throws SAXException {
			fRhqAnnotationModel.removeMarkers();
			fOpenArchiveName = null;
			fExistingTargets.clear();
			fRequiredTargets.clear();
			

	}
	
	@Override
	public void endDocument() throws SAXException {
		manageTargets();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		//if task isn't rhq task do nothing
		RhqTask currentTask = getReader().getTask(qName);
		if(currentTask == null){
			openElements.push(qName);
			super.startElement(uri, localName, qName, attributes);
			return;
		}
			
		
		
		//managing targets
		String attrValue;
		IPath attrPath ;
		checkAttributesAndSetMarkers(qName, attributes);
		checkParentAndSetMarkers(currentTask,openElements.peek());
		
		
		switch(currentTask.getName()){
		case "archive":
			
			attrValue = attributes.getValue("name");
			if(attrValue == null)
				break;
			attrPath = new Path(attrValue);
			
			if(!fRhqPathExtractor.isPathToArchiveValid(attrPath))
				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Archive not found", IMarker.SEVERITY_WARNING);
 			fOpenArchiveName = null;
			attrValue = attributes.getValue(RhqConstants.RHQ_ATTRIBUTE_NAME);
			if(attrValue == null){
				fOpenArchiveName = EMPTY_STRING;
				break;
			}
			fOpenArchiveName = attrValue;
			break;
		}
		
		
		
		
		
		
		
//		switch(qName){
//		
//		case RhqConstants.RHQ_TYPE_BUNDLE:
//			checkAttributesAndSetMarkers(qName, attributes);
//			break;
//			
//		case RhqConstants.RHQ_TYPE_DEPLOYMENT_UNIT:
//			checkAttributesAndSetMarkers(qName, attributes);
//			break;
//			
//		case RhqConstants.RHQ_TYPE_ARCHIVE:
//			checkAttributesAndSetMarkers(qName, attributes);
//			
//			attrValue = attributes.getValue("name");
//			if(attrValue == null)
//				break;
//			attrPath = new Path(attrValue);
//			
//			if(!fRhqPathExtractor.isPathToArchiveValid(attrPath))
//				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Archive not found", IMarker.SEVERITY_WARNING);
// 			fOpenArchiveName = null;
//			attrValue = attributes.getValue(RhqConstants.RHQ_ATTRIBUTE_NAME);
//			if(attrValue == null){
//				fOpenArchiveName = EMPTY_STRING;
//				break;
//			}
//			fOpenArchiveName = attrValue;
//			break;
//		
//		
//		
//		case RhqConstants.RHQ_TYPE_FILESET:
//			checkAttributesAndSetMarkers(qName, attributes);
//			if(!openElements.peek().equals(RhqConstants.RHQ_TYPE_IGNORE) &&
//					!openElements.peek().equals(RhqConstants.RHQ_TYPE_REPLACE)){
//				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Misplaced element "+RhqConstants.RHQ_TYPE_FILESET, IMarker.SEVERITY_WARNING);
//				break;
//			}
//		
//			attrValue = attributes.getValue("includes");
//			//includes not found or no parent archive open
//			if(attrValue == null || fOpenArchiveName == null )
//				break;
//			
//			attrPath = new Path(attrValue);
//			if(fOpenArchiveName.equals(EMPTY_STRING)){
//				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Unrecognized path to archive", IMarker.SEVERITY_WARNING);
//			}else{
//				if(!fRhqPathExtractor.isPathToArchiveFileValid(attrPath, fOpenArchiveName))
//					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "There's no such file in archive", IMarker.SEVERITY_WARNING);
//			}
//			
//			break;
//		
//		
//			
//		case RhqConstants.RHQ_ELEMENT_TARGET:
//			checkAttributesAndSetMarkers(qName, attributes);
//			
//			attrValue = attributes.getValue("name");
//		    if(attrValue == null)
//		    	break;
//		    fExistingTargets.put(attrValue, new Integer(locator.getLineNumber()));		
//		    break;
//		
//		case RhqConstants.RHQ_TYPE_FILE:
//			checkAttributesAndSetMarkers(qName, attributes);
//			attrValue = attributes.getValue("name");
//			
//			if(attrValue == null)
//				break;
//			
//			attrPath = new Path(attrValue);
//
//			//file can have only one of following
//			if(attributes.getIndex("destinationFile") > -1 && attributes.getIndex("destinationDir") > -1){
//				fRhqAnnotationModel.addMarker(locator.getLineNumber(), 
//						"File can't have specified destinationFile and destinationDir at the same time", IMarker.SEVERITY_WARNING);
//			}
//				
//			if(!fRhqPathExtractor.isPathToFileValid(attrPath))
//				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "File not found", IMarker.SEVERITY_WARNING);			
//			break;
//
//			
//		case RhqConstants.RHQ_TYPE_URL_FILE:
//			checkAttributesAndSetMarkers(qName, attributes);
//			
//			if(attributes.getIndex("destinationFile") > -1 && attributes.getIndex("destinationDir") > -1){
//				fRhqAnnotationModel.addMarker(locator.getLineNumber(), 
//						"File can't have specified destinationFile and destinationDir at the same time", IMarker.SEVERITY_WARNING);
//			}
//			
//			break;
//		case RhqConstants.RHQ_TYPE_REPLACE:
//			checkAttributesAndSetMarkers(qName, attributes);
//			//replace must be direct descendant of archive
//			if(!openElements.peek().equals(RhqConstants.RHQ_TYPE_ARCHIVE) &&
//					!openElements.peek().equals(RhqConstants.RHQ_TYPE_URL_ARCHIVE)){
//				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Misplaced element "+RhqConstants.RHQ_TYPE_REPLACE, IMarker.SEVERITY_WARNING);
//			} 
//			break;
//		
//		case "include" :
//		case "exclude" :
//			
//			checkAttributesAndSetMarkers(qName, attributes);
//			attrValue = attributes.getValue("name");
//			//only include with parent archive matters
//			System.out.println("include "+ fOpenArchiveName);
//			if(attrValue == null || fOpenArchiveName == null){
//				return;
//			}
//			
//		    attrPath = new Path(attrValue);
//			
//			if(!fRhqPathExtractor.isPathToArchiveFileValid(attrPath, fOpenArchiveName))
//				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "File not found in archive", IMarker.SEVERITY_WARNING);
//			
//		    break;
//		}
		openElements.push(qName);
		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		if(openElements.peek() != qName){
			//this means not well formed document which will cause SAX exception anyway
			return;
		}
		openElements.pop();
		if(qName.equals(RhqConstants.RHQ_TYPE_ARCHIVE)){
			fOpenArchiveName = null;
		}
	}
	
	/**
	 * makes intersect of existing and required targets and puts marker on unpaired ones
	 */
	private void manageTargets(){
		for(String target: fRequiredTargets.keySet()){
			System.out.println(target);
			if(!fExistingTargets.containsKey(target)){
				fRhqAnnotationModel.addMarker(fRequiredTargets.get(target).intValue(), "Target\""+target+"not found", IMarker.SEVERITY_WARNING);
			}
		}
	}
	
	/**
	 * returns name of parent <rhq:archive name=".." tag in document. 
	 * @return archive name or empty String
	 */
	public static  String getParentArchiveFilename(IDocument document, int position){
		final String EMPTY_STRING = "";
		if(position < 0)
			return EMPTY_STRING;
		String toSearch = document.get().substring(0, position);
		
    	
		toSearch = toSearch.replaceAll("\\s","");
    	//finds last occurence of tag <rhq:archive
    	int lastArchiveIndex = toSearch.lastIndexOf("<" + RhqConstants.RHQ_TYPE_ARCHIVE);
    	
    	//there is no open <rhq:archive tag
    	if(lastArchiveIndex == -1){
    		return EMPTY_STRING;
    	}
    	toSearch =  toSearch.substring(lastArchiveIndex, toSearch.length());                	
    	//finds whether there is also closing tag
    	int closingIndex = toSearch.indexOf("</" + RhqConstants.RHQ_TYPE_ARCHIVE);
//    	System.out.println("SUBSTR: " + toSearch.substring(lastArchiveIndex, toSearch.length()));
    	
    	if(closingIndex > -1){
    		//rhq:archvive is closed, normal file proposal
    		return EMPTY_STRING;
    	}else{
    		//cursor is inside of <rhq:archive tag, archvive file proposal
    		int nameBegin = toSearch.indexOf("name=\"");
    		if(nameBegin > -1){
    			toSearch = toSearch.substring(nameBegin + "name=\"".length() , toSearch.length());
    		}else{
    			return EMPTY_STRING;
    		}
    		int nameEnd = toSearch.indexOf("\"");
    		
    		if(nameEnd > -1){
    			String archiveName = toSearch.substring(0, nameEnd);
    			return archiveName;
    		}else{
    			return EMPTY_STRING;
    		}
    	}

	}
	
	/**
	 * cheks whether element has all required attributes
	 * @param elementName
	 * @param atts
	 */
	private void checkAttributesAndSetMarkers(String elementName, Attributes atts){
		RhqTask task = getReader().getTask(elementName);
		if(task == null)
			return;
		
		for(RhqAttribute attr: task.getAttributes()){
			if(attr.isRequired() && atts.getValue(attr.getName()) == null){
				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Attribute is mandatory: "+attr.getName(), IMarker.SEVERITY_WARNING);
			}
			
		}
	}
	
	/**
	 * checks whet
	 */
	private void checkParentAndSetMarkers(RhqTask child, String parentName){
		if(child == null || parentName == null)
			return;
		for(String parent: child.getAllParentNames()){
			if(parent.equals(getReader().removeNamespacePrefix(parentName)))
				return;
		}
		fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Misplaced element "+child.getName(), IMarker.SEVERITY_WARNING);
	}
	

}
