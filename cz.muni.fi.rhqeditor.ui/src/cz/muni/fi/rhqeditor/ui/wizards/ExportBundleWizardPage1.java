package cz.muni.fi.rhqeditor.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.muni.fi.rhqeditor.core.utils.ExtractorProvider;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;


public class ExportBundleWizardPage1 extends WizardPage {

	private Composite fContainer;
	private Text txtExportDir;
	
	private boolean fActive = true;
	
	private Combo fComboProject;

	
	
	private String EMPTY_STRING = "";

	private String fProjectName = EMPTY_STRING;
	private String fBundleName = EMPTY_STRING;
			
	protected ExportBundleWizardPage1(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}
	


	@Override
	public void createControl(Composite parent) {
		
		
		fContainer = new Composite(parent, SWT.NONE);
        fContainer.setEnabled(fActive);
        setErrorMessage(null);
        setDescription("Export RHQ Bundle");
        
        setControl(fContainer);
        fContainer.setLayout(new GridLayout(4, false));
        
        		
                Label lblSelectProjectTo = new Label(fContainer, SWT.NONE);
                lblSelectProjectTo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
                lblSelectProjectTo.setText("Select project to export:");
        
        fComboProject = new Combo(fContainer, SWT.READ_ONLY);
        ExtractorProvider provider = ExtractorProvider.getInstance();
		String[] projects = provider.listProjects();
		fComboProject.setItems(projects);
        
		for(int i = 0; i != fComboProject.getItemCount(); i++){
			if(fComboProject.getItem(i).equals(fProjectName)){
				fComboProject.select(i);
				break;
			}
				
		}
        
        fComboProject.addModifyListener(new ModifyListener() {
        	public void modifyText(ModifyEvent e) {
        		fProjectName = fComboProject.getItem(fComboProject.getSelectionIndex());
        	}
        });
   
        
        Label lblExportDir = new Label(fContainer, SWT.NONE);
        lblExportDir.setText("Export as:");

        
        txtExportDir = new Text(fContainer, SWT.BORDER);
        GridData gd_txtExportDir = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gd_txtExportDir.widthHint = 463;
        txtExportDir.setLayoutData(gd_txtExportDir);
        txtExportDir.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(txtExportDir.getText().isEmpty()){
					fBundleName = EMPTY_STRING;
				}
				else{
					fBundleName = txtExportDir.getText();
				}
				if(!validateFileName()){
        			setErrorMessage("Only .zip of .jar archives allowed.");
        		} else {
        			setErrorMessage(null);
        		}
				pageComplete();
			}
		});
       

        Button btnBrowse = new Button(fContainer, SWT.NONE);
        btnBrowse.setText("Browse");
        btnBrowse.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		openFileDialog(new Shell());
        		if(!validateFileName()){
        			setErrorMessage("Only .zip of .jar archives allowed.");
        		} else {
        			setErrorMessage(null);
        		}
       			pageComplete();
        		super.widgetSelected(e);
        	}
        });
    
        pageComplete();
        
	}
	
	public void setInactive(){
		fActive = false;
	}
	
	public void setProject(String projectName){
		if(projectName == null)
			fProjectName = EMPTY_STRING;
		else
			fProjectName = projectName;
		
	}
	
	public String getProject(){
		return fProjectName;
	}
	
	public String getTargetFile(){
		return fBundleName;
	}
	
	private void openFileDialog(Shell shell){
		FileDialog dialog = new FileDialog(shell,SWT.SAVE);
		dialog.setFilterExtensions(new String[]{"*.zip","*.jar"});
		fBundleName = dialog.open();	
		if(fBundleName == null)
			fBundleName = EMPTY_STRING;
		txtExportDir.setText(fBundleName);
	}
	
	protected boolean validateFileName(){
		if(fBundleName.endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX)
				|| fBundleName.endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX))
			return true;
		return false;
	}
	

	private void pageComplete(){
		boolean valid = (fProjectName.equals(EMPTY_STRING) ? false : true);
		setPageComplete(validateFileName() && valid);
	}
}
