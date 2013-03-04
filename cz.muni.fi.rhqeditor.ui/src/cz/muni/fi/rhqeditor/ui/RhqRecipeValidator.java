package cz.muni.fi.rhqeditor.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
	
	//contains all rhq tasks and required atts
	private static HashMap<String, ArrayList<String> >   fRequiredAtts = null;
	
	Locator locator;
	
	private final String EMPTY_STRING = "";
	
	
	/**
	 * 
	 * @param recipe	- IResource corresponding to recipe ("deploy.xml")
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public RhqRecipeValidator() {
		setRequiredAtts();
		fRequiredTargets = new HashMap<String, Integer>();
		fExistingTargets = new HashMap<String, Integer>();
		try {			
			fParserFactory = SAXParserFactory.newInstance();
			fParser = fParserFactory.newSAXParser();
		} catch (SAXException e) {
			e.printStackTrace();	
		} catch(ParserConfigurationException e){
			System.err.println("parser e");
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
			//do nothing
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
		//managing targets
		String attrValue;
		IPath attrPath ;
		
		
		
		switch(qName){
		
		case RhqConstants.RHQ_TYPE_BUNDLE:
			manageAttributesAndSetMarkers(qName, attributes);
			break;
			
		case RhqConstants.RHQ_TYPE_DEPLOYMENT_UNIT:
			manageAttributesAndSetMarkers(qName, attributes);
			break;
			
		case RhqConstants.RHQ_TYPE_ARCHIVE:
			manageAttributesAndSetMarkers(qName, attributes);
 			fOpenArchiveName = null;
			attrValue = attributes.getValue(RhqConstants.RHQ_ATTRIBUTE_NAME);
			if(attrValue == null){
				fOpenArchiveName = EMPTY_STRING;
				break;
			}
			fOpenArchiveName = attrValue;
			break;
		
		
		
		case RhqConstants.RHQ_TYPE_FILESET:
			manageAttributesAndSetMarkers(qName, attributes);
		
			attrValue = attributes.getValue("includes");
			//includes not found or no parent archive open
			if(attrValue == null || fOpenArchiveName == null )
				break;
			
			attrPath = new Path(attrValue);
			if(fOpenArchiveName.equals(EMPTY_STRING)){
				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Unrecognized path to archive", IMarker.SEVERITY_WARNING);
			}else{
				if(!fRhqPathExtractor.isPathToArchiveFileValid(attrPath, fOpenArchiveName))
					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "There's no such file in archive", IMarker.SEVERITY_WARNING);
			}
			
			
			
			
			
//				if(!fRhqPathExtractor.isPathToArchiveFileValid(attrPath, fOpenArchiveName))
//					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "There's no such file in archive", IMarker.SEVERITY_WARNING);
//			}
			break;
		
			
		case RhqConstants.RHQ_ELEMENT_TARGET:
			manageAttributesAndSetMarkers(qName, attributes);
			
			attrValue = attributes.getValue("name");
		    if(attrValue == null)
		    	break;
		    fExistingTargets.put(attrValue, new Integer(locator.getLineNumber()));		
		    break;
		
		case RhqConstants.RHQ_TYPE_FILE:
			manageAttributesAndSetMarkers(qName, attributes);
			attrValue = attributes.getValue("name");
			
			if(attrValue == null)
				break;
			
			attrPath = new Path(attrValue);

			if(fOpenArchiveName == null){
				//handling simple <rhq:file name=".."
				if(!fRhqPathExtractor.isPathToFileValid(attrPath))
					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "File not found", IMarker.SEVERITY_WARNING);
				break;
			}
			
			if(fOpenArchiveName.equals(EMPTY_STRING)){
					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Unrecognized path to archive", IMarker.SEVERITY_WARNING);
			}else{
				if(!fRhqPathExtractor.isPathToArchiveFileValid(attrPath, fOpenArchiveName))
					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "File not found in archive", IMarker.SEVERITY_WARNING);
			}
			break;
			
		case "include" :
		case "exclude" :
			
			manageAttributesAndSetMarkers(qName, attributes);
			attrValue = attributes.getValue("name");
			//only include with parent archive matters
			System.out.println("include "+ fOpenArchiveName);
			if(attrValue == null || fOpenArchiveName == null){
				return;
			}
			
		    attrPath = new Path(attrValue);
			
			if(!fRhqPathExtractor.isPathToArchiveFileValid(attrPath, fOpenArchiveName))
				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "File not found in archive", IMarker.SEVERITY_WARNING);
			
		    break;
		}
		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
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
	
	private void manageAttributesAndSetMarkers(String elementName, Attributes atts){
		if(!fRequiredAtts.containsKey(elementName))
			return;
	
		ArrayList<String> requiredAttributes = fRequiredAtts.get(elementName);
		for(String attName: requiredAttributes){
			if(atts.getValue(attName) == null || atts.getValue(attName).isEmpty()){
				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Attribute is mandatory: "+attName, IMarker.SEVERITY_WARNING);
			}
			
		}
	}
	
	private void setRequiredAtts(){
		fRequiredAtts = new HashMap<>();
		ArrayList<String> archive = new ArrayList<>();
		archive.add("name");
		fRequiredAtts.put(RhqConstants.RHQ_TYPE_ARCHIVE, archive);
		fRequiredAtts.put(RhqConstants.RHQ_TYPE_URL_ARCHIVE, archive);
		
		ArrayList<String> audit = new ArrayList<>();
		audit.add("status");
		audit.add("action");
		fRequiredAtts.put(RhqConstants.RHQ_TYPE_AUDIT, audit);
		
		ArrayList<String> bundle = new ArrayList<>();
		bundle.add("name");
		bundle.add("version");
		fRequiredAtts.put(RhqConstants.RHQ_TYPE_BUNDLE, bundle);
		
		ArrayList<String> unit = new ArrayList<>();
		unit.add("name");
		fRequiredAtts.put(RhqConstants.RHQ_TYPE_DEPLOYMENT_UNIT, unit);
		
		ArrayList<String> file = new ArrayList<>();
		file.add("name");
		file.add("replace");
		fRequiredAtts.put(RhqConstants.RHQ_TYPE_FILE, file);
		fRequiredAtts.put(RhqConstants.RHQ_TYPE_URL_FILE, file);
		
		ArrayList<String> property = new ArrayList<>();
		property.add("name");
		property.add("required");
		property.add("type");
		fRequiredAtts.put(RhqConstants.RHQ_TYPE_INPUT_PROPERTY, property);
		
		ArrayList<String> include = new ArrayList<>();
		include.add("name");
		fRequiredAtts.put("include", include);
		fRequiredAtts.put("exclude", include);
	
		
	}

}
