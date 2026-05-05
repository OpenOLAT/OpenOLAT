/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.notifications.tool;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;

import org.olat.modules.selectus.ui.notifications.NotificationUserSettingsController;

/**
 * 
 * Initial date: 25 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangeNotificationsSettingsController extends BasicController {
	
	private Link notificationsSettingsLink;
	
	private CloseableModalController cmc;
	private NotificationUserSettingsController notificationUserSettingsCtrl;
	
	public ChangeNotificationsSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(NotificationUserSettingsController.class, ureq.getLocale()));
		
		VelocityContainer mainVC = createVelocityContainer("change_settings");
		mainVC.setDomReplacementWrapperRequired(false);
		
		notificationsSettingsLink = LinkFactory.createLink("change.notifications.settings", "change.notifications.settings", mainVC, this);
		notificationsSettingsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_change_notifications_settings");
		notificationsSettingsLink.setDomReplacementWrapperRequired(false);
		mainVC.put("change.notifications.settings", notificationsSettingsLink);

		StackedPanel p = new SimpleStackedPanel("notificationsSettingsPanel");
		p.setDomReplacementWrapperRequired(false);
		p.setContent(notificationsSettingsLink);
		putInitialPanel(p);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(notificationsSettingsLink == source) {
			doEditNotificationSettings(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(notificationUserSettingsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(notificationUserSettingsCtrl);
		removeAsListenerAndDispose(cmc);
		notificationUserSettingsCtrl = null;
		cmc = null;
	}
	
	private void doEditNotificationSettings(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(notificationUserSettingsCtrl);
		
		notificationUserSettingsCtrl = new NotificationUserSettingsController(ureq, getWindowControl(), getIdentity());
		listenTo(notificationUserSettingsCtrl);
		String title = translate("notifications.title");
		cmc = new CloseableModalController(getWindowControl(), "c", notificationUserSettingsCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
}
