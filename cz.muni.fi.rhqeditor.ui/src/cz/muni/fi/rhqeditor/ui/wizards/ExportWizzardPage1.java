package cz.muni.fi.rhqeditor.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.muni.fi.rhqeditor.core.utils.ExtractorProvider;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;


public class ExportWizzardPage1 extends WizardPage {

	private Composite fContainer;
	private Text txtExportDir;
	
	private boolean fActive = true;
	
	private Combo fComboProject;

	
	
	private String EMPTY_STRING = "";

	private String fProjectName = EMPTY_STRING;
	private String fBundleName = EMPTY_STRING;
			
	protected ExportWizzardPage1(String pageName, String title,
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
        fContainer.setLayout(new FormLayout());
        
        Label lblExportDir = new Label(fContainer, SWT.NONE);
        FormData fd_lblExportDir = new FormData();
        fd_lblExportDir.left = new FormAttachment(0, 10);
        lblExportDir.setLayoutData(fd_lblExportDir);
        lblExportDir.setText("Export as:");

        
        txtExportDir = new Text(fContainer, SWT.BORDER);
        FormData fd_txtExportDir = new FormData();
        fd_txtExportDir.left = new FormAttachment(lblExportDir, 6);
        txtExportDir.setLayoutData(fd_txtExportDir);
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
        fd_txtExportDir.right = new FormAttachment(100, -140);
        FormData fd_btnBrowse = new FormData();
        fd_btnBrowse.bottom = new FormAttachment(txtExportDir, 0, SWT.BOTTOM);
        fd_btnBrowse.left = new FormAttachment(txtExportDir, 6);
        btnBrowse.setLayoutData(fd_btnBrowse);
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
        
        fComboProject = new Combo(fContainer, SWT.READ_ONLY);
        fComboProject.addModifyListener(new ModifyListener() {
        	public void modifyText(ModifyEvent e) {
        		fProjectName = fComboProject.getItem(fComboProject.getSelectionIndex());
        	}
        });
        fd_txtExportDir.top = new FormAttachment(fComboProject, 11);
        FormData fd_combo = new FormData();
        fd_combo.top = new FormAttachment(0, 5);
        fd_combo.left = new FormAttachment(0, 122);
        fComboProject.setLayoutData(fd_combo);
        
		ExtractorProvider provider = ExtractorProvider.getInstance();
		String[] projects = provider.listProjects();
		fComboProject.setItems(projects);
		
		for(int i = 0; i != fComboProject.getItemCount(); i++){
			if(fComboProject.getItem(i).equals(fProjectName)){
				fComboProject.select(i);
				break;
			}
				
		}

		
        Label lblSelectProjectTo = new Label(fContainer, SWT.NONE);
        fd_lblExportDir.top = new FormAttachment(lblSelectProjectTo, 21);
        FormData fd_lblSelectProjectTo = new FormData();
        fd_lblSelectProjectTo.top = new FormAttachment(0, 10);
        fd_lblSelectProjectTo.right = new FormAttachment(fComboProject, -6);
        lblSelectProjectTo.setLayoutData(fd_lblSelectProjectTo);
        lblSelectProjectTo.setText("Select project to export:");
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
