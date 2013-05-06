package cz.muni.fi.rhqeditor.core.launch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import cz.muni.fi.rhqeditor.core.Activator;
import cz.muni.fi.rhqeditor.core.utils.ArchiveReader;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;



/**
 * class represents singleton provider, which creates and holds information about Deployer in temp direcory
 * @author syche
 *
 */
public enum DeployerProvider {
	
	INSTANCE;
    
	private Path fDeployerDirPath = null;
    private Path fDeployerPath = null;
    private static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    private DeployerProvider() {
    }
 
    /**
     * @return returns java.nio.file.Path representing current deployer path (rhq-ant or rhq-ant.bat)
     */
    public Path getDeployerPath(){
    	return fDeployerPath;
    }
    
    /**
     * sets current create directory in temp folder for unpacking deployer files
     * @param prefix prefix of the directory name
     * @throws IOException 
     */
    private void setDirectory(String prefix) throws IOException {
    	fDeployerDirPath = Files.createTempDirectory(prefix);
    	fDeployerDirPath.toFile().deleteOnExit();
    	
    }
    
    /**
     * initializes local deployer
     * @return String reprezenting path to local deployer
     */
    public String initializeLocalDeployer() {
		try {
			initializeDeployer(Activator.getFileURL(RhqConstants.RHQ_STANDALONE_DEPLOYER_URL));
		} catch (IOException e) {
			Activator.getLog().log(new Status(IStatus.WARNING,RhqConstants.PLUGIN_CORE_ID,"DeployerProvider.initializeLocalDeployer " + e.getMessage()));
		}
		Path path = getDeployerPath();
		if (path == null || !isExexutable()) {
			return RhqConstants.NOT_FOUND;
		}
		return path.toString();
    }
    

    
    /**
     * determinate whether is fDeployerPath executable
     * @return false if fDeployerPath is null of not executable
     */
    private boolean isExexutable(){
    	if(fDeployerPath == null)
    		return false;
    	return Files.isExecutable(fDeployerPath);
    }
    
    /**
     * initialize deployer
     * @param zippedDeployer URL of archive to unzip
     * @throws IOException
     */
    private void initializeDeployer(URL zippedDeployer) throws IOException{
 
		//avoid multiple initialization
		if(isExexutable())
			return;
		
		setDirectory("rhq_standalone_deployer");
		InputStream in = zippedDeployer.openStream();													
		String pathToArchive = fDeployerDirPath.toString() + FILE_SEPARATOR + RhqConstants.RHQ_STANDALONE_DEPLOYER;
		File f = new File(pathToArchive);
		FileOutputStream out = new FileOutputStream(f);
		f.deleteOnExit();
		ArchiveReader.copyInputStream(in, out);
		ArchiveReader.unzipArchive(pathToArchive, fDeployerDirPath.toString(),true);
		
		out.close();
		in.close();
		//removes .zip
		String deployerDir = RhqConstants.RHQ_STANDALONE_DEPLOYER.toString().substring(0,RhqConstants.RHQ_STANDALONE_DEPLOYER.length() - 4);
		
		if(System.getProperty("os.name").equals("Windows")) {
			fDeployerPath = FileSystems.getDefault().getPath(fDeployerDirPath +FILE_SEPARATOR+
					deployerDir+FILE_SEPARATOR+"bin"+FILE_SEPARATOR+"rhq-ant.bat");
		} else {
    		fDeployerPath = FileSystems.getDefault().getPath(fDeployerDirPath +FILE_SEPARATOR+
    				deployerDir+FILE_SEPARATOR+"bin"+FILE_SEPARATOR+"rhq-ant");
		}
		setPermisions();
		if(!isExexutable())
			throw new IOException("Deployer isn't executable");
			
    }
    
    	
    /**
     * adds posix permission to run deployer, if unix-like system is used.
     */
    private void setPermisions() throws IOException{
    	if(!isExexutable()){
    		if(System.getProperty("os.name").equalsIgnoreCase("Windows")) {
    			//do nothing?
    		} else {
				Set<PosixFilePermission> perms = Files.getPosixFilePermissions(fDeployerPath);
				perms.add(PosixFilePermission.OWNER_EXECUTE);
				Files.setPosixFilePermissions(fDeployerPath, perms);
				
    		}
    	}
    	
    }
    
    

}
