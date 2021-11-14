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
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormStatistic;
import org.olat.modules.forms.Figure;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.handler.EvaluationFormReportHandler;
import org.olat.modules.forms.handler.EvaluationFormReportProvider;
import org.olat.modules.forms.handler.RubricSliderAvgBarChartHandler;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.ui.component.ResponsiveBarChartComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.05.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormOverviewController extends BasicController {
	
	private static final OverviewProvider PROVIDER = new OverviewProvider();

	private VelocityContainer mainVC;

	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public EvaluationFormOverviewController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage,
			SessionFilter filter, Figures figures) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("overview");

		List<Figure> allFigures = new ArrayList<>();
		if (figures != null) {
			allFigures.addAll(figures.getCustomFigures());
		}	
		
		EvaluationFormStatistic statistic = evaluationFormManager.getSessionsStatistic(filter);
		
		long numOfDoneSessions = statistic.getNumOfDoneSessions();
		if (figures != null && figures.getNumberOfParticipations() != null) {
			double percent = figures.getNumberOfParticipations() > 0
					? (double)numOfDoneSessions / figures.getNumberOfParticipations() * 100.0d
					: 0.0;
			long percentRounded = Math.round(percent);
			
			String[] args = new String[] {
					String.valueOf(numOfDoneSessions),
					String.valueOf(figures.getNumberOfParticipations()),
					String.valueOf(percentRounded)
			};
			String numberSessions = translate("report.overview.figures.number.done.session.of", args);
			allFigures.add(new Figure(translate("report.overview.figures.number.done.session.percent"),
					numberSessions));
		} else {
			String numberSessions = String.valueOf(numOfDoneSessions);
			allFigures.add(new Figure(translate("report.overview.figures.number.done.session"),
					numberSessions));
		}

		String submissionPeriod = EvaluationFormFormatter.period(statistic.getFirstSubmission(),
				statistic.getLastSubmission(), getLocale());
		allFigures.add(
				new Figure(translate("report.overview.figures.submission.period"), submissionPeriod));
		allFigures.add(new Figure(translate("report.overview.figures.average.duration"),
				EvaluationFormFormatter.duration(statistic.getAverageDuration())));
		mainVC.contextPut("figures", allFigures);

		if (hasRubrics(form)) {
			Controller reportCtrl = new EvaluationFormReportController(ureq, wControl, form, storage, filter, PROVIDER);
			listenTo(reportCtrl);
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

	private ResponsiveBarChartComponent initDurationHistogram(long[] durations) {
		ResponsiveBarChartComponent chart = new ResponsiveBarChartComponent("o_eve_duration_chart");
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
		long maxCount = 1;
		for (DurationCategory category : durationCategories) {
			series.add(category.getCount(), category.getName());
			if (category.getCount() > maxCount) {
				maxCount = category.getCount();
			}
		}
		chart.addSeries(series);
		chart.setYMax(Double.valueOf(maxCount));
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
	
	private static final class OverviewProvider implements EvaluationFormReportProvider {
		
		RubricSliderAvgBarChartHandler rubricHandler = new RubricSliderAvgBarChartHandler();

		@Override
		public EvaluationFormReportHandler getReportHandler(PageElement element) {
			if (Rubric.TYPE.equals(element.getType())) {
				return rubricHandler;
			}
			return null;
		}
		
	}
	
}
