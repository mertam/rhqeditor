package preferences;


import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import utils.InputPropertiesManager;
import utils.RhqConstants;


public class RHQPropertyPage  extends PropertyPage implements IWorkbenchPropertyPage{


	private TextViewer fViewer;
	private IAdaptable fElement;
	private IEclipsePreferences fPrefs = null;
	
	private Text txtDeployPath;
	
	private Label lblDeployDir;
	
	private Button btnDirectoryDialog;
	
	private String fDeployDir = "";
	
	private InputPropertiesManager fManager;
	
	private ArrayList<String> fRecipeProperties;
	
	
	
	public RHQPropertyPage(){
	}
	@Override
	public IAdaptable getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setElement(IAdaptable element) {
		// TODO Auto-generated method stub
		fElement = element;
		fManager = new InputPropertiesManager((IProject)element);
		fRecipeProperties = fManager.getInputPropertiesFromRecipe();
	}
	
	@Override
	public boolean performOk(){
		if(fPrefs != null){
			//store all properties, attach set value of empty string
			HashMap<String,String> propertyMap = getPropertyValues();
			fPrefs.put(RhqConstants.RHQ_DEPLOY_DIR, txtDeployPath.getText());
			String value;
			for(String property: fRecipeProperties){
				value = propertyMap.get(property);
				if(value == null)
					fPrefs.put(RhqConstants.RHQ_PROPERTY_INPUT+property, "");
				else
					fPrefs.put(RhqConstants.RHQ_PROPERTY_INPUT+property, value);
			}
			try {
				fPrefs.flush();
			} catch (BackingStoreException e) {
				//TODO zalogovat chybu pri ukladani
				e.printStackTrace();
			}
		}
		return true;
	}
	
	
	/**
	 * creates content of property page
	 */
	@Override
	protected Control createContents(Composite parent) {
		IProject project = (IProject)fElement;
		ArrayList<String> propertyNames = fRecipeProperties;
		
		IScopeContext projectScope = new ProjectScope(project);
		fPrefs = projectScope.getNode(RhqConstants.RHQ_PROPERTY_NODE);		
		
		StringBuilder initialContent = new StringBuilder();
		String propertyValue;
		//search for stored value of each property
		for(String property: propertyNames){
			propertyValue = fPrefs.get(RhqConstants.RHQ_PROPERTY_INPUT+property,RhqConstants.NOT_FOUND);
			initialContent.append(property + "=");
			if(!propertyValue.equals(RhqConstants.NOT_FOUND)){
				initialContent.append(propertyValue);
			}
			initialContent.append(System.getProperty("line.separator"));
		}
		
        Composite myComposite = new Composite(parent, SWT.NONE);
        myComposite.setLayout(new GridLayout(3, false));
        
        lblDeployDir = new Label(myComposite, SWT.NONE);
        GridData gd_lblNewLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gd_lblNewLabel.widthHint = 80;
        lblDeployDir.setLayoutData(gd_lblNewLabel);
        lblDeployDir.setText("rhq.deploy.dir");
        
        txtDeployPath = new Text(myComposite, SWT.BORDER);
        txtDeployPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtDeployPath.setText(fPrefs.get(RhqConstants.RHQ_PROPERTY_INPUT+RhqConstants.RHQ_DEPLOY_DIR,""));
        
        btnDirectoryDialog = new Button(myComposite, SWT.NONE);
        btnDirectoryDialog.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        btnDirectoryDialog.setText("Browse...");
        btnDirectoryDialog.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		openSelectDirectoryDialog(new Shell());
        		super.widgetSelected(e);
        	}
        });
        
        Label mylabel = new Label(myComposite, SWT.NONE);
        mylabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        mylabel.setText("Input properties specification (one prer line, i.e. rhq.deploy.dir=...");
                                        
        fViewer = new TextViewer(myComposite, SWT.BORDER);
        StyledText styledText = fViewer.getTextWidget();
        styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        fViewer.setDocument(new Document(initialContent.toString()));

        return myComposite;
	
	}
	
	private void openSelectDirectoryDialog(Shell shell){
		DirectoryDialog selectDirDialog = new DirectoryDialog(shell);
		selectDirDialog.setMessage("Choose deployment directory");
		fDeployDir = selectDirDialog.open();	
		txtDeployPath.setText(fDeployDir);
	}
	
	
	/**
	 * reads value of properties from textviever
	 * @return
	 */
	private HashMap<String,String> getPropertyValues(){
		HashMap<String, String> propertyMap = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder(fViewer.getDocument().get());
		String propertyValue;
		for(String propertyName: fRecipeProperties){
			int startIndex = sb.indexOf(propertyName) + propertyName.length()+1;
			int endIndex = sb.indexOf(System.getProperty("line.separator"),startIndex);
			propertyValue = sb.substring(startIndex, endIndex);
			propertyValue.trim();
			if(!propertyValue.isEmpty())
				propertyMap.put(propertyName, propertyValue);
		}
		return propertyMap;
	}


}
