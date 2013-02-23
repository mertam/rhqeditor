package cz.muni.fi.rhqeditor.ui;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IStartup;

import cz.muni.fi.rhqeditor.core.ExtractorProvider;
import cz.muni.fi.rhqeditor.core.ProjectScanner;
import cz.muni.fi.rhqeditor.core.RhqPathExtractor;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		ProjectScanner scan = new ProjectScanner();
		Map<IProject,RhqPathExtractor> map = ExtractorProvider.getInstance().getMap();
		for(IProject proj: map.keySet()){
			System.out.println(proj.getName());
		}
		
	}

}
