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
package org.olat.modules.forms.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarChartComponent;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.EvaluationFormStatistic;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.05.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormOverviewController extends BasicController {

	private VelocityContainer mainVC;

	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public EvaluationFormOverviewController(UserRequest ureq, WindowControl wControl,
			Form form, List<? extends EvaluationFormSessionRef> sessions) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("overview");

		EvaluationFormStatistic statistic = evaluationFormManager.getSessionsStatistic(sessions);
		mainVC.contextPut("numDoneSession", statistic.getNumOfDoneSessions());
		String submissionPeriod = EvaluationFormFormatter.period(statistic.getFirstSubmission(),
				statistic.getLastSubmission(), getLocale());
		mainVC.contextPut("submissionPeriod", submissionPeriod);
		mainVC.contextPut("averageDuration", EvaluationFormFormatter.duration(statistic.getAverageDuration()));
		
		if (hasRubrics(form)) {
			Controller reportCtrl = new RubricsTotalController(ureq, getWindowControl(), form, sessions);
			mainVC.put("report", reportCtrl.getInitialComponent());
		}

		mainVC.put("durationHistogram", initDurationHistogram(statistic.getDurations()));

		putInitialPanel(mainVC);
	}

	private boolean hasRubrics(Form form) {
		for (AbstractElement element: form.getElements()) {
			if (element instanceof Rubric) {
				return true;
			}
		}
		return false;
	}

	private BarChartComponent initDurationHistogram(long[] durations) {
		BarChartComponent chart = new BarChartComponent("o_eve_duration_chart");
		chart.setYLegend(translate("report.overview.duration.count"));
		chart.setXLegend(translate("report.overview.duration"));

		List<DurationCategory> durationCategories = new ArrayList<>();
		durationCategories.add(new DurationCategory("< 1 min", Long.MIN_VALUE, 60 * 1000));
		durationCategories.add(new DurationCategory("1 min - 5 min", 60 * 1000, 5 * 60 * 1000));
		durationCategories.add(new DurationCategory("5 min - 10 min", 5 * 60 * 1000, 10 * 60 * 1000));
		durationCategories.add(new DurationCategory("10 min - 1 h", 10 * 60 * 1000, 60 * 60 * 1000));
		durationCategories.add(new DurationCategory("1 h - 1 d", 60 * 60 * 1000, 24 * 60 * 60 * 1000));
		durationCategories.add(new DurationCategory("> 1 d", 24 * 60 * 60 * 1000, Long.MAX_VALUE));

		for (long duration : durations) {
			DurationCategory durationCategory = getContainingCategory(durationCategories, duration);
			if (durationCategory != null) {
				durationCategory.increaseCount();
			}
		}

		BarSeries series = new BarSeries("o_eva_bar");
		for (DurationCategory category : durationCategories) {
			series.add(category.getCount(), category.getName());
		}
		chart.addSeries(series);
		return chart;
	}

	private DurationCategory getContainingCategory(List<DurationCategory> durationCategories, long duration) {
		for (DurationCategory category : durationCategories) {
			if (duration > category.getLowerBound() && duration <= category.getUpperBound()) {
				return category;
			}
		}
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

	private static final class DurationCategory {

		private final String name;
		private final long lowerBound;
		private final long upperBound;
		private long count;

		DurationCategory(String name, long lowerBound, long upperBound) {
			super();
			this.name = name;
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}

		String getName() {
			return name;
		}

		long getLowerBound() {
			return lowerBound;
		}

		long getUpperBound() {
			return upperBound;
		}

		long getCount() {
			return count;
		}

		void increaseCount() {
			count++;
		}
	}
	
}
