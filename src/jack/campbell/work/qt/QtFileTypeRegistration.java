package jack.campbell.work.qt;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by jack on 4/20/17.
 */
public class QtFileTypeRegistration implements ApplicationComponent {

	public void initComponent() {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				ApplicationManager.getApplication().runWriteAction(new Runnable() {
					@Override
					public void run() {
						FileType cpp = FileTypeManager.getInstance().getFileTypeByExtension("cpp");
						FileTypeManager.getInstance().associateExtension(cpp, "h");
						//FileTypeManager.getInstance().associateExtension(cpp, "ui");
						//FileTypeManager.getInstance().removeAssociatedExtension(cpp, "qrs");
					}
				});
			}
		});
	}

	public void disposeComponent() {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				ApplicationManager.getApplication().runWriteAction(new Runnable() {
					@Override
					public void run() {
						FileType cpp = FileTypeManager.getInstance().getFileTypeByExtension("cpp");
						FileTypeManager.getInstance().removeAssociatedExtension(cpp, "h");
						//FileTypeManager.getInstance().removeAssociatedExtension(cpp, "ui");
						//FileTypeManager.getInstance().removeAssociatedExtension(cpp, "qrs");
					}
				});
			}
		});
	}

	@NotNull
	public String getComponentName() {
		return "QtFileTypeRegistration";
	}
}
