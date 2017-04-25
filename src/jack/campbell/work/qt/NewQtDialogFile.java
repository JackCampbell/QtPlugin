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
public class NewQtDialogFile extends AnAction {
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
		String content =("#include <QtCore>\n" +
						 "#include <QtWidgets>\n" +
						 "#include <QApplication>\n" +
						 "#pragma once\n" +
						 "\n" +
						 "namespace Ui {\n" +
							 "\tclass {NAME}Dialog;\n" +
						 "}\n" +
						 "\n" +
						 "class {NAME}Dialog : public QDialog {\n" +
							 "\tQ_OBJECT\n" +
						 "public:\n" +
							 "\texplicit {NAME}Dialog(QWidget *parent = nullptr);\n" +
							 "\t\t\t\t\t~{NAME}Dialog();\n" +
						 "public slots:\n" +
						 "private:\n" +
							 "\tUi::{NAME}Dialog *ui;\n" +
						 "};").replace("{NAME}", name);
		VirtualFile vfile = CreateFile(project, directoryVirtualFile, name + "Dialog.h", content);
		return vfile;
	}

	private VirtualFile CreateSourceFile(Project project, VirtualFile directoryVirtualFile, String name) {
		String content =("#include \"{NAME}Dialog.h\"\n" +
						 "#include \"ui_{NAME}Dialog.h\"\n" +
						 "\n" +
						 "\n" +
						 "{NAME}Dialog::{NAME}Dialog(QWidget *parent) : QDialog(parent), ui(new Ui::{NAME}Dialog) {\n" +
						 "\tui->setupUi(this);\n" +
						 "}\n" +
						 "\n" +
						 "{NAME}Dialog::~{NAME}Dialog() {\n" +
							 "\tdelete ui;\n" +
						 "}\n").replace("{NAME}", name);
		VirtualFile vfile = CreateFile(project, directoryVirtualFile, name + "Dialog.cpp", content);
		//CMakeListsEditor.getInstance(project.getBaseDir()).SetVariable("SOURCE_FILES", vfile.getName());
		return vfile;
	}

	public VirtualFile CreateUserInterfaceFile(Project project, VirtualFile directoryVirtualFile, String name) {
		String content =
				("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<ui version=\"4.0\">\n" +
				"   <class>{NAME}Dialog</class>\n" +
				"       <widget class=\"QDialog\" name=\"{NAME}Dialog\">\n" +
				"        <property name=\"geometry\">\n" +
				"            <rect>\n" +
				"                <x>0</x>\n" +
				"                <y>0</y>\n" +
				"                <width>615</width>\n" +
				"                <height>524</height>\n" +
				"            </rect>\n" +
				"        </property>\n" +
				"        <property name=\"windowTitle\">\n" +
				"            <string>{NAME}</string>\n" +
				"        </property>\n" +
				"        <layout class=\"QVBoxLayout\" name=\"verticalLayout_2\" stretch=\"1,0\">\n" +
				"            <item>\n" +
				"                <widget class=\"QLabel\" name=\"label\">\n" +
				"                    <property name=\"text\">\n" +
				"                        <string>Application</string>\n" +
				"                    </property>\n" +
				"                </widget>\n" +
				"            </item>\n" +
				"            <item>\n" +
				"                <layout class=\"QHBoxLayout\" name=\"horizontalLayout\">\n" +
				"                    <item>\n" +
				"                        <spacer name=\"horizontalSpacer\">\n" +
				"                            <property name=\"orientation\">\n" +
				"                                <enum>Qt::Horizontal</enum>\n" +
				"                            </property>\n" +
				"                            <property name=\"sizeHint\" stdset=\"0\">\n" +
				"                                <size>\n" +
				"                                    <width>40</width>\n" +
				"                                    <height>20</height>\n" +
				"                                </size>\n" +
				"                            </property>\n" +
				"                        </spacer>\n" +
				"                    </item>\n" +
				"                    <item>\n" +
				"                        <widget class=\"QPushButton\" name=\"pushButton\">\n" +
				"                            <property name=\"text\">\n" +
				"                                <string>OK</string>\n" +
				"                            </property>\n" +
				"                        </widget>\n" +
				"                    </item>\n" +
				"                </layout>\n" +
				"            </item>\n" +
				"        </layout>\n" +
				"    </widget>\n" +
				"    <resources/>\n" +
				"    <connections>\n" +
				"        <connection>\n" +
				"            <sender>pushButton</sender>\n" +
				"            <signal>clicked()</signal>\n" +
				"            <receiver>Dialog</receiver>\n" +
				"            <slot>accept()</slot>\n" +
				"            <hints>\n" +
				"                <hint type=\"sourcelabel\">\n" +
				"                    <x>548</x>\n" +
				"                    <y>491</y>\n" +
				"                </hint>\n" +
				"                <hint type=\"destinationlabel\">\n" +
				"                    <x>335</x>\n" +
				"                    <y>489</y>\n" +
				"                </hint>\n" +
				"            </hints>\n" +
				"        </connection>\n" +
				"    </connections>\n" +
				"</ui>\n").replace("{NAME}", name);
		VirtualFile vfile = CreateFile(project, directoryVirtualFile, name + "Dialog.ui", content);
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
		return Messages.showInputDialog(project, "<name>Dialog ?", "New Dialog", null);
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