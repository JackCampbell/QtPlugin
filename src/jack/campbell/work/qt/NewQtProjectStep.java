package jack.campbell.work.qt;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.cidr.cpp.cmake.projectWizard.CMakeProjectStepAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jack on 4/19/17.
 */
public class NewQtProjectStep extends CMakeProjectStepAdapter {
	private JPanel panelLayout;
	private TextFieldWithBrowseButton fldProjectPath;
	private JTextField fldCompany;
	private JCheckBox coreCheckBox;
	private JCheckBox widgetsCheckBox;
	private JCheckBox GUICheckBox;
	private JCheckBox serialPortCheckBox;
	private JCheckBox printSupportCheckBox;
	private JCheckBox networkCheckBox;
	private JCheckBox sqlCheckBox;
	private JTextField fldDomain;
	private JTextField fldProject;
	private JTextField fldVersion;
	private JTextField fldAuthor;
	private JTextField fldIdentifier;
	private TextFieldWithBrowseButton fldMacPath;
	private TextFieldWithBrowseButton fldWinPath;

	private String lastProjectDir;
	private QtPreference preference;
	public NewQtProjectStep(String defaultProjectName, String defaultProjectPath) {
		fldProject.setText(defaultProjectName);
		fldProjectPath.setText(defaultProjectPath);

		preference = new QtPreference(); //ServiceManager.getService(QtPreference.class);

		if(preference.author == null) {
			preference.author = System.getProperty("user.name");
		}
		if(preference.winPath == null) {
			preference.winPath = "C:\\Qt\\5.8";
		}
		if(preference.macPath == null) {
			preference.macPath = "/Users/" + preference.author + "/Qt/5.8";
		}
		lastProjectDir = defaultProjectPath + "/";

		fldMacPath.setText(preference.macPath);
		fldWinPath.setText(preference.winPath);
		fldAuthor.setText(preference.author);

		fldCompany.setText(preference.company);
		fldDomain.setText(preference.domain);
//		try {
//			String host = InetAddress.getLocalHost().getHostName();
//			fldDomain.setText(host);
//			fldCompany.setText(host);
//		} catch (UnknownHostException e) {
//			//e.printStackTrace();
//		}

		fldProjectPath.addBrowseFolderListener( "Select Target Project Directory", null, null, new FileChooserDescriptor(
				false, true, false, false, false, false));
		fldProjectPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lastProjectDir = fldProjectPath.getText();
			}
		});
		fldProject.getDocument().addDocumentListener(new DocumentListener() {
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
		});
		fldAuthor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				SyncIdentifier();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				SyncIdentifier();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				SyncIdentifier();
			}
		});
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
		return fldProject;
	}



	private void SyncProjectPath() {
		String projectName = fldProject.getText();
		String lastPath = lastProjectDir; //fldProjectPath.getText();
		if (!lastPath.endsWith("/" + projectName)) {
			lastPath = Paths.get(lastProjectDir, fldProject.getText()).toString();

			fldProjectPath.setText(lastPath);
			fldProjectPath.setAutoscrolls(true);
		}

		String identifier = (fldAuthor.getText() + "." + projectName)
							.toLowerCase().replace(' ', '.')
							.replace('-', '.');
		fldIdentifier.setText(identifier);
	}

	private void SyncIdentifier() {
		String projectName = fldProject.getText();
		String identifier = (fldAuthor.getText() + "." + projectName)
				.toLowerCase().replace(' ', '.')
				.replace('-', '.');
		fldIdentifier.setText(identifier);
	}

	public String GetLocation() {
		return fldProjectPath.getText();
	}

	public String GetName() {
		return fldProject.getText();
	}

	public File CopyAssetFile(File parent, String base, String assets) throws IOException {
		File localFile = new File(parent, base);
		try(InputStream input = getClass().getResourceAsStream(assets);
		    OutputStream output = new FileOutputStream(localFile)) {
			FileUtil.copy(input, output);
		}
		return localFile;
	}

	public String GetAssetContent(String assets) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try(InputStream input = getClass().getResourceAsStream(assets)) {
		    FileUtil.copy(input, output);
		}
		return new String(output.toByteArray(), "UTF-8");
	}

	public File CopyDiskFile(File parent, String base, String assets) throws IOException {
		File localFile = new File(parent, base);
		try(InputStream input = new FileInputStream(assets);
		    OutputStream output = new FileOutputStream(localFile)) {
			FileUtil.copy(input, output);
		}
		return localFile;
	}

	public File CreateContentFile(File parent, String base, String content) throws IOException {
		File file = new File(parent, base);
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Cannot create file " + file);
		}
		FileUtil.writeToFile(file, content);
		return file;
	}

	public File CreateResourceFile(File projectRoot) throws IOException {
		File resource = new File(projectRoot, "resources");
		FileUtil.createDirectory(resource);

		CopyAssetFile(resource, "AppIcon.icns", "/AppIcon.icns");
		CopyAssetFile(resource, "AppIcon.ico", "/AppIcon.ico");
		CopyAssetFile(resource, "AppIcon32.png", "/AppIcon32.png");
		CopyAssetFile(resource, "AppIcon128.png", "/AppIcon128.png");
		CopyAssetFile(projectRoot, "resources.qrc", "/resources.qrc");
		return resource;
	}

	public File CreateToolchain(File projectRoot) throws IOException {
		File make = new File(projectRoot, "cmake");
		FileUtil.createDirectory(make);

		CopyAssetFile(make, "MacOSXBundleInfo.plist.in", "/MacOSXBundleInfo.plist.in");
		CopyAssetFile(make, "windows_metafile.rc.in", "/windows_metafile.rc.in");
		String content = GetAssetContent("/Bundle.cmake")
				.replace("#MAC_PATH#", fldMacPath.getText())
				.replace("#WIN_PATH#", fldWinPath.getText());
		CreateContentFile(make, "Bundle.cmake", content);
		return make;
	}

	public File CreatePrecompiled(File projectRoot) throws IOException {
		String content = "// precompiled...\n";
		if(coreCheckBox.isSelected()) {
			content += "#include <QtCore>\n";
		}
		if(widgetsCheckBox.isSelected()) {
			content += "#include <QtWidgets>\n";
		}
		if(GUICheckBox.isSelected()) {
			content += "#include <QtGui>\n";
		}
		if(serialPortCheckBox.isSelected()) {
			content += "#include <QtSerialPort>\n";
		}
		if(sqlCheckBox.isSelected()) {
			content += "#include <QtSql>\n";
		}
		if(printSupportCheckBox.isSelected()) {
			content += "#include <QtPrintSupport>\n";
		}
		if(networkCheckBox.isSelected()) {
			content += "#include <QtNetwork>\n";
		}
		content += "\n";
		content += "#pragma once\n";
		content += "\n";

		return CreateContentFile(projectRoot, "precompiled.h", content);
	}

	public File CreateMainFile(File projectRoot) throws IOException {
		String content = GetAssetContent("/main.cpp")
				.replace("#APP_NAME#", fldProject.getText())
				.replace("#ORG_DOMAIN#", fldDomain.getText())
				.replace("#ORG_NAME#", fldCompany.getText());
		return CreateContentFile(projectRoot, "main.cpp", content);
	}

	//-DCMAKE_PREFIX_PATH=/Users/jack/Qt/5.8/clang_64/lib/cmake/
	public File CreateMakeFile(File projectRoot) throws IOException {
		Date date = new Date();

		String content = GetAssetContent("/CMakeLists.txt")
				.replace("#PROJECT#", fldProject.getText())
				.replace("#MODULE#", UseModule())
				.replace("#PACKAGES#", FindModule())
				.replace("#COMPANY#", fldCompany.getText())
				.replace("#YEAR#", "" + (1900 + date.getYear()))
				.replace("#AUTHOR#", fldAuthor.getText())
				.replace("#IDENTIFIER#", fldIdentifier.getText());

		return CreateContentFile(projectRoot, "CMakeLists.txt", content);
	}

	private String UseModule() {
		List<String> list = new ArrayList<String>();
		if(coreCheckBox.isSelected()) {
			list.add("Core");
		}
		if(widgetsCheckBox.isSelected()) {
			list.add("Widgets");
		}
		if(GUICheckBox.isSelected()) {
			list.add("gui");
		}
		if(serialPortCheckBox.isSelected()) {
			list.add("SerialPort");
		}
		if(sqlCheckBox.isSelected()) {
			list.add("Sql");
		}
		if(printSupportCheckBox.isSelected()) {
			list.add("PrintSupport");
		}
		if(networkCheckBox.isSelected()) {
			list.add("Network");
		}
		return String.join(" ", list);
	}

	private String FindModule() {
		String content = "";
		if(coreCheckBox.isSelected()) {
			content += "find_package(Qt5Core REQUIRED)\n";
		}
		if(widgetsCheckBox.isSelected()) {
			content += "find_package(Qt5Widgets REQUIRED)\n";
		}
		if(GUICheckBox.isSelected()) {
			content += "find_package(Qt5Gui REQUIRED)\n";
		}
		if(serialPortCheckBox.isSelected()) {
			content += "find_package(Qt5SerialPort REQUIRED)\n";
		}
		if(sqlCheckBox.isSelected()) {
			content += "find_package(Qt5Sql REQUIRED)\n";
		}
		if(printSupportCheckBox.isSelected()) {
			content += "find_package(Qt5PrintSupport REQUIRED)\n";
		}
		if(networkCheckBox.isSelected()) {
			content += "find_package(Qt5Network REQUIRED)\n";
		}
		return content;
	}

	public void Apply() {
		preference.author = fldAuthor.getText();
		preference.macPath = fldMacPath.getText();
		preference.winPath = fldWinPath.getText();
		preference.company = fldCompany.getText();
		preference.domain = fldDomain.getText();
	}
}
