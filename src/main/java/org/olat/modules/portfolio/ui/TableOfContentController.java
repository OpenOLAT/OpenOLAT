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
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.SectionRefImpl;
import org.olat.modules.portfolio.ui.event.SectionSelectionEvent;
import org.olat.modules.portfolio.ui.renderer.PortfolioRendererHelper;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableOfContentController extends BasicController implements TooledController, Activateable2 {
	
	private Link newSectionTool, newSectionButton, newEntryLink, editBinderMetadataLink;
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private UserCommentsController commentsCtrl;
	private SectionEditController newSectionCtrl;
	private SectionEditController editSectionCtrl;
	private SectionDatesEditController editSectionDatesCtrl;
	private BinderMetadataEditController binderMetadataCtrl;
	private DialogBoxController confirmCloseSectionCtrl, confirmReopenSectionCtrl;
	
	private PageRunController pageCtrl;
	private PageMetadataEditController newPageCtrl;
	
	private int counter = 0;
	private Binder binder;
	private final List<Identity> owners;
	private final BinderConfiguration config;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;

	public TableOfContentController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl);
		this.binder = binder;
		this.config = config;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("table_of_contents");

		owners = portfolioService.getMembers(binder, PortfolioRoles.owner.name());
		StringBuilder ownerSb = new StringBuilder();
		for(Identity owner:owners) {
			if(ownerSb.length() > 0) ownerSb.append(", ");
			ownerSb.append(userManager.getUserDisplayName(owner));
		}
		mainVC.contextPut("owners", ownerSb.toString());

		putInitialPanel(mainVC);
		loadModel();
	}
	
	@Override
	public void initTools() {
		if(secCallback.canEditMetadataBinder()) {
			editBinderMetadataLink = LinkFactory.createToolLink("edit.binder.metadata", translate("edit.binder.metadata"), this);
			editBinderMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(editBinderMetadataLink, Align.left);
		}
		
		if(secCallback.canAddSection()) {
			newSectionTool = LinkFactory.createToolLink("new.section", translate("create.new.section"), this);
			newSectionTool.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(newSectionTool, Align.right);
		
			newSectionButton = LinkFactory.createButton("create.new.section", mainVC, this);
			newSectionButton.setCustomEnabledLinkCSS("btn btn-primary");
		}
		
		if(secCallback.canAddPage(null)) {
			newEntryLink = LinkFactory.createToolLink("new.page", translate("create.new.page"), this);
			newEntryLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(newEntryLink, Align.right);
		}
	}
	
	protected void loadModel() {
		mainVC.contextPut("binderTitle", StringHelper.escapeHtml(binder.getTitle()));
		
		List<SectionRow> sectionList = new ArrayList<>();
		Map<Long,SectionRow> sectionMap = new HashMap<>();
		
		List<AssessmentSection> assessmentSections = portfolioService.getAssessmentSections(binder, getIdentity());
		Map<Long,Long> numberOfCommentsMap = portfolioService.getNumberOfComments(binder);
		
		Map<Section,AssessmentSection> sectionToAssessmentSectionMap = new HashMap<>();
		for(AssessmentSection assessmentSection:assessmentSections) {
			sectionToAssessmentSectionMap.put(assessmentSection.getSection(), assessmentSection);
		}
		
		//assignments
		List<Assignment> assignments = portfolioService.getAssignments(binder);
		Map<Section,List<Assignment>> sectionToAssignmentMap = new HashMap<>();
		for(Assignment assignment:assignments) {
			List<Assignment> assignmentList;
			if(sectionToAssignmentMap.containsKey(assignment.getSection())) {
				assignmentList = sectionToAssignmentMap.get(assignment.getSection());
			} else {
				assignmentList = new ArrayList<>();
				sectionToAssignmentMap.put(assignment.getSection(), assignmentList);
			}
			assignmentList.add(assignment);
		}


		List<Section> sections = portfolioService.getSections(binder);
		int count = 0;
		for(Section section:sections) {
			boolean first = count == 0;
			boolean last = count == sections.size() - 1;
			count++;
			SectionRow sectionRow = forgeSectionRow(section,
					sectionToAssessmentSectionMap.get(section),
					sectionToAssignmentMap.get(section),
					first, last);
			sectionList.add(sectionRow);
			sectionMap.put(section.getKey(), sectionRow);
		}

		List<Page> pages = portfolioService.getPages(binder, null);
		for(Page page:pages) {
			if(secCallback.canViewElement(page)) {
				Section section = page.getSection();
				SectionRow sectionRow = sectionMap.get(section.getKey());
				PageRow pageRow = forgePageRow(page, numberOfCommentsMap);
				sectionRow.getPages().add(pageRow);
			}
		}
		mainVC.contextPut("sections", sectionList);
	}
	
	private SectionRow forgeSectionRow(Section section, AssessmentSection assessmentSection, List<Assignment> assignemnts, boolean first, boolean last) {
		String sectionId = "section" + (++counter);
		String title = StringHelper.escapeHtml(section.getTitle());
		
		List<Assignment> notAssignedAssignments = new ArrayList<>();
		if(assignemnts != null) {
			for(Assignment assignemnt:assignemnts) {
				if(assignemnt.getPage() == null) {
					notAssignedAssignments.add(assignemnt);
				}
			}
		}
		
		Link sectionLink = LinkFactory.createCustomLink(sectionId, "open_section", title, Link.LINK | Link.NONTRANSLATED, mainVC, this);
		SectionRow sectionRow = new SectionRow(section, sectionLink, assessmentSection, notAssignedAssignments);
		sectionLink.setUserObject(sectionRow);
		
		Dropdown editDropdown = new Dropdown(sectionId.concat("_dropdown"), null, false, getTranslator());
		editDropdown.setTranslatedLabel("");
		editDropdown.setOrientation(DropdownOrientation.right);
		editDropdown.setIconCSS("o_icon o_icon_actions");
		
		if(secCallback.canCloseSection(section)) {
			if(SectionStatus.isClosed(section)) {
				Link reopenLink = LinkFactory.createLink(sectionId.concat("_ropens"), "reopen.section", "reopen.section", mainVC, this);
				reopenLink.setUserObject(sectionRow);
				editDropdown.addComponent(reopenLink);
			} else {
				Link closeLink = LinkFactory.createLink(sectionId.concat("_closes"), "close.section", "close.section", mainVC, this);
				closeLink.setUserObject(sectionRow);
				editDropdown.addComponent(closeLink);
			}
			
			if(section.getEndDate() != null) {
				Link overrideDatesLink = LinkFactory.createLink(sectionId.concat("_overd"), "override.dates.section", "override.dates.section", mainVC, this);
				overrideDatesLink.setUserObject(sectionRow);
				editDropdown.addComponent(overrideDatesLink);
			}
		}
		
		if(secCallback.canEditSection()) {
			Link editSectionLink = LinkFactory.createLink(sectionId.concat("_edit"), "section.edit", "edit_section", mainVC, this);
			editSectionLink.setIconLeftCSS("o_icon o_icon_edit");
			editSectionLink.setUserObject(sectionRow);
			editDropdown.addComponent(editSectionLink);
			
			Link deleteSectionLink = LinkFactory.createLink(sectionId.concat("_delete"), "section.delete", "delete_section", mainVC, this);
			deleteSectionLink.setIconLeftCSS("o_icon o_icon_delete_item");
			deleteSectionLink.setUserObject(sectionRow);
			editDropdown.addComponent(deleteSectionLink);
			
			Link upSectionLink = LinkFactory.createCustomLink(sectionId.concat("_up"), "up_section", "", Link.LINK | Link.NONTRANSLATED, mainVC, this);
			upSectionLink.setIconLeftCSS("o_icon o_icon o_icon-lg o_icon_move_up");
			upSectionLink.setUserObject(sectionRow);
			upSectionLink.setEnabled(!first);
			sectionRow.setUpSectionLink(upSectionLink);
			
			Link downSectionLink = LinkFactory.createCustomLink(sectionId.concat("_down"), "down_section", "", Link.LINK | Link.NONTRANSLATED, mainVC, this);
			downSectionLink.setIconLeftCSS("o_icon o_icon o_icon-lg o_icon_move_down");
			downSectionLink.setUserObject(sectionRow);
			downSectionLink.setEnabled(!last);
			sectionRow.setDownSectionLink(downSectionLink);
		}
		
		if(editDropdown.getComponents().iterator().hasNext()) {
			mainVC.put(editDropdown.getComponentName(), editDropdown);
			sectionRow.setEditDropdown(editDropdown);
		}
		
		return sectionRow;
	}
	
	private PageRow forgePageRow(Page page, Map<Long,Long> numberOfCommentsMap) {
		PageRow pageRow = new PageRow(page);

		String pageId = "page" + (++counter);
		String title = StringHelper.escapeHtml(page.getTitle());
		Link openLink = LinkFactory.createCustomLink(pageId, "open_page", title, Link.LINK | Link.NONTRANSLATED, mainVC, this);
		openLink.setUserObject(pageRow);
		pageRow.setOpenLink(openLink);

		Long numOfComments = numberOfCommentsMap.get(page.getKey());
		if(numOfComments != null && numOfComments.longValue() > 0) {
			Link commentLink = LinkFactory.createCustomLink("com_" + (++counter), "comments", "(" + numOfComments + ")", Link.LINK | Link.NONTRANSLATED, mainVC, this);
			commentLink.setDomReplacementWrapperRequired(false);
			commentLink.setIconLeftCSS("o_icon o_icon-fw o_icon_comments");
			commentLink.setUserObject(pageRow);
			pageRow.setCommentLink(commentLink);
		}
		
		return pageRow;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Page".equalsIgnoreCase(resName) || "Entry".equalsIgnoreCase(resName)) {
			Long pageKey = entries.get(0).getOLATResourceable().getResourceableId();
			Page page = portfolioService.getPageByKey(pageKey);
			if(page != null && page.getSection() != null && binder.equals(page.getSection().getBinder())) {
				Activateable2 activateable = doOpenPage(ureq, page);
				if(activateable != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					activateable.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		} else if("Section".equalsIgnoreCase(resName)) {
			Long sectionKey = entries.get(0).getOLATResourceable().getResourceableId();
			Section section = portfolioService.getSection(new SectionRefImpl(sectionKey));
			if(section != null && binder.equals(section.getBinder())) {
				doOpenSection(ureq, section);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newSectionCtrl == source || editSectionCtrl == source 
				|| editSectionDatesCtrl == source || newPageCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(binderMetadataCtrl == source) {
			if(event == Event.DONE_EVENT) {
				binder = binderMetadataCtrl.getBinder();
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmCloseSectionCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				SectionRow row = (SectionRow)confirmCloseSectionCtrl.getUserObject();
				doClose(row);
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(confirmReopenSectionCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				SectionRow row = (SectionRow)confirmReopenSectionCtrl.getUserObject();
				doReopen(row);
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}	
		} else if(commentsCtrl == source) {
			if("comment_count_changed".equals(event.getCommand())) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editSectionDatesCtrl);
		removeAsListenerAndDispose(binderMetadataCtrl);
		removeAsListenerAndDispose(editSectionCtrl);
		removeAsListenerAndDispose(newSectionCtrl);
		removeAsListenerAndDispose(commentsCtrl);
		removeAsListenerAndDispose(newPageCtrl);
		removeAsListenerAndDispose(cmc);
		editSectionDatesCtrl = null;
		binderMetadataCtrl = null;
		editSectionCtrl = null;
		newSectionCtrl = null;
		commentsCtrl = null;
		newPageCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(newSectionTool == source || newSectionButton == source) {
			doCreateNewSection(ureq);
		} else if(newEntryLink == source) {
			doCreateNewEntry(ureq);
		} else if(editBinderMetadataLink == source) {
			doEditBinderMetadata(ureq);
		} else if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("open_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doOpenSection(ureq, row.getSection());
			} else if("edit_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doEditSection(ureq, row); 
			} else if("open_page".equals(cmd)) {
				PageRow row = (PageRow)link.getUserObject();
				doOpenPage(ureq, row.getPage());
			} else if("reopen.section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doConfirmReopenSection(ureq, row);
			} else if("close.section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doConfirmCloseSection(ureq, row);
			} else if("override.dates.section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doOverrideDatesSection(ureq, row);
			} else if("comments".equals(cmd)) {
				PageRow row = (PageRow)link.getUserObject();
				doOpenComments(ureq, row);
			} else if("up_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doMoveSectionUp(row);
			} else if("down_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doMoveSectionDown(row);
			}
		}
	}
	
	private void doMoveSectionUp(SectionRow sectionRow) {
		binder = portfolioService.moveUpSection(binder, sectionRow.getSection());
		loadModel();
	}
	
	private void doMoveSectionDown(SectionRow sectionRow) {
		binder = portfolioService.moveDownSection(binder, sectionRow.getSection());
		loadModel();
	}
	
	private void doOpenComments(UserRequest ureq, PageRow pageRow) {
		CommentAndRatingSecurityCallback commentSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, pageRow.getKey());
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), ores, null, commentSecCallback);
		listenTo(commentsCtrl);
		
		String title = translate("comment.title");
		cmc = new CloseableModalController(getWindowControl(), null, commentsCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenSection(UserRequest ureq, Section section) {
		fireEvent(ureq, new SectionSelectionEvent(section));
	}
	
	private void doEditSection(UserRequest ureq, SectionRow sectionRow) {
		if(editSectionCtrl != null) return;
		
		editSectionCtrl = new SectionEditController(ureq, getWindowControl(), sectionRow.getSection(), secCallback);
		editSectionCtrl.setUserObject(sectionRow);
		listenTo(editSectionCtrl);
		
		String title = translate("section.edit");
		cmc = new CloseableModalController(getWindowControl(), null, editSectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateNewSection(UserRequest ureq) {
		if(newSectionCtrl != null) return;
		
		newSectionCtrl = new SectionEditController(ureq, getWindowControl(), binder, secCallback);
		listenTo(newSectionCtrl);
		
		String title = translate("create.new.section");
		cmc = new CloseableModalController(getWindowControl(), null, newSectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private PageRunController doOpenPage(UserRequest ureq, Page page) {
		removeAsListenerAndDispose(pageCtrl);

		OLATResourceable pageOres = OresHelper.createOLATResourceableInstance("Entry", page.getKey());
		WindowControl swControl = addToHistory(ureq, pageOres, null);
		Page reloadedPage = portfolioService.getPageByKey(page.getKey());
		pageCtrl = new PageRunController(ureq, swControl, stackPanel, secCallback, reloadedPage);
		listenTo(pageCtrl);
		stackPanel.pushController(page.getTitle(), pageCtrl);
		return pageCtrl;
	}
	
	private void doCreateNewEntry(UserRequest ureq) {
		if(newPageCtrl != null) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), binder, false, null, true);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditBinderMetadata(UserRequest ureq) {
		if(binderMetadataCtrl != null) return;
		
		Binder reloadedBinder = portfolioService.getBinderByKey(binder.getKey());
		binderMetadataCtrl = new BinderMetadataEditController(ureq, getWindowControl(), reloadedBinder);
		listenTo(binderMetadataCtrl);
		
		String title = translate("edit.binder.metadata");
		cmc = new CloseableModalController(getWindowControl(), null, binderMetadataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOverrideDatesSection(UserRequest ureq, SectionRow sectionRow) {
		if(editSectionDatesCtrl != null) return;
		
		editSectionDatesCtrl = new SectionDatesEditController(ureq, getWindowControl(), sectionRow.getSection());
		editSectionDatesCtrl.setUserObject(sectionRow);
		listenTo(editSectionDatesCtrl);
		
		String title = translate("override.dates.section");
		cmc = new CloseableModalController(getWindowControl(), null, editSectionDatesCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmCloseSection(UserRequest ureq, SectionRow row) {
		String title = translate("close.section.confirm.title");
		String text = translate("close.section.confirm.descr", new String[]{ row.getTitle() });
		confirmCloseSectionCtrl = activateYesNoDialog(ureq, title, text, confirmCloseSectionCtrl);
		confirmCloseSectionCtrl.setUserObject(row);
	}
	
	private void doClose(SectionRow row) {
		Section section = row.getSection();
		section = portfolioService.changeSectionStatus(section, SectionStatus.closed, getIdentity());
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_SECTION_CLOSE, getClass(),
				LoggingResourceable.wrap(section));
	}
	
	private void doConfirmReopenSection(UserRequest ureq, SectionRow row) {
		String title = translate("reopen.section.confirm.title");
		String text = translate("reopen.section.confirm.descr", new String[]{ row.getTitle() });
		confirmReopenSectionCtrl = activateYesNoDialog(ureq, title, text, confirmReopenSectionCtrl);
		confirmReopenSectionCtrl.setUserObject(row);
	}
	
	private void doReopen(SectionRow row) {
		Section section = row.getSection();
		section = portfolioService.changeSectionStatus(section, SectionStatus.inProgress, getIdentity());
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_SECTION_REOPEN, getClass(),
				LoggingResourceable.wrap(section));
	}
	
	public class PageRow {
		
		private final Page page;
		private Link openLink;
		private Link commentLink;

		public PageRow(Page page) {
			this.page = page;
		}
		
		public Long getKey() {
			return page.getKey();
		}
		
		public Page getPage() {
			return page;
		}
		
		public String getCssClassStatus() {
			return page.getPageStatus() == null
					? PageStatus.draft.cssClass() : page.getPageStatus().cssClass();
		}

		public Link getOpenLink() {
			return openLink;
		}

		public void setOpenLink(Link openLink) {
			this.openLink = openLink;
		}

		public Link getCommentLink() {
			return commentLink;
		}

		public void setCommentLink(Link commentLink) {
			this.commentLink = commentLink;
		}
	}
	
	public class SectionRow {
		
		private final Section section;
		private final Link sectionLink;
		private Link upSectionLink, downSectionLink;
		private Dropdown editDropdown;
		private final List<PageRow> pages = new ArrayList<>();
		private final List<Assignment> assignments;
		
		private AssessmentSection assessmentSection;
		
		public SectionRow(Section section, Link sectionLink, AssessmentSection assessmentSection, List<Assignment> assignments) {
			this.section = section;
			this.sectionLink = sectionLink;
			this.assessmentSection = assessmentSection;
			this.assignments = assignments;
		}

		public String getTitle() {
			return section.getTitle();
		}
		
		public String getCssClassStatus() {
			return section.getSectionStatus() == null
					? SectionStatus.notStarted.cssClass() : section.getSectionStatus().cssClass();
		}
		
		public boolean isAssessable() {
			return config.isAssessable();
		}
		
		/**
		 * It use the same format as the cell renderer.
		 * @return
		 */
		public String getFormattedResult() {
			if(config.isAssessable()) {
				return PortfolioRendererHelper.getFormattedResult(assessmentSection, getTranslator());
			}
			return "";
		}
		
		public Section getSection() {
			return section;
		}
		
		public List<Assignment> getAssignments() {
			return assignments;
		}

		public List<PageRow> getPages() {
			return pages;
		}

		public void setEditDropdown(Dropdown editDropdown) {
			this.editDropdown = editDropdown;
		}
		
		public boolean hasEditDropdown() {
			return editDropdown != null;
		}
		
		public Dropdown getEditDropdown() {
			return editDropdown;
		}
		
		public Link getSectionLink() {
			return sectionLink;
		}

		public Link getUpSectionLink() {
			return upSectionLink;
		}

		public void setUpSectionLink(Link upSectionLink) {
			this.upSectionLink = upSectionLink;
		}

		public Link getDownSectionLink() {
			return downSectionLink;
		}

		public void setDownSectionLink(Link downSectionLink) {
			this.downSectionLink = downSectionLink;
		}
		
		
	}
}
