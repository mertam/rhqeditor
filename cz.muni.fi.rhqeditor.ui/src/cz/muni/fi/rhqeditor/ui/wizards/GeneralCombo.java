package cz.muni.fi.rhqeditor.ui.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import cz.muni.fi.rhqeditor.core.Activator;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;

/**
 *  class represents SWT Combo with history support
 * @author syche
 *
 */
public class GeneralCombo {

	private String strComboName;
	private String strComboStateID = "rhqeditor.generalcombo.state.";
	private String strComboSectionID = "rhqeditor.generalcombo.section.";
	private int intComboHistory = 5;
	private int comboStateIndex = 0;

	private Combo combo;
	private CircularFifoBuffer buffer;
	
	public GeneralCombo(String name, Composite parent, int style, int historySize) {
		combo = new Combo(parent, style);

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Invalid argument 'name'");
		}
		
		if (historySize < 0) {
			throw new IllegalArgumentException("Invalid argument 'bufferSize");
		}

		strComboName = name;
		strComboStateID = strComboStateID.concat(name);
		strComboSectionID = strComboSectionID.concat(name);
		intComboHistory = historySize;
		buffer = new CircularFifoBuffer(historySize);
	}


	public String getStrComboName() {
		return strComboName;
	}

	public int getIntComboHistory() {
		return intComboHistory;
	}

	public void setIntComboHistory(int intComboHistory) {
		this.intComboHistory = intComboHistory;
	}
	
	public Combo getCombo() {
		return this.combo;
	}

	public void setComboState(List<String> comboStateList) {
		this.setComboState(comboStateList.toArray(new String[comboStateList.size()]));
	}
	
	public void setComboState(String[] comboStateArray) {
		this.combo.setItems(comboStateArray);
		this.buffer.add(new ArrayList<String>(Arrays.asList(comboStateArray)));
	}

	public int getComboStateIndex() {
		return comboStateIndex;
	}

	public void setComboStateIndex(int comboStateIndex) {
		this.comboStateIndex = comboStateIndex;
	}

	public void setStrComboName(String strComboName) {
		this.strComboName = strComboName;
	}

	/**
	 * saves state of combo to file
	 */
	public void saveComboState() {
		DialogSettings settings = new DialogSettings(strComboSectionID);
		String[] items = toArray();
		settings.put(strComboStateID, items);
		try {
			settings.save(RhqConstants.RHQ_DIALOG_SETTINGS);
		} catch (IOException e) {
			Activator.getLog().log(
					new Status(IStatus.WARNING, RhqConstants.PLUGIN_UI_ID,
							"Saving combo state failed", e));
		}
	}
	
	/**
	 * restores state of combo from file
	 */
	public void loadComboState() {
		try {
			DialogSettings settings = new DialogSettings(strComboSectionID);
			settings.load(RhqConstants.RHQ_DIALOG_SETTINGS);
			String[] items = settings.getArray(strComboStateID);
			setComboState(items);
			
		} catch (IOException e) {
			
		}
		
	}

	public void addItemToCombo (String itemName) {
		buffer.add(itemName);
		this.combo.setItems(toArray());
	}
	
	private String[] toArray() {
		Object[] array = buffer.toArray();
		String[] items = new String[buffer.size()];
		for (int i = 0; i < array.length; i++) {
			if (array[i] instanceof String) {
				items[array.length-i-1] = (String) array[i];
			}
		}
		return items;
	}
}
