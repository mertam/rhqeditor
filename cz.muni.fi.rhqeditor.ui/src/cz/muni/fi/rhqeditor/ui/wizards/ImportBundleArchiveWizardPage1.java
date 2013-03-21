package cz.muni.fi.rhqeditor.ui.wizards;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.muni.fi.rhqeditor.core.utils.RhqConstants;

public class ImportBundleArchiveWizardPage1 extends WizardPage{


	private Composite fContainer;
	private Text txtPathToArchive;
	private Label lblRhqBundle;
	private Button fBtnBrowse;
	
	private final String EMPTY_STRING = "";
	

	protected ImportBundleArchiveWizardPage1(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		
		setDescription("Import RHQ bundle");
		fContainer = new Composite(parent, SWT.NONE);
		setControl(fContainer);
		fContainer.setLayout(new GridLayout(3, false));
		
		lblRhqBundle = new Label(fContainer, SWT.NONE);
		lblRhqBundle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblRhqBundle.setText("RHQ Bundle");
		
		txtPathToArchive = new Text(fContainer, SWT.BORDER);
		txtPathToArchive.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
			    copmpetePage();
			}
		});
		txtPathToArchive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		fBtnBrowse = new Button(fContainer, SWT.NONE);
		fBtnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = new Shell();
				openSelectFileDialog(shell);
				copmpetePage();
				
			}
		});
		fBtnBrowse.setText("Browse");
		copmpetePage();
	}
	
	private void openSelectFileDialog(Shell shell){
		FileDialog selectFileDialog = new FileDialog(shell);
		selectFileDialog.setFilterExtensions(new String[]{".zip",".jar"});
		String dir = selectFileDialog.open();	
		if(dir == null)
			dir = EMPTY_STRING;
		txtPathToArchive.setText(dir);
	}
	
	private void copmpetePage(){
		if(txtPathToArchive == null)
			setPageComplete(false);
		
		boolean complete = txtPathToArchive.getText().endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX) ||
				txtPathToArchive.getText().endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX);
		
		if(!complete){
			setErrorMessage("Only .zip and .jar archives are supported");
		}
		
		setPageComplete(complete);
			
		
	}

}
