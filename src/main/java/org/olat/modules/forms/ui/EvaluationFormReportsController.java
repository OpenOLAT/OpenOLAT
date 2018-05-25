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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.handler.DefaultReportProvider;
import org.olat.modules.forms.handler.MultipleChoiceTableHandler;
import org.olat.modules.forms.handler.RubricTableHandler;
import org.olat.modules.forms.handler.SingleChoiceTableHandler;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.SingleChoice;

/**
 * 
 * Initial date: 18.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormReportsController extends BasicController {
	
	private static final String SEGMENTS_CMP = "segmentCmp";
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private final Link overviewReportLink;
	private final Link tableReportLink;
	private final Link diagramReportLink;
	private final Link sessionSelectionLink;
	
	private final ReportHelper reportHelper;
	private EvaluationFormOverviewController overviewCtrl;
	private EvaluationFormReportController tableReportCtrl;
	private EvaluationFormReportController diagramReportCtrl;
	private BreadcrumbedStackedPanel stackedSessionPanel;
	private EvaluationFormSessionSelectionController sessionSelectionCtrl;
	
	private final Form form;
	private final List<? extends EvaluationFormSessionRef> sessions;

	public EvaluationFormReportsController(UserRequest ureq, WindowControl wControl, Form form,
			List<? extends EvaluationFormSessionRef> sessions) {
		super(ureq, wControl);
		this.form = form;
		this.sessions = sessions;
		
		LegendNameGenerator legendNameGenerator = new SessionInformationLegendNameGenerator(sessions);
		this.reportHelper = ReportHelper.builder(getLocale())
				.withLegendNameGenrator(legendNameGenerator)
				.withColors()
				.build();
		
		mainVC = createVelocityContainer("reports");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		overviewReportLink = LinkFactory.createLink("reports.table.overview", mainVC, this);
		segmentView.addSegment(overviewReportLink, true);
		tableReportLink = LinkFactory.createLink("reports.table.report", mainVC, this);
		segmentView.addSegment(tableReportLink, false);
		diagramReportLink = LinkFactory.createLink("reports.diagram.report", mainVC, this);
		segmentView.addSegment(diagramReportLink, false);
		sessionSelectionLink = LinkFactory.createLink("reports.session.selection", mainVC, this);
		segmentView.addSegment(sessionSelectionLink, false);
		
		doOpenOverviewReport(ureq);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView && event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent)event;
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == overviewReportLink) {
				doOpenOverviewReport(ureq);
			} else if (clickedLink == tableReportLink) {
				doOpenTableReport(ureq);
			} else if (clickedLink == diagramReportLink) {
				doOpenDiagramReport(ureq);
			} else if (clickedLink == sessionSelectionLink) {
				doOpenSessionSelection(ureq);
			}
		}
	}

	private void doOpenOverviewReport(UserRequest ureq) {
		if (overviewCtrl == null) {
			overviewCtrl = new EvaluationFormOverviewController(ureq, getWindowControl(), form, sessions);
		}
		mainVC.put(SEGMENTS_CMP, overviewCtrl.getInitialComponent());
	}

	private void doOpenTableReport(UserRequest ureq) {
		if (tableReportCtrl == null) {
			DefaultReportProvider provider = new DefaultReportProvider();
			provider.put(Rubric.TYPE, new RubricTableHandler());
			provider.put(SingleChoice.TYPE, new SingleChoiceTableHandler());
			provider.put(MultipleChoice.TYPE, new MultipleChoiceTableHandler());
			tableReportCtrl = new EvaluationFormReportController(ureq, getWindowControl(), form, sessions, provider, reportHelper);
			listenTo(tableReportCtrl);
		}
		mainVC.put(SEGMENTS_CMP, tableReportCtrl.getInitialComponent());
	}
	
	private void doOpenDiagramReport(UserRequest ureq) {
		if (diagramReportCtrl == null) {
			DefaultReportProvider provider = new DefaultReportProvider();
			diagramReportCtrl = new EvaluationFormReportController(ureq, getWindowControl(), form, sessions, provider, reportHelper);
			listenTo(diagramReportCtrl);
		}
		mainVC.put(SEGMENTS_CMP, diagramReportCtrl.getInitialComponent());
	}

	private void doOpenSessionSelection(UserRequest ureq) {
		if (sessionSelectionCtrl == null) {
			sessionSelectionCtrl = new EvaluationFormSessionSelectionController(ureq, getWindowControl(), form, sessions, reportHelper);
			stackedSessionPanel = new BreadcrumbedStackedPanel("forms", getTranslator(), sessionSelectionCtrl);
			stackedSessionPanel.pushController(translate("reports.session.forms"), sessionSelectionCtrl);
			sessionSelectionCtrl.setBreadcrumbPanel(stackedSessionPanel);
		}
		mainVC.put(SEGMENTS_CMP, stackedSessionPanel);	
	}

	@Override
	protected void doDispose() {
		//
	}

}
