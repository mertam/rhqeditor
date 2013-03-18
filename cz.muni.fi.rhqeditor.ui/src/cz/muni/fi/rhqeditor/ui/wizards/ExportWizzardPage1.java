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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.muni.fi.rhqeditor.core.utils.RhqConstants;


public class ExportWizzardPage1 extends WizardPage {

	private Composite fContainer;
	private Text txtExportDir;
	private Text txtFileName;
	
	private Label lblWarning;
	
	private boolean fActive = true;
	
	private boolean fileNameValid = false;
	private boolean folderValid = false;

	
	private String EMPTY_STRING = "";
	
	private String exportDir = EMPTY_STRING;
			
	protected ExportWizzardPage1(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}
	
	public String getTargetFile(){
		String target = txtExportDir.getText();
		if(!target.endsWith(System.getProperty("file.separator"))){
			target = target.concat(System.getProperty("file.separator"));
		}
		target = target.concat(txtFileName.getText());
		return target;
	}

	@Override
	public void createControl(Composite parent) {
		
		
		fContainer = new Composite(parent, SWT.NONE);
        fContainer.setLayout(new GridLayout(4, false));
        fContainer.setEnabled(fActive);
        if(!fActive)
        	setErrorMessage("Only RHQ project can be exported");
        pageComplete();
        
        setControl(fContainer);
        
        Label lblExportDir = new Label(fContainer, SWT.NONE);
        lblExportDir.setText("Export into directory");
        new Label(fContainer, SWT.NONE);

        
        txtExportDir = new Text(fContainer, SWT.BORDER);
        GridData gd_txtExportDir = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_txtExportDir.widthHint = 435;
        txtExportDir.setLayoutData(gd_txtExportDir);
        txtExportDir.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(txtExportDir.getText().isEmpty()){
					folderValid = false;
					exportDir = txtExportDir.getText();
				}
				else{
					exportDir = EMPTY_STRING;
					folderValid = true;
				}
			}
		});
       

        Button btnBrowse = new Button(fContainer, SWT.NONE);
        btnBrowse.setText("Browse");
        btnBrowse.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		openSelectDirectoryDialog(new Shell());
        		if(txtExportDir.getText().isEmpty())
        			folderValid = false;
        		else
        			folderValid = true;
        		super.widgetSelected(e);
        	}
        });
        
        Label lblFileName = new Label(fContainer, SWT.NONE);
        lblFileName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblFileName.setText("Bundle name:");
        new Label(fContainer, SWT.NONE);
        
        txtFileName = new Text(fContainer, SWT.BORDER);
        txtFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(fContainer, SWT.NONE);
        new Label(fContainer, SWT.NONE);
        new Label(fContainer, SWT.NONE);
        
        
        lblWarning = new Label(fContainer, SWT.NONE);
        lblWarning.setText("warning");
        lblWarning.setVisible(true);
        new Label(fContainer, SWT.NONE);
        txtFileName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				System.out.println("modify " + txtFileName.getText());
				if(validateFileName()){
					lblWarning.setVisible(false);
					fileNameValid = true;
				}else{
					lblWarning.setText("Only .jar or .zip archives supported");
					lblWarning.setVisible(true);
					fileNameValid = false;
				}
				pageComplete();
			}
		});
        
	}
	
	public void setInactive(){
		fActive = false;
	}
	
	
	
	private void openSelectDirectoryDialog(Shell shell){
		DirectoryDialog selectDirDialog = new DirectoryDialog(shell);
		selectDirDialog.setMessage("Choose export directory");
		exportDir = selectDirDialog.open();	
		if(exportDir == null)
			exportDir = EMPTY_STRING;
		txtExportDir.setText(exportDir);
	}
	
	protected boolean validateFileName(){
		if(txtFileName.getText().endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX)
				|| txtFileName.getText().endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX))
			return true;
		return false;
	}
	

	private void pageComplete(){
		setPageComplete(fileNameValid && folderValid);
	}

}
