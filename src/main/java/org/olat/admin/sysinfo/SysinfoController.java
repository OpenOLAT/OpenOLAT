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

package org.olat.admin.sysinfo;

import java.io.File;
import java.io.IOException;
import java.lang.management.MemoryType;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.admin.sysinfo.model.SessionsStats;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.springframework.beans.factory.annotation.Autowired;


/**
*  Description:<br>
*  all you wanted to know about your running OLAT system
*
* @author Felix Jost
*/
public class SysinfoController extends FormBasicController {
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private SessionStatsManager sessionStatsManager;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public SysinfoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "sysinfo");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Formatter format = Formatter.getInstance(getLocale());

		//runtime informations
		FormLayoutContainer runtimeCont = FormLayoutContainer.createDefaultFormLayout("runtime", getTranslator());
		formLayout.add(runtimeCont);
		formLayout.add("runtime", runtimeCont);

		String startup = format.formatDateAndTime(new Date(WebappHelper.getTimeOfServerStartup()));
		uifactory.addStaticTextElement("runtime.startup", "runtime.startup", startup, runtimeCont);

		String time = format.formatDateAndTime(new Date()) + " (" + Calendar.getInstance().getTimeZone().getDisplayName(false, TimeZone.SHORT, ureq.getLocale()) + ")";
		uifactory.addStaticTextElement("runtime.time", "runtime.time", time, runtimeCont);

		//memory
		uifactory.addMemoryView("memoryHeap", "runtime.memory", MemoryType.HEAP, runtimeCont);
		uifactory.addMemoryView("memoryNonHeap", "runtime.memory.permGen", MemoryType.NON_HEAP, runtimeCont);
		
		//controllers
		int controllerCnt = DefaultController.getControllerCount();
		uifactory.addStaticTextElement("controllercount", "runtime.controllercount", Integer.toString(controllerCnt), runtimeCont);
		int numOfDispatchingThreads = sessionStatsManager.getConcurrentCounter();
		int numOfDispatchingStreams =  sessionStatsManager.getConcurrentStreamCounter();
		String threadsInfos;
		if(numOfDispatchingStreams == 0) {
			threadsInfos = Integer.toString(numOfDispatchingThreads);
		} else {
			threadsInfos = translate("runtime.dispatchingthreads.infos", new String[] {
					Integer.toString(numOfDispatchingThreads - numOfDispatchingStreams), Integer.toString(numOfDispatchingStreams)
			});
		}
		uifactory.addStaticTextElement("dispatchingthreads", "runtime.dispatchingthreads", threadsInfos, runtimeCont);
		
		//sessions and clicks
		String sessionAndClicksPage = velocity_root + "/session_clicks.html";
		FormLayoutContainer sessionAndClicksCont = FormLayoutContainer.createCustomFormLayout("session_clicks", getTranslator(), sessionAndClicksPage);
		runtimeCont.add(sessionAndClicksCont);
		sessionAndClicksCont.setLabel("sess.and.clicks", null);
		
		Calendar lastLoginMonthlyLimit = Calendar.getInstance();
		//users monthly
		lastLoginMonthlyLimit.add(Calendar.MONTH, -1);
		Long userLastMonth = securityManager.countUniqueUserLoginsSince(lastLoginMonthlyLimit.getTime());
		lastLoginMonthlyLimit.add(Calendar.MONTH, -5); // -1 -5 = -6 for half a year
		Long userLastSixMonths = securityManager.countUniqueUserLoginsSince(lastLoginMonthlyLimit.getTime());
		lastLoginMonthlyLimit.add(Calendar.MONTH, -11); // -1 -11 = -12 for one year
		Long userLastYear = securityManager.countUniqueUserLoginsSince(lastLoginMonthlyLimit.getTime());
		sessionAndClicksCont.contextPut("users1month", userLastMonth.toString());
		sessionAndClicksCont.contextPut("users6month", userLastSixMonths.toString());
		sessionAndClicksCont.contextPut("usersyear", userLastYear.toString());
		
		//users daily
		Calendar lastLoginDailyLimit = Calendar.getInstance();
		lastLoginDailyLimit.add(Calendar.DAY_OF_YEAR, -1);
		Long userLastDay = securityManager.countUniqueUserLoginsSince(lastLoginDailyLimit.getTime());
		lastLoginDailyLimit.add(Calendar.DAY_OF_YEAR, -6); // -1 - 6 = -7 for last week
		Long userLast6Days = securityManager.countUniqueUserLoginsSince(lastLoginDailyLimit.getTime());
		sessionAndClicksCont.contextPut("userslastday", userLastDay.toString());
		sessionAndClicksCont.contextPut("userslastweek", userLast6Days.toString());
		
		//last 5 minutes
		long activeSessions = sessionStatsManager.getActiveSessions(300);
		sessionAndClicksCont.contextPut("count5Minutes", String.valueOf(activeSessions));
		SessionsStats stats = sessionStatsManager.getSessionsStatsLast(300);
		sessionAndClicksCont.contextPut("click5Minutes", String.valueOf(stats.getAuthenticatedClickCalls()));
		sessionAndClicksCont.contextPut("poll5Minutes", String.valueOf(stats.getAuthenticatedPollerCalls()));
		sessionAndClicksCont.contextPut("request5Minutes", String.valueOf(stats.getRequests()));
		sessionAndClicksCont.contextPut("minutes", String.valueOf(5));
		
		//last minute
		activeSessions = sessionStatsManager.getActiveSessions(60);
		sessionAndClicksCont.contextPut("count1Minute", String.valueOf(activeSessions));
		stats = sessionStatsManager.getSessionsStatsLast(60);
		sessionAndClicksCont.contextPut("click1Minute", String.valueOf(stats.getAuthenticatedClickCalls()));
		sessionAndClicksCont.contextPut("poll1Minute", String.valueOf(stats.getAuthenticatedPollerCalls()));
		sessionAndClicksCont.contextPut("request1Minute", String.valueOf(stats.getRequests()));
		sessionAndClicksCont.contextPut("oneMinute", "1");

		//server informations
		FormLayoutContainer serverCont = FormLayoutContainer.createDefaultFormLayout("server", getTranslator());
		formLayout.add(serverCont);
		formLayout.add("server", serverCont);
		
		//version
		uifactory.addStaticTextElement("version", "sysinfo.version", Settings.getFullVersionInfo(), serverCont);
		uifactory.addStaticTextElement("version.git", "sysinfo.version.git", WebappHelper.getChangeSet(), serverCont);
		String buildDate = format.formatDateAndTime(Settings.getBuildDate());
		uifactory.addStaticTextElement("version.date", "sysinfo.version.date", buildDate, serverCont);

		//cluster
		boolean clusterMode = "Cluster".equals(Settings.getClusterMode());
		MultipleSelectionElement clusterEl
			= uifactory.addCheckboxesHorizontal("cluster", "sysinfo.cluster", serverCont, new String[]{"xx"}, new String[]{""});
		clusterEl.setEnabled(false);
		clusterEl.select("xx", clusterMode);
		
		String nodeId = StringHelper.containsNonWhitespace(Settings.getNodeInfo()) ? Settings.getNodeInfo() : "N1";
		uifactory.addStaticTextElement("node", "sysinfo.node", nodeId, serverCont);

		File baseDir = new File(WebappHelper.getContextRoot());
		String baseDirPath = null;
		try {
			baseDirPath = baseDir.getCanonicalPath();
		} catch (IOException e1) {
			baseDirPath = baseDir.getAbsolutePath();
		}
		uifactory.addStaticTextElement("sysinfo.basedir", "sysinfo.basedir", baseDirPath, serverCont);
		uifactory.addStaticTextElement("sysinfo.olatdata", "sysinfo.olatdata", WebappHelper.getUserDataRoot(), serverCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}