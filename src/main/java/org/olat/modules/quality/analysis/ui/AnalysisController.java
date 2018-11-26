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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.ReportSegment;
import org.olat.modules.forms.ui.ReportSegmentEvent;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSegment;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.ui.PresentationEvent.Action;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisController extends BasicController implements TooledController {
	
	private Link editPresentationLink;
	private Link deletePresentationLink;
	private Link showFilterLink;
	private Link hideFilterLink;

	private VelocityContainer mainVC;
	private Controller filterCtrl;
	private FilterableController filterableCtrl;
	private BreadcrumbedStackedPanel stackedDetailsPanel;
	private CloseableModalController cmc;
	private PresentationController presentationCtrl;
	private PresentationDeleteConfirmationController presentationDeleteCtrl;
	private final MainSecurityCallback secCallback;
	private final TooledStackedPanel stackPanel;
	
	private final Form form;
	private final AvailableAttributes availableAttributes;
	private AnalysisPresentation presentation;
	private ReportSegment currentReportSegment;
	
	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	protected AnalysisController(UserRequest ureq, WindowControl wControl, MainSecurityCallback secCallback,
			TooledStackedPanel stackPanel, AnalysisPresentation presentation) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.presentation = presentation;
		this.form = evaluationFormManager.loadForm(presentation.getFormEntry());
		mainVC = createVelocityContainer("analysis");
		putInitialPanel(mainVC);
		
		availableAttributes = analysisService.getAvailableAttributes(presentation.getSearchParams());
		filterCtrl= new FilterController(ureq, wControl, form, presentation.getSearchParams(), availableAttributes);
		listenTo(filterCtrl);
		mainVC.put("filter", filterCtrl.getInitialComponent());
	}

	@Override
	public void initTools() {
		initPresentationTools();
		initFilterTools();
	}

	private void initPresentationTools() {
		stackPanel.removeTool(editPresentationLink);
		if (secCallback.canEditPresentations()) {
			editPresentationLink = LinkFactory.createToolLink("presentation.edit", translate("presentation.edit"), this);
			editPresentationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_pres_edit");
			stackPanel.addTool(editPresentationLink, Align.left);
		}
		stackPanel.removeTool(deletePresentationLink);
		if (secCallback.canDeletePresentation(presentation)) {
			deletePresentationLink = LinkFactory.createToolLink("presentation.delete", translate("presentation.delete"), this);
			deletePresentationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_pres_delete");
			stackPanel.addTool(deletePresentationLink, Align.left);
		}
	}
	
	private void initFilterTools() {
		showFilterLink = LinkFactory.createToolLink("filter.show", translate("filter.show"), this);
		showFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_show_filter");
		hideFilterLink = LinkFactory.createToolLink("filter.hide", translate("filter.hide"), this);
		hideFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_hide_filter");
		doHideFilter();
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editPresentationLink) {
			doEditPresentation(ureq);
		} else if (source == deletePresentationLink) {
			doConfirmDeletePresentation(ureq);
		} else if (source == showFilterLink) {
			doShowFilter();
		} else if (source == hideFilterLink) {
			doHideFilter();
		} else if (stackPanel == source && stackPanel.getLastController() == this && event instanceof PopEvent) {
			PopEvent popEvent = (PopEvent) event;
			if (popEvent.isClose()) {
				stackPanel.popController(this);
			} else {
				setSegment(ureq, AnalysisSegment.OVERVIEW);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == filterCtrl && event instanceof AnalysisFilterEvent) {
			AnalysisFilterEvent filterEvent = (AnalysisFilterEvent) event;
			AnalysisSearchParameter searchParams = filterEvent.getSearchParams();
			presentation.setSearchParams(searchParams);
			filterableCtrl.onFilter(ureq, searchParams);
		} else if (source == filterableCtrl) {
			if (event instanceof ReportSegmentEvent) {
				// Save current segment between analysis segment changes
				ReportSegmentEvent rsEvent = (ReportSegmentEvent) event;
				currentReportSegment = rsEvent.getSegment();
			} else if (event instanceof AnalysisGroupingEvent) {
				AnalysisGroupingEvent groupingEvent = (AnalysisGroupingEvent) event;
				MultiGroupBy multiGroupBy = groupingEvent.getMultiGroupBy();
				presentation.setHeatMapGrouping(multiGroupBy);
			} else if (event instanceof AnalysisInsufficientOnlyEvent) {
				AnalysisInsufficientOnlyEvent iEvent = (AnalysisInsufficientOnlyEvent) event;
				Boolean insufficientOnly = iEvent.getInsufficientOnly();
				presentation.setHeatMapInsufficientOnly(insufficientOnly);
			}
		} else if (source == presentationCtrl) {
			if (event instanceof PresentationEvent) {
				PresentationEvent apEvent = (PresentationEvent) event;
				AnalysisPresentation editetPresentation = apEvent.getPresentation();
				Action action = apEvent.getAction();
				if (Action.SAVE.equals(action)) {
					doSavePresentation(editetPresentation);
				} else if (Action.CLONE.equals(action)) {
					doClonePresentation(editetPresentation);
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == presentationDeleteCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				doDeletePresentation();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(presentationDeleteCtrl);
		removeAsListenerAndDispose(presentationCtrl);
		removeAsListenerAndDispose(cmc);
		presentationDeleteCtrl = null;
		presentationCtrl = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

	public void setSegment(UserRequest ureq, AnalysisSegment segment) {
		removeAsListenerAndDispose(filterableCtrl);
		filterableCtrl = null;
		
		presentation.setAnalysisSegment(segment);
		switch (segment) {
		case OVERVIEW:
			filterableCtrl = new AnalysisReportController(ureq, getWindowControl(), form,
					presentation.getFormEntry().getDisplayname(), currentReportSegment);
			break;
		case HEAT_MAP:
			filterableCtrl = new HeatMapController(ureq, getWindowControl(), form, availableAttributes, presentation.getHeatMapGrouping(),
					presentation.getHeatMapInsufficientOnly());
			break;
		default:
			filterableCtrl = new AnalysisReportController(ureq, getWindowControl(), form,
					presentation.getFormEntry().getDisplayname(), currentReportSegment);
			break;
		}
		listenTo(filterableCtrl);
		filterableCtrl.onFilter(ureq, presentation.getSearchParams());
		stackedDetailsPanel = new BreadcrumbedStackedPanel("forms", getTranslator(), filterableCtrl);
		stackedDetailsPanel.pushController(translate("analysis.details"), filterableCtrl);
		filterableCtrl.setBreadcrumbPanel(stackedDetailsPanel);
		mainVC.put("presentation", stackedDetailsPanel);
		mainVC.setDirty(true);
	}
	
	private void doEditPresentation(UserRequest ureq) {
		presentationCtrl = new PresentationController(ureq, getWindowControl(), presentation);
		listenTo(presentationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				presentationCtrl.getInitialComponent(), true, translate("presentation.edit.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doSavePresentation(AnalysisPresentation editetPresentation) {
		presentation = analysisService.savePresentation(editetPresentation);
		// refresh to avoid LazyInitializationException if access formEntry
		presentation = analysisService.loadPresentationByKey(presentation);
		initPresentationTools();
	}

	private void doClonePresentation(AnalysisPresentation editetPresentation) {
		presentation = analysisService.clonePresentation(editetPresentation);
		doSavePresentation(presentation);
	}
	
	private void doConfirmDeletePresentation(UserRequest ureq) {
		presentationDeleteCtrl = new PresentationDeleteConfirmationController(ureq, getWindowControl(), presentation);
		listenTo(presentationDeleteCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				presentationDeleteCtrl.getInitialComponent(), true, translate("presentation.delete.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDeletePresentation() {
		AnalysisPresentation clone = analysisService.clonePresentation(presentation);
		clone.setName(null);
		analysisService.deletePresentation(presentation);
		presentation = clone;
		initPresentationTools();
	}

	private void doShowFilter() {
		stackPanel.addTool(hideFilterLink, Align.right);
		stackPanel.removeTool(showFilterLink);
		mainVC.contextPut("filterSwitch", Boolean.TRUE);
	}
	
	private void doHideFilter() {
		stackPanel.addTool(showFilterLink, Align.right);
		stackPanel.removeTool(hideFilterLink);
		mainVC.contextPut("filterSwitch", Boolean.FALSE);
	}
	
}
