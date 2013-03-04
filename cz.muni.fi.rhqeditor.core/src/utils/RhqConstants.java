package utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RhqConstants {

	public static final int 	RHQ_VERSION_DEFAULT 	= 0;
	public static final int 	RHQ_VERSION_4_5_1 		= 0;
	
									
	public static final String 	RHQ_NATURE_ID =  "cz.muni.fi.rhqeditor.natures.rhqeditornature";
	public static final String	RHQ_PREFIX = "rhq:";
	
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
	public static final String  RHQ_TYPE_BUNDLE				= "rhq:bundle";
	
	
	
	public static final String RHQ_ATTRIBUTE_NAME		= "name";
	public static final String RHQ_ATTRIBUTE_INCLUDES   = "includes";
	public static final String RHQ_ATTRIBUTE_PREINSTALL_TARGET = "preinstallTarget";
	public static final String RHQ_ATTRIBUTE_POSTINSTALL_TARGET = "postinstallTarget";	
	
	public static final String RHQ_ELEMENT_TARGET 		= "target";
	
	
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
	
	
	//used for handling persistent proterties
	public static final String RHQ_PROPERTY_NODE = "rhq";
	public static final String RHQ_PROPERTY_INPUT = "input-property:";
	public static final String RHQ_DEPLOY_DIR = "rhq.deploy.dir";
	public static final String RHQ_DEPLOY_NAME = "rhq.deploy.name";
	public static final String RHQ_DEPLOY_ID = "rhq.deploy.id";
	public static final String RHQ_DEPLOYER_PATH = "rhq.deployer.path";
	
	public static final String NOT_FOUND = "not found";

}
