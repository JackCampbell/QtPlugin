package jack.campbell.work.qt;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import org.antlr.v4.runtime.misc.Nullable;

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
			VirtualFile sourceFile = dialog.CreateSourceFile(project, directoryVirtualFile);
			VirtualFile designerFile = dialog.CreateDesignFile(project, directoryVirtualFile);
			VirtualFile headerFile = dialog.CreateHeaderFile(project, directoryVirtualFile);


			//CheckPrecompiledHeader(directoryVirtualFile, headerFile);
			CheckMakefile(directoryVirtualFile, headerFile, sourceFile, designerFile);

			FileEditorManager.getInstance(project).openFile(headerFile, true, true); //open in editor
			directoryVirtualFile.refresh(true, false);

			//ProjectManager.getInstance().reloadProject(project);
		}
	}

	public void CheckPrecompiledHeader(VirtualFile directoryVirtualFile, VirtualFile headerName) {
		VirtualFile precompiled = directoryVirtualFile.findChild("precompiled.h");
		if(precompiled == null) {
			return;
		}
		final Document document = FileDocumentManager.getInstance().getDocument(precompiled);
		final String insertText = String.format("#include \"%s\"\n", headerName.getName());
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				CommandProcessor.getInstance().executeCommand(null, new Runnable() {
					@Override
					public void run() {
						int index = document.getText().indexOf("// QTP_AUTO_GENERATE");
						if(index == -1) {
							return;
						}
						document.insertString(index, insertText);
						FileDocumentManager.getInstance().saveDocument(document);
					}
				}, null, null, document);
			}
		});
	}

	public void CheckMakefile(VirtualFile directoryVirtualFile, VirtualFile header, VirtualFile source, @Nullable VirtualFile designer) {
		VirtualFile makelist = directoryVirtualFile.findChild("CMakeLists.txt");
		if(makelist == null) {
			return;
		}
		final Document document = FileDocumentManager.getInstance().getDocument(makelist);
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				CommandProcessor.getInstance().executeCommand(null, new Runnable() {
					@Override
					public void run() {
						int index = document.getText().indexOf("\t)# QTP_APPEND_UI\n");
						if(index != -1 && designer != null) {
							document.insertString(index, "\t" + designer.getName() + "\n");
						}
						index = document.getText().indexOf("\t)# QTP_SOURCE_FILE\n");
						if(index != -1) {
							document.insertString(index, "\t" + source.getName() + " " + header.getName() + "\n");
						}
						FileDocumentManager.getInstance().saveDocument(document);
					}
				}, null, null, document);
			}
		});
	}
}
