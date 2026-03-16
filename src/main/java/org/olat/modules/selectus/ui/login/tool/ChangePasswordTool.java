/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.login.tool;

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
public class ChangePasswordTool implements UserTool {
	
	private WindowControl wControl;
	private ChangePasswordToolController changePasswordCtrl;
	
	public ChangePasswordTool(WindowControl wControl) {
		this.wControl = wControl;
	}

	@Override
	public Component getMenuComponent(UserRequest ureq, VelocityContainer container, boolean iconOnly) {//TODO selectus iconOnly
		if(changePasswordCtrl == null) {
			changePasswordCtrl = new ChangePasswordToolController(ureq, wControl);
		}
		Component cmp = changePasswordCtrl.getInitialComponent();
		container.put(cmp.getComponentName(), cmp);
		return cmp;
	}

	@Override
	public void dispose() {
		if(changePasswordCtrl != null) {
			changePasswordCtrl.dispose();
		}
	}
}
