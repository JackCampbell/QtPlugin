package jack.campbell.work.qt;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.wm.impl.welcomeScreen.NewWelcomeScreen;

/**
 * Created by jack on 4/20/17.
 */
public class NewQtProject extends AnAction {
	public void update(AnActionEvent event) {
		Presentation presentation = event.getPresentation();
		if (ActionPlaces.isMainMenuOrActionSearch(event.getPlace())) {
			presentation.setIcon(null);
		}

		if (NewWelcomeScreen.isNewWelcomeScreen(event)) {
			event.getPresentation().setIcon(AllIcons.Welcome.CreateNewProject);
		}
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		NewQtProjectWizard wizard = new NewQtProjectWizard();
		wizard.runWizard();
	}
}
