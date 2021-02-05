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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.spacesaver.ToggleBoxController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CategoryToElement;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageUserInformations;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.ExtendedMediaRenderingHints;
import org.olat.modules.portfolio.ui.component.TimelinePoint;
import org.olat.modules.portfolio.ui.export.ExportBinderAsCPResource;
import org.olat.modules.portfolio.ui.export.ExportBinderAsPDFResource;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;
import org.olat.modules.portfolio.ui.renderer.SharedPageStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderPageListController extends AbstractPageListController {
	
	private Link newSectionLink, newEntryLink, newAssignmentLink,
		exportBinderAsCpLink, exportBinderAsPdfLink, printLink;
	private FormLink newSectionButton, previousSectionLink, nextSectionLink, showAllSectionsLink;
	
	private CloseableModalController cmc;
	private TextComponent summaryComp;
	private ToggleBoxController summaryCtrl;
	private SectionEditController newSectionCtrl;
	private PageMetadataEditController newPageCtrl;
	private AssignmentEditController newAssignmentCtrl;

	private final Binder binder;
	private final List<Identity> owners;
	private Section filteringSection;
	
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private UserManager userManager;
	
	public BinderPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl, stackPanel, secCallback, config, "binder_pages", true, true, false);
		this.binder = binder;
		stackPanel.addListener(this);
		owners = portfolioService.getMembers(binder, PortfolioRoles.owner.name());
		
		RepositoryEntry repoEntry = binder.getEntry();
		if (repoEntry != null) {
			flc.contextPut("referenceEntryName", repoEntry.getDisplayname());
			String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repoEntry.getKey();
			flc.contextPut("referenceEntryUrl", url);
		}
		
		summaryComp = TextFactory.createTextComponentFromString("summaryCmp" + CodeHelper.getRAMUniqueID(), "", "o_block_large_bottom", false, null);
		summaryCtrl = new ToggleBoxController(ureq, wControl, getGuiPrefsKey(binder), translate("summary.open"),
				translate("summary.close"), summaryComp);
		
		initForm(ureq);
		loadModel(ureq, null);
		
		if (secCallback.canNewAssignment()) {
			// in template mode, add editor class to toolbar
			initialPanel.setCssClass("o_edit_mode");
		}
	}

	@Override
	protected String getTimelineSwitchPreferencesName() {
		return "binder-timeline-switch-" + binder.getKey();
	}
	
	private String getGuiPrefsKey(OLATResourceable binderOres) {
		return new StringBuilder()
				.append(binderOres.getResourceableTypeName())
				.append("::")
				.append(binderOres.getResourceableId())
				.toString();
	}

	@Override
	public void initTools() {
		if(secCallback.canExportBinder()) {
			Dropdown exportTools = new Dropdown("export.binder", "export.binder", false, getTranslator());
			exportTools.setElementCssClass("o_sel_pf_export_tools");
			exportTools.setIconCSS("o_icon o_icon_download");
			stackPanel.addTool(exportTools, Align.left);

			exportBinderAsCpLink = LinkFactory.createToolLink("export.binder.cp", translate("export.binder.cp"), this);
			exportBinderAsCpLink.setIconLeftCSS("o_icon o_icon_download");
			exportTools.addComponent(exportBinderAsCpLink);
			
			if(pdfModule.isEnabled()) {
				exportBinderAsPdfLink = LinkFactory.createToolLink("export.binder.pdf", translate("export.binder.pdf"), this);
				exportBinderAsPdfLink.setIconLeftCSS("o_icon o_filetype_pdf");
				exportTools.addComponent(exportBinderAsPdfLink);
			}
			
			printLink = LinkFactory.createToolLink("export.binder.onepage", translate("export.binder.onepage"), this);
			printLink.setIconLeftCSS("o_icon o_icon_print");
			printLink.setPopup(new LinkPopupSettings(950, 750, "binder"));
			exportTools.addComponent(printLink);
		}
		
		if(secCallback.canAddSection()) {
			newSectionLink = LinkFactory.createToolLink("new.section", translate("create.new.section"), this);
			newSectionLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			newSectionLink.setElementCssClass("o_sel_pf_new_section");
			stackPanel.addTool(newSectionLink, Align.right);
		}
		
		if(secCallback.canAddPage(null) || secCallback.canInstantianteBinderAssignment()) {
			newEntryLink = LinkFactory.createToolLink("new.page", translate("create.new.page"), this);
			newEntryLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			newEntryLink.setElementCssClass("o_sel_pf_new_entry");
			newEntryLink.setVisible(model.getRowCount() > 0);
			stackPanel.addTool(newEntryLink, Align.right);
		}
		
		if(secCallback.canNewAssignment()) {
			newAssignmentLink = LinkFactory.createToolLink("new.assignment", translate("create.new.assignment"), this);
			newAssignmentLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			newAssignmentLink.setElementCssClass("o_sel_pf_new_assignment");
			newAssignmentLink.setVisible(model.getRowCount() > 0);
			stackPanel.addTool(newAssignmentLink, Align.right);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			StringBuilder ownerSb = new StringBuilder();
			for(Identity owner:owners) {
				if(ownerSb.length() > 0) ownerSb.append(", ");
				ownerSb.append(userManager.getUserDisplayName(owner));
			}
			layoutCont.contextPut("owners", ownerSb.toString());
			layoutCont.contextPut("binderKey", binder.getKey());
			layoutCont.contextPut("binderTitle", StringHelper.escapeHtml(binder.getTitle()));
		}

		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setFromColumnModel(false);
		options.setDefaultOrderBy(new SortKey(null, false));
		tableEl.setSortSettings(options);
		
		previousSectionLink = uifactory.addFormLink("section.paging.previous", formLayout, Link.BUTTON | Link.NONTRANSLATED);
		previousSectionLink.setVisible(false);
		previousSectionLink.setIconLeftCSS("o_icon o_icon_move_left");
		nextSectionLink = uifactory.addFormLink("section.paging.next", formLayout, Link.BUTTON | Link.NONTRANSLATED);
		nextSectionLink.setVisible(false);
		nextSectionLink.setIconRightCSS("o_icon o_icon_move_right");
		showAllSectionsLink = uifactory.addFormLink("section.paging.all", formLayout, Link.BUTTON);
		showAllSectionsLink.setVisible(false);
		
		if(secCallback.canAddSection()) {
			newSectionButton = uifactory.addFormLink("create.new.section", formLayout, Link.BUTTON);
			newSectionButton.setCustomEnabledLinkCSS("btn btn-primary o_sel_pf_new_section");
		}
	}
	
	@Override
	public int getNumOfPages() {
		int countPages = 0;
		if(model != null) {
			List<PortfolioElementRow> rows = model.getObjects();
			for(PortfolioElementRow row:rows) {
				if(row.isPage()) {
					countPages++;
				}
			}
		}
		return countPages;
	}

	@Override
	protected void loadModel(UserRequest ureq, String searchString) {
		if (StringHelper.containsNonWhitespace(binder.getSummary())) {
			summaryComp.setText(StringHelper.xssScan(binder.getSummary()));
			flc.getFormItemComponent().put("summary", summaryCtrl.getInitialComponent());
		} else {
			flc.getFormItemComponent().remove("summary");
		}
		
		List<Section> sections = portfolioService.getSections(binder);

		List<CategoryToElement> categorizedElements = portfolioService.getCategorizedSectionsAndPages(binder);
		Map<OLATResourceable,List<Category>> categorizedElementMap = new HashMap<>();
		Map<Section,Set<String>> sectionAggregatedCategoriesMap = new HashMap<>();
		for(CategoryToElement categorizedElement:categorizedElements) {
			List<Category> categories = categorizedElementMap.get(categorizedElement.getCategorizedResource());
			if(categories == null) {
				categories = new ArrayList<>();
				categorizedElementMap.put(categorizedElement.getCategorizedResource(), categories);
			}
			categories.add(categorizedElement.getCategory());
		}
		
		//comments
		Map<Long,Long> numberOfCommentsMap = portfolioService.getNumberOfComments(binder);
		
		//assessment sections
		List<AssessmentSection> assessmentSections = portfolioService.getAssessmentSections(binder, getIdentity());
		Map<Section,AssessmentSection> sectionToAssessmentSectionMap = assessmentSections.stream()
				.collect(Collectors.toMap(AssessmentSection::getSection, as -> as));

		List<PortfolioElementRow> rows = new ArrayList<>();

		//assignments
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, searchString);
		Map<Section,List<Assignment>> sectionToAssignmentMap = new HashMap<>();
		for(Assignment assignment:assignments) {
			List<Assignment> assignmentList;
			Section section = assignment.getSection();
			if(sectionToAssignmentMap.containsKey(section)) {
				assignmentList = sectionToAssignmentMap.get(section);
			} else {
				assignmentList = new ArrayList<>();
				sectionToAssignmentMap.put(section, assignmentList);
			}
			assignmentList.add(assignment);
		}
		
		for(Assignment assignment:assignments) {
			Section section = assignment.getSection();
			if(assignment.getPage() == null && secCallback.canViewPendingAssignments(section)) {
				List<Assignment> sectionAssignments = sectionToAssignmentMap.get(section);
				PortfolioElementRow row = forgePendingAssignmentRow(assignment, section, sectionAssignments);
				rows.add(row);
				
				if(secCallback.canAddPage(section)) {
					FormLink newEntryButton = uifactory.addFormLink("new.entry." + (++counter), "new.entry", "create.new.page", null, flc, Link.BUTTON);
					newEntryButton.setCustomEnabledLinkCSS("btn btn-primary o_sel_pf_new_entry");
					newEntryButton.setUserObject(row);
					row.setNewEntryLink(newEntryButton);
				}
			}
		}
		
		boolean userInfos = secCallback.canPageUserInfosStatus();
		Map<Long,PageUserInformations> userInfosToPage = new HashMap<>();
		rowVC.contextPut("userInfos", Boolean.valueOf(userInfos));
		if(userInfos) {
			rowVC.contextPut("userInfosRenderer", new SharedPageStatusCellRenderer(getTranslator()));
			List<PageUserInformations> userInfoList = portfolioService.getPageUserInfos(binder, getIdentity());
			for(PageUserInformations userInfo:userInfoList) {
				userInfosToPage.put(userInfo.getPage().getKey(), userInfo);
			}
		}

		List<Page> pages = portfolioService.getPages(binder, searchString);
		for (Page page : pages) {
			boolean viewElement = secCallback.canViewElement(page);
			boolean viewTitleElement = viewElement || secCallback.canViewTitleOfElement(page);
			if(!viewTitleElement) {
				continue;
			}
			
			Section section = page.getSection();
			PortfolioElementRow pageRow = forgePageRow(ureq, page, sectionToAssessmentSectionMap.get(section),
					sectionToAssignmentMap.get(section), categorizedElementMap, numberOfCommentsMap, viewElement);
			rows.add(pageRow);
			if(secCallback.canAddPage(section)) {
				FormLink newEntryButton = uifactory.addFormLink("new.entry." + (++counter), "new.entry", "create.new.page", null, flc, Link.BUTTON);
				newEntryButton.setCustomEnabledLinkCSS("btn btn-primary o_sel_pf_new_entry");
				newEntryButton.setUserObject(pageRow);
				pageRow.setNewEntryLink(newEntryButton);
			}
			
			if(secCallback.canNewAssignment() && section != null) {
				FormLink newAssignmentButton = uifactory.addFormLink("new.assignment." + (++counter), "new.assignment", "create.new.assignment", null, flc, Link.BUTTON);
				newAssignmentButton.setCustomEnabledLinkCSS("btn btn-primary o_sel_pf_new_assignment");
				newAssignmentButton.setUserObject(pageRow);
				pageRow.setNewAssignmentLink(newAssignmentButton);
			}
			
			if(userInfos) {
				PageUserInformations infos = userInfosToPage.get(page.getKey());
				if(infos != null) {
					pageRow.setUserInfosStatus(infos.getStatus());
				}
			}
			
			if(section != null) {
				Set<String> categories = sectionAggregatedCategoriesMap.get(section);
				if(categories == null) {
					categories = new HashSet<>();
					sectionAggregatedCategoriesMap.put(section, categories);
				}
				if(pageRow.getPageCategories() != null && !pageRow.getPageCategories().isEmpty()) {
					categories.addAll(pageRow.getPageCategories());
				}
				
				pageRow.setSectionCategories(categories);
			}
		}
		
		//sections without pages
		if(!StringHelper.containsNonWhitespace(searchString)) {
			for(Section section:sections) {
				if(!secCallback.canViewElement(section)) {
					continue;
				}
				
				PortfolioElementRow sectionRow = forgeSectionRow(section, sectionToAssessmentSectionMap.get(section),
						sectionToAssignmentMap.get(section), categorizedElementMap);
				rows.add(sectionRow);

				if(secCallback.canAddPage(section)) {
					FormLink newEntryButton = uifactory.addFormLink("new.entry." + (++counter), "new.entry", "create.new.page", null, flc, Link.BUTTON);
					newEntryButton.setCustomEnabledLinkCSS("btn btn-primary o_sel_pf_new_entry");
					newEntryButton.setUserObject(sectionRow);
					sectionRow.setNewEntryLink(newEntryButton);
				}
				
				if(secCallback.canNewAssignment() && section != null) {
					FormLink newAssignmentButton = uifactory.addFormLink("new.assignment." + (++counter), "new.assignment", "create.new.assignment", null, flc, Link.BUTTON);
					newAssignmentButton.setCustomEnabledLinkCSS("btn btn-primary o_sel_pf_new_assignment");
					newAssignmentButton.setUserObject(sectionRow);
					sectionRow.setNewAssignmentLink(newAssignmentButton);
				}
			}
		}

		if(newSectionButton != null && rows.isEmpty()) {
			flc.add("create.new.section", newSectionButton);
		} else if (newSectionButton != null)  {
			flc.remove(newSectionButton);
		}
		if(newEntryLink != null && !newEntryLink.isVisible()) {
			newEntryLink.setVisible(!rows.isEmpty());
			stackPanel.setDirty(true);
		}
		if(newAssignmentLink != null && !newAssignmentLink.isVisible()) {
			newAssignmentLink.setVisible(!rows.isEmpty());
			stackPanel.setDirty(true);
		}

		disposeRows();//clean up the posters
		model.setObjects(rows);
		if(filteringSection != null) {
			doFilterSection(filteringSection);
		} else {
			tableEl.reloadData();
			updateTimeline();
		}
	}
	
	private void updateTimeline() {
		List<PortfolioElementRow> pages = model.getObjects();
		List<TimelinePoint> points = new ArrayList<>(pages.size());
		for(PortfolioElementRow page:pages) {
			if(page.isPage()) {
				String s = page.getPageStatus() == null ? "draft" : page.getPageStatus().name();
				points.add(new TimelinePoint(page.getKey().toString(), page.getTitle(), page.getCreationDate(), s));
			}
		}
		timelineEl.setPoints(points);
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		removeAsListenerAndDispose(summaryCtrl);
		summaryCtrl = null;
		super.doDispose();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableRenderEvent) {
				FlexiTableRenderEvent re = (FlexiTableRenderEvent)event;
				if(re.getRendererType() == FlexiTableRendererType.custom) {
					tableEl.sort(new SortKey(null, false));
				}
			} else if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select-page".equals(cmd)) {
					PortfolioElementRow row = model.getObject(se.getIndex());
					if(row.isPendingAssignment()) {
						doStartAssignment(ureq, row);
					} else {
						doOpenRow(ureq, row, false);
					}
				}
			}
		} else if(previousSectionLink == source) {
			Section previousSection = (Section)previousSectionLink.getUserObject();
			doFilterSection(previousSection);
		} else if(nextSectionLink == source) {
			Section nextSection = (Section)nextSectionLink.getUserObject();
			doFilterSection(nextSection);
		} else if(showAllSectionsLink == source) {
			doShowAll();
		} else if(newSectionButton == source) {
			doCreateNewSection(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("new.entry".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doCreateNewPage(ureq, row.getSection());
			} else if("new.assignment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doCreateNewAssignment(ureq, row.getSection());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(newEntryLink == source) {
			doCreateNewPage(ureq, filteringSection);
		} else if(newSectionLink == source) {
			doCreateNewSection(ureq);
		} else if(newAssignmentLink == source) {
			if(filteringSection == null) {
				doCreateNewAssignment(ureq);
			} else {
				doCreateNewAssignment(ureq, filteringSection);
			}
		} else if(exportBinderAsCpLink == source) {
			doExportBinderAsCP(ureq);
		} else if(exportBinderAsPdfLink == source) {
			doExportBinderAsPdf(ureq);
		} else if(printLink == source) {
			doPrint(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent && pageCtrl != null && ((PopEvent)event).getController() == pageCtrl && pageCtrl.getSection() != null) {
				doFilterSection(pageCtrl.getSection());
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Section".equalsIgnoreCase(resName)) {
			Long resId = entries.get(0).getOLATResourceable().getResourceableId();
			
			PortfolioElementRow activatedRow = null;
			for(PortfolioElementRow row :model.getObjects()) {
				if(row.getSection() != null && row.getSection().getKey().equals(resId)) {
					activatedRow = row;
					break;
				}
			}
			
			if(activatedRow != null) {
				doFilterSection(activatedRow.getSection());
			}
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(newSectionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				filteringSection = newSectionCtrl.getSection();
				loadModel(ureq, null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newPageCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq, null);
				doOpenPage(ureq, newPageCtrl.getPage(), true);				
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newAssignmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq, null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newAssignmentCtrl);
		removeAsListenerAndDispose(newSectionCtrl);
		removeAsListenerAndDispose(newPageCtrl);
		removeAsListenerAndDispose(cmc);
		newAssignmentCtrl = null;
		newSectionCtrl = null;
		newPageCtrl = null;
		cmc = null;
	}
	
	private void doShowAll() {
		this.filteringSection = null;
		model.filter(null);
		tableEl.reloadData();
		updateTimeline();
		
		previousSectionLink.setVisible(false);
		nextSectionLink.setVisible(false);
		showAllSectionsLink.setVisible(false);
	}
	
	protected void doFilterSection(Section section) {
		this.filteringSection = section;
		List<Section> currentSections = model.filter(section);
		tableEl.reloadData();
		updateTimeline();
		
		int index = currentSections.indexOf(section);

		previousSectionLink.setEnabled(index > 0);
		if(index > 0) {
			String previousTitle = currentSections.get(index - 1).getTitle();
			previousSectionLink.setI18nKey(translate("section.paging.with.title", new String[]{ previousTitle }));
			previousSectionLink.setUserObject(currentSections.get(index - 1));
		} else {
			previousSectionLink.setI18nKey(translate("section.paging.previous"));
		}
		
		if(index >= 0 && index + 1 < currentSections.size()) {
			String nextTitle = currentSections.get(index + 1).getTitle();
			nextSectionLink.setI18nKey(translate("section.paging.with.title", new String[]{ nextTitle }));
			nextSectionLink.setEnabled(true);
			nextSectionLink.setUserObject(currentSections.get(index + 1));
		} else {
			nextSectionLink.setI18nKey(translate("section.paging.next"));
			nextSectionLink.setEnabled(false);
		}
		
		boolean visible = currentSections.size() > 1;
		previousSectionLink.setVisible(visible);
		nextSectionLink.setVisible(visible);
		showAllSectionsLink.setVisible(visible);
		flc.setDirty(true);
	}
	
	private void doCreateNewSection(UserRequest ureq) {
		if(guardModalController(newSectionCtrl)) return;
		
		newSectionCtrl = new SectionEditController(ureq, getWindowControl(), binder, secCallback);
		listenTo(newSectionCtrl);
		
		String title = translate("create.new.section");
		cmc = new CloseableModalController(getWindowControl(), null, newSectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateNewPage(UserRequest ureq, Section preSelectedSection) {
		if(guardModalController(newPageCtrl)) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), secCallback,
				binder, false, preSelectedSection, true, null);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateNewAssignment(UserRequest ureq) {
		if(guardModalController(newAssignmentCtrl)) return;

		newAssignmentCtrl = new AssignmentEditController(ureq, getWindowControl(), binder);
		listenTo(newAssignmentCtrl);
		
		String title = translate("create.new.assignment");
		cmc = new CloseableModalController(getWindowControl(), null, newAssignmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateNewAssignment(UserRequest ureq, Section section) {
		if(guardModalController(newAssignmentCtrl)) return;
		
		newAssignmentCtrl = new AssignmentEditController(ureq, getWindowControl(), section);
		listenTo(newAssignmentCtrl);
		
		String title = translate("create.new.assignment");
		cmc = new CloseableModalController(getWindowControl(), null, newAssignmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doExportBinderAsCP(UserRequest ureq) {
		MediaResource resource = new ExportBinderAsCPResource(binder, ureq, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doExportBinderAsPdf(UserRequest ureq) {
		MediaResource resource = new ExportBinderAsPDFResource(binder, ureq, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doPrint(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			BinderOnePageController printCtrl = new BinderOnePageController(lureq, lwControl, binder,
					ExtendedMediaRenderingHints.toPrint(), true);
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, printCtrl);
			layoutCtr.addDisposableChildController(printCtrl); // dispose controller on layout dispose
			return layoutCtr;				
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(ctrlCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr);
	}
	
	protected void doOpenRow(UserRequest ureq, Page page) {
		List<PortfolioElementRow> rows = model.getObjects();
		for(PortfolioElementRow row:rows) {
			if(page.equals(row.getPage())) {
				doOpenRow(ureq, row, false);
				break;
			}
		}
	}
	
	@Override
	protected void doOpenRow(UserRequest ureq, PortfolioElementRow row, boolean newElement) {
		if(row.isSection()) {
			doFilterSection(row.getSection());
		} else {
			super.doOpenRow(ureq, row, newElement);
		}
	}

	@Override
	protected Assignment doStartAssignment(UserRequest ureq, PortfolioElementRow row) {
		if(secCallback.canInstantiateAssignment()) {
			return super.doStartAssignment(ureq, row);
		} else if(secCallback.canNewAssignment()) {
			doEditAssignment(ureq, row);
		}
		return null;
	}
}
