package cz.muni.fi.rhqeditor.ui.launch;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import cz.muni.fi.rhqeditor.core.utils.InputPropertiesManager;
import cz.muni.fi.rhqeditor.core.utils.InputProperty;
import cz.muni.fi.rhqeditor.core.utils.RecipeReader;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;
import org.eclipse.swt.layout.GridLayout;


public class LaunchPropertiesTab extends AbstractLaunchConfigurationTab{
	
	private Table 		fTable;
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
		Composite myComposite = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        setControl(myComposite);
        myComposite.setLayout(new GridLayout(2,false));
       
        
        fViewer = new TableViewer(myComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.FILL);
        fTable = fViewer.getTable();
        GridData gd_fTable = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gd_fTable.heightHint = 239;
        fTable.setLayoutData(gd_fTable);
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);
        
        
       
        
        //create columns
        TableColumn columnName 		= new TableColumn (fTable, SWT.NONE);
        TableColumn columnValue 	= new TableColumn (fTable, SWT.NONE);
        TableColumn columnType	 	= new TableColumn (fTable, SWT.NONE);
        TableColumn columnRequired 	= new TableColumn (fTable, SWT.NONE);
        
        columnName.setWidth(120);
        columnName.setText("name");
        columnValue.setWidth(150);
        columnValue.setText("value");
        columnRequired.setWidth(50);
        columnRequired.setText("required");
        columnType.setWidth(100);
        columnType.setText("type");
        
        
        final TableEditor tableEditor = new TableEditor(fTable);
        
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
        		System.getProperty("file.separator")+RhqConstants.RHQ_DEFAULT_DEPLOY_DIR);
        new Label(myComposite, SWT.NONE);
        
        fLblPathToDeploy = new Label(myComposite, SWT.NONE);
        fLblPathToDeploy.setText("Path to deploy directory (rhq.deploy.dir)");
        new Label(myComposite, SWT.NONE);
        
        fTxtDeployDir = new Text(myComposite, SWT.BORDER);
        GridData gd_fTxtDeployDir = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_fTxtDeployDir.widthHint = 381;
        fTxtDeployDir.setLayoutData(gd_fTxtDeployDir);
        fTxtDeployDir.addModifyListener(new ModifyListener() {
        	public void modifyText(ModifyEvent e) {
        		updateLaunchConfigurationDialog();
        	}
        });
        
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
        fTable.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				 Rectangle clientArea = fTable.getClientArea();
		         Point pt = new Point(event.x, event.y);
		         int index = fTable.getTopIndex();
		         while (index < fTable.getItemCount()) {
		           boolean visible = false;
		           final TableItem item = fTable.getItem(index);
		           
		             Rectangle rect = item.getBounds(1);
		             if (rect.contains(pt)) {
		               final int column = 1;
		               final Text text = new Text(fTable, SWT.NONE);
		               Listener textListener = new Listener() {
		                 public void handleEvent(final Event e) {
		                   switch (e.type) {
		                   case SWT.FocusOut:
		                     item.setText(column, text.getText());
		                     text.dispose();
		                     break;
		                   case SWT.Traverse:
		                     switch (e.detail) {
		                     case SWT.TRAVERSE_RETURN:
		                       item
		                           .setText(column, text
		                               .getText());
		                     // FALL THROUGH
		                     case SWT.TRAVERSE_ESCAPE:
		                       text.dispose();
		                       e.doit = false;
		                     }
		                     break;
		                   }
		                 }
		               };
		               text.addListener(SWT.FocusOut, textListener);
		               text.addListener(SWT.Traverse, textListener);
		               tableEditor.setEditor(text, item, 1);
		               text.setText(item.getText(1));
		               text.selectAll();
		               text.setFocus();
		               updateLaunchConfigurationDialog();
		               return;
		             }
		             if (!visible && rect.intersects(clientArea)) {
		               visible = true;
		             }
		           
		           if (!visible){
		        	   updateLaunchConfigurationDialog();
		        	   return;
		           }
		           index++;
		         }
		         updateLaunchConfigurationDialog();
		       }
		     });
        tableEditor.horizontalAlignment = SWT.LEFT;
	    tableEditor.grabHorizontal = true;
        
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
		        		fProjectName + System.getProperty("file.separator") + RhqConstants.RHQ_DEFAULT_DEPLOY_DIR);
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
