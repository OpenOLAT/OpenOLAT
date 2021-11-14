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
package org.olat.admin.statistics;

import java.text.DateFormat;
import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.statistic.StatisticUpdateManager;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin Controller for statistics - similar to the notifications controller.
 * <p>
 * The idea is that you can go on the single-service node to see how the 
 * statistic update cronjob is configured.
 * Plus optionally (to be decided) whether you can trigger the statistic update
 * manually.
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
public class StatisticsAdminController extends BasicController {

	/** the logging object used in this class **/
	private static final Logger log_ = Tracing.createLoggerFor(StatisticsAdminController.class);

	private static final String STATISTICS_FULL_RECALCULATION_TRIGGER_BUTTON = "statistics.fullrecalculation.trigger.button";
	private static final String STATISTICS_UPDATE_TRIGGER_BUTTON = "statistics.update.trigger.button";

	private VelocityContainer content;

	private DialogBoxController dialogCtr_;
	
	@Autowired
	private Scheduler scheduler;
	
	public StatisticsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		content = createVelocityContainer("index");
		LinkFactory.createButton(STATISTICS_FULL_RECALCULATION_TRIGGER_BUTTON, content, this);
		LinkFactory.createButton(STATISTICS_UPDATE_TRIGGER_BUTTON, content, this);

		refreshUIState();
		
		putInitialPanel(content);
	}

	private void refreshUIState() {

		log_.info("refreshUIState: schedulerFactoryBean found");

		boolean enabled = false;
		String cronExpression = "";
		Trigger.TriggerState triggerState;
		try {
			TriggerKey triggerKey = new TriggerKey("updateStatisticsTrigger", null/*trigger group*/);
			triggerState = scheduler.getTriggerState(triggerKey);
			enabled = triggerState != Trigger.TriggerState.NONE && triggerState!=Trigger.TriggerState.ERROR;
			
			Trigger trigger = scheduler.getTrigger(triggerKey);
			if(trigger == null) {
				enabled &= false;
			} else {
				enabled &= trigger.getJobKey().getName().equals("org.olat.statistics.job.enabled");
				if(trigger instanceof CronTrigger) {
					log_.info("refreshUIState: org.olat.statistics.job.enabled check, enabled now: "+enabled);
					cronExpression = ((CronTrigger)trigger).getCronExpression();
				}
			}
			
			log_.info("refreshUIState: updateStatisticsTrigger state was "+triggerState+", enabled now: "+enabled);
		} catch (SchedulerException e) {
			log_.warn("refreshUIState: Got a SchedulerException while asking for the updateStatisticsTrigger's state", e);
		}
		
		StatisticUpdateManager statisticUpdateManager = getStatisticUpdateManager();
		if (statisticUpdateManager==null) {
			log_.info("refreshUIState: statisticUpdateManager not configured");
			enabled = false;
		} else {
			enabled &= statisticUpdateManager.isEnabled();
			log_.info("refreshUIState: statisticUpdateManager configured, enabled now: "+enabled);
		}

		if (enabled) {
			content.contextPut("status", getTranslator().translate("statistics.status.enabled", new String[]{ cronExpression }));
		} else {
			content.contextPut("status", getTranslator().translate("statistics.status.disabled"));
		}
		content.contextPut("statisticEnabled", enabled);

		recalcLastUpdated();
		
		updateStatisticUpdateOngoingFlag();
	}
	
	private void updateStatisticUpdateOngoingFlag() {
		StatisticUpdateManager statisticUpdateManager = getStatisticUpdateManager();
		if (statisticUpdateManager==null) {
			log_.info("event: UpdateStatisticsJob configured, but no StatisticManager available");
			content.contextPut("statisticUpdateOngoing", Boolean.TRUE);
		} else {
			content.contextPut("statisticUpdateOngoing", statisticUpdateManager.updateOngoing());
		}
	}

	private void recalcLastUpdated() {
		try{
			long lastUpdated = getStatisticUpdateManager().getLastUpdated();
			if (lastUpdated==-1) {
				content.contextPut("lastupdated", getTranslator().translate("statistics.lastupdated.never", null));
			} else {
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, getLocale());
				content.contextPut("lastupdated", getTranslator().translate("statistics.lastupdated", new String[] {df.format(new Date(lastUpdated))}));
			}
		} catch(Exception e) {
			content.contextPut("lastupdated", getTranslator().translate("statistics.lastupdated", null));
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogCtr_) {
			if (DialogBoxUIFactory.isYesEvent(event)) {				
				StatisticUpdateManager statisticUpdateManager = getStatisticUpdateManager();
				if (statisticUpdateManager==null) {
					log_.info("event: UpdateStatisticsJob configured, but no StatisticManager available");
				} else {
					statisticUpdateManager.updateStatistics(true, getUpdateFinishedCallback());
					refreshUIState();
					getInitialComponent().setDirty(true);
				}
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (STATISTICS_FULL_RECALCULATION_TRIGGER_BUTTON.equals(event.getCommand())) {
			StatisticUpdateManager statisticUpdateManager = getStatisticUpdateManager();
			if (statisticUpdateManager==null) {
				log_.info("event: UpdateStatisticsJob configured, but no StatisticManager available");
			} else {
				
				String title = getTranslator().translate("statistics.fullrecalculation.really.title");
				String text = getTranslator().translate("statistics.fullrecalculation.really.text");
				dialogCtr_ = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), title, text);
				listenTo(dialogCtr_);
				dialogCtr_.activate();
				
			}
		} else if (STATISTICS_UPDATE_TRIGGER_BUTTON.equals(event.getCommand())) {
			StatisticUpdateManager statisticUpdateManager = getStatisticUpdateManager();
			if (statisticUpdateManager==null) {
				log_.info("event: UpdateStatisticsJob configured, but no StatisticManager available");
			} else {
				statisticUpdateManager.updateStatistics(false, getUpdateFinishedCallback());
				refreshUIState();
				getInitialComponent().setDirty(true);
			}
		}
	}
	
	private Runnable getUpdateFinishedCallback() {
		return new Runnable() {
			@Override
			public void run() {
				Component updatecontrol = content.getComponent("updatecontrol");
				if (updatecontrol!=null) {
					content.remove(updatecontrol);
				}
				refreshUIState();
				showInfo("statistics.generation.feedback");
				getInitialComponent().setDirty(true);
			}
		};
	}

	/**
	 * Returns the StatisticUpdateManager bean (created via spring)
	 * @return the StatisticUpdateManager bean (created via spring)
	 */
	private StatisticUpdateManager getStatisticUpdateManager() {
		if (CoreSpringFactory.containsBean("org.olat.course.statistic.StatisticUpdateManager")) {
			return (StatisticUpdateManager) CoreSpringFactory.getBean("org.olat.course.statistic.StatisticUpdateManager");
		} else {
			return null;
		}
	}
}
