package cz.muni.fi.rhqeditor.ui.launch;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cz.muni.fi.rhqeditor.core.utils.ExtractorProvider;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;


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
	
	

	/**
	 * creates content of tab
	 * 
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createControl(Composite parent) {

		Composite myComposite = new Composite(parent, SWT.NONE);
		setControl(myComposite);
		setMessage("Main RHQ Standalone deployment run configuration");
		myComposite.setLayout(new GridLayout(3, false));

		fLblProject = new Label(myComposite, SWT.NONE);
		GridData gd_fLblProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_fLblProject.widthHint = 114;
		fLblProject.setLayoutData(gd_fLblProject);
		fLblProject.setText("Project:");

		fComboProject = new Combo(myComposite, SWT.READ_ONLY);
		GridData gd_fComboProject = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_fComboProject.widthHint = 189;
		fComboProject.setLayoutData(gd_fComboProject);

		ExtractorProvider provider = ExtractorProvider.getInstance();
		String[] projects = provider.listProjects();
		fComboProject.setItems(projects);
				new Label(myComposite, SWT.NONE);
		
				Label label = new Label(myComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
				label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

		// enable - disable own deployer selection
		fBtnUseDafaultDeployer = new Button(myComposite, SWT.CHECK);
		fBtnUseDafaultDeployer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		fBtnUseDafaultDeployer.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				enableLocalDeployerWidgets(!fBtnUseDafaultDeployer
						.getSelection());
				updateLaunchConfigurationDialog();
				
			}
		});

		fBtnUseDafaultDeployer.setSelection(fUseDefault);
		fBtnUseDafaultDeployer.setText("Use dafault deployer (version 4.5.1)");
				new Label(myComposite, SWT.NONE);
		
				fLblPathToLocal = new Label(myComposite, SWT.NONE);
				fLblPathToLocal.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
//				fd_text.top = new FormAttachment(fLblPathToLocal, 6);
				fLblPathToLocal.setText("Path to local standalone deployer:");
		new Label(myComposite, SWT.NONE);
		FormData fd_text = new FormData();
		fd_text.left = new FormAttachment(0, 21);
				
						fTextDeployerPath = new Text(myComposite, SWT.BORDER);
						GridData gd_fTextDeployerPath = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
						gd_fTextDeployerPath.widthHint = 303;
						fTextDeployerPath.setLayoutData(gd_fTextDeployerPath);
						fTextDeployerPath.setTouchEnabled(true);
						fTextDeployerPath.setMessage("Path to local deployer");

		fBtnBrowseDeployer = new Button(myComposite, SWT.NONE);

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

		// for default purposes
		fBtnUseDafaultDeployer.setSelection(true);
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
			ExtractorProvider provider = ExtractorProvider.getInstance();
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
			e.printStackTrace();
			// use default setting when error occurs

			try {
				setDefaults(configuration.getWorkingCopy());
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_PROJECT,
				fComboProject.getItem(fComboProject.getSelectionIndex()));
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
