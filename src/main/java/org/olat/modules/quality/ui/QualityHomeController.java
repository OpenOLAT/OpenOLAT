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
package org.olat.modules.quality.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnalysisPresentationSearchParameter;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.ui.AnalysisListController;
import org.olat.modules.quality.generator.ui.GeneratorListController;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityHomeController extends BasicController implements Activateable2{

	private static final String ORES_MY_TYPE = "my";
	private static final String ORES_SUGGESTION_TYPE = "suggestion";
	private static final String ORES_DATA_COLLECTIONS_TYPE = "datacollections";
	private static final String ORES_GENERATORS_TYPE = "generators";
	private static final String ORES_ANALYSIS_TYPE = "analysis";
	
	private final VelocityContainer mainVC;
	private Link executorParticipationLink;
	private Link suggestionLink;
	private Link dataCollectionLink;
	private Link generatorsLink;
	private Link analysisLink;
	private List<Component> analysisPresenatationLinks = Collections.emptyList();
	
	private final TooledStackedPanel stackPanel;
	private ExecutorParticipationsListController executorParticipationListCtrl;
	private SuggestionController suggestionCtrl;
	private DataCollectionListController dataCollectionListCtrl;
	private GeneratorListController generatorsListCtrl;
	private AnalysisListController analysisListCtrl;
	
	private final MainSecurityCallback secCallback;
	
	@Autowired
	private QualityAnalysisService analysisService;
	
	public QualityHomeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, MainSecurityCallback secCallback) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.stackPanel.setToolbarAutoEnabled(true);
		stackPanel.addListener(this);
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("home");
		createPanels();
		putInitialPanel(mainVC);
	}

	private void createPanels() {
		List<PanelWrapper> wrappers = new ArrayList<>(4);
		executorParticipationLink = LinkFactory.createLink("goto.executor.participation.link", mainVC, this);
		executorParticipationLink.setIconRightCSS("o_icon o_icon_start");
		wrappers.add(new PanelWrapper(translate("goto.executor.participation.title"),
				translate("goto.executor.participation.help"), executorParticipationLink, null));
		
		if (secCallback.canCreateSuggestion()) {
			suggestionLink = LinkFactory.createLink("goto.suggestion.link", mainVC, this);
			suggestionLink.setIconRightCSS("o_icon o_icon_start");
			wrappers.add(new PanelWrapper(translate("goto.suggestion.title"),
					translate("goto.suggestion.help"), suggestionLink, null));
		}
		
		if (secCallback.canViewDataCollections()) {
			dataCollectionLink = LinkFactory.createLink("goto.data.collection.link", mainVC, this);
			dataCollectionLink.setIconRightCSS("o_icon o_icon_start");
			wrappers.add(new PanelWrapper(translate("goto.data.collection.title"),
					translate("goto.data.collection.help"), dataCollectionLink, null));
		}
		
		if (secCallback.canViewGenerators()) {
			generatorsLink = LinkFactory.createLink("goto.generator.link", mainVC, this);
			generatorsLink.setIconRightCSS("o_icon o_icon_start");
			wrappers.add(new PanelWrapper(translate("goto.generator.title"),
					translate("goto.generator.help"), generatorsLink, null));
		}
		
		if (secCallback.canViewAnalysis()) {
			analysisLink = LinkFactory.createLink("goto.analysis.link", mainVC, this);
			analysisLink.setIconRightCSS("o_icon o_icon_start");
			analysisPresenatationLinks = getAnalysisPresentationLinks();
			wrappers.add(new PanelWrapper(translate("goto.analysis.title"),
					translate("goto.analysis.help"), analysisLink, analysisPresenatationLinks));
		}
		
		mainVC.contextPut("panels", wrappers);
	}
	
	private List<Component> getAnalysisPresentationLinks() {
		int counter = 0;
		AnalysisPresentationSearchParameter searchParams = new AnalysisPresentationSearchParameter();
		searchParams.setOrganisationRefs(secCallback.getViewPresentationOrganisationRefs());
		List<AnalysisPresentation> presentations = analysisService.loadPresentations(searchParams);
		List<Component> links = new ArrayList<>(presentations.size());
		for (AnalysisPresentation presentation : presentations) {
			Link link = LinkFactory.createLink("analysis.link" + counter++, "analysis.link", null, mainVC, this, Link.NONTRANSLATED);
			link.setCustomDisplayText(presentation.getName());
			link.setIconRightCSS("o_icon o_icon_start");
			link.setUserObject(presentation);
			links.add(link);
		}
		return links;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			if (canOnlyExecute()) {
				doOpenUserParticipations(ureq);
			}
			return;
		}
		
		OLATResourceable resource = entries.get(0).getOLATResourceable();
		if (ORES_MY_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())) {
			doOpenUserParticipations(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			executorParticipationListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if (ORES_SUGGESTION_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())
				&& secCallback.canCreateSuggestion()) {
			doOpenSuggestion(ureq);
		} else if (ORES_DATA_COLLECTIONS_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())
				&& secCallback.canViewDataCollections()) {
			doOpenDataCollection(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			dataCollectionListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			if (dataCollectionLink == null) {
				createPanels();
			}
		} else if (ORES_GENERATORS_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())
				&& secCallback.canViewGenerators()) {
			doOpenGenerators(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			generatorsListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if (ORES_ANALYSIS_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())
				&& secCallback.canViewAnalysis()) {
			doOpenAnalysis(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			analysisListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if (canOnlyExecute()) {
			doOpenUserParticipations(ureq);
		}
	}

	private boolean canOnlyExecute() {
		return suggestionLink == null
				&& dataCollectionLink == null
				&& generatorsLink == null
				&& analysisLink == null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (executorParticipationLink == source) {
			doOpenUserParticipations(ureq);
		} else if (suggestionLink == source) {
			doOpenSuggestion(ureq);
		} else if (dataCollectionLink == source) {
			doOpenDataCollection(ureq);
		} else if (generatorsLink == source) {
			doOpenGenerators(ureq);
		} else if (analysisLink == source) {
			doOpenAnalysis(ureq);
		} else if (analysisPresenatationLinks.contains(source)) {
			Link link = (Link) source;
			AnalysisPresentation presentation = (AnalysisPresentation) link.getUserObject();
			doOpenPresentation(ureq, presentation);
		} else if (stackPanel == source && stackPanel.getLastController() == this && event instanceof PopEvent) {
			// Recreate panes to refresh the analysis presentations
			createPanels();
		}
	}

	private void doOpenUserParticipations(UserRequest ureq) {
		// reuse existing list controller allow full screen mode et prevent unwanted disposed controllers
		if(!stackPanel.hasController(executorParticipationListCtrl)) {
			stackPanel.popUpToRootController(ureq);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_MY_TYPE, 0l);
			WindowControl bwControl = addToHistory(ureq, ores, null);
			executorParticipationListCtrl = new ExecutorParticipationsListController(ureq, bwControl, secCallback);
			listenTo(executorParticipationListCtrl);
			stackPanel.pushController(translate("breadcrumb.executor.participations"), executorParticipationListCtrl);
		}
	}
	
	private void doOpenSuggestion(UserRequest ureq) {
		stackPanel.popUpToRootController(ureq);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_SUGGESTION_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		suggestionCtrl = new SuggestionController(ureq, bwControl);
		listenTo(suggestionCtrl);
		stackPanel.pushController(translate("breadcrumb.suggestion"), suggestionCtrl);
	}

	private void doOpenDataCollection(UserRequest ureq) {
		stackPanel.popUpToRootController(ureq);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_DATA_COLLECTIONS_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		dataCollectionListCtrl = new DataCollectionListController(ureq, bwControl, stackPanel, secCallback);
		listenTo(dataCollectionListCtrl);
		stackPanel.pushController(translate("breadcrumb.data.collections"), dataCollectionListCtrl);
	}

	private void doOpenGenerators(UserRequest ureq) {
		stackPanel.popUpToRootController(ureq);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_GENERATORS_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		generatorsListCtrl = new GeneratorListController(ureq, bwControl, stackPanel, secCallback);
		listenTo(generatorsListCtrl);
		stackPanel.pushController(translate("breadcrumb.generators"), generatorsListCtrl);
	}
	
	private void doOpenAnalysis(UserRequest ureq) {
		doOpenAnalysis(ureq, null);
	}
	
	private void doOpenPresentation(UserRequest ureq, AnalysisPresentation presentation) {
		OLATResourceable ores = AnalysisListController.getOlatResourceable(presentation);
		ContextEntry contextEntry = BusinessControlFactory.getInstance().createContextEntry(ores);
		List<ContextEntry> entries = Collections.singletonList(contextEntry);
		doOpenAnalysis(ureq, entries);
	}

	private void doOpenAnalysis(UserRequest ureq, List<ContextEntry> entries) {
		stackPanel.popUpToRootController(ureq);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_ANALYSIS_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		analysisListCtrl = new AnalysisListController(ureq, bwControl, stackPanel, secCallback);
		listenTo(analysisListCtrl);
		stackPanel.pushController(translate("breadcrumb.analysis"), analysisListCtrl);
		analysisListCtrl.activate(ureq, entries, null);
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}
	
	public static class PanelWrapper {
		
		private final String title;
		private final String helpText;
		private final Component goToLink;
		private final List<Component> links;
		
		public PanelWrapper(String title, String helpText, Component goToLink, List<Component> links) {
			this.title = title;
			this.helpText = helpText;
			this.goToLink = goToLink;
			this.links = links;
		}

		public String getTitle() {
			return title;
		}

		public String getHelpText() {
			return helpText;
		}

		public Component getGoToLink() {
			return goToLink;
		}

		public List<Component> getLinks() {
			return links;
		}
		
	}
}
