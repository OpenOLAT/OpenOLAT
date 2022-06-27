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
import java.util.List;

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.ceditor.DataStorage;
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
import org.olat.modules.forms.ui.EvaluationFormPrintSelectionController.Target;
import org.olat.modules.forms.ui.EvaluationFormReportController;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.NameShuffleAnonymousComparator;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.ReportHelperUserColumns;
import org.olat.modules.forms.ui.SessionInformationLegendNameGenerator;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSegment;
import org.olat.modules.quality.analysis.AnlaysisFigures;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.ui.PresentationEvent.Action;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisController extends BasicController implements TooledController, Activateable2 {
	
	private final ButtonGroupComponent segmentButtonsCmp;
	private final Link overviewReportLink;
	private final Link tableReportLink;
	private final Link diagramReportLink;
	private final Link sessionSelectionLink;
	private final Link heatMapLink;
	private final Link trendDiagramLink;
	
	private Link editPresentationLink;
	private Link deletePresentationLink;
	private Link printLink;
	private Link printPopupLink;
	private Link pdfLink;
	private Link exportLink;
	private Link showFilterLink;
	private Link hideFilterLink;
	private ToolComponents toolComponents;
	
	private Analysis2ColController colsCtrl;
	private Controller overviewCtrl;
	private Controller tableReportCtrl;
	private Controller diagramReportCtrl;
	private AnalysisSessionSelectionController sessionSelectionCtrl;
	private HeatMapController heatMapCtrl;
	private TrendController trendDiagramCtrl;
	private FilterController filterCtrl;
	private CloseableModalController cmc;
	private PresentationController presentationCtrl;
	private PresentationDeleteConfirmationController presentationDeleteCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private EvaluationFormPrintSelectionController printSelectionCtrl;
	private final MainSecurityCallback secCallback;
	private final TooledStackedPanel stackPanel;
	
	private final Form form;
	private final DataStorage storage;
	private final AvailableAttributes availableAttributes;
	private SessionFilter reportSessionFilter;
	private ReportHelper reportHelper;
	private Figures reportFigures;
	private AnalysisPresentation presentation;
	
	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;

	protected AnalysisController(UserRequest ureq, WindowControl wControl, MainSecurityCallback secCallback,
			TooledStackedPanel stackPanel, AnalysisPresentation presentation) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.presentation = presentation;
		this.form = evaluationFormManager.loadForm(presentation.getFormEntry());
		this.storage = evaluationFormManager.loadStorage(presentation.getFormEntry());
		
		presentation.getSearchParams().setDataCollectionOrganisationRefs(secCallback.getViewAnalysisOrganisationRefs());
		
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
		trendDiagramLink = LinkFactory.createLink("segments.trend.link", getTranslator(), this);
		segmentButtonsCmp.addButton(trendDiagramLink, false);
		
		availableAttributes = analysisService.getAvailableAttributes(presentation.getSearchParams());
		filterCtrl= new FilterController(ureq, wControl, form, presentation.getSearchParams(), availableAttributes);
		listenTo(filterCtrl);
		
		StackedPanel mainPanel = putInitialPanel(new SimpleStackedPanel("analysisSegments"));
		mainPanel.setContent(new Panel("empty"));
	}

	@Override
	public void initTools() {
		initPresentationTools();
		initOutputTools();
		stackPanel.addTool(segmentButtonsCmp, true);
	}

	private void initPresentationTools() {
		editPresentationLink = LinkFactory.createToolLink("presentation.edit", translate("presentation.edit"), this);
		editPresentationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_pres_edit");
		stackPanel.addTool(editPresentationLink, Align.left, true);
		
		deletePresentationLink = LinkFactory.createToolLink("presentation.delete", translate("presentation.delete"), this);
		deletePresentationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_pres_delete");
		stackPanel.addTool(deletePresentationLink, Align.left, true);
		doShowHildePresentationLinks();
	}
	
	private void initOutputTools() {
		exportLink = LinkFactory.createToolLink("analysis.export", translate("analysis.export"), this);
		exportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_export");
		stackPanel.addTool(exportLink, Align.right, true);
		
		if (pdfModule.isEnabled()) {
			pdfLink = LinkFactory.createToolLink("analysis.pdf", translate("analysis.pdf"), this);
			pdfLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_pdf");
			stackPanel.addTool(pdfLink, Align.right, true);
		}
		
		printLink = LinkFactory.createToolLink("analysis.print", translate("analysis.print"), this);
		printLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_print");
		stackPanel.addTool(printLink, Align.right, true);
		
		printPopupLink = LinkFactory.createToolLink("analysis.print.popup", translate("analysis.print"), this);
		printPopupLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_print");
		printPopupLink.setPopup(new LinkPopupSettings(950, 750, "report-hm"));
		stackPanel.addTool(printPopupLink, Align.right, true);

		showFilterLink = LinkFactory.createToolLink("filter.show", translate("filter.show"), this);
		showFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_show_filter");
		stackPanel.addTool(showFilterLink, Align.right, true);
		
		hideFilterLink = LinkFactory.createToolLink("filter.hide", translate("filter.hide"), this);
		hideFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_hide_filter");
		stackPanel.addTool(hideFilterLink, Align.right, true);
		
		doHideFilter();
		
		toolComponents = new ToolComponents(stackPanel, printLink, printPopupLink, pdfLink, exportLink, showFilterLink,
				hideFilterLink);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editPresentationLink) {
			doEditPresentation(ureq);
		} else if (source == deletePresentationLink) {
			doConfirmDeletePresentation(ureq);
		} else if (source == printLink) {
			doOpenPrintSelection(ureq);
		} else if (source == printPopupLink) {
			doPopupPrint(ureq);
		} else if (source == pdfLink) {
			doExportPdf(ureq);
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
		} else if (source == trendDiagramLink) {
			doOpenSegment(ureq, AnalysisSegment.TREND);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == filterCtrl && event instanceof AnalysisFilterEvent) {
			AnalysisFilterEvent filterEvent = (AnalysisFilterEvent) event;
			AnalysisSearchParameter searchParams = filterEvent.getSearchParams();
			doFilter(ureq, searchParams);
		} else if (source == heatMapCtrl || source == trendDiagramCtrl) {
			if (event instanceof AnalysisGroupingEvent) {
				AnalysisGroupingEvent groupingEvent = (AnalysisGroupingEvent) event;
				MultiGroupBy multiGroupBy = groupingEvent.getMultiGroupBy();
				setHeatMapGrouping(multiGroupBy);
			} else if (event instanceof AnalysisInsufficientOnlyEvent) {
				AnalysisInsufficientOnlyEvent iEvent = (AnalysisInsufficientOnlyEvent) event;
				Boolean insufficientOnly = iEvent.getInsufficientOnly();
				setHeatMapInsufficientOnly(insufficientOnly);
			} else if (event instanceof TemporalGroupingEvent) {
				TemporalGroupingEvent tgEvent = (TemporalGroupingEvent) event;
				TemporalGroupBy temporalGroupBy = tgEvent.getTemporalGroupBy();
				presentation.setTemporalGroupBy(temporalGroupBy);
			} else if (event instanceof TrendDifferenceEvent) {
				TrendDifferenceEvent tdEvent = (TrendDifferenceEvent) event;
				TrendDifference trendDifference = tdEvent.getTrendDifference();
				presentation.setTrendDifference(trendDifference);
			} else if (event instanceof RubricIdEvent) {
				RubricIdEvent riEvent = (RubricIdEvent) event;
				String rubricId = riEvent.getRubricId();
				presentation.setRubricId(rubricId);
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
		removeAsListenerAndDispose(printSelectionCtrl);
		printSelectionCtrl = null;
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
        super.doDispose();
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
		
		doOpenSegment(ureq, presentation.getAnalysisSegment());
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		doOpenSegment(ureq, presentation.getAnalysisSegment());
	}
	
	private void doOpenSegment(UserRequest ureq, AnalysisSegment segment) {
		presentation.setAnalysisSegment(segment);
		
		Boolean showFilter = colsCtrl != null? colsCtrl.getShowFilter(): Boolean.FALSE;
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
		case TREND:
			doOpenTrendDiagram(ureq);
			break;
		default:
			doOpenOverviewReport(ureq);
			break;
		}
		filterCtrl.setReadOnly(false);
		colsCtrl.setShowFilter(showFilter);
	}
	
	private void setEvaluationFormToolComponents() {
		toolComponents.setPrintVisibility(true);
		toolComponents.setPrintPopupVisibility(false);
		toolComponents.setPdfVisibility(true);
		toolComponents.setExportVisibility(true);
		toolComponents.setFilterVisibility(true);
	}
	
	private void doOpenOverviewReport(UserRequest ureq) {
		removeAsListenerAndDispose(overviewCtrl);
		
		overviewCtrl = new EvaluationFormOverviewController(ureq, getWindowControl(), form, storage, getReportSessionFilter(), getReportFigures());
		listenTo(overviewCtrl);
		
		colsCtrl = new Analysis2ColController(ureq, getWindowControl(), overviewCtrl, filterCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("segments.table.overview"), colsCtrl);
		segmentButtonsCmp.setSelectedButton(overviewReportLink);
		setEvaluationFormToolComponents();
	}

	private void doOpenTableReport(UserRequest ureq) {
		removeAsListenerAndDispose(tableReportCtrl);
		
		DefaultReportProvider provider = new DefaultReportProvider(storage);
		provider.put(Rubric.TYPE, new RubricTableHandler());
		provider.put(SingleChoice.TYPE, new SingleChoiceTableHandler());
		provider.put(MultipleChoice.TYPE, new MultipleChoiceTableHandler());
		tableReportCtrl = new EvaluationFormReportController(ureq, getWindowControl(), form, storage, getReportSessionFilter(), provider, getReportHelper(), null);
		listenTo(tableReportCtrl);
		
		colsCtrl = new Analysis2ColController(ureq, getWindowControl(), tableReportCtrl, filterCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("segments.table.report"), colsCtrl);
		segmentButtonsCmp.setSelectedButton(tableReportLink);
		setEvaluationFormToolComponents();
	}
	
	private void doOpenDiagramReport(UserRequest ureq) {
		removeAsListenerAndDispose(diagramReportCtrl);
		
		DefaultReportProvider provider = new DefaultReportProvider(storage);
		diagramReportCtrl = new EvaluationFormReportController(ureq, getWindowControl(), form, storage,
				getReportSessionFilter(), provider, getReportHelper(), null);
		listenTo(diagramReportCtrl);
		
		colsCtrl = new Analysis2ColController(ureq, getWindowControl(), diagramReportCtrl, filterCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("segments.diagram.report"), colsCtrl);
		segmentButtonsCmp.setSelectedButton(diagramReportLink);
		setEvaluationFormToolComponents();
	}

	private void doOpenSessionSelection(UserRequest ureq) {
		removeAsListenerAndDispose(sessionSelectionCtrl);

		sessionSelectionCtrl = new AnalysisSessionSelectionController(ureq, getWindowControl(), form,
				storage, getReportSessionFilter(), getReportHelper(), stackPanel, toolComponents);
		
		colsCtrl = new Analysis2ColController(ureq, getWindowControl(), sessionSelectionCtrl, filterCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("segments.session.selection"), colsCtrl);
		segmentButtonsCmp.setSelectedButton(sessionSelectionLink);
	}
	
	private void doOpenHeatMap(UserRequest ureq) {
		if (heatMapCtrl == null) {
			heatMapCtrl = new HeatMapController(ureq, getWindowControl(), stackPanel, filterCtrl, form,
					availableAttributes, presentation.getHeatMapGrouping(), presentation.getHeatMapInsufficientOnly(),
					presentation.getTemporalGroupBy(), presentation.getTrendDifference(), presentation.getRubricId());
			listenTo(heatMapCtrl);
		}
		heatMapCtrl.onFilter(ureq, presentation.getSearchParams());
		heatMapCtrl.setToolComponents(toolComponents);
		
		colsCtrl = new Analysis2ColController(ureq, getWindowControl(), heatMapCtrl, filterCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("segments.heatmap.link"), colsCtrl);
		segmentButtonsCmp.setSelectedButton(heatMapLink);
	}
	
	private void doOpenTrendDiagram(UserRequest ureq) {
		if (trendDiagramCtrl == null) {
			trendDiagramCtrl = new TrendController(ureq, getWindowControl(), stackPanel, filterCtrl, form, availableAttributes,
					presentation.getHeatMapGrouping(), presentation.getHeatMapInsufficientOnly(),
					presentation.getTemporalGroupBy(), presentation.getTrendDifference(), presentation.getRubricId());
			listenTo(trendDiagramCtrl);
		}
		trendDiagramCtrl.onFilter(ureq, presentation.getSearchParams());
		trendDiagramCtrl.setToolComponents(toolComponents);

		colsCtrl = new Analysis2ColController(ureq, getWindowControl(), trendDiagramCtrl, filterCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("segments.trend.link"), colsCtrl);
		segmentButtonsCmp.setSelectedButton(trendDiagramLink);
	}
	
	private void setHeatMapInsufficientOnly(Boolean insufficientOnly) {
		presentation.setHeatMapInsufficientOnly(insufficientOnly);
		if (heatMapCtrl != null) {
			heatMapCtrl.setInsufficientOnly(insufficientOnly);
		}
		if (trendDiagramCtrl != null) {
			trendDiagramCtrl.setInsufficientOnly(insufficientOnly);
		}
	}

	private void setHeatMapGrouping(MultiGroupBy multiGroupBy) {
		presentation.setHeatMapGrouping(multiGroupBy);
		if (heatMapCtrl != null) {
			heatMapCtrl.setMultiGroupBy(multiGroupBy);
		}
		if (trendDiagramCtrl != null) {
			trendDiagramCtrl.setMultiGroupBy(multiGroupBy);
		}
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
		presentation.getSearchParams().setDataCollectionOrganisationRefs(secCallback.getViewAnalysisOrganisationRefs());
		doShowHildePresentationLinks();
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
		doShowHildePresentationLinks();
		stackPanel.changeDisplayname(presentation.getFormEntry().getDisplayname());
	}
	
	private void doOpenPrintSelection(UserRequest ureq) {
		doOpenPrintSelection(ureq, printLink, Target.PRINT);
	}

	private void doOpenPrintSelection(UserRequest ureq, Link targetLink, Target target) {
		removeAsListenerAndDispose(printSelectionCtrl);
		printSelectionCtrl = new EvaluationFormPrintSelectionController(ureq, getWindowControl(), form, storage, getReportSessionFilter(),
				getReportFigures(), getReportHelper(), target, presentation.getFormEntry().getDisplayname());
		listenTo(printSelectionCtrl);

		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				printSelectionCtrl.getInitialComponent(), targetLink, "", true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doPopupPrint(UserRequest ureq) {
		ControllerCreator printControllerCreator = getPrintControllerCreator();
		if (printControllerCreator != null) {
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
			openInNewBrowserWindow(ureq, layoutCtrlr, true);
		}
	}
	
	private void doExportPdf(UserRequest ureq) {
		Link selectedButton = segmentButtonsCmp.getSelectedButton();
		if (selectedButton == overviewReportLink
				|| selectedButton == tableReportLink
				|| selectedButton == diagramReportLink
				|| selectedButton == sessionSelectionLink) {
			doOpenPrintSelection(ureq, pdfLink, Target.PDF);
		} else {
			doExportAnalysis(ureq);
		}
	}

	private void doExportAnalysis(UserRequest ureq) {
		ControllerCreator printControllerCreator = getPrintControllerCreator();
		if (printControllerCreator != null) {
			String title = presentation.getFormEntry().getDisplayname();
			MediaResource resource = pdfService.convert(title, getIdentity(), printControllerCreator, getWindowControl());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}

	private ControllerCreator getPrintControllerCreator() {
		ControllerCreator printControllerCreator = null;
		Link selectedButton = segmentButtonsCmp.getSelectedButton();
		if (selectedButton == heatMapLink) {
			printControllerCreator = heatMapCtrl.getDetailsControllerCreator(presentation.getFormEntry().getDisplayname());
			if (printControllerCreator == null) {
				printControllerCreator = getHeatMapControllerCreator();
			}
		} else if (selectedButton == trendDiagramLink) {
			printControllerCreator = trendDiagramCtrl.getDetailsControllerCreator(presentation.getFormEntry().getDisplayname());
			if (printControllerCreator == null) {
				printControllerCreator = getTrendControllerCreator();
			}
		}
		return printControllerCreator;
	}

	private ControllerCreator getHeatMapControllerCreator() {
		return (lureq, lwControl) -> {
			GroupByController groupByCtrl = new HeatMapController(lureq, lwControl, stackPanel, null, form,
					availableAttributes, presentation.getHeatMapGrouping(), presentation.getHeatMapInsufficientOnly(),
					presentation.getTemporalGroupBy(), presentation.getTrendDifference(), presentation.getRubricId());
			groupByCtrl.onFilter(lureq, presentation.getSearchParams());
			boolean insufficientOnly = groupByCtrl.getInsufficientOnly();
			return new FilteredPrintController(lureq, lwControl, groupByCtrl, presentation.getSearchParams(), insufficientOnly,
					presentation.getFormEntry().getDisplayname());
		};
	}

	private ControllerCreator getTrendControllerCreator() {
		return (lureq, lwControl) -> {
			GroupByController groupByCtrl = new TrendController(lureq, lwControl, stackPanel, null, form,
					availableAttributes, presentation.getHeatMapGrouping(), presentation.getHeatMapInsufficientOnly(),
					presentation.getTemporalGroupBy(), presentation.getTrendDifference(), presentation.getRubricId());
			groupByCtrl.onFilter(lureq, presentation.getSearchParams());
			boolean insufficientOnly = groupByCtrl.getInsufficientOnly();
			return new FilteredPrintController(lureq, lwControl, groupByCtrl, presentation.getSearchParams(), insufficientOnly,
					presentation.getFormEntry().getDisplayname());
		};
	}

	private void doExport(UserRequest ureq) {
		String surveyName = "survey";
		EvaluationFormExcelExport export = new EvaluationFormExcelExport(form, getReportSessionFilter(),
				getReportHelper().getComparator(), new ReportHelperUserColumns(getReportHelper()), surveyName);
		ureq.getDispatchResult().setResultingMediaResource(export.createMediaResource());
	}
	
	private void doShowHildePresentationLinks() {
		if (secCallback.canEditPresentations()) {
			editPresentationLink.setVisible(true);
		} else {
			editPresentationLink.setVisible(false);
		}
		if (secCallback.canDeletePresentation(presentation)) {
			deletePresentationLink.setVisible(true);
		} else {
			deletePresentationLink.setVisible(false);
		}
		stackPanel.setDirty(true);
	}

	private void doShowFilter() {
		showFilterLink.setVisible(false);
		hideFilterLink.setVisible(true);
		stackPanel.setDirty(true);
		if (colsCtrl != null) {
			setShowFilter(Boolean.TRUE);
		}
	}
	
	private void doHideFilter() {
		showFilterLink.setVisible(true);
		hideFilterLink.setVisible(false);
		stackPanel.setDirty(true);
		if (colsCtrl != null) {
			setShowFilter(Boolean.FALSE);
		}
	}

	private void setShowFilter(Boolean show) {
		colsCtrl.setShowFilter(show);
		if (heatMapCtrl != null) {
			heatMapCtrl.setShowFilter(show);
		}
		if (trendDiagramCtrl != null) {
			trendDiagramCtrl.setShowFilter(show);
		}
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
	
	static final class ToolComponents {

		private final TooledStackedPanel stackPanel;
		private final Link printLink;
		private final Link printPopupLink;
		private final Link pdfLink;
		private final Link exportLink;
		private final Link showFilterLink;
		private final Link hideFilterLink;
		
		private Link lastVisibleFilter;
		
		private ToolComponents(TooledStackedPanel stackPanel, Link printLink, Link printPopupLink, Link pdfLink,
				Link exportLink, Link showFilterLink, Link hideFilterLink) {
			this.stackPanel = stackPanel;
			this.printLink = printLink;
			this.printPopupLink = printPopupLink;
			this.pdfLink = pdfLink;
			this.exportLink = exportLink;
			this.showFilterLink = showFilterLink;
			this.hideFilterLink = hideFilterLink;
		}
		
		void setPrintVisibility(boolean visible) {
			if (printLink != null) {
				printLink.setVisible(visible);
				stackPanel.setDirty(true);
			}
		}
		
		void setPrintPopupVisibility(boolean visible) {
			if (printPopupLink != null) {
				printPopupLink.setVisible(visible);
				stackPanel.setDirty(true);
			}
		}
		
		void setPdfVisibility(boolean visible) {
			if (pdfLink != null) {
				pdfLink.setVisible(visible);
				stackPanel.setDirty(true);
			}
		}
		
		void setExportVisibility(boolean visible) {
			if (exportLink != null) {
				exportLink.setVisible(visible);
				stackPanel.setDirty(true);
			}
		}
		
		void setFilterVisibility(boolean visible) {
			if (visible) {
				if (!isFilterVisible()) {
					if (lastVisibleFilter == null || lastVisibleFilter == showFilterLink) {
						showFilterLink.setVisible(true);
						hideFilterLink.setVisible(false);
					} else {
						showFilterLink.setVisible(false);
						hideFilterLink.setVisible(true);
					}
				}
			} else {
				lastVisibleFilter = showFilterLink.isVisible()? showFilterLink: hideFilterLink;
				showFilterLink.setVisible(false);
				hideFilterLink.setVisible(false);
			}
		}

		private boolean isFilterVisible() {
			return showFilterLink.isVisible() || hideFilterLink.isVisible();
		}
		
	}
	
}
