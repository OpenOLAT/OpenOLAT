/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.admin.sysinfo;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.LogRealTimeViewerController;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 16.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ErrorLogLevelController extends BasicController {

	private static final String ACTION_SETLEVEL = "setlevel";
	private static final String ACTION_VIEWLOG	 = "viewlog";
	private static final String ACTION_VIEWLOG_PACKAGE = "p";

	private Link resetloglevelsButton;
	private final VelocityContainer myLoglevels;

	private CloseableModalController cmc;	
	private LogRealTimeViewerController logViewerCtr;
	
	public ErrorLogLevelController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		myLoglevels = createVelocityContainer("loglevels");
		resetloglevelsButton = LinkFactory.createButton("resetloglevels", myLoglevels, this);
		putInitialPanel(myLoglevels);
		loadModel();
	}
	
	private void loadModel() {
		List<Logger> loggers = Tracing.getLoggersSortedByName(); // put it in a list in case of a reload (enum can only be used once)
		myLoglevels.contextPut("loggers", loggers);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == myLoglevels) {
			if (event.getCommand().equals(ACTION_SETLEVEL)) {
				doSetLevel(ureq);
			} else if (event.getCommand().equals(ACTION_VIEWLOG)) {
				doViewLog(ureq);
			}
		} else if (source == resetloglevelsButton) {
			doResetLogLevels();
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(logViewerCtr);
		removeAsListenerAndDispose(cmc);
		logViewerCtr = null;
		cmc = null;
	}
	
	private void doViewLog(UserRequest ureq) {
		String toBeViewed = ureq.getParameter(ACTION_VIEWLOG_PACKAGE);
		if (toBeViewed == null) {
			return; // should not happen
		}
		removeAsListenerAndDispose(logViewerCtr);
		removeAsListenerAndDispose(cmc);

		logViewerCtr = new LogRealTimeViewerController(ureq, getWindowControl(), toBeViewed, Level.ALL, false);
		listenTo(logViewerCtr);
		cmc = new CloseableModalController(getWindowControl(), "close", logViewerCtr.getInitialComponent(), true, toBeViewed);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSetLevel(UserRequest ureq) {
		String level = ureq.getParameter("level");
		String logger = ureq.getParameter("logger");
		if (logger.equals(org.olat.core.logging.Tracing.class.getName())) {
			getWindowControl().setError("log level of "+org.olat.core.logging.Tracing.class.getName()+" must not be changed!");
			return;
		}
		Level l;
		switch(level) {
			case "debug": l = Level.DEBUG; break;
			case "info": l = Level.INFO; break;
			case "warn": l = Level.WARN; break;
			default: l = Level.ERROR; break;
		}
		Tracing.setLevelForLogger(l, logger);
		getWindowControl().setInfo("Set logger " + logger + " to level " + level);
		loadModel();
	}
	
	private void doResetLogLevels() {
		Tracing.resetLevelForAllLoggers();
		getWindowControl().setInfo("Reload all log levels from configuration");
		loadModel();
	}
}