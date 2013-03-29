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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
		container.setLayout(new GridLayout(4, false));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		setErrorMessage(null);
		setDescription("Enter project name.");
		new Label(container, SWT.NONE);
		
		lblProjectName = new Label(container, SWT.NONE);
		lblProjectName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblProjectName.setText("Project name:");
		
		txtProjectName = new Text(container, SWT.BORDER);
		GridData gd_txtProjectName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_txtProjectName.widthHint = 217;
		txtProjectName.setLayoutData(gd_txtProjectName);
		txtProjectName.addModifyListener(new ModifyListener() {
		
			@Override
			public void modifyText(ModifyEvent e) {
			//project name validation
			validateProjectName();
			}
		});
		
		Label lblBundleName = new Label(container, SWT.NONE);
		lblBundleName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblBundleName.setText("Bundle name:");
		
		txtBundleName = new Text(container, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 150;
		txtBundleName.setLayoutData(gd_text);
		new Label(container, SWT.NONE);
		
		Label lblBundleVersion = new Label(container, SWT.NONE);
		lblBundleVersion.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblBundleVersion.setText("Bundle version:");
		
		txtBundleVersion = new Text(container, SWT.BORDER);
		GridData gd_text_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text_1.widthHint = 100;
		txtBundleVersion.setLayoutData(gd_text_1);
		new Label(container, SWT.NONE);

		btnUseDeafultLocation = new Button(container, SWT.CHECK);
		btnUseDeafultLocation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
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
		new Label(container, SWT.NONE);
		
		lblLocation = new Label(container, SWT.NONE);
		lblLocation.setEnabled(false);
		lblLocation.setText("Location:");


		txtProjectLocation = new Text(container, SWT.BORDER);
		txtProjectLocation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		txtProjectLocation.setEnabled(false);
		txtProjectLocation.setMessage(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
				
		btnDirectoryDialog = new Button(container, SWT.BUTTON1);
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