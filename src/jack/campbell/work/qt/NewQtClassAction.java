package jack.campbell.work.qt;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;

/**
 * Created by jack on 4/29/17.
 */
public class NewQtClassAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		DataContext dataContext = event.getDataContext();
		IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
		if (view == null) {
			return;
		}

		Project project = PlatformDataKeys.PROJECT.getData(dataContext);
		PsiDirectory directory = view.getOrChooseDirectory();
		if (directory == null) {
			return;
		}
		VirtualFile directoryVirtualFile = directory.getVirtualFile();
		NewQtClassDialog dialog = new NewQtClassDialog(project, directoryVirtualFile);
		dialog.setResizable(true);
		dialog.pack();
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE)  {
			dialog.CreateSourceFile(project, directoryVirtualFile);
			dialog.CreateDesignFile(project, directoryVirtualFile);
			VirtualFile vfile = dialog.CreateHeaderFile(project, directoryVirtualFile);

			FileEditorManager.getInstance(project).openFile(vfile, true, true); //open in editor
			directoryVirtualFile.refresh(true, false);
			//ProjectManager.getInstance().reloadProject(project);
			CMakeWorkspace.forceReloadOnOpening(vfile);
		}
	}
}
