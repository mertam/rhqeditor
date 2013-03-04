package cz.muni.fi.rhqeditor.ui;

import org.eclipse.ui.IStartup;

import cz.muni.fi.rhqeditor.core.ProjectScanner;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		ProjectScanner scan = new ProjectScanner();
		scan.initAllProjects();
	}

}
