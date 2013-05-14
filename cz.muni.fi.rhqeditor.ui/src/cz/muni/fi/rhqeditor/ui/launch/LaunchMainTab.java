package cz.muni.fi.rhqeditor.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.muni.fi.rhqeditor.core.utils.ExtractorProvider;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;
import cz.muni.fi.rhqeditor.ui.UiActivator;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;


public class LaunchMainTab extends AbstractLaunchConfigurationTab {

	// name of the project attached to this launch configuration
	private String fProjectName;
	private String flocalDeployer;
	private boolean fUseDefault;

	// swt list of possible projects
	private Combo fComboProject;
	
	private Text fTextDeployerPath;
	private Label fLblProject;
	private Label fLblPathToLocal;
	private Button fBtnUseDafaultDeployer;
	private Button fBtnBrowseDeployer;
	private Group grpDeployerSettings;
	
	

	/**
	 * creates content of tab
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createControl(Composite parent) {

		Composite myComposite = new Composite(parent, SWT.NONE);
		setControl(myComposite);
		setMessage("Main RHQ Standalone deployment run configuration");
		myComposite.setLayout(new FormLayout());

		fLblProject = new Label(myComposite, SWT.NONE);
		FormData fd_fLblProject = new FormData();
		fd_fLblProject.right = new FormAttachment(0, 119);
		fd_fLblProject.top = new FormAttachment(0, 10);
		fd_fLblProject.left = new FormAttachment(0, 5);
		fLblProject.setLayoutData(fd_fLblProject);
		fLblProject.setText("Project:");

		fComboProject = new Combo(myComposite, SWT.READ_ONLY);
		FormData fd_fComboProject = new FormData();
		fd_fComboProject.right = new FormAttachment(0, 313);
		fd_fComboProject.top = new FormAttachment(0, 5);
		fd_fComboProject.left = new FormAttachment(0, 124);
		fComboProject.setLayoutData(fd_fComboProject);

		ExtractorProvider provider = ExtractorProvider.INSTANCE;
		String[] projects = provider.listProjects();
		fComboProject.setItems(projects);
		FormData fd_text = new FormData();
		fd_text.left = new FormAttachment(0, 21);
		
		grpDeployerSettings = new Group(myComposite, SWT.NONE);
		grpDeployerSettings.setText("Deployer settings");
		FormData fd_grpDeployerSettings = new FormData();
		fd_grpDeployerSettings.bottom = new FormAttachment(100, -165);
		fd_grpDeployerSettings.top = new FormAttachment(fComboProject, 6);
		fd_grpDeployerSettings.left = new FormAttachment(0, 5);
		fd_grpDeployerSettings.right = new FormAttachment(0, 440);
		grpDeployerSettings.setLayoutData(fd_grpDeployerSettings);
		
				// enable - disable own deployer selection
				fBtnUseDafaultDeployer = new Button(grpDeployerSettings, SWT.CHECK);
				fBtnUseDafaultDeployer.setBounds(10, 24, 192, 22);
				fBtnUseDafaultDeployer.addSelectionListener(new SelectionAdapter() {

					public void widgetSelected(SelectionEvent e) {
						enableLocalDeployerWidgets(!fBtnUseDafaultDeployer
								.getSelection());
						updateLaunchConfigurationDialog();
						
					}
				});
				
		fBtnUseDafaultDeployer.setSelection(fUseDefault);
		fBtnUseDafaultDeployer.setText("Use dafault deployer (version 4.6.0)");
		
		// for default purposes
		fBtnUseDafaultDeployer.setSelection(true);
						
		fLblPathToLocal = new Label(grpDeployerSettings, SWT.NONE);
		fLblPathToLocal.setBounds(10, 51, 157, 14);
		//				fd_text.top = new FormAttachment(fLblPathToLocal, 6);
		fLblPathToLocal.setText("Path to local standalone deployer:");

		fTextDeployerPath = new Text(grpDeployerSettings, SWT.BORDER);
		fTextDeployerPath.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		fTextDeployerPath.setBounds(10, 71, 354, 24);
		fTextDeployerPath.setTouchEnabled(true);
		fTextDeployerPath.setMessage("Path to local deployer");
																
		fBtnBrowseDeployer = new Button(grpDeployerSettings, SWT.NONE);
		fBtnBrowseDeployer.setBounds(370, 71, 55, 26);
		
				// selection of deployer
		fBtnBrowseDeployer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = new Shell();
				openSelectFileDialog(shell);
				updateLaunchConfigurationDialog();
			}
		});
		fd_text.right = new FormAttachment(fBtnBrowseDeployer, -6);
		fBtnBrowseDeployer.setText("Browse...");
		enableLocalDeployerWidgets(false);

	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {

			fProjectName = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_PROJECT,
					RhqConstants.NOT_FOUND);

			flocalDeployer = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_LOCAL_DEPLOYER,
					"");
			fUseDefault = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_USE_DEFAULT_DEPLOYER, true);

			int selectedIndex = 0;
			ExtractorProvider provider = ExtractorProvider.INSTANCE;
			String[] projects = provider.listProjects();
			fComboProject.setItems(projects);

			for (int i = 0; i < projects.length; i++) {
				if (projects[i].equals(fProjectName))
					selectedIndex = i;
			}

			fTextDeployerPath.setText(flocalDeployer);

			fComboProject.select(selectedIndex);
			
			fBtnUseDafaultDeployer.setSelection(fUseDefault);
			enableLocalDeployerWidgets(!fUseDefault);
			// updateLaunchConfigurationDialog();
		} catch (CoreException e) {
			UiActivator.getLogger().log(new Status(IStatus.WARNING,RhqConstants.PLUGIN_UI_ID,"LaunchMainTab.initializeFrom " + e.getMessage()));
			// use default setting when error occurs

			try {
				setDefaults(configuration.getWorkingCopy());
			} catch (CoreException e1) {
				UiActivator.getLogger().log(new Status(IStatus.WARNING,RhqConstants.PLUGIN_UI_ID,"LaunchMainTab.initializeFrom " + e1.getMessage()));
			}
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		int selectionIndex = fComboProject.getSelectionIndex();
		if (selectionIndex  > -1) {
		configuration.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_PROJECT,
				fComboProject.getItem(selectionIndex));
		}
		configuration.setAttribute(
				RhqConstants.RHQ_LAUNCH_ATTR_USE_DEFAULT_DEPLOYER,
				fBtnUseDafaultDeployer.getSelection());
		configuration.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_LOCAL_DEPLOYER,
				fTextDeployerPath.getText());
	}

	@Override
	public String getName() {
		return "Main";
	}

	private void enableLocalDeployerWidgets(boolean enabled) {
		fBtnBrowseDeployer.setEnabled(enabled);
		fTextDeployerPath.setEnabled(enabled);
		fLblPathToLocal.setEnabled(enabled);
	}

	private void openSelectFileDialog(Shell shell) {
		FileDialog selectFileDialog = new FileDialog(shell);
		selectFileDialog.setText("Select deployer");
		String path = selectFileDialog.open();
		if (path == null)
			fTextDeployerPath.setText("");
		else
			fTextDeployerPath.setText(path);
	}
}
