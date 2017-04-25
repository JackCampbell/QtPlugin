package jack.campbell.work.qt;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;

import java.io.IOException;

/**
 * Created by jack on 4/20/17.
 */
public class NewQtWindowFile extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
		final IdeView view = e.getRequiredData(LangDataKeys.IDE_VIEW);

		PsiDirectory directory = view.getOrChooseDirectory();
		if (directory == null) {
			return;
		}
		final VirtualFile directoryVirtualFile = directory.getVirtualFile();

		boolean loop = false;
		do {
			String filename = getDesiredFilename(project);
			if (filename == null) { //cancel
				return;
			}
			if (filename.isEmpty()) { //no name entered
				showEmptyFilenameError(project);
				loop = true;
				continue;
			}

			VirtualFile existingFile = directoryVirtualFile.findChild(filename);
			if (existingFile != null) {
				int overwriteChoice = getOverwriteChoice(project); //ask to overwrite file
				switch (overwriteChoice) {
					case Messages.YES:
						deleteVirtualFile(existingFile);
						loop = false;
						break;
					case Messages.NO:
						loop = true;
						continue;
					case Messages.CANCEL:
						return;
				}
			}
			CreateUserInterfaceFile(project, directoryVirtualFile, filename);
			CreateHeaderFile(project, directoryVirtualFile, filename);
			VirtualFile vfile = CreateSourceFile(project, directoryVirtualFile, filename);

			FileEditorManager.getInstance(project).openFile(vfile, true, true); //open in editor
		} while (loop);
	}

	private VirtualFile CreateHeaderFile(Project project, VirtualFile directoryVirtualFile, String name) {
		String content =   ("#include <QtCore>\n" +
							"#include <QtWidgets>\n" +
							"#include <QApplication>\n" +
							"#pragma once\n" +
							"\n" +
							"namespace Ui {\n" +
								"\tclass {NAME}Window;\n" +
							"}\n" +
							"\n" +
							"class {NAME}Window : public QMainWindow {\n" +
								"\tQ_OBJECT\n" +
							"public:\n" +
								"\texplicit {NAME}Window(QWidget *parent = nullptr);\n" +
								"\t\t\t\t\t~{NAME}Window();\n" +
							"public slots:\n" +
							"private:\n" +
								"\tUi::{NAME}Window *ui;\n" +
							"};\n").replace("{NAME}", name);
		VirtualFile vfile = CreateFile(project, directoryVirtualFile, name + "Window.h", content);
		return vfile;
	}

	private VirtualFile CreateSourceFile(Project project, VirtualFile directoryVirtualFile, String name) {
		String content =   ("#include \"{NAME}Window.h\"\n" +
							"#include \"ui_{NAME}Window.h\"\n" +
							"\n" +
							"\n" +
							"{NAME}Window::{NAME}Window(QWidget *parent) : QMainWindow(parent), ui(new Ui::{NAME}Window) {\n" +
							"\tui->setupUi(this);\n" +
							"}\n" +
							"\n" +
							"{NAME}Window::~{NAME}Window() {\n" +
							"\tdelete ui;\n" +
							"}\n").replace("{NAME}", name);
		VirtualFile vfile = CreateFile(project, directoryVirtualFile, name + "Window.cpp", content);
		//CMakeListsEditor.getInstance(project.getBaseDir()).SetVariable("SOURCE_FILES", vfile.getName());
		return vfile;
	}

	public VirtualFile CreateUserInterfaceFile(Project project, VirtualFile directoryVirtualFile, String name) {
		String content =("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<ui version=\"4.0\">\n" +
						" <class>MainWindow</class>\n" +
						" <widget class=\"QMainWindow\" name=\"{NAME}Window\">\n" +
						"  <property name=\"geometry\">\n" +
						"   <rect>\n" +
						"    <x>0</x>\n" +
						"    <y>0</y>\n" +
						"    <width>897</width>\n" +
						"    <height>484</height>\n" +
						"   </rect>\n" +
						"  </property>\n" +
						"  <property name=\"windowTitle\">\n" +
						"   <string>{NAME}</string>\n" +
						"  </property>\n" +
						"  <widget class=\"QWidget\" name=\"centralWidget\">\n" +
						"   <layout class=\"QVBoxLayout\" name=\"verticalLayout\"/>\n" +
						"  </widget>\n" +
						"  <widget class=\"QMenuBar\" name=\"menuBar\">\n" +
						"   <property name=\"geometry\">\n" +
						"    <rect>\n" +
						"     <x>0</x>\n" +
						"     <y>0</y>\n" +
						"     <width>897</width>\n" +
						"     <height>22</height>\n" +
						"    </rect>\n" +
						"   </property>\n" +
						"   <widget class=\"QMenu\" name=\"menuExit\">\n" +
						"    <property name=\"title\">\n" +
						"     <string>File</string>\n" +
						"    </property>\n" +
						"    <addaction name=\"actionExit\"/>\n" +
						"   </widget>\n" +
						"   <widget class=\"QMenu\" name=\"menuHelp\">\n" +
						"    <property name=\"title\">\n" +
						"     <string>Help</string>\n" +
						"    </property>\n" +
						"    <addaction name=\"actionAbout\"/>\n" +
						"   </widget>\n" +
						"   <addaction name=\"menuExit\"/>\n" +
						"   <addaction name=\"menuHelp\"/>\n" +
						"  </widget>\n" +
						"  <widget class=\"QToolBar\" name=\"mainToolBar\">\n" +
						"   <attribute name=\"toolBarArea\">\n" +
						"    <enum>TopToolBarArea</enum>\n" +
						"   </attribute>\n" +
						"   <attribute name=\"toolBarBreak\">\n" +
						"    <bool>false</bool>\n" +
						"   </attribute>\n" +
						"  </widget>\n" +
						"  <widget class=\"QStatusBar\" name=\"statusBar\"/>\n" +
						"  <action name=\"actionExit\">\n" +
						"   <property name=\"text\">\n" +
						"    <string>Exit</string>\n" +
						"   </property>\n" +
						"  </action>\n" +
						"  <action name=\"actionAbout\">\n" +
						"   <property name=\"text\">\n" +
						"    <string>About</string>\n" +
						"   </property>\n" +
						"  </action>\n" +
						" </widget>\n" +
						" <layoutdefault spacing=\"6\" margin=\"11\"/>\n" +
						" <resources/>\n" +
						" <connections/>\n" +
						"</ui>").replace("{NAME}", name);
		VirtualFile vfile = CreateFile(project, directoryVirtualFile, name + "Window.ui", content);
		//CMakeListsEditor.getInstance(project.getBaseDir()).SetVariable("SOURCE_FILES", vfile.getName());
		return vfile;
	}

	private void showEmptyFilenameError(Project project) {
		Messages.showErrorDialog(project, "Wrong name", "Error");
	}

	private int getOverwriteChoice(Project project) {
		return Messages.showYesNoCancelDialog(project, "Override", "File exists", null);
	}

	private String getDesiredFilename(Project project) {
		return Messages.showInputDialog(project, "Window Name ?", "New Window", null);
	}


	private void deleteVirtualFile(final VirtualFile virtualFile) {
		if (virtualFile == null) {
			return;
		}
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				try {
					virtualFile.delete(this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}


	public static VirtualFile CreateFile(final Project project, final VirtualFile directory, final String name, final String result) {
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				try {
					VirtualFile sketch = directory.createChildData(this, name);
					final Document sketchDocument = FileDocumentManager.getInstance().getDocument(sketch);
					if (sketchDocument != null) {
						CommandProcessor.getInstance().executeCommand(project, new Runnable() {
							@Override
							public void run() {
								sketchDocument.setText(result);
							}
						}, null, null, sketchDocument);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return directory.findChild(name);
	}
}