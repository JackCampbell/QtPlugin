package jack.campbell.work.qt;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created by jack on 6/7/17.
 */
@State(name = "QtPreference", storages = {
		@Storage(id = "other", file = "QtPreferenceConfig.xml")
})
public class QtPreference implements PersistentStateComponent<QtPreference> {
	public String winPath;
	public String macPath;
	public String author;
	public String company;
	public String domain;

	@Nullable
	@Override
	public QtPreference getState() {
		return this;
	}

	@Override
	public void loadState(QtPreference qtPreference) {
		XmlSerializerUtil.copyBean(qtPreference, this);
	}
}
