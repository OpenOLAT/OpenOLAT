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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.admin.notifications;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;


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
	
	private final JobKey notificationsJobKey = new JobKey("org.olat.notifications.job.enabled", Scheduler.DEFAULT_GROUP);

	private Link startNotifyButton;
	
	@Autowired
	private Scheduler scheduler;

	public NotificationsEmailAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer content = createVelocityContainer("index");
		boolean enabled;
		String cronExpression = "";
		try {
			CoreSpringFactory.getBean("org.olat.notifications.job.enabled");
			enabled = true;
			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(notificationsJobKey);
			if(triggers.size() == 1 && triggers.get(0) instanceof CronTrigger) {
				cronExpression = ((CronTrigger)triggers.get(0)).getCronExpression();
			}
		} catch (Exception e) {
			enabled = false;
		}
		content.contextPut("status", getTranslator().translate("notification.status", new String[]{ String.valueOf(enabled), cronExpression }));
		startNotifyButton = LinkFactory.createButton(TRIGGER_NOTIFY, content, this);
		putInitialPanel(content);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startNotifyButton) {
			//trigger the cron job
			try {
				CoreSpringFactory.getImpl(Scheduler.class).triggerJob(notificationsJobKey);
			} catch (SchedulerException e) {
				logError("", e);
			}
		}
	}
}