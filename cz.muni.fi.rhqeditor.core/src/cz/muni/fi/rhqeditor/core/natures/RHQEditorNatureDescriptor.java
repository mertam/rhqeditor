package cz.muni.fi.rhqeditor.core.natures;

import org.eclipse.core.resources.IProjectNatureDescriptor;

public class RHQEditorNatureDescriptor implements IProjectNatureDescriptor{

	private final String NATURE_ID = "cz.muni.fi.rhqeditor.natures.rhqeditornature";
	
	@Override
	public String getNatureId() {
		return NATURE_ID;
	}

	@Override
	public String getLabel() {
		return "Project nature for RHQ bundle editor";
	}

	@Override
	public String[] getRequiredNatureIds() {
		return null;
	}

	@Override
	public String[] getNatureSetIds() {
		String[] ids = new String[]{NATURE_ID};
		return ids;
	}

	@Override
	public boolean isLinkingAllowed() {
		return false;
	}
	
}