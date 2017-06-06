package jack.campbell.work.qt;

import com.intellij.ide.RecentProjectsManager;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.project.impl.ProjectManagerImpl;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.CPPLog;
import com.jetbrains.cidr.cpp.cmake.CMakeProjectOpenProcessor;
import com.jetbrains.cidr.cpp.cmake.projectWizard.CLionProjectWizardUtils;
import com.jetbrains.cidr.cpp.cmake.projectWizard.CMakeProjectWizard;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by jack on 4/19/17.
 */

public class NewQtProjectWizard extends CMakeProjectWizard {
	private NewQtProjectStep adapter;
	public NewQtProjectWizard() {
		super("New QT Project", "NewQtProjectWizard");

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
		String projectName = FileUtil.sanitizeFileName(adapter.GetName());
		File projectRoot = new File(projectRootPath);

		adapter.CreateResourceFile(projectRoot);
		adapter.CreatePrecompiled(projectRoot);
		adapter.CreateMainFile(projectRoot);
		adapter.CreateToolchain(projectRoot);
		adapter.CreateMakeFile(projectRoot);
		adapter.Apply();

		//VirtualFile cMakeListsVirtualFile = VfsUtil.findFileByIoFile(cMakeLists, true);
		//CMakeWorkspace.forceReloadOnOpening(cMakeListsVirtualFile);
		//VfsUtil.findFileByIoFile(projectRoot, true);
		return projectName;
	}

	@Override
	protected void doRunWizard() {
		String projectAdapterPath = adapter.GetLocation();
		String projectName = adapter.GetName();
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

		Project project = ProjectManagerImpl.getInstanceEx().newProject(projectName, projectAdapterPath, true,false);
		ProjectManagerEx.getInstanceEx().openProject(project);

		ProjectUtil.openOrImport(projectAdapterPath, null, false);
		ProjectManagerEx.getInstanceEx().reloadProject(project);


//		Project project = null;
//		try {
//			project = ProjectManager.getInstance().loadAndOpenProject(projectAdapterPath);
//		} catch (IOException | JDOMException e) {
//			CPPLog.LOG.warn(e);
//		}

		CMakeProjectOpenProcessor.OpenProjectSpec projectSpec = CMakeProjectOpenProcessor.getHelper().getAndClearFileToOpenData(project);

		deleteBuildOutputDir(projectSpec);
		(new OpenFileDescriptor(project, cMakeLists)).navigate(false);
		(new OpenFileDescriptor(project, mainFile)).navigate(true);
	}

	private void deleteBuildOutputDir(CMakeProjectOpenProcessor.OpenProjectSpec projectSpec) {
		if (projectSpec != null && projectSpec.generationDir != null) {
			FileUtil.delete(projectSpec.generationDir);
		}
	}
}