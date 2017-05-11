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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	private TextFieldWithBrowseButton fldIconFile;
	private JCheckBox coreCheckBox;
	private JCheckBox widgetsCheckBox;
	private JCheckBox GUICheckBox;
	private JCheckBox serialPortCheckBox;
	private JCheckBox printSupportCheckBox;
	private JCheckBox networkCheckBox;
	private JCheckBox precompiledHeaderCheckBox;
	private JCheckBox sqlCheckBox;
	private JCheckBox fileGlobCheckBox;

	private String lastProjectDir;
	public NewQtProjectStep(String defaultProjectName, String defaultProjectPath) {
		fldProjectName.setText(defaultProjectName);
		fldProjectPath.setText(defaultProjectPath);
		fldMakePrefixPath.setText("~/Qt/5.8/clang_64/lib/cmake");
		lastProjectDir = defaultProjectPath + "/";

		fldOrgName.setText(System.getProperty("user.name"));
		try {
			fldOrgDomain.setText(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			//e.printStackTrace();
		}

		fldProjectPath.addBrowseFolderListener( "Select Target Project Directory", null, null, new FileChooserDescriptor(
				false, true, false, false, false, false));

		fldMakePrefixPath.addBrowseFolderListener( "Select QT Project Directory", null, null, new FileChooserDescriptor(
				false, true, false, false, false, false));

		fldIconFile.addBrowseFolderListener( "Select QT Project Icon", null, null, new FileChooserDescriptor(
				true, false, false, false, false, false));
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
		return fldProjectName;
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


	@Override
	public void actionPerformed(ActionEvent e) {
		lastProjectDir = fldProjectPath.getText();
	}

	public File CreatePrecompiledHeader(File projectRoot) throws IOException {
		String content = "// Project: " + fldProjectName.getText() + "\n";
		if(coreCheckBox.isSelected()) {
			content += "#include <QtCore>\n";
		}
		if(widgetsCheckBox.isSelected()) {
			content += "#include <QtWidgets>\n";
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
		content += "// QTP_AUTO_GENERATE\n";
		File file = new File(projectRoot, "precompiled.h");
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Cannot create file " + file);
		}
		FileUtil.writeToFile(file, content);
		return file;
	}

	public File CreateMainFile(File projectRoot) throws IOException {
		String project = fldProjectName.getText(),
			   orgDomain = fldOrgDomain.getText(),
			   orgName = fldOrgName.getText(),
			   content = "";
		if(precompiledHeaderCheckBox.isSelected()) {
			CreatePrecompiledHeader(projectRoot);
			content += "#include \"precompiled.h\"\n";
		} else {
			content += "#include <QtCore>\n";
			content += "#include <QtWidgets>\n";
		}
		content += "\n";
		content += "int main(int argc, char **argv) {\n";
		content += "\tQApplication application(argc, argv);\n";
		content += "\tapplication.setApplicationName(\"" + project + "\");\n";
		content += "\tapplication.setOrganizationName(\"" + orgName + "\");\n";
		content += "\tapplication.setOrganizationDomain(\"" + orgDomain + "\");\n";
		content += "\n";
		content += "\treturn application.exec();\n";
		content += "}";
		File file = new File(projectRoot, "main.cpp");
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Cannot create file " + file);
		}
		FileUtil.writeToFile(file, content);
		return file;
	}

	public File CreateResource(File projectRoot) throws IOException {
		CreateIcon(projectRoot);

		String content = "<RCC version=\"1.0\">\n";
		content += "<qresource>\n";
		content += "</qresource>\n";
		content += "</RCC>\n";
		File file = new File(projectRoot, "resources.qrc");
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Cannot create file " + file);
		}
		FileUtil.writeToFile(file, content);
		return file;
	}

	//-DCMAKE_PREFIX_PATH=/Users/jack/Qt/5.8/clang_64/lib/cmake/
	public File CreateMakeFile(File projectRoot) throws IOException {
		Date date = new Date();
		String project = fldProjectName.getText(),
			   orgDomain = fldOrgDomain.getText(),
			   orgName = fldOrgName.getText(),
			   toolChain = fldMakePrefixPath.getText();
		String content = "cmake_minimum_required(VERSION 3.7)\n";
		content += "project(" + project + ")\n";
		content += "\n";
		content += "set(CMAKE_OSX_DEPLOYMENT_TARGET 10.10)\n";
		content += "set(CMAKE_PREFIX_PATH " + toolChain + ")\n";
		content += "set(CMAKE_CXX_STANDARD 11)\n";
		content += "set(PROJECT_VERSION 1.0)\n";
		content += "\n";
		content += "MESSAGE(STATUS \"Qt version: ${Qt5Core_VERSION_STRING}\")\n";
		content += "include_directories(cmake-build-debug)\n";
		content += FindModule();
		content += "\n";
		content += "set(CMAKE_INCLUDE_CURRENT_DIR ON) # Find includes in corresponding build directories\n";
		content += "set(CMAKE_AUTOMOC ON) # Instruct CMake to run moc automatically when needed.\n";
		content += "\n";
		if(fileGlobCheckBox.isSelected()) {
			content += "file(GLOB UI_FILES *.ui)\n";
			content += "file(GLOB SRC_FILES *.cpp *.h)\n";
			content += "\n";
			content += "qt5_wrap_ui(UI_HEADERS ${UI_FILES})\n";
			content += "qt5_add_resources(UI_RESOURCES resources.qrc)\n";
			content += "\n";
			content += "set(SOURCE_FILES ${SRC_FILES})\n";
		} else {
			content += "qt5_wrap_ui(UI_HEADERS\n";
			content += "\t)# QTP_APPEND_UI\n";
			content += "qt5_add_resources(UI_RESOURCES resources.qrc)\n";
			content += "\n";
			content += "set(SOURCE_FILES\n";
			content += "\tmain.cpp" + (precompiledHeaderCheckBox.isSelected() ? " precompiled.h\n" : "\n");
			content += "\t)# QTP_SOURCE_FILE\n";
			content += "\n";
		}
		content += "if(APPLE)\n";
		content += "\tset(MACOSX_BUNDLE_INFO_STRING \"${PROJECT_NAME} ${PROJECT_VERSION}\")\n";
		content += "\tset(MACOSX_BUNDLE_BUNDLE_VERSION \"${PROJECT_NAME} ${PROJECT_VERSION}\")\n";
		content += "\tset(MACOSX_BUNDLE_LONG_VERSION_STRING \"${PROJECT_NAME} ${PROJECT_VERSION}\")\n";
		content += "\tset(MACOSX_BUNDLE_SHORT_VERSION_STRING \"${PROJECT_VERSION}\")\n";
		content += "\tset(MACOSX_BUNDLE_COPYRIGHT \"" + date.getYear() + " " + orgName + "\")\n";
		content += "\tset(MACOSX_BUNDLE_ICON_FILE \"launcher.icns\")\n";
		content += "\tset(MACOSX_BUNDLE_GUI_IDENTIFIER \"" + orgDomain + "\")\n";
		content += "\tset(MACOSX_BUNDLE_BUNDLE_NAME \"${PROJECT_NAME}\")\n";
		content += "\tset(MACOSX_BUNDLE_RESOURCES \"${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}.app/Contents/Resources\")\n";
		content += "\tset(MACOSX_BUNDLE_ICON \"../resources/${MACOSX_BUNDLE_ICON_FILE}\")\n";
		content += "\tadd_custom_target( OSX_BUNDLE\n";
		content += "\t\tCOMMAND ${CMAKE_COMMAND} -E make_directory ${MACOSX_BUNDLE_RESOURCES}\n";
		content += "\t\tCOMMAND ${CMAKE_COMMAND} -E copy_if_different ${MACOSX_BUNDLE_ICON} ${MACOSX_BUNDLE_RESOURCES}\n";
		content += "\t\t#COMMAND ${CMAKE_COMMAND} -E copy *.qm ${MACOSX_BUNDLE_RESOURCES}\n";
		content += "\t\t)\n";
		content += "\tadd_executable(" + project + " MACOSX_BUNDLE ${SOURCE_FILES} ${UI_HEADERS} ${UI_RESOURCES})\n";
		content += "\tadd_dependencies(" + project + " OSX_BUNDLE)\n";
		content += "\tset_source_files_properties(${ProjectName_RESOURCES} ${ProjectName_TRANSLATIONS} PROPERTIES MACOSX_PACKAGE_LOCATION Resources)\n";
		content += "else()\n";
		content += "\tadd_executable(" + project + " WIN32 ${SOURCE_FILES} ${UI_HEADERS} ${UI_RESOURCES})\n";
		content += "endif()\n";
		content += "\n";
		content += "target_link_libraries(" + project + " ${QT_LIBRARIES})\n";
		content += "qt5_use_modules(" + project + " " + UseModule() + ")\n";
		File file = new File(projectRoot, "CMakeLists.txt");
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Cannot create file " + file);
		}
		FileUtil.writeToFile(file, content);
		return file;
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

	public InputStream GetIconFileStream() throws FileNotFoundException {
		String file = fldIconFile.getText();
		if(file.isEmpty()) {
			return NewQtProjectWizart.class.getResourceAsStream("/app_icon.icns");
		}
		return new FileInputStream(file);
	}

	public void CreateIcon(File projectRoot) throws IOException {
		File resource = new File(projectRoot, "resources");
		FileUtil.createDirectory(resource);
		File localFile = new File(resource, "launcher.icns");
		try(InputStream input = GetIconFileStream();
		    OutputStream output = new FileOutputStream(localFile)) {
			FileUtil.copy(input, output);
		}
	}

	public String GetLocation() {
		return fldProjectPath.getText();
	}

	public String GetName() {
		return fldProjectName.getText();
	}
}
