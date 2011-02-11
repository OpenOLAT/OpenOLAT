/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.admin.notifications;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.notifications.NotificationsManager;
import org.springframework.scheduling.quartz.CronTriggerBean;

/**
 * Description:<br>
 * Manually trigger sending of notification email which are normally sent only once a day.
 * <P>
 * Initial Date: Dec 19, 2006 <br>
 * 
 * @author guido
 */
public class NotificationsEmailAdminController extends BasicController {
	private static final String TRIGGER_NOTIFY = "notification.start.button";

	private VelocityContainer content;
	private Link startNotifyButton;

	public NotificationsEmailAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		content = createVelocityContainer("index");
		boolean enabled;
		String cronExpression = "";
		try {
			CoreSpringFactory.getBean("org.olat.notifications.job.enabled");
			enabled = true;
			CronTriggerBean bean = (CronTriggerBean)CoreSpringFactory.getBean("sendNotificationsEmailTrigger");
			cronExpression = bean.getCronExpression();
		} catch (Exception e) {
			enabled = false;
		}
		content.contextPut("status", getTranslator().translate("notification.status", new String[]{ String.valueOf(enabled), cronExpression }));
		startNotifyButton = LinkFactory.createButton(TRIGGER_NOTIFY, content, this);
		putInitialPanel(content);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startNotifyButton) {
			NotificationsManager.getInstance().notifyAllSubscribersByEmail();
		}

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//nothing to do
	}

}
