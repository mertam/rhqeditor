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

import utils.ExtractorProvider;
import utils.RhqConstants;

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
		myComposite.setLayout(new FormLayout());

		fLblProject = new Label(myComposite, SWT.NONE);
		FormData fd_lblProject = new FormData();
		fLblProject.setLayoutData(fd_lblProject);
		fLblProject.setText("Project:");

		fComboProject = new Combo(myComposite, SWT.NONE);
		fd_lblProject.right = new FormAttachment(fComboProject, -18);
		FormData fd_combo = new FormData();
		fd_combo.right = new FormAttachment(100, -194);
		fd_combo.left = new FormAttachment(0, 80);
		fd_combo.bottom = new FormAttachment(0, 56);
		fd_combo.top = new FormAttachment(0, 34);
		fComboProject.setLayoutData(fd_combo);

		ExtractorProvider provider = ExtractorProvider.getInstance();
		String[] projects = provider.listProjects();
		fComboProject.setItems(projects);

		// enable - disable own deployer selection
		fBtnUseDafaultDeployer = new Button(myComposite, SWT.CHECK);
		fBtnUseDafaultDeployer.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				enableLocalDeployerWidgets(!fBtnUseDafaultDeployer
						.getSelection());
				updateLaunchConfigurationDialog();
				
			}
		});

		fBtnUseDafaultDeployer.setSelection(fUseDefault);
		fd_lblProject.bottom = new FormAttachment(fBtnUseDafaultDeployer, -40);
		FormData fd_btnUseDafaultDeployer = new FormData();
		fd_btnUseDafaultDeployer.top = new FormAttachment(0, 96);
		fd_btnUseDafaultDeployer.left = new FormAttachment(fLblProject, 0,
				SWT.LEFT);
		fBtnUseDafaultDeployer.setLayoutData(fd_btnUseDafaultDeployer);
		fBtnUseDafaultDeployer.setText("Use dafault deployer (version 4.5.1)");

		fTextDeployerPath = new Text(myComposite, SWT.BORDER);
		FormData fd_fTextDeployerPath = new FormData();
		fd_fTextDeployerPath.right = new FormAttachment(fLblProject, 326);
		fd_fTextDeployerPath.left = new FormAttachment(fLblProject, 0, SWT.LEFT);
		fTextDeployerPath.setLayoutData(fd_fTextDeployerPath);
		fTextDeployerPath.setTouchEnabled(true);
		FormData fd_text = new FormData();
		fd_text.left = new FormAttachment(0, 21);

		fBtnBrowseDeployer = new Button(myComposite, SWT.NONE);
		fd_fTextDeployerPath.bottom = new FormAttachment(fBtnBrowseDeployer, 0,
				SWT.BOTTOM);

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
		FormData fd_btnBrowse = new FormData();
		fd_btnBrowse.bottom = new FormAttachment(100, -126);
		fd_btnBrowse.right = new FormAttachment(100, -23);
		fBtnBrowseDeployer.setLayoutData(fd_btnBrowse);
		fBtnBrowseDeployer.setText("Browse...");

		fLblPathToLocal = new Label(myComposite, SWT.NONE);
		fd_text.top = new FormAttachment(fLblPathToLocal, 6);
		FormData fd_lblPathToLocal = new FormData();
		fd_lblPathToLocal.top = new FormAttachment(fBtnUseDafaultDeployer, 6);
		fd_lblPathToLocal.left = new FormAttachment(fLblProject, 0, SWT.LEFT);
		fLblPathToLocal.setLayoutData(fd_lblPathToLocal);
		fLblPathToLocal.setText("Path to local standalone deployer:");

		Label label = new Label(myComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_label = new FormData();
		fd_label.top = new FormAttachment(fBtnUseDafaultDeployer, -8, SWT.TOP);
		fd_label.bottom = new FormAttachment(fBtnUseDafaultDeployer, -6);
		fd_label.right = new FormAttachment(0, 440);
		fd_label.left = new FormAttachment(0, 10);
		label.setLayoutData(fd_label);

		// for default purposes
		fBtnUseDafaultDeployer.setSelection(true);
		enableLocalDeployerWidgets(false);
		fTextDeployerPath.setMessage("Path to local deployer");

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
