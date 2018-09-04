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
package org.olat.modules.quality.analysis.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.analysis.EvaluationFormView;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisSegmentsController extends BasicController implements TooledController, Activateable2 {

	private final TooledStackedPanel stackPanel;
	private final StackedPanel mainPanel;
	private final ButtonGroupComponent segmentButtonsCmp;
	private Link reportLink;
	private Link heatMapLink;

	private AnalysisController analysisCtrl;
	
	private final QualitySecurityCallback secCallback;
	private final EvaluationFormView formView;
	
	//TODO uh Wo wird das StackPanel und das SecCallback benätigt

	public AnalysisSegmentsController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, EvaluationFormView formView) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.formView = formView;
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		reportLink = LinkFactory.createLink("segments.report.link", getTranslator(), this);
		segmentButtonsCmp.addButton(reportLink, false);
		heatMapLink = LinkFactory.createLink("segments.heatmap.link", getTranslator(), this);
		segmentButtonsCmp.addButton(heatMapLink, false);
		
		mainPanel = putInitialPanel(new SimpleStackedPanel("analysisSegments"));
		mainPanel.setContent(new Panel("empty"));
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		doOpenReport(ureq);
	}

	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, true);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (reportLink == source) {
			doOpenReport(ureq);
		} else if(heatMapLink == source) {
			doOpenHeatMap(ureq);
		} else if (stackPanel == source && stackPanel.getLastController() == this && event instanceof PopEvent) {
			PopEvent popEvent = (PopEvent) event;
			if (popEvent.isClose()) {
				stackPanel.popController(this);
			} else {
				doOpenReport(ureq);
			}
		}
	}
	
	private void doOpenAnalysis(UserRequest ureq) {
		if (analysisCtrl == null) {
			analysisCtrl = new AnalysisController(ureq, getWindowControl(), secCallback, stackPanel, formView);
			listenTo(analysisCtrl);
			stackPanel.pushController("segments.report.breadcrumb", analysisCtrl);
		}
	}

	private void doOpenReport(UserRequest ureq) {
		doOpenAnalysis(ureq);
		analysisCtrl.setPresentation(ureq, AnalysisController.Presentation.REPORT);
		stackPanel.changeDisplayname(translate("segments.report.breadcrumb"));
		segmentButtonsCmp.setSelectedButton(reportLink);
	}

	private void doOpenHeatMap(UserRequest ureq) {
		doOpenAnalysis(ureq);
		analysisCtrl.setPresentation(ureq, AnalysisController.Presentation.HEAT_MAP);
		stackPanel.changeDisplayname(translate("segments.heatmap.breadcrumb"));
		segmentButtonsCmp.setSelectedButton(heatMapLink);
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

}
