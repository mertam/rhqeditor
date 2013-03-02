package preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import utils.RhqConstants;

import cz.muni.fi.rhqeditor.ui.UiActivator;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	
	public PreferencePage(){
		super(GRID);
	}
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(UiActivator.getDefault().getPreferenceStore());
	    setDescription("RHQ Editor conguration "+ System.getProperty("line.delimiter") +
	    		"(NOTE: All configuration is inherited from Ant Editor");
		
	}

	@Override
	protected void createFieldEditors() {
		addField(new FileFieldEditor(RhqConstants.RHQ_DEPLOYER_PATH, "Path to the standlalone deployer", getFieldEditorParent()));
		
	}



}
