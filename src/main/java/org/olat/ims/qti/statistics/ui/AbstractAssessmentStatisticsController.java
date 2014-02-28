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
package org.olat.ims.qti.statistics.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.HistogramComponent;
import org.olat.core.gui.components.chart.Scale;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.model.StatisticAssessment;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbstractAssessmentStatisticsController extends BasicController  {
	
	protected final VelocityContainer mainVC;
	protected final QTIStatisticResourceResult resourceResult;
	
	protected final QTIStatisticsManager qtiStatisticsManager;
	
	public AbstractAssessmentStatisticsController(UserRequest ureq, WindowControl wControl,
			QTIStatisticResourceResult resourceResult, boolean printMode, String page) {
		super(ureq, wControl);
		
		this.resourceResult = resourceResult;
		qtiStatisticsManager = CoreSpringFactory.getImpl(QTIStatisticsManager.class);
		
		mainVC = createVelocityContainer(page);
		mainVC.contextPut("printMode", new Boolean(printMode));
		
		initDurationHistogram(resourceResult.getQTIStatisticAssessment());
		
		putInitialPanel(mainVC);
	}
	
	private void initDurationHistogram(StatisticAssessment stats) {
		HistogramComponent scoreHistogram = new HistogramComponent("scoreHistogram");
		scoreHistogram.setLongValues(stats.getDurations());
		scoreHistogram.setYLegend(translate("chart.percent.participants"));
		scoreHistogram.setXScale(Scale.hour);
		mainVC.put("durationHistogram", scoreHistogram);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if("print".equals(event.getCommand())){
			printPages(ureq);
		}
	}

	private void printPages(UserRequest ureq) {
		ControllerCreator printControllerCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				Controller printCtr = new QTI12PrintController(lureq, lwControl, resourceResult);
				Component view = printCtr.getInitialComponent();
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, view, null);
				return layoutCtr;
			}					
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr);
	}
	
	public static class ItemInfos {
		
		private final String label;
		private final String text;
		
		public ItemInfos(String label, String text) {
			this.label = label;
			this.text = text;
		}

		public String getLabel() {
			return label;
		}

		public String getText() {
			return text;
		}
	}
}
