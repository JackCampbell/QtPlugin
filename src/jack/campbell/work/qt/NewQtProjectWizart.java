package jack.campbell.work.qt;

import com.intellij.ide.RecentProjectsManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.CPPLog;
import com.jetbrains.cidr.cpp.cmake.CMakeProjectOpenProcessor;
import com.jetbrains.cidr.cpp.cmake.projectWizard.CLionProjectWizardUtils;
import com.jetbrains.cidr.cpp.cmake.projectWizard.CMakeProjectWizard;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by jack on 4/19/17.
 */

public class NewQtProjectWizart extends CMakeProjectWizard {
	private NewQtProjectStep adapter;
	public NewQtProjectWizart() {
		super("New QT Project", "NewQtProjectWizart");

		String lastDir = Optional.ofNullable(RecentProjectsManager.getInstance().getLastProjectCreationLocation()).orElse("");
		adapter = new NewQtProjectStep("", new File(lastDir).getPath());
		initWithStep(adapter);
	}

	@Override
	protected boolean tryFinish() {
		String projectRootPath = adapter.GetLocation();
		File projectRootDir = new File(projectRootPath);
		if (projectRootDir.exists()) {
			String[] fileList = projectRootDir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return !".DS_Store".equalsIgnoreCase(name) && !"Thumbs.db".equalsIgnoreCase(name);
				}
			});
			if (fileList != null && fileList.length > 0) {
				String msg = String.format("Directory \'%s\' already exists and not empty.\nWould you like to continue?", projectRootPath);
				int dialogAnswer = Messages.showYesNoDialog(msg, "Project Directory Already Exists", Messages.getQuestionIcon());
				if (dialogAnswer != 0) {
					return false;
				}
			}
		} else {
			try {
				//ProjectManager.getInstance().createProject(projectRootDirParentPath, adapter.GetName());
				VfsUtil.createDirectories(projectRootPath);
			} catch (IOException | RuntimeException e) {
				CPPLog.LOG.warn(e);
				return false;
			}
		}

		String projectRootDirParentPath = projectRootDir.getParent();
		if (projectRootDirParentPath != null) {
			RecentProjectsManager.getInstance().setLastProjectCreationLocation(projectRootDirParentPath);
		}

		try {
			CreateProject(projectRootPath, adapter);
			return true;
		} catch (IOException e) {
			CPPLog.LOG.warn(e);
			return false;
		}
	}



	public static String CreateProject(String projectRootPath, NewQtProjectStep adapter) throws IOException {
		String cmakePrefix = adapter.GetToolPath();
		String projectName = adapter.GetName();

		// check CMakeList
		File projectRoot = new File(projectRootPath);

		File cMakeLists = CreateMakeFile(projectRoot);
		projectName = FileUtil.sanitizeFileName(projectName);

		CreateMainFile(projectRoot, adapter);
		CreateResourceFile(projectRoot, adapter);

		VirtualFile cMakeListsVirtualFile = VfsUtil.findFileByIoFile(cMakeLists, true);
		CMakeListsEditor cMakeListsEditor = CMakeListsEditor.getInstance(cMakeListsVirtualFile);
		cMakeListsEditor.Clear();
		cMakeListsEditor.AppendLine("cmake_minimum_required(VERSION 3.7)");
		cMakeListsEditor.SetMethod("project", projectName);
		cMakeListsEditor.blankLine();
		cMakeListsEditor.SetVariable("CMAKE_PREFIX_PATH", cmakePrefix);
		cMakeListsEditor.SetVariable("CMAKE_CXX_STANDARD", "11");                   //-DCMAKE_PREFIX_PATH=/Users/jack/Qt/5.8/clang_64/lib/cmake/
		cMakeListsEditor.blankLine();
		cMakeListsEditor.AppendLine("include_directories(cmake-build-debug)");

		cMakeListsEditor.AppendLine("file(GLOB UI_FILES *.ui)");
		cMakeListsEditor.AppendLine("file(GLOB SRC_FILES *.cpp)");
//		cMakeListsEditor.AppendLine("#set(QT_PATH " + qtToolPath + ")");
		cMakeListsEditor.AppendLine("find_package(Qt5Core REQUIRED)");
		cMakeListsEditor.AppendLine("find_package(Qt5Widgets REQUIRED)");
		cMakeListsEditor.AppendLine("find_package(Qt5Gui REQUIRED)");
		cMakeListsEditor.AppendLine("# find_package(Qt5Sql REQUIRED)");
		cMakeListsEditor.AppendLine("# find_package(Qt5SerialPort REQUIRED)");
		cMakeListsEditor.AppendLine("# find_package(Qt5PrintSupport REQUIRED)");
		cMakeListsEditor.blankLine();
		cMakeListsEditor.AppendLine("# Find includes in corresponding build directories");
		cMakeListsEditor.SetVariable("CMAKE_INCLUDE_CURRENT_DIR", "ON");
		cMakeListsEditor.AppendLine("# Instruct CMake to run moc automatically when needed.");
		cMakeListsEditor.SetVariable("CMAKE_AUTOMOC", "ON");
		cMakeListsEditor.blankLine();
		cMakeListsEditor.AppendLine("qt5_wrap_ui(UI_HEADERS ${UI_FILES})");
		cMakeListsEditor.AppendLine("qt5_add_resources(UI_RESOURCES resources.qrc)");
		cMakeListsEditor.AppendLine("#set(MACOSX_BUNDLE_ICON_FILE " + projectName + ".icns)");
		cMakeListsEditor.AppendLine("#SET_SOURCE_FILES_PROPERTIES(" + projectName + ".icns PROPERTIES MACOSX_PACKAGE_LOCATION Resources)");
		cMakeListsEditor.blankLine();
		cMakeListsEditor.SetVariable("SOURCE_FILES", "${SRC_FILES}");
		cMakeListsEditor.blankLine();
		cMakeListsEditor.AppendLine("add_executable(" + projectName + " ${SOURCE_FILES} ${UI_HEADERS} ${UI_RESOURCES})");
//		cMakeListsEditor.AppendLine("add_executable(" + projectName + " ${SOURCE_FILES})");
		cMakeListsEditor.AppendLine("target_link_libraries(" + projectName + " ${Qt5Core_QTMAIN_LIBRARIES})");
		cMakeListsEditor.AppendLine("qt5_use_modules(" + projectName + " Core Widgets Gui)");

		//CMakeWorkspace.forceReloadOnOpening(cMakeListsVirtualFile);
		VfsUtil.findFileByIoFile(projectRoot, true);
		return projectName;
	}

	@Override
	protected void doRunWizard() {
		String projectAdapterPath = adapter.GetLocation();
		VirtualFile projectRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(projectAdapterPath);
		if (projectRoot == null) {
			return;
		}

		CLionProjectWizardUtils.refreshProjectDir(projectRoot);

		final VirtualFile cMakeLists = projectRoot.findChild("CMakeLists.txt");
		if (cMakeLists == null) {
			return;
		}
		final VirtualFile mainFile = projectRoot.findChild("main.cpp");
		if (mainFile == null) {
			return;
		}
		final VirtualFile resourceFile = projectRoot.findChild("resources.qrc");
		if (resourceFile == null) {
			return;
		}

		Project project = null;
		try {

			project = ProjectManager.getInstance().loadAndOpenProject(projectAdapterPath);
		} catch (IOException | JDOMException e) {
			CPPLog.LOG.warn(e);
		}

		/*final Project project = CMakeWorkspace.openProject(cMakeLists, null, false);
		if (project == null) {
			return;
		}*/
		if (project == null) {
			project = ProjectManager.getInstance().getDefaultProject();
		}

		CMakeProjectOpenProcessor.OpenProjectSpec projectSpec = CMakeProjectOpenProcessor.getHelper().getAndClearFileToOpenData(project);

		deleteBuildOutputDir(projectSpec);
		(new OpenFileDescriptor(project, cMakeLists)).navigate(false);
		(new OpenFileDescriptor(project, mainFile)).navigate(true);
		(new OpenFileDescriptor(project, resourceFile)).navigate(true);
	}

	private void deleteBuildOutputDir(CMakeProjectOpenProcessor.OpenProjectSpec projectSpec) {
		if (projectSpec != null && projectSpec.generationDir != null) {
			FileUtil.delete(projectSpec.generationDir);
		}
	}






	public static final String defMainFile =
					"#include <QtCore>\n" +
					"#include <QtWidgets>\n" +
					"\n" +
					"int main(int argc, char **argv) {\n" +
						"\tQApplication application(argc, argv);\n" +
						"\tapplication.setApplicationName(\"%s\");\n" +
						"\tapplication.setOrganizationDomain(\"%s\");\n" +
						"\tapplication.setOrganizationName(\"%s\");\n" +
						"\n" +
						"\treturn application.exec();\n" +
					"}";
	public static File CreateMainFile(File projectRoot, NewQtProjectStep adapter) throws IOException {
		// Create main
		File file = new File(projectRoot, "main.cpp");
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Cannot create file " + file);
		}
		String orgName = adapter.GetOrganizationName();
		if(orgName.isEmpty()) {
			orgName = adapter.GetName().toLowerCase();
		}
		String orgDomain = adapter.GetOrganizationDomain();
		if(orgDomain.isEmpty()) {
			orgDomain = orgName.toLowerCase() + ".ws";
		}
		FileUtil.writeToFile(file, String.format(defMainFile, adapter.GetName(), orgDomain, orgName));
		return file;
	}



	public static final String defResourceFile = "<RCC version=\"1.0\">\n" +
												 "<qresource>\n" +
												 "</qresource>\n" +
												 "</RCC>";
	public static File CreateResourceFile(File projectRoot, NewQtProjectStep adapter) throws IOException {
		File file = new File(projectRoot, "resources.qrc");
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Cannot create file " + file);
		}
		FileUtil.writeToFile(file, defResourceFile);
		return file;
	}

	public static File CreateMakeFile(File projectRoot) throws IOException {
		File file = new File(projectRoot, "CMakeLists.txt");
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Cannot create file " + file);
		}
		return file;
	}
}