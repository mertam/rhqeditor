package cz.muni.fi.rhqeditor.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RhqConstants {

	public static final int 	RHQ_VERSION_DEFAULT 	= 0;
	public static final int 	RHQ_VERSION_4_5_1 		= 0;
	
	public static final String 	RHQ_ARCHIVE_ZIP_SUFFIX 	= ".zip";
	public static final String	RHQ_ARCHIVE_JAR_SUFFIX 	= ".jar";
	
	public static final String 	RHQ_RECIPE_FILE 			= "deploy.xml";
	
	public static final String	RHQ_TYPE_FILE 				= "rhq:file";
	public static final String 	RHQ_TYPE_ARCHIVE 			= "rhq:archive";
	public static final String 	RHQ_TYPE_DEPLOYMENT_UNIT 	= "rhq:deployment-unit";
	public static final String	RHQ_TYPE_INPUT_PROPERTY 	= "rhq:input-property";
	public static final String 	RHQ_TYPE_URL_ARCHIVE 		= "rhq:url_archive";
	public static final String 	RHQ_TYPE_REPLACE 			= "rhq:replace";	
	public static final String	RHQ_TYPE_IGNORE 			= "rhq:ignore";
	public static final String 	RHQ_TYPE_URL_FILE 			= "rhq:url_file";
	public static final String 	RHQ_TYPE_AUDIT			 	= "rhq:audit";	
	public static final String 	RHQ_TYPE_FILESET			= "rhq:fileset";
	
	
	
	public static final String RHQ_ATTRIBUTE_NAME		= "name";
	public static final String RHQ_ATTRIBUTE_PREINSTALL_TARGET = "preinstallTarget";
	public static final String RHQ_ATTRIBUTE_POSTINSTALL_TARGET = "postinstallTarget";	
	
	public static final String RHQ_ELEMENT_TARGET 		= "target";
	public static final String RHQ_ELEMENT_ARCHIVE 		= "rhq:archive";	
	
	public static final Set<String> RHQ_TASK_SET = new HashSet<String>(Arrays.asList(new String[] { 
			RHQ_TYPE_FILE,
			RHQ_TYPE_ARCHIVE, 
			RHQ_TYPE_DEPLOYMENT_UNIT, 
			RHQ_TYPE_INPUT_PROPERTY,
			RHQ_TYPE_URL_ARCHIVE,
			RHQ_TYPE_REPLACE,
			RHQ_TYPE_IGNORE,
			RHQ_TYPE_URL_FILE,
			RHQ_TYPE_AUDIT,
			RHQ_TYPE_FILESET}));

	//following tags should be displayed as paired tags in editor
	public static final Set<String> RHQ_PAIRED_TAGS = new HashSet<String>(Arrays.asList(new String[] { 
			RHQ_TYPE_ARCHIVE, 
			RHQ_TYPE_DEPLOYMENT_UNIT, 
			RHQ_TYPE_REPLACE,
			RHQ_TYPE_IGNORE,
			RHQ_TYPE_AUDIT}));

	
	public static final String RHQ_MARKER_TYPE = "cz.muni.fi.rhqeditor.ui.rhqproblemmarker";

}
