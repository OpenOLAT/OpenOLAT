package org.olat.core.helpers;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GUISettings extends AbstractSpringModule {

	private static final String KEY_GUI_THEME_IDENTIFYER = "layout.theme";
	
	/**
	 * Set the system theme here. Make sure the directory webapp/WEB-INF/static/themes/YOURTHEME exists. 
	 * This is only the default value in case no user configuration is found. Use the administration GUI to
	 * Set a specific theme.
	 */
	@Value("${layout.theme:light}")
	private String guiThemeIdentifyer;
	
	@Autowired
	public GUISettings(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		//module enabled/disabled
		String guiThemeIdentifyerObj = getStringPropertyValue(KEY_GUI_THEME_IDENTIFYER, true);
		if(StringHelper.containsNonWhitespace(guiThemeIdentifyerObj)) {
			guiThemeIdentifyer = guiThemeIdentifyerObj;
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	/**
	 * @return the CSS theme used for this webapp
	 */
	public String getGuiThemeIdentifyer() {
		return guiThemeIdentifyer;			
	}

	/**
	 * Set the CSS theme used for this webapp. Only used by spring. Use static
	 * method to change the theme at runtime!
	 * 
	 * @param guiTheme
	 */
	public void setGuiThemeIdentifyer(String guiThemeIdentifyer) {
		this.guiThemeIdentifyer = guiThemeIdentifyer;
		setStringProperty(KEY_GUI_THEME_IDENTIFYER, guiThemeIdentifyer, true);
	}
}
