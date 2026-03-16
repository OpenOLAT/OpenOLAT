/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.notifications.tool;

import org.olat.admin.user.tools.UserTool;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 25 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangeNotificationsSettingsTool implements UserTool {

	private WindowControl wControl;
	private ChangeNotificationsSettingsController settingsCtrl;
	
	public ChangeNotificationsSettingsTool(WindowControl wControl) {
		this.wControl = wControl;
	}

	@Override
	public Component getMenuComponent(UserRequest ureq, VelocityContainer container, boolean iconOnly) {
		if(settingsCtrl == null) {
			settingsCtrl = new ChangeNotificationsSettingsController(ureq, wControl);
		}
		Component cmp = settingsCtrl.getInitialComponent();
		container.put(cmp.getComponentName(), cmp);
		return cmp;
	}
	
	@Override
	public void dispose() {
		if(settingsCtrl != null) {
			settingsCtrl.dispose();
		}
	}
}
