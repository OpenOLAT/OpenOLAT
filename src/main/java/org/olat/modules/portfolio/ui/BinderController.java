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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.event.DeleteBinderEvent;
import org.olat.modules.portfolio.ui.event.OpenPageEvent;
import org.olat.modules.portfolio.ui.event.PageSelectionEvent;
import org.olat.modules.portfolio.ui.event.RestoreBinderEvent;
import org.olat.modules.portfolio.ui.event.SectionSelectionEvent;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderController extends BasicController implements TooledController, Activateable2 {
	
	private Link overviewLink;
	private Link entriesLink;
	private Link historyLink;
	private Link assessmentLink;
	private Link templatesListLink;
	private Link templatesEditLink;
	private Link publishLink;
	private final ButtonGroupComponent segmentButtonsCmp;
	private final TooledStackedPanel stackPanel;
	private StackedPanel mainPanel;
	
	private HistoryController historyCtrl;
	private PublishController publishCtrl;
	private BinderPageListController entriesCtrl;
	private TableOfContentController overviewCtrl;
	private BinderAssessmentController assessmentCtrl;
	private AssignmentTemplatesListController templatesListCtrl;
	private AssignmentTemplatesEditController editTemplatesCtrl;
	
	private Binder binder;
	private final BinderConfiguration config;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private PortfolioService portfolioService;

	public BinderController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl);
		this.binder = binder;
		this.config = config;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;

		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(binder));

		segmentButtonsCmp = new ButtonGroupComponent("segments");
		segmentButtonsCmp.setElementCssClass("o_sel_pf_binder_navigation");
		if (portfolioModule.isOverviewEnabled()) {
			overviewLink = LinkFactory.createLink("portfolio.overview", getTranslator(), this);
			overviewLink.setElementCssClass("o_sel_pf_toc");
			segmentButtonsCmp.addButton(overviewLink, false);
		}
		if (portfolioModule.isEntriesEnabled()) {
			entriesLink = LinkFactory.createLink("portfolio.entries", getTranslator(), this);
			entriesLink.setElementCssClass("o_sel_pf_entries");
			segmentButtonsCmp.addButton(entriesLink, false);
		}
		if(secCallback.canInstantianteBinderAssignment()) {
			templatesListLink = LinkFactory.createLink("portfolio.templates", getTranslator(), this);
			templatesListLink.setElementCssClass("o_sel_pf_templates");
			segmentButtonsCmp.addButton(templatesListLink, false);
		}
		if (portfolioModule.isHistoryEnabled()) {
			historyLink = LinkFactory.createLink("portfolio.history", getTranslator(), this);
			historyLink.setElementCssClass("o_sel_pf_history");
			segmentButtonsCmp.addButton(historyLink, false);
		}
		if(config.isShareable() && secCallback.canViewAccessRights()) {
			publishLink = LinkFactory.createLink("portfolio.publish", getTranslator(), this);
			publishLink.setElementCssClass("o_sel_pf_publication");
			segmentButtonsCmp.addButton(publishLink, false);
		}
		if(config.isAssessable() && secCallback.canViewAssessment()) {
			assessmentLink = LinkFactory.createLink("portfolio.assessment", getTranslator(), this);
			assessmentLink.setElementCssClass("o_sel_pf_assessment");
			segmentButtonsCmp.addButton(assessmentLink, false);
		}
		if(secCallback.canNewBinderAssignment()) {
			templatesEditLink = LinkFactory.createLink("portfolio.templates.edit", getTranslator(), this);
			templatesEditLink.setElementCssClass("o_sel_pf_edit_templates");
			segmentButtonsCmp.addButton(templatesEditLink, false);
		}
		
		mainPanel = putInitialPanel(new SimpleStackedPanel("portfolioSegments"));
		if (secCallback.canNewAssignment()) {
			// in template mode, add editor class to toolbar
			mainPanel.setCssClass("o_edit_mode");
		}
		mainPanel.setContent(new Panel("empty"));
		stackPanel.addListener(this);
	}

	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, true);

		if(segmentButtonsCmp.getSelectedButton() == overviewLink) {
			if(overviewCtrl != null) {
				overviewCtrl.initTools();
			}
		}
	}
	
	public Binder getBinder() {
		return binder;
	}
	
	protected void setSegmentButtonsVisible(boolean enabled) {
		if(segmentButtonsCmp != null) {
			segmentButtonsCmp.setVisible(enabled);
			stackPanel.addTool(segmentButtonsCmp, true);
		}
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			String ePoint = portfolioModule.getBinderEntryPoint();
			if(binder != null && binder.getBinderStatus() == BinderStatus.deleted) {
				if (PortfolioV2Module.ENTRY_POINT_TOC.equals(ePoint)) {
					doOpenOverview(ureq);
				} else {
					doOpenEntries(ureq);
				}
			} else if(PortfolioV2Module.ENTRY_POINT_TOC.equals(ePoint)) {
				int numOfSections = doOpenOverview(ureq).getNumOfSections();
				if(numOfSections == 0 && !secCallback.canEditBinder()) {
					activateEntries(ureq);
				}
			} else {
				activateEntries(ureq);
			}
			return;
		}
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Page".equalsIgnoreCase(resName) || "Entry".equalsIgnoreCase(resName) || "Section".equalsIgnoreCase(resName)) {
			doOpenEntries(ureq).activate(ureq, entries, state);
		} else if("Entries".equalsIgnoreCase(resName)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenEntries(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Publish".equalsIgnoreCase(resName)) {
			if(config.isShareable()) {
				doOpenPublish(ureq);
			}
		} else if("Assessment".equalsIgnoreCase(resName)) {
			if(config.isAssessable()) {
				doOpenAssessment(ureq);
			}
		} else if("History".equalsIgnoreCase(resName)) {
			doOpenHistory(ureq);
		} else if("Toc".equalsIgnoreCase(resName)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if(!subEntries.isEmpty()) {
				doOpenEntries(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			} else {
				doOpenOverview(ureq);
			}
		} else if("Templates".equalsIgnoreCase(resName)) {
			if(secCallback.canNewBinderAssignment()) {
				doOpenTemplatesEditor(ureq);
			} else if(secCallback.canInstantianteBinderAssignment()) {
				doOpenTemplatesList(ureq);
			}
		}
	}
	
	private void activateEntries(UserRequest ureq) {
		int numOfPages = doOpenEntries(ureq).getNumOfPages();
		if(numOfPages == 1 && !secCallback.canEditBinder()) {
			PortfolioElementRow firstPage = entriesCtrl.getFirstPage();
			if(firstPage != null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("Page", firstPage.getPage().getKey());
				List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(ores);
				entriesCtrl.activate(ureq, entries, null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(entriesCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				if(overviewCtrl != null) {
					overviewCtrl.loadModel();
				}
			}
		} else if(overviewCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				removeAsListenerAndDispose(entriesCtrl);
				entriesCtrl = null;
				binder = portfolioService.getBinderByKey(binder.getKey());
			} else if(event instanceof SectionSelectionEvent sse) {
				List<ContextEntry> entries = new ArrayList<>();
				entries.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance("Section", sse.getSection().getKey())));
				doOpenEntries(ureq).activate(ureq, entries, null);
			} else if(event instanceof PageSelectionEvent pse) {
				List<ContextEntry> entries = new ArrayList<>();
				entries.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance("Page", pse.getPage().getKey())));
				doOpenEntries(ureq).activate(ureq, entries, null);
			} else if(event instanceof DeleteBinderEvent || event instanceof RestoreBinderEvent) {
				fireEvent(ureq, event);
			}
		} else if(templatesListCtrl == source) {
			if(event instanceof OpenPageEvent ope) {
				doOpenPage(ureq, ope.getPage());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (overviewLink == source) {
			doOpenOverview(ureq);
		} else if(entriesLink == source) {
			doOpenEntries(ureq);
		} else if(publishLink == source) {
			doOpenPublish(ureq);
		} else if(assessmentLink == source) {
			doOpenAssessment(ureq);
		} else if(historyLink == source) {
			doOpenHistory(ureq);
		} else if(templatesEditLink == source) {
			doOpenTemplatesEditor(ureq);
		} else if(templatesListLink == source) {
			doOpenTemplatesList(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				if(stackPanel.getLastController() == this) {
					doOpenOverview(ureq);
				}
			}
		}
	}
	
	private void popUpToBinderController(UserRequest ureq) {
		if(stackPanel.getRootController() == this) {
			stackPanel.popUpToRootController(ureq);
		} else {
			stackPanel.popUpToController(this);
		}
	}
	
	private TableOfContentController doOpenOverview(UserRequest ureq) {
		popUpToBinderController(ureq);
		
		if(overviewCtrl == null) {
			OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Toc", 0l);
			WindowControl swControl = addToHistory(ureq, bindersOres, null);
			overviewCtrl = new TableOfContentController(ureq, swControl, stackPanel, secCallback, binder, config);
			overviewCtrl.initTools();//because it will not end in the stackPanel as a pushed controller
			listenTo(overviewCtrl);
		} else {
			overviewCtrl.loadModel();
			overviewCtrl.updateSummaryView(ureq);
		}

		segmentButtonsCmp.setSelectedButton(overviewLink);
		mainPanel.setContent(overviewCtrl.getInitialComponent());
		return overviewCtrl;
	}
	
	private BinderPageListController doOpenEntries(UserRequest ureq) {
		removeAsListenerAndDispose(entriesCtrl);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Entries", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		entriesCtrl = new BinderPageListController(ureq, swControl, stackPanel, secCallback, binder, config);
		listenTo(entriesCtrl);

		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.entries"), entriesCtrl);
		segmentButtonsCmp.setSelectedButton(entriesLink);
		return entriesCtrl;
	}
	
	private PublishController doOpenPublish(UserRequest ureq) {
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Publish", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		publishCtrl = new PublishController(ureq, swControl, stackPanel, secCallback, binder, config);
		listenTo(publishCtrl);

		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.publish"), publishCtrl);
		segmentButtonsCmp.setSelectedButton(publishLink);
		return publishCtrl;
	}
	
	private BinderAssessmentController doOpenAssessment(UserRequest ureq) {
		removeAsListenerAndDispose(assessmentCtrl);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Assessment", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		assessmentCtrl = new BinderAssessmentController(ureq, swControl, secCallback, binder, config);
		listenTo(assessmentCtrl);

		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.assessment"), assessmentCtrl);
		segmentButtonsCmp.setSelectedButton(assessmentLink);
		return assessmentCtrl;
	}
	
	private HistoryController doOpenHistory(UserRequest ureq) {
		removeAsListenerAndDispose(historyCtrl);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("History", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		historyCtrl = new HistoryController(ureq, swControl, secCallback, binder);
		listenTo(historyCtrl);
		
		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.history"), historyCtrl);
		segmentButtonsCmp.setSelectedButton(historyLink);
		return historyCtrl;
	}
	
	private AssignmentTemplatesEditController doOpenTemplatesEditor(UserRequest ureq) {
		removeAsListenerAndDispose(editTemplatesCtrl);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Templates", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		editTemplatesCtrl = new AssignmentTemplatesEditController(ureq, swControl, binder);
		listenTo(editTemplatesCtrl);
		
		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.templates.edit"), editTemplatesCtrl);
		segmentButtonsCmp.setSelectedButton(templatesEditLink);
		return editTemplatesCtrl;
	}
	
	private AssignmentTemplatesListController doOpenTemplatesList(UserRequest ureq) {
		removeAsListenerAndDispose(templatesListCtrl);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Templates", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		templatesListCtrl = new AssignmentTemplatesListController(ureq, swControl, stackPanel, secCallback, binder);
		listenTo(templatesListCtrl);
		
		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.templates"), templatesListCtrl);
		segmentButtonsCmp.setSelectedButton(templatesListLink);
		return templatesListCtrl;
	}
	
	private void doOpenPage(UserRequest ureq, Page page) {
		if(page == null) return;
		
		BinderPageListController pagesCtrl = doOpenEntries(ureq);
		pagesCtrl.doFilterSection(page.getSection());
		pagesCtrl.doOpenRow(ureq, page);
	}
}