package cz.muni.fi.rhqeditor.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WorkingSetGroup;

public class NewProjectWizardPage1 extends WizardNewProjectCreationPage{

	private Text				txtBundleName;
	private Text 				txtBundleVersion;
	
	private IStructuredSelection selection;
	
	private WorkingSetGroup workingSetGroup;

	public NewProjectWizardPage1(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
	}
	
	public void setSelection(IStructuredSelection selection) {
		this.selection = selection;
	}
	
	@Override
	public void createControl(Composite parent) {
		this.setMessage("Create new RHQ bundle project");
		super.createControl(parent);    
		Composite container = (Composite) getControl();
		
		Group bunleGroup = new Group(container, SWT.NONE);
		bunleGroup.setFont(container.getFont());
		
		bunleGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		bunleGroup.setLayout(new GridLayout(3,false));
		bunleGroup.setText("RHQ Bundle");
		
		Label lblNewLabel = new Label(bunleGroup, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Bundle name: ");
		
		txtBundleName = 	new Text(bunleGroup, SWT.BORDER);
		GridData gd_txtBundleName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtBundleName.widthHint = 317;
		txtBundleName.setLayoutData(gd_txtBundleName);
		
		Label dummyLabel = new Label(bunleGroup, SWT.NONE);
		GridData gd_lblNewLabel_1 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_1.widthHint = 295;
		gd_lblNewLabel_1.heightHint = -3;
		dummyLabel.setLayoutData(gd_lblNewLabel_1);
		dummyLabel.setVisible(false);
		
		
		Label lblNewLabel_2 = new Label(bunleGroup, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Bundle version: ");
		
		txtBundleVersion = new Text(bunleGroup, SWT.BORDER);
		txtBundleVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Label dummyLabel2 = new Label(bunleGroup, SWT.NONE);
		dummyLabel2.setVisible(false);
		

		workingSetGroup = createWorkingSetGroup(container, selection, new String[]{"org.eclipse.ui.resourceWorkingSetPage"});
		setControl(container);
	}
	
	public String getBundleName(){
		return txtBundleName.getText();
	}
	
	public String getBundleVersion(){
		return txtBundleVersion.getText();
	}

	public IWorkingSet[] getWorkingSets(){
		return workingSetGroup.getSelectedWorkingSets();
	}
}
