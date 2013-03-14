package cz.muni.fi.rhqeditor.ui.launch;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.MultipleInputDialog;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import utils.InputPropertiesManager;
import utils.InputProperty;
import utils.RecipeReader;
import utils.RhqConstants;

public class LaunchPropertiesTab extends AbstractLaunchConfigurationTab{
	
	private Table 		fTable;
	private Button 		fBtnSelect;
	private TableViewer fViewer;
	private Text 		fTxtDeployDir;
	private Label		fLblPathToDeploy;
	private Button 		fBtnUseDefaultRhqdeploydir;
	private Button		fBtnBrowse;
	private HashMap<String, TableItem> fItems = new HashMap<>();
	
	private String		fProjectName;
	private boolean 	fUseDefaultDir;
	private String 		fLocalDirPath;
	private final String EMPTY_VALUE = "";
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createControl(Composite parent) {
		System.out.println("create control");
		Composite myComposite = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);
		GridLayout gridLayout = (GridLayout) myComposite.getLayout();
		gridLayout.horizontalSpacing = 7;
		gridLayout.makeColumnsEqualWidth = true;
        setControl(myComposite);
        
        Group grpInput = new Group(myComposite, SWT.NONE);
        GridData gd_grpInput = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_grpInput.widthHint = 471;
        gd_grpInput.heightHint = 225;
        grpInput.setLayoutData(gd_grpInput);
        grpInput.setText("Input Properties");
        
        fViewer = new TableViewer(grpInput, SWT.BORDER | SWT.FULL_SELECTION);
        fTable = fViewer.getTable();
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);
        fTable.setBounds(10, 20, 455, 163);
       
        
        //create columns
        TableColumn columnName 		= new TableColumn (fTable, SWT.NONE);
        TableColumn columnValue 	= new TableColumn (fTable, SWT.NONE);
        TableColumn columnType	 	= new TableColumn (fTable, SWT.NONE);
        TableColumn columnRequired 	= new TableColumn (fTable, SWT.NONE);
        
        columnName.setWidth(100);
        columnName.setText("name");
        columnValue.setWidth(150);
        columnValue.setText("value");
        columnRequired.setWidth(50);
        columnRequired.setText("required");
        columnType.setWidth(100);
        columnType.setText("type");
        
        
        fBtnSelect = new Button(grpInput, SWT.NONE);
        fBtnSelect.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		
        
        		TableItem items[] = fTable.getSelection();
        		if(items.length < 1)
        			return;
        		
        		TableItem item = items[0];

        		Shell shell = new Shell();
    			MultipleInputDialog dialog = new MultipleInputDialog(shell, "Input property settings");
    			dialog.addTextField(items[0].getText() , "", true);
    			dialog.create();
    			dialog.open();
    			TableItem newItem = fItems.get(item.getText(0));
        		String propertyValue = dialog.getStringValue(items[0].getText());
        		if(propertyValue == null)
        			newItem.setText(1, EMPTY_VALUE);
        		else
        			newItem.setText(1,propertyValue);
        		updateLaunchConfigurationDialog();
        		
        	}
        });
        fBtnSelect.setBounds(10, 189, 45, 29);
        fBtnSelect.setText("Edit");
        new Label(myComposite, SWT.NONE);
        
        fBtnUseDefaultRhqdeploydir = new Button(myComposite, SWT.CHECK);
        fBtnUseDefaultRhqdeploydir.setSelection(true);
        fBtnUseDefaultRhqdeploydir.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		enableCustomDeployDirWidgets(!fBtnUseDefaultRhqdeploydir.getSelection());
        		updateLaunchConfigurationDialog();
        	}
        });
        fBtnUseDefaultRhqdeploydir.setText("Use default rhq.deploy.dir "+
        		System.getProperty("file.separator")+RhqConstants.RHQ_DEFAULT_DEPLOY_DIR_PATH);
        new Label(myComposite, SWT.NONE);
        
        fLblPathToDeploy = new Label(myComposite, SWT.NONE);
        fLblPathToDeploy.setText("Path to deploy directory (rhq.deploy.dir)");
        new Label(myComposite, SWT.NONE);
        
        fTxtDeployDir = new Text(myComposite, SWT.BORDER);
        fTxtDeployDir.addModifyListener(new ModifyListener() {
        	public void modifyText(ModifyEvent e) {
        		updateLaunchConfigurationDialog();
        	}
        });
        fTxtDeployDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        fBtnBrowse = new Button(myComposite, SWT.NONE);
        fBtnBrowse.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
    		Shell shell = new Shell();
			openSelectDirDialog(shell);
			updateLaunchConfigurationDialog();
        	}
        });
        fBtnBrowse.setText("Browse");
        
        enableCustomDeployDirWidgets(!fUseDefaultDir);
		
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

		
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try{
			fTable.removeAll();
			fItems.clear();
			fProjectName = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_PROJECT,
					RhqConstants.NOT_FOUND);
			
			fUseDefaultDir = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_USE_DEFAULT_DIRECTORY,
					true);
					
			if(fProjectName.equals(RhqConstants.NOT_FOUND))
				return;
			
			fLocalDirPath = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_LOCAL_DIRECTORY,
					EMPTY_VALUE);
			
			fBtnUseDefaultRhqdeploydir.setText("Use default rhq.deploy.dir "+
		        		fProjectName + System.getProperty("file.separator") + RhqConstants.RHQ_DEFAULT_DEPLOY_DIR_PATH);
			fBtnUseDefaultRhqdeploydir.setSelection(fUseDefaultDir);
			
			fTxtDeployDir.setText(fLocalDirPath);
			enableCustomDeployDirWidgets(!fUseDefaultDir);
			fBtnUseDefaultRhqdeploydir.setSelection(fUseDefaultDir);
			initializeTable(configuration);
			
			
			
		} catch(CoreException e){
			
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_USE_DEFAULT_DIRECTORY, fBtnUseDefaultRhqdeploydir.getSelection());
		configuration.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_LOCAL_DIRECTORY, fTxtDeployDir.getText());
		applyInputProperties(configuration);
		
		
	}

	@Override
	public String getName() {
		return "Properties";
	}
	
	/**
	 * reads all input properties from recipe and stores them into table. Ignores properties without attribute name
	 */
	private void initializeTable(ILaunchConfiguration configuration){
		InputPropertiesManager manager = new InputPropertiesManager(fProjectName);
		if(RecipeReader.hasRrecipe(fProjectName) == false){
			System.out.println("no recipe");
			return;
		}
		
		try{
			//if searchedContext == null, properties are extracted from saved file
			for(InputProperty property: manager.getInputPropertiesFromRecipe(false)){
				//filter rhq.deploy.dir
				if(property.getName().equals(RhqConstants.RHQ_DEPLOY_DIR))
					continue;
				
				TableItem item = new TableItem(fTable, SWT.FULL_SELECTION);
				fItems.put(property.getName(), item);
				String value = property.getName();
				item.setText(0, (value == null ? EMPTY_VALUE : value));
				
				//loads saved value
				item.setText(1,loadProperty(configuration, property.getName()));
				
				value = property.getType();
				item.setText(2, (value == null ? EMPTY_VALUE : value));
				item.setText(3, (property.isRequired() ? "yes" : "no"));
			}

		} catch(CoreException e){
			//loading property error
			e.printStackTrace();
		}
		
	}
	
	/**
	 * loads value of property from saved configuration
	 * @param propertyName
	 * @return
	 */
	private String loadProperty(ILaunchConfiguration configuration, String propertyName){
		try {
			String value = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_INPUT_PROPERTY+"."+propertyName,
					EMPTY_VALUE);
			return value;
		} catch (CoreException e) {
			//return empty value if problem occures
			return EMPTY_VALUE;
		}

		
	}
	
	private void applyInputProperties(ILaunchConfigurationWorkingCopy configuration){
		TableItem tempItem;
		for(String propertyName: fItems.keySet()){
			tempItem = fItems.get(propertyName);
			if(tempItem == null || tempItem.getText(1) == null){
				configuration.setAttribute(
						RhqConstants.RHQ_LAUNCH_ATTR_INPUT_PROPERTY+"."+propertyName,
						EMPTY_VALUE);
			} else if(tempItem.getText(3) != null && tempItem.getText(3).equalsIgnoreCase("boolean")) {
					//handle boolean
					configuration.setAttribute(
							RhqConstants.RHQ_LAUNCH_ATTR_INPUT_PROPERTY+"."+propertyName,
							tempItem.getText(1).equalsIgnoreCase("true"));
				} else {
					//other values
					configuration.setAttribute(
							RhqConstants.RHQ_LAUNCH_ATTR_INPUT_PROPERTY+"."+propertyName,
							tempItem.getText(1));
			}
		}
		
	}
	
	private void openSelectDirDialog(Shell shell) {
		DirectoryDialog selectDirDialog = new DirectoryDialog(shell);
		selectDirDialog.setText("Select deployer");
		String path = selectDirDialog.open();
		if (path == null)
			fTxtDeployDir.setText("");
		else
			fTxtDeployDir.setText(path);
	}
	
	private void enableCustomDeployDirWidgets(boolean enabled) {
		fBtnBrowse.setEnabled(enabled);
		fTxtDeployDir.setEnabled(enabled);
		fLblPathToDeploy.setEnabled(enabled);
	}
}
