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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class NewProjectWizardPage1 extends WizardPage{

	/**
	 * @uml.property  name="container"
	 * @uml.associationEnd  
	 */
	private Composite 			container;
	/**
	 * @uml.property  name="btnUseDeafultLocation"
	 * @uml.associationEnd  
	 */
	private Button 				btnUseDeafultLocation;
	/**
	 * @uml.property  name="btnDirectoryDialog"
	 * @uml.associationEnd  
	 */
	private Button 				btnDirectoryDialog;
	/**
	 * @uml.property  name="txtProjectName"
	 * @uml.associationEnd  
	 */
	private Text 				txtProjectName;
	/**
	 * @uml.property  name="txtProjectLocation"
	 * @uml.associationEnd  
	 */
	private Text 				txtProjectLocation;
	/**
	 * @uml.property  name="lblLocation"
	 * @uml.associationEnd  
	 */
	private Label				lblLocation;
	/**
	 * @uml.property  name="lblProjectName"
	 * @uml.associationEnd  
	 */
	private Label 				lblProjectName;
	/**
	 * @uml.property  name="lblProjectNameCheck"
	 * @uml.associationEnd  
	 */
	private Label				lblProjectNameCheck;
	
	//deafult null, only DirectoryDialog can reset
	/**
	 * @uml.property  name="newProjectPath"
	 */
	private String				newProjectPath = null;
	
	/**
	 * @uml.property  name="newProjectName"
	 */
	private String				newProjectName = null;

	
//	protected NewProjectWizardPage1(String pageName) {
//		super(pageName);
//		// TODO Auto-generated constructor stub
//	}

	protected NewProjectWizardPage1(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	protected NewProjectWizardPage1(String pageName){
		super(pageName);
	}
	

	@Override
	public void createControl(Composite parent) {
		
		setTitle("RHQ bunle");
		setPageComplete(false);

		container = new Composite(parent, SWT.NONE);
		container.setBounds(47, 39, 512, 213);
		lblProjectNameCheck = new Label(container, SWT.NONE);
		lblProjectNameCheck.setBounds(118, 16, 100, 20);
		setDescription("Enter project name.");
		
		lblProjectName = new Label(container, SWT.NONE);
		lblProjectName.setBounds(10, 43, 108, 17);
		lblProjectName.setText("Project name:");
		
		txtProjectName = new Text(container, SWT.BORDER);
		txtProjectName.setBounds(118, 36, 334, 29);
		txtProjectName.addModifyListener(new ModifyListener() {
		
			@Override
			public void modifyText(ModifyEvent e) {
			//project name validation
			validateProjectName();
			}
		});
		
		lblLocation = new Label(container, SWT.NONE);
		lblLocation.setEnabled(false);
		lblLocation.setBounds(10, 106, 65, 17);
		lblLocation.setText("Location:");

		btnUseDeafultLocation = new Button(container, SWT.CHECK);
		btnUseDeafultLocation.setSelection(true);
		btnUseDeafultLocation.setBounds(10, 78, 175, 22);
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
			}
		});


		txtProjectLocation = new Text(container, SWT.BORDER);
		txtProjectLocation.setEnabled(false);
		txtProjectLocation.setText(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
		txtProjectLocation.setBounds(86, 107, 291, 25);
				
		btnDirectoryDialog = new Button(container, SWT.BUTTON1);
		btnDirectoryDialog.setText("Browse...");
		btnDirectoryDialog.setBounds(394, 103, 108, 29);
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
					System.out.println(projects[i].getName());
					projectExist = true;
					break;
				}
			}
				
			if(!projectExist){
				setDescription("Create a RHQ Bundle project in the workspace or in an external location.");
				newProjectName = txtProjectName.getText();
				setPageComplete(true);
				return;
			}else{
				setDescription("Project with this name already in workspace");		
				setPageComplete(false);
				return;
			}

		}else{
			setDescription("Project name contains invalid characters");		
			setPageComplete(false);
			return;
		}
	}
	
//---------------------------------------------------------------------------------	

//---------------------------------------------------------------------------------
	/**
	 * @return
	 * @uml.property  name="newProjectName"
	 */
	public String getNewProjectName()
	{
		return newProjectName;
	}
	
	/**
	 * @return
	 * @uml.property  name="newProjectPath"
	 */
	public String getNewProjectPath()
	{
		return newProjectPath;
	}
	


}