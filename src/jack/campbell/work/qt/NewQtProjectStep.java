package jack.campbell.work.qt;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.jetbrains.cidr.cpp.cmake.projectWizard.CMakeProjectStepAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;

/**
 * Created by jack on 4/19/17.
 */
public class NewQtProjectStep extends CMakeProjectStepAdapter implements DocumentListener, ActionListener {
	private JPanel panelLayout;
	private JTextField fldProjectName;
	private TextFieldWithBrowseButton fldProjectPath;
	private TextFieldWithBrowseButton fldMakePrefixPath;
	private JTextField fldOrgName;
	private JTextField fldOrgDomain;

	private String lastProjectDir;
	public NewQtProjectStep(String defaultProjectName, String defaultProjectPath) {
		fldProjectName.setText(defaultProjectName);
		fldProjectPath.setText(defaultProjectPath);
		fldMakePrefixPath.setText("~/Qt/5.8/clang_64/lib/cmake");
		lastProjectDir = defaultProjectPath + "/";

		fldProjectPath.addBrowseFolderListener( "Select Target Project Directory", null, null, new FileChooserDescriptor(
				false, true, false, false, false, false));

		fldMakePrefixPath.addBrowseFolderListener( "Select QT Project Directory", null, null, new FileChooserDescriptor(
				false, true, false, false, false, false));

		fldProjectPath.addActionListener(this);
		fldProjectName.getDocument().addDocumentListener(this);
	}

	@Override
	protected void init() {
		panelLayout.setVisible(true);
	}

	@Override
	public void dispose() {
		panelLayout.setVisible(false);
	}

	@Override
	public JComponent getComponent() {
		return panelLayout;
	}

	@Override
	public JComponent getPreferredFocusedComponent() {
		return panelLayout;
	}



	@Override
	public void insertUpdate(DocumentEvent e) {
		SyncProjectPath();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		SyncProjectPath();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		SyncProjectPath();
	}

	private void SyncProjectPath() {
		String projectName = fldProjectName.getText();
		String lastPath = lastProjectDir; //fldProjectPath.getText();
		if (!lastPath.endsWith("/" + projectName)) {
			lastPath = Paths.get(lastProjectDir, fldProjectName.getText()).toString();

			fldProjectPath.setText(lastPath);
			fldProjectPath.setAutoscrolls(true);
		}

//		if(fldOrgName.getText().isEmpty()) {
//			fldOrgName.setText(projectName);
//		}
//		if(fldOrgDomain.getText().isEmpty()) {
//			fldOrgDomain.setText(projectName.toLowerCase().concat(".ws"));
//		}
	}

	public String GetName() {
		return fldProjectName.getText();
	}

	public String GetLocation() {
		return fldProjectPath.getText();
	}

	public String GetToolPath() {
		return fldMakePrefixPath.getText();
	}

	public String GetOrganizationDomain() {
		return fldOrgDomain.getText();
	}

	public String GetOrganizationName() {
		return fldOrgName.getText();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		lastProjectDir = fldProjectPath.getText();
	}
}
