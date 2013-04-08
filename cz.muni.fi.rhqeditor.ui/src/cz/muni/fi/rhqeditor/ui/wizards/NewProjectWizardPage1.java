package cz.muni.fi.rhqeditor.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class NewProjectWizardPage1 extends WizardPage{


	private Composite 			container;

	private Button 				btnUseDeafultLocation;

	private Button 				btnDirectoryDialog;

	private Text 				txtProjectName;

	private Text 				txtProjectLocation;

	private Label				lblLocation;

	private Label 				lblProjectName;
	
	private Text				txtBundleName;
	private Text 				txtBundleVersion;
	
	//deafult null, only DirectoryDialog can reset

	private String				newProjectPath = null;
	
	private String				newProjectName = null;

	private boolean 			firstChange = true;

	
//	protected NewProjectWizardPage1(String pageName) {
//		super(pageName);
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * @wbp.parser.constructor
	 */
	protected NewProjectWizardPage1(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	protected NewProjectWizardPage1(String pageName){
		super(pageName);
	}
	

	/**
	 * @wbp.parser.constructor
	 */
	@Override
	public void createControl(Composite parent) {
		
		setTitle("RHQ bunle");
		setPageComplete(false);

		container = new Composite(parent, SWT.NONE);
		container.setBounds(47, 39, 512, 213);
		
		setErrorMessage(null);
		setDescription("Enter project name.");
		container.setLayout(new FormLayout());
		
		lblProjectName = new Label(container, SWT.NONE);
		FormData fd_lblProjectName = new FormData();
		fd_lblProjectName.top = new FormAttachment(0, 29);
		fd_lblProjectName.left = new FormAttachment(0, 5);
		lblProjectName.setLayoutData(fd_lblProjectName);
		lblProjectName.setText("Project name:");
		
		txtProjectName = new Text(container, SWT.BORDER);
		FormData fd_txtProjectName = new FormData();
		fd_txtProjectName.right = new FormAttachment(0, 362);
		fd_txtProjectName.top = new FormAttachment(0, 24);
		fd_txtProjectName.left = new FormAttachment(0, 78);
		txtProjectName.setLayoutData(fd_txtProjectName);
		txtProjectName.addModifyListener(new ModifyListener() {
		
			@Override
			public void modifyText(ModifyEvent e) {
			//project name validation
			validateProjectName();
			}
		});
		
		Label lblBundleName = new Label(container, SWT.NONE);
		FormData fd_lblBundleName = new FormData();
		fd_lblBundleName.top = new FormAttachment(0, 58);
		fd_lblBundleName.left = new FormAttachment(0, 5);
		lblBundleName.setLayoutData(fd_lblBundleName);
		lblBundleName.setText("Bundle name:");
		
		txtBundleName = new Text(container, SWT.BORDER);
		FormData fd_txtBundleName = new FormData();
		fd_txtBundleName.right = new FormAttachment(0, 239);
		fd_txtBundleName.top = new FormAttachment(0, 53);
		fd_txtBundleName.left = new FormAttachment(0, 78);
		txtBundleName.setLayoutData(fd_txtBundleName);
		
		Label lblBundleVersion = new Label(container, SWT.NONE);
		FormData fd_lblBundleVersion = new FormData();
		fd_lblBundleVersion.top = new FormAttachment(0, 87);
		fd_lblBundleVersion.left = new FormAttachment(0, 5);
		lblBundleVersion.setLayoutData(fd_lblBundleVersion);
		lblBundleVersion.setText("Bundle version:");
		
		txtBundleVersion = new Text(container, SWT.BORDER);
		FormData fd_txtBundleVersion = new FormData();
		fd_txtBundleVersion.right = new FormAttachment(txtBundleName, 0, SWT.RIGHT);
		fd_txtBundleVersion.top = new FormAttachment(0, 82);
		fd_txtBundleVersion.left = new FormAttachment(0, 78);
		txtBundleVersion.setLayoutData(fd_txtBundleVersion);

		btnUseDeafultLocation = new Button(container, SWT.CHECK);
		FormData fd_btnUseDeafultLocation = new FormData();
		fd_btnUseDeafultLocation.top = new FormAttachment(0, 111);
		fd_btnUseDeafultLocation.left = new FormAttachment(0, 5);
		btnUseDeafultLocation.setLayoutData(fd_btnUseDeafultLocation);
		btnUseDeafultLocation.setSelection(true);
		btnUseDeafultLocation.setText("Use deafult location");
		btnUseDeafultLocation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(btnUseDeafultLocation.getSelection())
				{
					btnDirectoryDialog.setEnabled(false);
					lblLocation.setEnabled(false);
					txtProjectLocation.setEnabled(false);
					btnUseDeafultLocation.setSelection(true);
				}
				else
				{
					btnDirectoryDialog.setEnabled(true);
					lblLocation.setEnabled(true);
					txtProjectLocation.setEnabled(true);
					btnUseDeafultLocation.setSelection(false);
				}
				
				if(firstChange){
					txtProjectLocation.setText("");
					firstChange = false;
				}
			}
		});
		
		lblLocation = new Label(container, SWT.NONE);
		FormData fd_lblLocation = new FormData();
		fd_lblLocation.top = new FormAttachment(0, 144);
		fd_lblLocation.left = new FormAttachment(0, 5);
		lblLocation.setLayoutData(fd_lblLocation);
		lblLocation.setEnabled(false);
		lblLocation.setText("Location:");


		txtProjectLocation = new Text(container, SWT.BORDER);
		FormData fd_txtProjectLocation = new FormData();
		fd_txtProjectLocation.top = new FormAttachment(0, 139);
		fd_txtProjectLocation.left = new FormAttachment(0, 52);
		txtProjectLocation.setLayoutData(fd_txtProjectLocation);
		txtProjectLocation.setEnabled(false);
		txtProjectLocation.setMessage(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
				
		btnDirectoryDialog = new Button(container, SWT.BUTTON1);
		fd_txtProjectLocation.right = new FormAttachment(btnDirectoryDialog, -6);
		FormData fd_btnDirectoryDialog = new FormData();
		fd_btnDirectoryDialog.top = new FormAttachment(0, 139);
		fd_btnDirectoryDialog.right = new FormAttachment(100, -97);
		btnDirectoryDialog.setLayoutData(fd_btnDirectoryDialog);
		btnDirectoryDialog.setText("Browse...");
		btnDirectoryDialog.setEnabled(false);		
		btnDirectoryDialog.addSelectionListener(new SelectionAdapter() {
		      @Override
		      public void widgetSelected(SelectionEvent e) {
		    	Shell shell = new Shell();
		        openSelectDirectoryDialog(shell);
		        if(newProjectPath != null)
		        	txtProjectLocation.setText(newProjectPath);
		      }
		    });

		this.setControl(container);
		
	}
//---------------------------------------------------------------------------------
	private void openSelectDirectoryDialog(Shell shell){
		DirectoryDialog selectDirDialog = new DirectoryDialog(shell);
		selectDirDialog.setMessage("Choose directory for project content");
		newProjectPath = selectDirDialog.open();		
	}
//---------------------------------------------------------------------------------
	/**
	 * Validates project name 
	 * Sets titles and page complete
	 */
	private void validateProjectName()
	{
		boolean projectExist = false;
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		if(txtProjectName.getText().isEmpty())
		{
			setErrorMessage(null);
			setDescription("Enter project name.");
			setPageComplete(false);
			return;
		}
		
		if(ResourcesPlugin.getWorkspace().validateName(txtProjectName.getText(),IResource.PROJECT).isOK())
		{
		
			for(int i = 0; i < projects.length; i++)
			{
				if(projects[i].getName().equals(txtProjectName.getText()))
				{
					projectExist = true;
					break;
				}
			}
				
			if(!projectExist){
				setErrorMessage(null);
				setDescription("Create a RHQ Bundle project in the workspace or in an external location.");
				newProjectName = txtProjectName.getText();
				setPageComplete(true);
				return;
			}else{
				setErrorMessage("Project with this name already in workspace");		
				setPageComplete(false);
				return;
			}

		}else{
			setErrorMessage("Project name contains invalid characters");		
			setPageComplete(false);
			return;
		}
	}
	
	

	public String getNewProjectName()
	{
		return newProjectName;
	}
	
	public String getNewProjectPath()
	{
		return (txtProjectLocation.getText().isEmpty() ? null : txtProjectLocation.getText());
	}
	
	public String getBundleName(){
		return txtBundleName.getText();
	}
	
	public String getBundleVersion(){
		return txtBundleVersion.getText();
	}
}