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
package org.olat.course.statistic.weekly;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.statistic.DateChooserForm;
import org.olat.course.statistic.IStatisticManager;
import org.olat.course.statistic.StatisticDisplayController;
import org.olat.course.statistic.StatisticResult;

public class WeeklyStatisticDisplayController extends StatisticDisplayController {

	private VelocityContainer weeklyStatisticFormVc_;
	private VelocityContainer weeklyStatisticVc_;
	private DateChooserForm form_;

	public WeeklyStatisticDisplayController(UserRequest ureq, WindowControl windowControl, ICourse course, IStatisticManager statisticManager) {
		super(ureq, windowControl, course, statisticManager);
	}
	
	@Override
	protected Component createInitialComponent(UserRequest ureq) {
		setVelocityRoot(Util.getPackageVelocityRoot(getClass()));

		weeklyStatisticVc_ = createVelocityContainer("weeklystatisticparent");
		
		weeklyStatisticFormVc_ = createVelocityContainer("weeklystatisticform");
		form_ = new DateChooserForm(ureq, getWindowControl(), 8*7);
		listenTo(form_);
		weeklyStatisticFormVc_.put("statisticForm", form_.getInitialComponent());
		weeklyStatisticFormVc_.contextPut("statsSince", getStatsSinceStr());

		weeklyStatisticVc_.put("weeklystatisticform", weeklyStatisticFormVc_);

		Component parentInitialComponent = super.createInitialComponent(ureq);
		weeklyStatisticVc_.put("statistic", parentInitialComponent);
		
		return weeklyStatisticVc_;
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == form_ && event == Event.DONE_EVENT) {
			// need to regenerate the statisticResult
			// and now recreate the table controller
			recreateTableController(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected StatisticResult recalculateStatisticResult(UserRequest ureq) {
		// recalculate the statistic result based on the from and to dates.
		// do this by going via sql (see WeeklyStatisticManager)
		IStatisticManager weeklyStatisticManager = getStatisticManager();			
		StatisticResult statisticResult = 
			weeklyStatisticManager.generateStatisticResult(ureq, getCourse(), getCourseRepositoryEntryKey(), form_.getFromDate(), form_.getToDate());
		return statisticResult;
	}
}
