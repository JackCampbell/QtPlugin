package jack.campbell.work.qt;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

/**
 * Created by jack on 4/29/17.
 */
public class NewQtClassDialog extends DialogWrapper implements DocumentListener, ItemListener {
	private JPanel mainPanel;
	private JTextField fldTitle;
	private JComboBox fldBaseClass;
	private JTextField fldClassName;
	private JCheckBox btnSettings;
	private JCheckBox btnDesign;
	private JCheckBox btnSlot;
	private JTextField fldSourceFile;
	private JTextField fldHeaderFile;
	private JTextField fldDesignFile;

	private Project project;
	private VirtualFile directoryVirtualFile;
	protected NewQtClassDialog(@Nullable Project project, VirtualFile directoryVirtualFile) {
		super(project, true);
		setTitle("Create Qt Class Dialog");
		fldTitle.getDocument().addDocumentListener(this);
		fldBaseClass.addItemListener(this);
		btnDesign.addItemListener(this);
		this.directoryVirtualFile = directoryVirtualFile;
		this.project = project;
		init();
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return mainPanel;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		SyncClassName(null);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		SyncClassName(null);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		SyncClassName(null);
	}

	@Nullable
	@Override
	protected ValidationInfo doValidate() {
		String title = fldTitle.getText();
		if(title.isEmpty()) {
			return new ValidationInfo("Required", fldTitle);
		}
		return null;
	}

	private void SyncClassName(String selectItem) {
		if(selectItem == null) {
			selectItem = fldBaseClass.getSelectedItem().toString();
		}
		String title = fldTitle.getText();
		if(title.isEmpty()) {
			return;
		}
		String className = title;
		if(selectItem.equals("QDialog")) {
			btnDesign.setEnabled(true);
			btnSlot.setEnabled(true);
			className = title + "Dialog";
		} else if(selectItem.equals("QMainWindow")) {
			btnDesign.setEnabled(true);
			btnSlot.setEnabled(false);
			className = title + "Window";
		} else if(selectItem.equals("QDockWidget")) {
			btnDesign.setEnabled(true);
			btnSlot.setEnabled(false);
			className = title + "Panel";
		} else if(selectItem.equals("QWidget")) {
			btnSlot.setEnabled(false);
			btnDesign.setEnabled(true);
			className = title + "View";
		} else if(selectItem.equals("QMdiSubWindow")) {
			btnSlot.setEnabled(false);
			btnDesign.setEnabled(true);
			className = title + "Child";
		} else if(selectItem.equals("QObject")) {
			btnSlot.setEnabled(false);
			btnDesign.setEnabled(false);
		}
		fldDesignFile.setEnabled(btnDesign.isSelected());
		fldClassName.setText(className);
		fldSourceFile.setText(className + ".cpp");
		fldHeaderFile.setText(className + ".h");
		fldDesignFile.setText(className + ".ui");
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		//if (event.getStateChange() == ItemEvent.SELECTED) {
			//String selectItem = event.getItem().toString();
			SyncClassName(null);
		//}
	}

	public VirtualFile CreateHeaderFile(Project project, VirtualFile directoryVirtualFile) {
		String selectItem = fldBaseClass.getSelectedItem().toString();
		String title = fldTitle.getText();
		String className = fldClassName.getText();
		String headerFile = fldHeaderFile.getText();
		String content = "// Generator id: " + title + "\n";
		content += "#include <QtCore>\n";
		content += "#include <QtWidgets>\n";
		content += "#include <QtCore>\n";
		content += "#pragma once\n";
		content += "\n";
		if(btnDesign.isSelected()) {
			content += "namespace Ui {\n";
			content += "\tclass " + className + ";\n";
			content += "}\n";
			content += "\n";
		}
		content += "class " + className + " : public " + selectItem + " {\n";
		content += "\tQ_OBJECT\n";
		content += "public:\n";
		if(selectItem.equals("QDialog") || selectItem.equals("QMainWindow") || selectItem.equals("QDockWidget") ||
				selectItem.equals("QWidget") || selectItem.equals("QMdiSubWindow")) {
			content += "\texplicit\t" + className + "(QWidget *parent = nullptr);\n";
		} else {
			content += "\texplicit\t" + className + "(QObject *parent = nullptr);\n";
		}
		content += "\t\t\t\t~" + className + "();\n";
		if(btnSettings.isSelected()) {
			content += "protected:\n";
			content += "\tvoid\tLoadSetting();\n";
			content += "\tvoid\tSaveSetting();\n";
		}
		content += "signals:\n";
		content += "private slots:\n";
		content += "private:\n";
		if(btnDesign.isSelected()) {
			content += "\tUi::" + className + " *\tui;\n";
		}
		if(btnSettings.isSelected()) {
			content += "\tQSettings\t\t\t\tsettings;\n";
		}
		content += "};\n";
		return CreateFile(project, directoryVirtualFile, headerFile, content);
	}


	public VirtualFile CreateSourceFile(Project project, VirtualFile directoryVirtualFile) {
		String selectItem = fldBaseClass.getSelectedItem().toString();
		String title = fldTitle.getText();
		String className = fldClassName.getText();
		String headerFile = fldHeaderFile.getText();
		String baseClass = fldBaseClass.getSelectedItem().toString();
		String sourceFile = fldSourceFile.getText();
		String designFile = fldDesignFile.getText();
		String content = "// Generator id: " + title + "\n";
		content += "#include \"" + headerFile + "\"\n";
		if(btnDesign.isSelected()) {
			content += "#include \"ui_" + designFile.replace(".ui", ".h") + "\"\n";
		}
		content += "\n";
		if(selectItem.equals("QDialog") || selectItem.equals("QMainWindow") || selectItem.equals("QDockWidget") ||
				selectItem.equals("QWidget") || selectItem.equals("QMdiSubWindow")) {
			content += className + "::" + className + "(QWidget *parent) : ";
		} else {
			content += className + "::" + className + "(QObject *parent) : ";
		}
		content += " " + baseClass + "(parent)";
		if(btnDesign.isSelected()) {
			content += ", ui(new Ui::" + className + ") {\n";
			content += "\tui->setupUi(this);\n";
		} else {
			content += " {\n";
		}
		if(btnSettings.isSelected()) {
			content += "\tLoadSetting();\n";
		}
		content += "\t// QTP_BEGIN_SLOT\n";
		content += "\t// QTP_END_SLOT\n";
		content += "}\n";
		content += "\n";
		content += className + "::~" + className + "() {\n";
		if(btnDesign.isSelected()) {
			content += "\tdelete ui;\n";
		}
		content += "}\n";
		content += "\n";
		if(btnSettings.isSelected()) {
			content += "void " + className + "::LoadSetting() {\n";
			content += "\tsettings.beginGroup(\"" + title.toLowerCase() + "\");\n";
			content += "\t// TODO load ui elements ...\n";
			content += "\tsettings.endGroup();\n";
			content += "}\n";
			content += "\n";

			content += "void " + className + "::SaveSetting() {\n";
			content += "\tsettings.beginGroup(\"" + title.toLowerCase() + "\");\n";
			content += "\t// TODO save ui elements ...\n";
			content += "\tsettings.endGroup();\n";
			content += "}\n";
			content += "\n";
		}

		return CreateFile(project, directoryVirtualFile, sourceFile, content);
	}


	public VirtualFile CreateDesignFile(Project project, VirtualFile directoryVirtualFile) {
		if(!btnDesign.isSelected()) {
			return null;
		}
		String selectItem = fldBaseClass.getSelectedItem().toString();
		String title = fldTitle.getText();
		String className = fldClassName.getText();
		String baseClass = fldBaseClass.getSelectedItem().toString();
		String designFile = fldDesignFile.getText();
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		content += "<ui version=\"4.0\">\n";
		content += "<class>" + className + "</class>\n";
		content += "\t<widget class=\"" + baseClass + "\" name=\"" + className + "\">\n";
		content += "\t\t<property name=\"geometry\">\n";
		content += "\t\t\t<rect>\n";
		content += "\t\t\t\t<x>0</x>\n";
		content += "\t\t\t\t<y>0</y>\n";
		content += "\t\t\t\t<width>615</width>\n";
		content += "\t\t\t\t<height>524</height>\n";
		content += "\t\t\t</rect>\n";
		content += "\t\t</property>\n";
		content += "\t\t<property name=\"windowTitle\">\n";
		content += "\t\t\t<string>" + title + "</string>\n";
		content += "\t\t</property>\n";
		if(selectItem.equals("QMainWindow")) {
			content += "\t\t\t<widget class=\"QMenuBar\" name=\"menuBar\" />\n";
			content += "\t\t\t<widget class=\"QStatusBar\" name=\"statusBar\" />\n";
			content += "\t\t\t<widget class=\"QToolBar\" name=\"toolBar\" />\n";
		} else if(btnSlot.isSelected() && selectItem.equals("QDialog")) {
			content += "\t\t<layout class=\"QVBoxLayout\" name=\"verticalLayout_2\" stretch=\"1,0\">\n";
			content += "\t\t\t<item>\n";
			content += "\t\t\t\t<widget class=\"QLabel\" name=\"label\">\n";
			content += "\t\t\t\t\t<property name=\"text\">\n";
			content += "\t\t\t\t\t\t<string>Application</string>\n";
			content += "\t\t\t\t\t\t</property>\n";
			content += "\t\t\t\t</widget>\n";
			content += "\t\t\t</item>\n";
			content += "\t\t\t<item>\n";
			content += "\t\t\t\t<layout class=\"QHBoxLayout\" name=\"horizontalLayout\">\n";
			content += "\t\t\t\t\t<item>\n";
			content += "\t\t\t\t\t\t<spacer name=\"horizontalSpacer\">\n";
			content += "\t\t\t\t\t\t\t<property name=\"orientation\">\n";
			content += "\t\t\t\t\t\t\t\t<enum>Qt::Horizontal</enum>\n";
			content += "\t\t\t\t\t\t\t</property>\n";
			content += "\t\t\t\t\t\t\t<property name=\"sizeHint\" stdset=\"0\">\n";
			content += "\t\t\t\t\t\t\t\t<size>\n";
			content += "\t\t\t\t\t\t\t\t\t<width>40</width>\n";
			content += "\t\t\t\t\t\t\t\t\t<height>20</height>\n";
			content += "\t\t\t\t\t\t\t\t</size>\n";
			content += "\t\t\t\t\t\t\t</property>\n";
			content += "\t\t\t\t\t\t</spacer>\n";
			content += "\t\t\t\t\t</item>\n";
			content += "\t\t\t\t\t<item>\n";
			content += "\t\t\t\t\t\t<widget class=\"QPushButton\" name=\"btnOK\">\n";
			content += "\t\t\t\t\t\t\t<property name=\"text\">\n";
			content += "\t\t\t\t\t\t\t\t<string>OK</string>\n";
			content += "\t\t\t\t\t\t\t</property>\n";
			content += "\t\t\t\t\t\t</widget>\n";
			content += "\t\t\t\t\t</item>\n";
			content += "\t\t\t\t</layout>\n";
			content += "\t\t\t</item>\n";
			content += "\t\t</layout>\n";
        } else if(selectItem.equals("QDockWidget")) {
            content += "<widget class=\"QWidget\" name=\"dockWidgetContents\">";
            content += "</widget>";
        }
		content += "\t</widget>\n";
		content += "\t<resources/>\n";
		if(btnSlot.isSelected() && selectItem.equals("QDialog")) {
			content += "\t<connections>\n";
			content += "\t\t<connection>\n";
			content += "\t\t\t<sender>btnOK</sender>\n";
			content += "\t\t\t<signal>clicked()</signal>\n";
			content += "\t\t\t<receiver>" + className + "</receiver>\n";
			content += "\t\t\t<slot>accept()</slot>\n";
			content += "\t\t\t<hints>\n";
			content += "\t\t\t\t<hint type=\"sourcelabel\">\n";
			content += "\t\t\t\t\t<x>548</x>\n";
			content += "\t\t\t\t\t<y>491</y>\n";
			content += "\t\t\t\t</hint>\n";
			content += "\t\t\t\t<hint type=\"destinationlabel\">\n";
			content += "\t\t\t\t\t<x>335</x>\n";
			content += "\t\t\t\t\t<y>489</y>\n";
			content += "\t\t\t\t</hint>\n";
			content += "\t\t\t</hints>\n";
			content += "\t\t</connection>\n";
			content += "\t</connections>\n";
		} else {
			content += "\t<connections/>\n";
		}
		content += "</ui>\n";
		return CreateFile(project, directoryVirtualFile, designFile, content);
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


	private boolean CheckDirectory(VirtualFile filename) {
		if(filename == null) {
			return true;
		}
		int overwriteChoice = Messages.showYesNoCancelDialog(project, "Override", "File exists: " + filename, null);
		switch (overwriteChoice) {
			case Messages.YES:
				deleteVirtualFile(filename);
				break;
			case Messages.NO:
				break;
			case Messages.CANCEL:
				return false;
		}
		return true;
	}

	@Override
	protected void doOKAction() {
		String selectItem = fldBaseClass.getSelectedItem().toString();
		if(selectItem.isEmpty()) {
			Messages.showErrorDialog(project, "Is empty base class", "Error");
			return;
		}
		String title = fldTitle.getText();
		if(title.isEmpty()) {
			Messages.showErrorDialog(project, "Is empty title", "Error");
			return;
		}
		String className = fldClassName.getText();
		if(className.isEmpty()) {
			Messages.showErrorDialog(project, "Is empty class name", "Error");
			return;
		}

		String designFile = fldDesignFile.getText();
		if(btnDesign.isSelected() && className.isEmpty()) {
			Messages.showErrorDialog(project, "Is empty design file", "Error");
			return;
		}
		VirtualFile existingFile = directoryVirtualFile.findChild(designFile);
		if(!CheckDirectory(existingFile)) {
			return;
		}
		String headerFile = fldHeaderFile.getText();
		if(headerFile.isEmpty()) {
			Messages.showErrorDialog(project, "Is empty header file", "Error");
			return;
		}
		existingFile = directoryVirtualFile.findChild(headerFile);
		if(!CheckDirectory(existingFile)) {
			return;
		}
		String sourceFile = fldSourceFile.getText();
		if(sourceFile.isEmpty()) {
			Messages.showErrorDialog(project, "Is empty source file", "Error");
			return;
		}
		existingFile = directoryVirtualFile.findChild(sourceFile);
		if(!CheckDirectory(existingFile)) {
			return;
		}

		super.doOKAction();
	}
	private Icon icon = IconLoader.findIcon("/qt_icon.png");
	private void showEmptyFilenameError(Project project) {
		Messages.showErrorDialog(project, "Wrong name", "Error");
	}

	private int getOverwriteChoice(Project project) {
		return Messages.showYesNoCancelDialog(project, "Override", "File exists", icon);
	}

	private String getDesiredFilename(Project project) {
		return Messages.showInputDialog(project, "<name>Dialog ?", "New Dialog", icon);
	}
}
