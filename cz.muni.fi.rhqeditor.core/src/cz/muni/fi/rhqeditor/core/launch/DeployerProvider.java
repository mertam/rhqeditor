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
 
//    public static DeployerProvider getInstance() {
//        return instance;
//    }
    
    public String getDirectory(){
    	return fDeployerDirPath.toString();
    }
    
    public void setDirectory(String prefix) throws IOException {
    	fDeployerDirPath = Files.createTempDirectory(prefix);
    	fDeployerDirPath.toFile().deleteOnExit();
    	
    }
    
    public Path getDeployerPath(){
    	return fDeployerPath;
    }
    
    public boolean isExexutable(){
    	if(fDeployerPath == null)
    		return false;
    	return Files.isExecutable(fDeployerPath);
    }
    
    public void initializeDeployer(URL zippedDeployer) throws IOException{
 
    		//avoid multiple initialization
    		if(isDeployerExecutable())
    			return;
    		
    		setDirectory("deployer");
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
			if(!isDeployerExecutable())
				throw new IOException("Deployer isn't executable");
			
    }
    
    /**
     * checks whether is deployer runnable
     * @return
     */
    private boolean isDeployerExecutable(){
    	if(fDeployerPath == null)
    		return false;
    	return (new File(fDeployerPath.toString())).canExecute();
    }
    	
    /**
     * adds posix permission to run deployer, if unix-like system is used.
     */
    private void setPermisions(){
    	if(!isExexutable()){
    		if(System.getProperty("os.name").equalsIgnoreCase("Windows")) {
    		} else {
    			 try {
					Set<PosixFilePermission> perms = Files.getPosixFilePermissions(fDeployerPath);
					perms.add(PosixFilePermission.OWNER_EXECUTE);
					Files.setPosixFilePermissions(fDeployerPath, perms);
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    	
    }
    
    

}
