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

import java.util.Comparator;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.FiguresBuilder;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.handler.DefaultReportProvider;
import org.olat.modules.forms.handler.MultipleChoiceTableHandler;
import org.olat.modules.forms.handler.RubricTableHandler;
import org.olat.modules.forms.handler.SingleChoiceTableHandler;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.EvaluationFormOverviewController;
import org.olat.modules.forms.ui.EvaluationFormPrintSelectionController;
import org.olat.modules.forms.ui.EvaluationFormReportController;
import org.olat.modules.forms.ui.EvaluationFormSessionSelectionController;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.NameShuffleAnonymousComparator;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.SessionInformationLegendNameGenerator;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSegment;
import org.olat.modules.quality.analysis.AnlaysisFigures;
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
	
	private static final String SEGMENTS_CMP = "presentation";

	private final VelocityContainer mainVC;
	private final ButtonGroupComponent segmentButtonsCmp;
	private final Link overviewReportLink;
	private final Link tableReportLink;
	private final Link diagramReportLink;
	private final Link sessionSelectionLink;
	private final Link heatMapLink;
	
	private Link editPresentationLink;
	private Link deletePresentationLink;
	private Link printLink;
	private Link exportLink;
	private Link showFilterLink;
	private Link hideFilterLink;

	private Controller overviewCtrl;
	private Controller tableReportCtrl;
	private Controller diagramReportCtrl;
	private EvaluationFormSessionSelectionController sessionSelectionCtrl;
	private FilterableController heatMapCtrl;
	private Controller filterCtrl;
	private BreadcrumbedStackedPanel stackedDetailsPanel;
	private CloseableModalController cmc;
	private PresentationController presentationCtrl;
	private PresentationDeleteConfirmationController presentationDeleteCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private EvaluationFormPrintSelectionController printSelectionCtrl;
	private final MainSecurityCallback secCallback;
	private final TooledStackedPanel stackPanel;
	
	private final Form form;
	private final AvailableAttributes availableAttributes;
	private SessionFilter reportSessionFilter;
	private ReportHelper reportHelper;
	private Figures reportFigures;
	private AnalysisPresentation presentation;
	
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
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		overviewReportLink = LinkFactory.createLink("segments.table.overview", getTranslator(), this);
		segmentButtonsCmp.addButton(overviewReportLink, true);
		tableReportLink = LinkFactory.createLink("segments.table.report", getTranslator(), this);
		segmentButtonsCmp.addButton(tableReportLink, false);
		diagramReportLink = LinkFactory.createLink("segments.diagram.report", getTranslator(), this);
		segmentButtonsCmp.addButton(diagramReportLink, false);
		sessionSelectionLink = LinkFactory.createLink("segments.session.selection", getTranslator(), this);
		segmentButtonsCmp.addButton(sessionSelectionLink, false);
		heatMapLink = LinkFactory.createLink("segments.heatmap.link", getTranslator(), this);
		segmentButtonsCmp.addButton(heatMapLink, false);

		mainVC = createVelocityContainer("analysis");
		putInitialPanel(mainVC);
		
		availableAttributes = analysisService.getAvailableAttributes(presentation.getSearchParams());
		filterCtrl= new FilterController(ureq, wControl, form, presentation.getSearchParams(), availableAttributes);
		listenTo(filterCtrl);
		mainVC.put("filter", filterCtrl.getInitialComponent());
		
		doOpenSegment(ureq, presentation.getAnalysisSegment());
	}

	@Override
	public void initTools() {
		initPresentationTools();
		initOutputTools();
		initFilterTools();
		stackPanel.addTool(segmentButtonsCmp, true);
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
	
	private void initOutputTools() {
		printLink = LinkFactory.createToolLink("analysis.print", translate("analysis.print"), this);
		printLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_print");
		stackPanel.addTool(printLink, Align.right);
		exportLink = LinkFactory.createToolLink("analysis.export", translate("analysis.export"), this);
		exportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_export");
		stackPanel.addTool(exportLink, Align.right);
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
		} else if (source == printLink) {
			doOpenPrintSelection(ureq);
		} else if (source == exportLink) {
			doExport(ureq);
		} else if (source == showFilterLink) {
			doShowFilter();
		} else if (source == hideFilterLink) {
			doHideFilter();
		} else if (source == overviewReportLink) {
			doOpenSegment(ureq, AnalysisSegment.OVERVIEW);
		} else if (source == tableReportLink) {
			doOpenSegment(ureq, AnalysisSegment.TABLES);
		} else if (source == diagramReportLink) {
			doOpenSegment(ureq, AnalysisSegment.DIAGRAMS);
		} else if (source == sessionSelectionLink) {
			doOpenSegment(ureq, AnalysisSegment.SESSIONS);
		} else if (source == heatMapLink) {
			doOpenSegment(ureq, AnalysisSegment.HEAT_MAP);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == filterCtrl && event instanceof AnalysisFilterEvent) {
			AnalysisFilterEvent filterEvent = (AnalysisFilterEvent) event;
			AnalysisSearchParameter searchParams = filterEvent.getSearchParams();
			doFilter(ureq, searchParams);
		} else if (source == heatMapCtrl) {
			if (event instanceof AnalysisGroupingEvent) {
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
		} else if (source == printSelectionCtrl) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(presentationDeleteCtrl);
		removeAsListenerAndDispose(presentationCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		presentationDeleteCtrl = null;
		presentationCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		cleanUpReports();
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}
	
	private void cleanUpReports() {
		removeAsListenerAndDispose(overviewCtrl);
		removeAsListenerAndDispose(tableReportCtrl);
		removeAsListenerAndDispose(diagramReportCtrl);
		removeAsListenerAndDispose(sessionSelectionCtrl);
		overviewCtrl = null;
		tableReportCtrl = null;
		diagramReportCtrl = null;
		sessionSelectionCtrl = null;
	}
	
	private void doFilter(UserRequest ureq, AnalysisSearchParameter searchParams) {
		presentation.setSearchParams(searchParams);
		
		// Report controller can not be filtered. So delete them and recreate it the next shown time.
		cleanUpReports();
		reportSessionFilter = null;
		reportHelper = null;
		reportFigures = null;
		
		if (heatMapCtrl != null) {
			heatMapCtrl.onFilter(ureq, searchParams);
		}
		
		doOpenSegment(ureq, presentation.getAnalysisSegment());
	}
	
	private void doOpenSegment(UserRequest ureq, AnalysisSegment segment) {
		presentation.setAnalysisSegment(segment);
		switch (segment) {
		case OVERVIEW:
			doOpenOverviewReport(ureq);
			break;
		case TABLES:
			doOpenTableReport(ureq);
			break;
		case DIAGRAMS:
			doOpenDiagramReport(ureq);
			break;
		case SESSIONS:
			doOpenSessionSelection(ureq);
			break;
		case HEAT_MAP:
			doOpenHeatMap(ureq);
			break;
		default:
			doOpenOverviewReport(ureq);
			break;
		}
	}
	
	private void doOpenOverviewReport(UserRequest ureq) {
		removeAsListenerAndDispose(overviewCtrl);
		
		overviewCtrl = new EvaluationFormOverviewController(ureq, getWindowControl(), form, getReportSessionFilter(), getReportFigures());
		mainVC.put(SEGMENTS_CMP, overviewCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(overviewReportLink);
	}

	private void doOpenTableReport(UserRequest ureq) {
		removeAsListenerAndDispose(tableReportCtrl);
		
		DefaultReportProvider provider = new DefaultReportProvider();
		provider.put(Rubric.TYPE, new RubricTableHandler());
		provider.put(SingleChoice.TYPE, new SingleChoiceTableHandler());
		provider.put(MultipleChoice.TYPE, new MultipleChoiceTableHandler());
		tableReportCtrl = new EvaluationFormReportController(ureq, getWindowControl(), form, getReportSessionFilter(), provider, getReportHelper(), null);
		listenTo(tableReportCtrl);
		mainVC.put(SEGMENTS_CMP, tableReportCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(tableReportLink);
	}
	
	private void doOpenDiagramReport(UserRequest ureq) {
		removeAsListenerAndDispose(diagramReportCtrl);
		
		DefaultReportProvider provider = new DefaultReportProvider();
		diagramReportCtrl = new EvaluationFormReportController(ureq, getWindowControl(), form, getReportSessionFilter(), provider, getReportHelper(), null);
		listenTo(diagramReportCtrl);
		mainVC.put(SEGMENTS_CMP, diagramReportCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(diagramReportLink);
	}

	private void doOpenSessionSelection(UserRequest ureq) {
		removeAsListenerAndDispose(sessionSelectionCtrl);
		
		sessionSelectionCtrl = new EvaluationFormSessionSelectionController(ureq, getWindowControl(), form, getReportSessionFilter(), getReportHelper(), null);
		BreadcrumbedStackedPanel stackedSessionPanel = new BreadcrumbedStackedPanel("forms", getTranslator(), sessionSelectionCtrl);
		stackedSessionPanel.pushController(translate("analysis.session.forms"), sessionSelectionCtrl);
		sessionSelectionCtrl.setBreadcrumbPanel(stackedSessionPanel);
		mainVC.put(SEGMENTS_CMP, stackedSessionPanel);
		segmentButtonsCmp.setSelectedButton(sessionSelectionLink);
	}
	
	private void doOpenHeatMap(UserRequest ureq) {
		if (heatMapCtrl == null) {
			heatMapCtrl = new HeatMapController(ureq, getWindowControl(), form, availableAttributes, presentation.getHeatMapGrouping(),
					presentation.getHeatMapInsufficientOnly());
			listenTo(heatMapCtrl);
			heatMapCtrl.onFilter(ureq, presentation.getSearchParams());
			stackedDetailsPanel = new BreadcrumbedStackedPanel("forms", getTranslator(), heatMapCtrl);
			stackedDetailsPanel.pushController(translate("analysis.details"), heatMapCtrl);
			heatMapCtrl.setBreadcrumbPanel(stackedDetailsPanel);
		}
		mainVC.put(SEGMENTS_CMP, stackedDetailsPanel);
		segmentButtonsCmp.setSelectedButton(heatMapLink);
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
		stackPanel.changeDisplayname(presentation.getName());
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
		stackPanel.changeDisplayname(presentation.getFormEntry().getDisplayname());
	}
	
	private void doOpenPrintSelection(UserRequest ureq) {
		if (printSelectionCtrl == null) {
			printSelectionCtrl = new EvaluationFormPrintSelectionController(ureq, getWindowControl(), form, getReportSessionFilter(),
					getReportFigures(), getReportHelper());
			listenTo(printSelectionCtrl);
		}

		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				printSelectionCtrl.getInitialComponent(), printLink, "", true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private void doExport(UserRequest ureq) {
		String surveyName = "survey";
		EvaluationFormExcelExport export = new EvaluationFormExcelExport(form, getReportSessionFilter(), getReportHelper(), surveyName);
		ureq.getDispatchResult().setResultingMediaResource(export.createMediaResource());
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

	private SessionFilter getReportSessionFilter() {
		if (reportSessionFilter == null) {
			reportSessionFilter = analysisService.createSessionFilter(presentation.getSearchParams());
		}
		return reportSessionFilter;
	}

	private ReportHelper getReportHelper() {
		if (reportHelper == null) {
			Comparator<EvaluationFormSession> comparator = new NameShuffleAnonymousComparator();
			LegendNameGenerator legendNameGenerator = new SessionInformationLegendNameGenerator(getReportSessionFilter());
			reportHelper = ReportHelper.builder(getLocale())
					.withLegendNameGenrator(legendNameGenerator)
					.withSessionComparator(comparator)
					.withColors()
					.build();
		}
		return reportHelper;
	}

	private Figures getReportFigures() {
		if (reportFigures == null) {
			FiguresBuilder figuresBuilder = FiguresBuilder.builder();
			figuresBuilder.addCustomFigure(translate("report.figure.form.name"), presentation.getFormEntry().getDisplayname());
			AnlaysisFigures analyticFigures = analysisService.loadFigures(presentation.getSearchParams());
			figuresBuilder.withNumberOfParticipations(analyticFigures.getParticipationCount());
			figuresBuilder.addCustomFigure(translate("report.figure.number.data.collections"),
					analyticFigures.getDataCollectionCount().toString());
			reportFigures = figuresBuilder.build();
		}
		return reportFigures;
	}
	
}
