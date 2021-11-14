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
package org.olat.course.nodes.livestream.ui;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamsController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(LiveStreamsController.class);

	private final VelocityContainer mainVC;
	
	private LiveStreamViewersController viewersCtrl;
	private LiveStreamListController listCtrl;
	
	private final ScheduledFuture<?> refreshFuture;

	@Autowired
	private LiveStreamService liveStreamService;

	public LiveStreamsController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration,
			CourseCalendars calendars) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("streams");
		
		viewersCtrl = new LiveStreamViewersController(ureq, wControl, moduleConfiguration, calendars);
		listenTo(viewersCtrl);
		mainVC.put("viewers", viewersCtrl.getInitialComponent());

		listCtrl = new LiveStreamListController(ureq, wControl, moduleConfiguration, calendars);
		listenTo(listCtrl);
		mainVC.put("list", listCtrl.getInitialComponent());
		
		ScheduledExecutorService scheduler = liveStreamService.getScheduler();
		refreshFuture = scheduler.scheduleAtFixedRate(new RefreshTask(ureq.getUserSession()), 10, 10, TimeUnit.SECONDS);
		
		putInitialPanel(mainVC);
	}

	public synchronized void refreshData(UserSession usess) {
		log.debug("Refresh live stream data of " + getIdentity());
		viewersCtrl.refresh(usess);
		listCtrl.refreshData();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		refreshFuture.cancel(true);
        super.doDispose();
	}

	private final class RefreshTask implements Runnable {

		private final UserSession usess;

		public RefreshTask(UserSession usess) {
			this.usess = usess;
			
		}

		@Override
		public void run() {
			refreshData(usess);
		}

	}

}
