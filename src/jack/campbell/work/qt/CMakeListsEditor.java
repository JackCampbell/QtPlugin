package jack.campbell.work.qt;

import com.intellij.openapi.application.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.vfs.*;

import java.util.*;

/**
 * Created by francois on 15-08-04.
 */
public class CMakeListsEditor {
    private VirtualFile cMakeListsVirtualFile;

    private CMakeListsEditor(VirtualFile cMakeLists) {
        this.cMakeListsVirtualFile = cMakeLists;
    }

    private VirtualFile GetCMakeListsVirtualFile() {
        return cMakeListsVirtualFile;
    }

    private Document GetCMakeListsDocument() {
        return FileDocumentManager.getInstance().getDocument(cMakeListsVirtualFile);
    }

    public void Clear() {
        final Document cMakeLists = GetCMakeListsDocument();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(null, new Runnable() {
                    @Override
                    public void run() {
                        cMakeLists.deleteString(0, cMakeLists.getTextLength());
                        FileDocumentManager.getInstance().saveDocument(cMakeLists);
                    }
                }, null, null, cMakeLists);
            }
        });
    }

    public void AddLine(int line, final String text) {
        final Document cMakeLists = GetCMakeListsDocument();
        final int lineEndOffset = cMakeLists.getLineEndOffset(line);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(null, new Runnable() {
                    @Override
                    public void run() {
                        cMakeLists.insertString(lineEndOffset, text + "\n");
                        FileDocumentManager.getInstance().saveDocument(cMakeLists);
                    }
                }, null, null, cMakeLists);
            }
        });
    }

    public void AppendLine(String text) {
        int lastLine = GetCMakeListsDocument().getLineCount();
        if (lastLine != 0) {
            lastLine--;
        }
        AddLine(lastLine, text);
    }

    public void blankLine() {
        AppendLine("");
    }

    public void SetVariable(String var, String value) {
	    SetMethod("set", var, value);
    }

    public void SetProject(String projectName) {
	    SetMethod("project", projectName);
    }

    public void SetMethod(String methodName, String... args) {
        AppendLine(methodName + "(" + String.join(" ", args) + ")");
    }

    private static Map<VirtualFile, CMakeListsEditor> INSTANCES = new WeakHashMap<VirtualFile, CMakeListsEditor>();
    public static CMakeListsEditor getInstance(VirtualFile cMakeLists) {
        if (!INSTANCES.containsKey(cMakeLists)) {
            INSTANCES.put(cMakeLists, new CMakeListsEditor(cMakeLists));
        }
        return INSTANCES.get(cMakeLists);
    }
}
