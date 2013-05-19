package cz.muni.fi.rhqeditor.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WorkingSetGroup;

import cz.muni.fi.rhqeditor.core.utils.ArchiveReader;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.TraverseEvent;

public class ImportBundleArchiveWizardPage1 extends WizardPage {
	private Table table;

	private CheckboxTableViewer fCheckBoxTableViewer;
	
	private Button btnBrowse;
	
	private 			String fBudleImportDirectory;
	private final 		String EMPTY_STRING = "";
	private Combo 		fCombo;
	
	private String 		lastSearchedPath = null;
	
	//saving settings
	private static final String COMBO_STATE 		= "rhq.import.combo.state";
	private static final String COMBO_SECTION 		= "rhq.import.combo.section";
	private static final int COMBO_HISTORY 			= 5;
	private String[] comboState;
	private int comboStateIndex = 0;
	
	/**
	 * mutex used to prevent multiple directory scanning
	 */
	private AtomicBoolean fMutex = new AtomicBoolean(false);
	
	//set of existing project in workspace
	private HashSet<String> existingProjects = new HashSet<>();
	
	private IStructuredSelection selection;
	
	private WorkingSetGroup group;
	
	protected ImportBundleArchiveWizardPage1(String pageName) {
		super(pageName);
		comboState = new String[COMBO_HISTORY];
		for(IProject project:  ResourcesPlugin.getWorkspace().getRoot().getProjects()){
			existingProjects.add(project.getName());
			System.out.println(project.getName());
		}
	}
	
	public void setSelection(IStructuredSelection selection) {
		this.selection = selection;
	}

	
	
	@Override
	public void createControl(Composite parent) {
//		Composite composite = (Composite) getControl();
		Composite composite = new Composite(parent, SWT.NONE);
		
		initializeDialogUnits(composite);
		
		GridLayout gl_composite = new GridLayout();
		gl_composite.verticalSpacing = 3;
		gl_composite.horizontalSpacing = 3;
		gl_composite.numColumns = 3;
		composite.setLayout(gl_composite);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		setControl(composite);

	    
		
	    setDescription("Import RHQ bundle");

	    Label lblRootFolder = new Label(composite, SWT.NONE);
	    lblRootFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
	    lblRootFolder.setText("Root folder:");
	    
	    fCombo = new Combo(composite, SWT.NONE);
	    fCombo.addTraverseListener(new TraverseListener() {
	    	public void keyTraversed(TraverseEvent arg0) {
	    		scanProjects();
		    }
	    	
	    });
	    fCombo.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetDefaultSelected(SelectionEvent e) {
	    		scanProjects();
		   	}
	    	
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		scanProjects();
	    	}
	    });
	    fCombo.addFocusListener(new FocusAdapter() {
	    	@Override
	    	public void focusLost(FocusEvent e) {
	    		scanProjects();
	    	}
	    });
	    restoreComboState();
	    GridData gd_fCombo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
	    gd_fCombo.widthHint = 413;
	    fCombo.setLayoutData(gd_fCombo);
	    
	    btnBrowse = new Button(composite, SWT.NONE);
	    btnBrowse.addSelectionListener(new SelectionAdapter() {
	      	@Override
	      	public void widgetSelected(SelectionEvent e) {
	      		openSelectDirectoryDialog(new Shell());
	      		scanProjects();	
	      	}
	      });
	    btnBrowse.setText("Browse");
	    btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    
	    fCheckBoxTableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.FILL);	
	    table = fCheckBoxTableViewer.getTable();
	    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1));
	    
	    fCheckBoxTableViewer.setCheckStateProvider(new ICheckStateProvider() {
			
			@Override
			public boolean isGrayed(Object element) {
				if(element instanceof DisplayedObject){
					if(existingProjects.contains(((DisplayedObject) element).getProjectName())){
						return true;
					}
				}
				
				return false;
			}
			
			@Override
			public boolean isChecked(Object element) {
				return false;
			}
		});
	    
	    fCheckBoxTableViewer.addCheckStateListener(new ICheckStateListener() {
	      	
	      	@Override
	      	public void checkStateChanged(CheckStateChangedEvent event) {
	
	      		fCheckBoxTableViewer.setChecked(event.getElement(), event.getChecked());
	      		if(fCheckBoxTableViewer.getGrayedElements().length > 0)
	      			fCheckBoxTableViewer.setGrayedElements(new Object[0]);
	      		setComplete();
	      		
	      	}
	      });
	    Composite set = new Composite(composite, SWT.NONE);
	    set.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
	    setControl(set);
	    group = new WorkingSetGroup(set, selection, new String[]{"org.eclipse.ui.resourceWorkingSetPage"});
	    set.setLayout(new GridLayout(1, false));
	    setControl(composite);
		setComplete();
	}
	

	private void scanProjects() {
		if(fMutex.compareAndSet(false, true) == false) {
			return;
		}
		fBudleImportDirectory = fCombo.getText();
		if(lastSearchedPath == null || !lastSearchedPath.equals(fBudleImportDirectory)){
			File dir = new File(fBudleImportDirectory);
			System.out.println(dir.getAbsolutePath());
		
			getItemsFromDirectory(dir);
			setComboAfterChange();
			setComplete();
		}
		
		lastSearchedPath = fBudleImportDirectory;
		fMutex.set(false);
	}
	
	private void getItemsFromDirectory(final File dir){
		
		
		table.clearAll();
		fCheckBoxTableViewer.refresh();
		if(!dir.isDirectory())
			return;
		
		final Stack<File> folders = new Stack<>();
		folders.push(dir);
	    final List<DisplayedObject> list = new ArrayList<>();
		
	    try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					File currentFile;
					int worked = 0;
					monitor.beginTask("Searching for RHQ bundles", 100);
						while(!folders.empty()){
								
							currentFile = folders.pop();	
							if (!currentFile.canRead()) {
								continue;
							}
							
							monitor.setTaskName(currentFile.getAbsolutePath());
							for(File file: currentFile.listFiles()){
								if(file.isDirectory() )
									folders.push(file);
								if(monitor.isCanceled())
									return;
								if(file.isFile() && file.canRead() && checkFile(file)){
									if(worked < 95){
										monitor.worked(++worked);
									}
									
									DisplayedObject obj = new DisplayedObject();
									obj.setName(file.getName());
									obj.setPath(file.toPath());
									obj.setRelativeParent(dir.toPath());
									list.add(obj);
									
								}
							}
								
							
						}
						
						Collections.sort(list);
						Display.getDefault().asyncExec(new Runnable() {
				               public void run() {  
				            	   fCheckBoxTableViewer.add(list.toArray());
				               }
				            });
						monitor.done();
					}
					
				
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			setErrorMessage(e1.getMessage());
		} 
	}
	
	/**
	 *
	 * @param file
	 * @return true if file should be added into table
	 */
	private boolean checkFile(File file) {
		if(file.getName().endsWith(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX) 
				|| file.getName().endsWith(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX))
			return ArchiveReader.isBundle(file);
		return false;
	}
	
	/**
	 * sets wizard complete, if satisfies conditions
	 */
	public void setComplete(){
		if(fCheckBoxTableViewer.getCheckedElements() == null){
			setPageComplete(false);
			return;
		}
		if(fCheckBoxTableViewer.getGrayedElements().length > 0){
			setPageComplete(false);
			return;
		}
		setPageComplete(fCheckBoxTableViewer.getCheckedElements().length > 0);
	}

	/**
	 * return paths to archives from selected values
	 * @return
	 */
	public List<String> getArchivePath(){
		List<String> result;
		if(fCheckBoxTableViewer == null)
			return null;
		Object[] checkedObjects = fCheckBoxTableViewer.getCheckedElements();
		if (checkedObjects == null)
			return null;
		result = new ArrayList<>();
		
		for (Object checked : checkedObjects) {
			if(checked instanceof DisplayedObject)
				result.add(((DisplayedObject) checked).getPath().toString());
		}
		return result;
	}
	
	
	/**
	 * sets content of fCombo after so change occured
	 */
	private void setComboAfterChange(){
		
		boolean isInHistory = false;
		for(String comboItem: fCombo.getItems()){
			isInHistory = isInHistory || fBudleImportDirectory.equals(comboItem);	
		}
		
		if(fCombo.getItemCount() == 0)
			isInHistory = false;
		if(!isInHistory && !fBudleImportDirectory.equals(EMPTY_STRING)){
			System.out.println(comboState.length);
			comboState[comboStateIndex % COMBO_HISTORY] = fBudleImportDirectory;
			fCombo.add(fBudleImportDirectory);
			comboStateIndex++;
		}
		fCombo.setText(fBudleImportDirectory);
		
		
	}
	
	/**
	 * opens dialog and saves result to global fBudleImportDirectory
	 * @param shell
	 */
	private void openSelectDirectoryDialog(Shell shell){
		DirectoryDialog selectDirDialog = new DirectoryDialog(shell);
		selectDirDialog.setMessage("Choose root directory");
		fBudleImportDirectory = selectDirDialog.open();
		if (fBudleImportDirectory == null) 
			fBudleImportDirectory = EMPTY_STRING;
		else
			fCombo.setText(fBudleImportDirectory);
	}
	
	/**
	 * restores state of the fCombo
	 */
	private void restoreComboState() {
		 try{
			 DialogSettings settings = new DialogSettings(COMBO_SECTION);
			 settings.load(RhqConstants.RHQ_DIALOG_SETTINGS);
			 comboState = settings.getArray(COMBO_STATE);
			 if(comboState != null){
				 for (String value : comboState) 
					 fCombo.add(value);
			 } else {
				 comboState = new String[COMBO_HISTORY];
			 }
		 } catch (IOException e){
			 //ignore exception
		 }
	}

	/**
	 * saves state of fCombo
	 */
	public void saveComboState() {
		 DialogSettings settings = new DialogSettings(COMBO_SECTION);
		 LinkedHashSet<String> newState = new LinkedHashSet<String>();
		 newState.add(fCombo.getText());
		 newState.addAll(Arrays.asList(fCombo.getItems()));

		 int size = Math.min(COMBO_HISTORY, newState.size());
		 String[] newStateArray = new String[size];
		 newState.toArray(newStateArray);
		 settings.put(COMBO_STATE, newStateArray);
		 try {
			settings.save(RhqConstants.RHQ_DIALOG_SETTINGS);
		} catch (IOException e) {
			//ignore exception
		}
	}

	
	/**
	 * class hold information about one object in CheckboxTableViewer
	 * @author syche
	 *
	 */
	private class DisplayedObject implements Comparable<DisplayedObject>{
		String name;
		Path path;
		Path relativeParent;
	
		
		public String getProjectName(){
			int end = name.indexOf(RhqConstants.RHQ_ARCHIVE_JAR_SUFFIX);
			if(end < 0)
				end = name.indexOf(RhqConstants.RHQ_ARCHIVE_ZIP_SUFFIX);
			return end < 0 ? name : name.substring(0, end);
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public Path getRelativeParent() {
			return relativeParent;
		}

		public void setRelativeParent(Path relativeParent) {
			this.relativeParent = relativeParent;
		}

		@Override
		public String toString(){
			return getName() + "  " + getRelativeParent().relativize(getPath()).toString();
		}

		@Override
		public int compareTo(DisplayedObject o) {
			return this.getRelativeParent().compareTo(o.getRelativeParent());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime
					* result
					+ ((relativeParent == null) ? 0 : relativeParent.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof DisplayedObject))
				return false;
			DisplayedObject other = (DisplayedObject) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			if (relativeParent == null) {
				if (other.relativeParent != null)
					return false;
			} else if (!relativeParent.equals(other.relativeParent))
				return false;
			return true;
		}

	}
	/**
	 * returns selected working sets
	 * @return
	 */
	public IWorkingSet[] getWorkingSets(){
		return group.getSelectedWorkingSets();
	}	
}

