package cz.muni.fi.rhqeditor.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

import cz.muni.fi.rhqeditor.core.RhqConstants;
import cz.muni.fi.rhqeditor.core.RhqPathExtractor;

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
//	private InputSource		 	fInput				= null;

	private IDocument			fDocument 			= null;
	
	//holds name of last open tag rhq:archive name=".."
	private String				fOpenArchiveName 	= null;
	//sets for validating targets <target name, line>
	private HashMap<String, Integer > fExistingTargets	= null;
	private HashMap<String,	Integer > fRequiredTargets  = null;
	
	Locator locator;
	
	private final String EMPTY_STRING = "";
	
	
	/**
	 * 
	 * @param recipe	- IResource corresponding to recipe ("deploy.xml")
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public RhqRecipeValidator() {
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
//	
//	public void setCoursorPosition(int pos){
//		fCursorPosition = pos;
//	}
//	
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
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
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
		
		if(qName.equals(RhqConstants.RHQ_ELEMENT_ARCHIVE)){
			fOpenArchiveName = null;
			attrValue = getAttributeValue(qName, attributes, RhqConstants.RHQ_ATTRIBUTE_NAME,true);
			if(attrValue == null){
				fOpenArchiveName = EMPTY_STRING;
				return;
			}
			fOpenArchiveName = attrValue;
		}
		
		if(qName.equals(RhqConstants.RHQ_ELEMENT_TARGET)){
			attrValue = getAttributeValue(qName, attributes, RhqConstants.RHQ_ATTRIBUTE_NAME,true);
		    if(attrValue == null)
		    	return;
		    fExistingTargets.put(attrValue, new Integer(locator.getLineNumber()));		
		}
		
		if(qName.equals(RhqConstants.RHQ_TYPE_DEPLOYMENT_UNIT)){
			attrValue = getAttributeValue(qName, attributes, RhqConstants.RHQ_ATTRIBUTE_PREINSTALL_TARGET,false);
			if(attrValue != null)
				fRequiredTargets.put(attrValue,new Integer(locator.getLineNumber()));
			attrValue = getAttributeValue(qName, attributes, RhqConstants.RHQ_ATTRIBUTE_POSTINSTALL_TARGET,false);
			if(attrValue != null)
				fRequiredTargets.put(attrValue,new Integer(locator.getLineNumber()));
			return;
		}
		//----------
		if(qName.equals(RhqConstants.RHQ_TYPE_FILE)){
			
			attrValue = getAttributeValue(qName, attributes, RhqConstants.RHQ_ATTRIBUTE_NAME,true);
			if(attrValue == null){
				//at this point markers are already set
				return;
			}
			IPath attrPath = new Path(attrValue);

			if(fOpenArchiveName == null){
				//handling simple <rhq:file name=".."
				if(!fRhqPathExtractor.isPathToFileValid(attrPath))
					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "File not found", IMarker.SEVERITY_WARNING);
				return;
			}
			
			if(fOpenArchiveName.equals(EMPTY_STRING)){
					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Unrecognized path to archive", IMarker.SEVERITY_WARNING);
			}else{
				if(!fRhqPathExtractor.isPathToArchiveFileValid(attrPath, fOpenArchiveName))
					fRhqAnnotationModel.addMarker(locator.getLineNumber(), "File not found in archive", IMarker.SEVERITY_WARNING);
			}
		
				

//			else if(attributes.getValue(attrIndex)){
//				
//			}
		}

		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals(RhqConstants.RHQ_ELEMENT_ARCHIVE)){
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
	 * returns value of attr. sets marker if missing and is required
	 * @return
	 */
	private String getAttributeValue(String elementName, Attributes attributes, String desiredName, boolean required){
		int attrIndex = attributes.getIndex(desiredName);
		if(attrIndex == -1){
			if(required)
				fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Attribute \""+desiredName+"\" is mandatory", IMarker.SEVERITY_WARNING);
			return null;
		} 
		String attrValue = attributes.getValue(attrIndex);
		//attribute is empty
		if(attrValue == null){
			fRhqAnnotationModel.addMarker(locator.getLineNumber(), "Missing path to file", IMarker.SEVERITY_WARNING);
		}
		return attrValue;
		
		
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

}
