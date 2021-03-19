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

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ReadOnlyCommentsSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.spacesaver.ToggleBoxController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.control.winmgr.ScrollTopCommand;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserInformations;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.model.ExtendedMediaRenderingHints;
import org.olat.modules.portfolio.model.SectionRefImpl;
import org.olat.modules.portfolio.ui.event.ClosePageEvent;
import org.olat.modules.portfolio.ui.event.DeleteBinderEvent;
import org.olat.modules.portfolio.ui.event.PageDeletedEvent;
import org.olat.modules.portfolio.ui.event.PageRemovedEvent;
import org.olat.modules.portfolio.ui.event.PageSelectionEvent;
import org.olat.modules.portfolio.ui.event.RestoreBinderEvent;
import org.olat.modules.portfolio.ui.event.SectionSelectionEvent;
import org.olat.modules.portfolio.ui.event.SelectPageEvent;
import org.olat.modules.portfolio.ui.export.ExportBinderAsCPResource;
import org.olat.modules.portfolio.ui.export.ExportBinderAsPDFResource;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;
import org.olat.modules.portfolio.ui.renderer.PortfolioRendererHelper;
import org.olat.modules.portfolio.ui.renderer.SharedPageStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
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
	
	private Link printLink;
	private Link newEntryLink;
	private Link newSectionTool;
	private Link deleteBinderLink;
	private Link newSectionButton;
	private Link restoreBinderLink;
	private Link newAssignmentLink;
	private Link toReferenceEntryLink;
	private Link editBinderMetadataLink;
	private Link moveToTrashBinderLink;
	private Link exportBinderAsCpLink;
	private Link exportBinderAsPdfLink;
	private Link importExistingEntryLink;
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private StepsMainRunController wizardCtrl;
	private TextComponent summaryComp;
	private ToggleBoxController summaryCtrl;
	private UserCommentsController commentsCtrl;
	private SectionEditController newSectionCtrl;
	private SectionEditController editSectionCtrl;
	private AssignmentEditController newAssignmentCtrl;
	private SelectPageListController selectPageListCtrl;
	private DialogBoxController confirmCloseSectionCtrl;
	private DialogBoxController confirmReopenSectionCtrl;
	private DialogBoxController confirmDeleteSectionCtrl;
	private DialogBoxController confirmRestoreBinderCtrl;
	private ConfirmDeleteBinderController deleteBinderCtrl;
	private SectionDatesEditController editSectionDatesCtrl;
	private BinderMetadataEditController binderMetadataCtrl;
	private ConfirmMoveBinderToTrashController moveBinderToTrashCtrl;
	
	private PageRunController pageCtrl;
	private PageMetadataEditController newPageCtrl;
	
	private int counter = 0;
	private Binder binder;
	private final List<Identity> owners;
	private List<SectionRow> sectionList;
	private final BinderConfiguration config;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioV2Module portfolioV2Module;

	public TableOfContentController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl);
		this.binder = binder;
		this.config = config;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		
		stackPanel.addListener(this);
		mainVC = createVelocityContainer("table_of_contents");

		owners = portfolioService.getMembers(binder, PortfolioRoles.owner.name());
		StringBuilder ownerSb = new StringBuilder();
		for(Identity owner:owners) {
			if(ownerSb.length() > 0) ownerSb.append(", ");
			ownerSb.append(userManager.getUserDisplayName(owner));
		}
		mainVC.contextPut("owners", ownerSb.toString());

		mainVC.contextPut("isTemplate", secCallback.canNewAssignment());
		mainVC.contextPut("isPersonalBinder", (!secCallback.canNewAssignment() && secCallback.canEditMetadataBinder()));

		RepositoryEntry repoEntry = binder.getEntry();
		if (repoEntry != null) {
			String repoEntryPath = "[RepositoryEntry:" + repoEntry.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(repoEntryPath);
			String name = translate("binder.entry.name") + ": " + StringHelper.escapeHtml(repoEntry.getDisplayname());
			toReferenceEntryLink = LinkFactory.createLink("to.ref.entry", "to.ref.entry", "to.ref.rentry", name, getTranslator(), mainVC, this, Link.NONTRANSLATED);
			toReferenceEntryLink.setDomReplacementWrapperRequired(false);
			toReferenceEntryLink.setIconLeftCSS("o_icon o_CourseModule_icon");
			toReferenceEntryLink.setElementCssClass("small");
			toReferenceEntryLink.setUrl(url);
		}
		
		summaryComp = TextFactory.createTextComponentFromString("summaryCmp" + CodeHelper.getRAMUniqueID(), "", null,
				false, null);
		summaryCtrl = new ToggleBoxController(ureq, wControl, getGuiPrefsKey(binder), translate("summary.open"),
				translate("summary.close"), summaryComp);
		
		putInitialPanel(mainVC);
		loadModel();
	}
	
	void updateSummaryView(UserRequest ureq) {
		if(summaryCtrl != null) {
			summaryCtrl.reload(ureq);
		}
	}
	
	private String getGuiPrefsKey(OLATResourceable binderOres) {
		return new StringBuilder()
				.append(binderOres.getResourceableTypeName())
				.append("::")
				.append(binderOres.getResourceableId())
				.toString();
	}
	
	public int getNumOfSections() {
		return sectionList == null ? 0 : sectionList.size();
	}
	
	@Override
	public void initTools() {
		if(secCallback.canEditMetadataBinder()) {
			editBinderMetadataLink = LinkFactory.createToolLink("edit.binder.metadata", translate("edit.binder.metadata"), this);
			editBinderMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_new_portfolio");
			stackPanel.addTool(editBinderMetadataLink, Align.left);
		}
		
		if(secCallback.canMoveToTrashBinder(binder)) {
			moveToTrashBinderLink = LinkFactory.createToolLink("delete.binder", translate("delete.binder"), this);
			moveToTrashBinderLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_delete_item");
			stackPanel.addTool(moveToTrashBinderLink, Align.left);
		}
		
		if(secCallback.canExportBinder()) {
			Dropdown exportTools = new Dropdown("export.binder", "export.binder", false, getTranslator());
			exportTools.setElementCssClass("o_sel_pf_export_tools");
			exportTools.setIconCSS("o_icon o_icon_download o_icon-fw ");
			stackPanel.addTool(exportTools, Align.left);

			exportBinderAsCpLink = LinkFactory.createToolLink("export.binder.cp", translate("export.binder.cp"), this);
			exportBinderAsCpLink.setIconLeftCSS("o_icon o_icon_download o_icon-fw ");
			exportTools.addComponent(exportBinderAsCpLink);
			
			if(pdfModule.isEnabled()) {
				exportBinderAsPdfLink = LinkFactory.createToolLink("export.binder.pdf", translate("export.binder.pdf"), this);
				exportBinderAsPdfLink.setIconLeftCSS("o_icon o_filetype_pdf o_icon-fw ");
				exportTools.addComponent(exportBinderAsPdfLink);
			}
			
			printLink = LinkFactory.createToolLink("export.binder.onepage", translate("export.binder.onepage"), this);
			printLink.setIconLeftCSS("o_icon o_icon_print o_icon-fw ");
			printLink.setPopup(new LinkPopupSettings(950, 750, "binder"));
			exportTools.addComponent(printLink);
		}
		
		if(secCallback.canDeleteBinder(binder)) {
			deleteBinderLink = LinkFactory.createToolLink("delete.binder", translate("delete.binder"), this);
			deleteBinderLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_delete_item");
			stackPanel.addTool(deleteBinderLink, Align.left);
			
			restoreBinderLink = LinkFactory.createToolLink("restore.binder", translate("restore.binder"), this);
			restoreBinderLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_restore");
			stackPanel.addTool(restoreBinderLink, Align.left);
		}
		
		if(secCallback.canAddSection()) {
			newSectionTool = LinkFactory.createToolLink("new.section", translate("create.new.section"), this);
			newSectionTool.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_new_portfolio");
			newSectionTool.setElementCssClass("o_sel_pf_new_section");
			stackPanel.addTool(newSectionTool, Align.right);
		}
		
		if(secCallback.canAddPage(null) || secCallback.canInstantianteBinderAssignment()) {
			newEntryLink = LinkFactory.createToolLink("new.page", translate("create.new.page"), this);
			newEntryLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_new_portfolio");
			newEntryLink.setElementCssClass("o_sel_pf_new_entry");
			newEntryLink.setVisible(sectionList != null && !sectionList.isEmpty());
			stackPanel.addTool(newEntryLink, Align.right);
		}
		
		if(secCallback.canAddPage(null)) {
			importExistingEntryLink = LinkFactory.createToolLink("import.page", translate("import.page"), this);
			importExistingEntryLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_import");
			importExistingEntryLink.setElementCssClass("o_sel_pf_existing_entry");
			importExistingEntryLink.setVisible(sectionList != null && !sectionList.isEmpty());
			stackPanel.addTool(importExistingEntryLink, Align.right);
		}
		
		
		if(secCallback.canNewAssignment()) {
			newAssignmentLink = LinkFactory.createToolLink("new.assignment", translate("create.new.assignment"), this);
			newAssignmentLink.setIconLeftCSS("o_icon o_icon-lg o_icon-fw o_icon_new_portfolio");
			newAssignmentLink.setElementCssClass("o_sel_pf_new_assignment");
			newAssignmentLink.setVisible(sectionList != null && !sectionList.isEmpty());
			stackPanel.addTool(newAssignmentLink, Align.right);
		}
	}
	
	protected void loadModel() {
		mainVC.contextPut("binderTitle", StringHelper.escapeHtml(binder.getTitle()));

		if (StringHelper.containsNonWhitespace(binder.getSummary())) {
			summaryComp.setText(StringHelper.xssScan(binder.getSummary()));
			mainVC.put("summary", summaryCtrl.getInitialComponent());
		} else {
			mainVC.remove("summary");
		}
		
		List<SectionRow> sectionRows = new ArrayList<>();
		Map<Long,SectionRow> sectionMap = new HashMap<>();
		
		List<AssessmentSection> assessmentSections = portfolioService.getAssessmentSections(binder, getIdentity());
		
		
		Map<Section,AssessmentSection> sectionToAssessmentSectionMap = new HashMap<>();
		for(AssessmentSection assessmentSection:assessmentSections) {
			sectionToAssessmentSectionMap.put(assessmentSection.getSection(), assessmentSection);
		}
		
		//assignments
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
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
			
			if(secCallback.canViewElement(section)) {
				SectionRow sectionRow = forgeSectionRow(section,
						sectionToAssessmentSectionMap.get(section),
						sectionToAssignmentMap.get(section),
						first, last);
				sectionRows.add(sectionRow);
				sectionMap.put(section.getKey(), sectionRow);
			}
		}
		loadPagesModel(sectionMap);
		
		mainVC.contextPut("sections", sectionRows);
		sectionList = sectionRows;
		
		if(secCallback.canAddSection()) {
			if(newSectionButton == null) {
				newSectionButton = LinkFactory.createButton("create.new.section", mainVC, this);
				newSectionButton.setCustomEnabledLinkCSS("btn btn-primary");
			}
			mainVC.put("create.new.section", newSectionButton);
		}
		
		boolean hasSection = (sectionList != null && !sectionList.isEmpty());
		if(newEntryLink != null && newEntryLink.isVisible() != hasSection) {
			newEntryLink.setVisible(hasSection);
			stackPanel.setDirty(true);
		}
		if(importExistingEntryLink != null && importExistingEntryLink.isVisible() != hasSection) {
			importExistingEntryLink.setVisible(hasSection);
			stackPanel.setDirty(true);
		}
		
		if(newAssignmentLink != null && newAssignmentLink.isVisible() != hasSection) {
			newAssignmentLink.setVisible(hasSection);
			stackPanel.setDirty(true);
		}
	}
	
	private void loadPagesModel(Map<Long,SectionRow> sectionMap) {
		boolean showUserInfos = secCallback.canPageUserInfosStatus();
		mainVC.contextPut("userInfos", Boolean.valueOf(showUserInfos));
		Map<Long,PageUserInformations> userInfosToPages = new HashMap<>();
		if(showUserInfos) {
			List<PageUserInformations> userInfos = portfolioService.getPageUserInfos(binder, getIdentity());
			for(PageUserInformations userInfo:userInfos) {
				userInfosToPages.put(userInfo.getPage().getKey(), userInfo);
			}
			mainVC.contextPut("userInfosRenderer", new SharedPageStatusCellRenderer(getTranslator()));
		}
		
		Map<Long,Long> numberOfCommentsMap = portfolioService.getNumberOfComments(binder);
		List<Page> pages = portfolioService.getPages(binder, null);
		for(Page page:pages) {
			Section section = page.getSection();
			if(section != null && sectionMap.containsKey(section.getKey())) {
				boolean viewElement = secCallback.canViewElement(page);
				boolean viewTitleElement = secCallback.canViewTitleOfElement(page);
				if(viewElement || viewTitleElement) {
					SectionRow sectionRow = sectionMap.get(section.getKey());
					PageRow pageRow = forgePageRow(page, numberOfCommentsMap, viewElement);
					sectionRow.getPages().add(pageRow);
					if(showUserInfos) {
						PageUserInformations userInfos = userInfosToPages.get(pageRow.getPage().getKey());
						if(userInfos != null) {
							pageRow.setUserInfosStatus(userInfos.getStatus());
						}
					}
				}
			}
		}
	}
	
	private SectionRow forgeSectionRow(Section section, AssessmentSection assessmentSection, List<Assignment> assignemnts, boolean first, boolean last) {
		String sectionId = "section" + (++counter);
		String title = StringHelper.escapeHtml(section.getTitle());
		
		List<Assignment> notAssignedAssignments = new ArrayList<>();
		if(secCallback.canViewPendingAssignments(section)) {
			if(assignemnts != null) {
				for(Assignment assignemnt:assignemnts) {
					if(assignemnt.getPage() == null) {
						notAssignedAssignments.add(assignemnt);
					}
				}
			}
		}
		
		Link sectionLink = LinkFactory.createCustomLink(sectionId, "open_section", title, Link.LINK | Link.NONTRANSLATED, mainVC, this);
		sectionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pf_section");
		SectionRow sectionRow = new SectionRow(section, sectionLink, assessmentSection, notAssignedAssignments);
		sectionLink.setUserObject(sectionRow);
		
		Dropdown editDropdown = new Dropdown(sectionId.concat("_dropdown"), null, false, getTranslator());
		editDropdown.setElementCssClass("o_sel_pf_section_tools");
		editDropdown.setOrientation(DropdownOrientation.right);
		editDropdown.setIconCSS("o_icon o_icon-fw o_icon-lg o_icon_actions");
		
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
			if(secCallback.canAddPage(section)) {
				Link createInSectionLink = LinkFactory.createLink(sectionId.concat("_create"), "create.new.page", "create_in_section", mainVC, this);
				createInSectionLink.setIconLeftCSS("o_icon o_icon_fw o_icon_new_portfolio");
				createInSectionLink.setUserObject(sectionRow);
				editDropdown.addComponent(createInSectionLink);
				
				Link importToSectionLink = LinkFactory.createLink(sectionId.concat("_import"), "import.page", "import_to_section", mainVC, this);
				importToSectionLink.setIconLeftCSS("o_icon o_icon_fw o_icon_import");
				importToSectionLink.setUserObject(sectionRow);
				editDropdown.addComponent(importToSectionLink);
			}
			
			Link editSectionLink = LinkFactory.createLink(sectionId.concat("_edit"), "section.edit", "edit_section", mainVC, this);
			editSectionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			editSectionLink.setUserObject(sectionRow);
			editDropdown.addComponent(editSectionLink);
			
			Link deleteSectionLink = LinkFactory.createLink(sectionId.concat("_delete"), "section.delete", "delete_section", mainVC, this);
			deleteSectionLink.setElementCssClass("o_sel_pf_delete_section");
			deleteSectionLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			deleteSectionLink.setUserObject(sectionRow);
			editDropdown.addComponent(deleteSectionLink);
			
			Link upSectionLink = LinkFactory.createCustomLink(sectionId.concat("_up"), "up_section", "", Link.LINK | Link.NONTRANSLATED, mainVC, this);
			upSectionLink.setIconLeftCSS("o_icon o_icon o_icon-fw o_icon_move_up");
			upSectionLink.setUserObject(sectionRow);
			upSectionLink.setEnabled(!first);
			upSectionLink.setTitle(translate("move.up"));
			sectionRow.setUpSectionLink(upSectionLink);
			
			Link downSectionLink = LinkFactory.createCustomLink(sectionId.concat("_down"), "down_section", "", Link.LINK | Link.NONTRANSLATED, mainVC, this);
			downSectionLink.setIconLeftCSS("o_icon o_icon o_icon-fw o_icon_move_down");
			downSectionLink.setUserObject(sectionRow);
			downSectionLink.setEnabled(!last);
			downSectionLink.setTitle(translate("move.down"));
			sectionRow.setDownSectionLink(downSectionLink);
		}
		
		if(editDropdown.getComponents().iterator().hasNext()) {
			mainVC.put(editDropdown.getComponentName(), editDropdown);
			sectionRow.setEditDropdown(editDropdown);
		}
		
		return sectionRow;
	}
	
	private PageRow forgePageRow(Page page, Map<Long,Long> numberOfCommentsMap, boolean selectElement) {
		PageRow pageRow = new PageRow(page);

		String pageId = "page" + (++counter);
		String title = StringHelper.escapeHtml(page.getTitle());
		Link openLink = LinkFactory.createCustomLink(pageId, "open_page", title, Link.LINK | Link.NONTRANSLATED, mainVC, this);
		openLink.setElementCssClass("o_pf_open_entry");
		openLink.setUserObject(pageRow);
		openLink.setEnabled(selectElement);
		openLink.setIconLeftCSS("o_icon o_icon_fw o_icon_pf_page");
		pageRow.setOpenLink(openLink);

		Long numOfComments = numberOfCommentsMap.get(page.getKey());
		if(portfolioV2Module.isOverviewCommentsEnabled() && numOfComments != null && numOfComments.longValue() > 0) {
			Link commentLink = LinkFactory.createCustomLink("com_" + (++counter), "comments", "(" + numOfComments + ")", Link.LINK | Link.NONTRANSLATED, mainVC, this);
			commentLink.setDomReplacementWrapperRequired(false);
			commentLink.setIconLeftCSS("o_icon o_icon-fw o_icon_comments");
			commentLink.setElementCssClass("o_comment");
			commentLink.setUserObject(pageRow);
			pageRow.setCommentLink(commentLink);
		}
		
		return pageRow;
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		removeAsListenerAndDispose(summaryCtrl);
		summaryCtrl = null;
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
				|| editSectionDatesCtrl == source || newPageCtrl == source
				|| newAssignmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(pageCtrl == source) {
			if(event == Event.CHANGED_EVENT || event instanceof ClosePageEvent) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof PageRemovedEvent || event instanceof PageDeletedEvent) {
				stackPanel.popController(pageCtrl);
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof SelectPageEvent) {
				SelectPageEvent spe = (SelectPageEvent)event;
				if(SelectPageEvent.NEXT_PAGE.equals(spe.getCommand())) {
					doNextPage(ureq, pageCtrl.getPage());
				} else if(SelectPageEvent.PREVIOUS_PAGE.equals(spe.getCommand())) {
					doPreviousPage(ureq, pageCtrl.getPage());
				} else if(SelectPageEvent.ALL_PAGES.equals(spe.getCommand())) {
					doAllPages();
				}
			}
		} else if(binderMetadataCtrl == source) {
			if(event == Event.DONE_EVENT) {
				binder = binderMetadataCtrl.getBinder();
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
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
		} else if(confirmDeleteSectionCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				SectionRow row = (SectionRow)confirmDeleteSectionCtrl.getUserObject();
				doDelete(row);
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}	
		} else if(moveBinderToTrashCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doMoveBinderToTrash();
				fireEvent(ureq, new DeleteBinderEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(deleteBinderCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doDeleteBinder();
				fireEvent(ureq, new DeleteBinderEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmRestoreBinderCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doRestore();
				loadModel();
				fireEvent(ureq, new RestoreBinderEvent());
			}	
		} else if(commentsCtrl == source) {
			if("comment_count_changed".equals(event.getCommand())) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(selectPageListCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event instanceof PageSelectionEvent) {
				PageSelectionEvent pageSelectionEvent = (PageSelectionEvent) event;
				doCreateNewEntryFrom(ureq, pageSelectionEvent.getPage(), pageSelectionEvent.getSection());
			}
		} else if (wizardCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
                // Reload data
				loadModel();
				
				// Close the dialog
                getWindowControl().pop();

                // Remove steps controller
                cleanUp();
            }
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(moveBinderToTrashCtrl);
		removeAsListenerAndDispose(editSectionDatesCtrl);
		removeAsListenerAndDispose(binderMetadataCtrl);
		removeAsListenerAndDispose(selectPageListCtrl);
		removeAsListenerAndDispose(newAssignmentCtrl);
		removeAsListenerAndDispose(deleteBinderCtrl);
		removeAsListenerAndDispose(editSectionCtrl);
		removeAsListenerAndDispose(newSectionCtrl);
		removeAsListenerAndDispose(commentsCtrl);
		removeAsListenerAndDispose(newPageCtrl);
		removeAsListenerAndDispose(wizardCtrl);
		removeAsListenerAndDispose(cmc);
		moveBinderToTrashCtrl = null;
		editSectionDatesCtrl = null;
		binderMetadataCtrl = null;
		selectPageListCtrl = null;
		newAssignmentCtrl = null;
		deleteBinderCtrl = null;
		editSectionCtrl = null;
		newSectionCtrl = null;
		commentsCtrl = null;
		newPageCtrl = null;
		wizardCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(newSectionTool == source || newSectionButton == source) {
			doCreateNewSection(ureq);
		} else if(newEntryLink == source) {
			doCreateNewEntry(ureq, null);
		} else if(importExistingEntryLink == source) {
			doImportExistingEntry(ureq, null);
		} else if(newAssignmentLink == source) {
			doCreateNewAssignment(ureq);
		} else if(editBinderMetadataLink == source) {
			doEditBinderMetadata(ureq);
		} else if(moveToTrashBinderLink == source) {
			doConfirmMoveToTrashBinder(ureq);
		} else if(deleteBinderLink == source) {
			doConfirmDeleteBinder(ureq);
		} else if(restoreBinderLink == source) {
			doConfirmRestore(ureq);
		} else if(exportBinderAsCpLink == source) {
			doExportBinderAsCP(ureq);
		} else if(printLink == source) {
			doPrint(ureq);
		} else if(exportBinderAsPdfLink == source) {
			doExportBinderAsPdf(ureq);
		} else if(toReferenceEntryLink == source) {
			doOpenReferenceEntry(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent && pageCtrl != null && ((PopEvent)event).getController() == pageCtrl && pageCtrl.getSection() != null) {
				stackPanel.popUserObject(new TOCSection(pageCtrl.getSection()));
				addToHistory(ureq, this);
			}
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
			} else if("delete_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doConfirmDeleteSection(ureq, row);
			} else if ("import_to_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doImportExistingEntry(ureq, row.getSection());
			} else if ("create_in_section".equals(cmd)) {
				SectionRow row = (SectionRow)link.getUserObject();
				doCreateNewEntry(ureq, row.getSection());
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
		CommentAndRatingSecurityCallback commentSecCallback;
		if(PageStatus.isClosed(pageRow.getPage())) {
			commentSecCallback = new ReadOnlyCommentsSecurityCallback();
		} else {
			commentSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
		}
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, pageRow.getKey());
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), ores, null, null, commentSecCallback);
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
		if(guardModalController(editSectionCtrl)) return;
		
		editSectionCtrl = new SectionEditController(ureq, getWindowControl(), sectionRow.getSection(), secCallback);
		editSectionCtrl.setUserObject(sectionRow);
		listenTo(editSectionCtrl);
		
		String title = translate("section.edit");
		cmc = new CloseableModalController(getWindowControl(), null, editSectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
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
	
	private void doCreateNewAssignment(UserRequest ureq) {
		if(guardModalController(newAssignmentCtrl)) return;

		newAssignmentCtrl = new AssignmentEditController(ureq, getWindowControl(), binder);
		listenTo(newAssignmentCtrl);
		
		String title = translate("create.new.assignment");
		cmc = new CloseableModalController(getWindowControl(), null, newAssignmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doPreviousPage(UserRequest ureq, Page currentPage) {
		Page selectedPage = currentPage;
		for(SectionRow sectionRow:sectionList) {
			int numOfPages = sectionRow.getPages() == null ? 0 : sectionRow.getPages().size();
			for(int i=0; i<numOfPages; i++) {
				PageRow pageRow = sectionRow.getPages().get(i);
				if(currentPage.equals(pageRow.getPage()) && i > 0) {
					selectedPage = sectionRow.getPages().get(i-1).getPage();
				}
			}
		}

		stackPanel.popController(pageCtrl);
		Page reloadedPage = portfolioService.getPageByKey(selectedPage.getKey());
		doOpenPage(ureq, reloadedPage);
		getWindowControl().getWindowBackOffice().sendCommandTo(new ScrollTopCommand());
	}
	
	protected void doNextPage(UserRequest ureq, Page currentPage) {
		Page selectedPage = currentPage;
		for(SectionRow sectionRow:sectionList) {
			int numOfPages = sectionRow.getPages() == null ? 0 : sectionRow.getPages().size();
			for(int i=0; i<numOfPages; i++) {
				PageRow pageRow = sectionRow.getPages().get(i);
				if(currentPage.equals(pageRow.getPage()) && i+1 < numOfPages) {
					selectedPage = sectionRow.getPages().get(i+1).getPage();
				}
			}
		}

		stackPanel.popController(pageCtrl);
		Page reloadedPage = portfolioService.getPageByKey(selectedPage.getKey());
		doOpenPage(ureq, reloadedPage);
		getWindowControl().getWindowBackOffice().sendCommandTo(new ScrollTopCommand());
	}
	
	protected void doAllPages() {
		stackPanel.popController(pageCtrl);
		getWindowControl().getWindowBackOffice().sendCommandTo(new ScrollTopCommand());
	}
	
	private PageRunController doOpenPage(UserRequest ureq, Page page) {
		removeAsListenerAndDispose(pageCtrl);

		OLATResourceable pageOres = OresHelper.createOLATResourceableInstance("Entry", page.getKey());
		WindowControl swControl = addToHistory(ureq, pageOres, null);
		Page reloadedPage = portfolioService.getPageByKey(page.getKey());
		
		boolean openInEditMode = (secCallback.canEditPage(reloadedPage)
				&& (reloadedPage.getPageStatus() == null || reloadedPage.getPageStatus() == PageStatus.draft || reloadedPage.getPageStatus() == PageStatus.inRevision));
		pageCtrl = new PageRunController(ureq, swControl, stackPanel, secCallback, reloadedPage, openInEditMode);
		listenTo(pageCtrl);
		
		Section section = page.getSection();
		if(section != null) {
			stackPanel.pushController(section.getTitle(), null, new TOCSection(section));
		}
		stackPanel.pushController(page.getTitle(), pageCtrl);
		
		for(SectionRow sectionRow:sectionList) {
			int numOfPages = sectionRow.getPages() == null ? 0 : sectionRow.getPages().size();
			for(int i=0; i<numOfPages; i++) {
				PageRow pageRow = sectionRow.getPages().get(i);
				if(page.equals(pageRow.getPage())) {
					boolean hasPrevious = (i > 0);
					boolean hasNext = (i + 1 < numOfPages);
					pageCtrl.initPaging(hasPrevious, hasNext);
				}
			}
		}
		
		return pageCtrl;
	}
	
	private void doOpenReferenceEntry(UserRequest ureq) {
		String businessPath = "[RepositoryEntry:" + binder.getEntry().getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doCreateNewEntry(UserRequest ureq, Section currentSection) {
		if(guardModalController(newPageCtrl)) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), secCallback, binder, false, currentSection, true, null);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doImportExistingEntry(UserRequest ureq, Section currentSection) {
		/*if(guardModalController(selectPageListCtrl)) return;

		selectPageListCtrl = new SelectPageListController(ureq, getWindowControl(), (TooledStackedPanel)null, currentSection, secCallback, false);
		listenTo(selectPageListCtrl);
		
		String title = translate("select.page");
		cmc = new CloseableModalController(getWindowControl(), null, selectPageListCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();*/
		
		PortfolioImportEntriesContext context = new PortfolioImportEntriesContext();
		context.setBinderSecurityCallback(BinderSecurityCallbackFactory.getCallbackFroImportPages());
		context.setCurrentSection(currentSection);
		context.setCurrentBinder(binder);
		
		SelectPagesStep selectEntriesStep = new SelectPagesStep(ureq, context);
		
		FinishCallback finish = new FinishCallback();
		CancelCallback cancel = new CancelCallback();
				
		wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), selectEntriesStep, finish, cancel, translate("import.entries"), null);
		listenTo(wizardCtrl);
        getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}
	
	private void doCreateNewEntryFrom(UserRequest ureq, Page page, Section currentSection) {
		if(guardModalController(newPageCtrl)) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), secCallback, binder, false, currentSection, true, page);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditBinderMetadata(UserRequest ureq) {
		if(guardModalController(binderMetadataCtrl)) return;
		
		Binder reloadedBinder = portfolioService.getBinderByKey(binder.getKey());
		binderMetadataCtrl = new BinderMetadataEditController(ureq, getWindowControl(), reloadedBinder);
		listenTo(binderMetadataCtrl);
		
		String title = translate("edit.binder.metadata");
		cmc = new CloseableModalController(getWindowControl(), null, binderMetadataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOverrideDatesSection(UserRequest ureq, SectionRow sectionRow) {
		if(guardModalController(editSectionDatesCtrl)) return;
		
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
		String text = translate("close.section.confirm.descr", new String[]{ StringHelper.escapeHtml(row.getTitle()) });
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
		String text = translate("reopen.section.confirm.descr", new String[]{ StringHelper.escapeHtml(row.getTitle()) });
		confirmReopenSectionCtrl = activateYesNoDialog(ureq, title, text, confirmReopenSectionCtrl);
		confirmReopenSectionCtrl.setUserObject(row);
	}
	
	private void doReopen(SectionRow row) {
		Section section = row.getSection();
		section = portfolioService.changeSectionStatus(section, SectionStatus.inProgress, getIdentity());
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_SECTION_REOPEN, getClass(),
				LoggingResourceable.wrap(section));
	}
	
	private void doConfirmDeleteSection(UserRequest ureq, SectionRow row) {
		String title = translate("delete.section.confirm.title");
		String text = translate("delete.section.confirm.descr", new String[]{ StringHelper.escapeHtml(row.getTitle()) });
		confirmDeleteSectionCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteSectionCtrl);
		confirmDeleteSectionCtrl.setUserObject(row);
	}
	
	private void doDelete(SectionRow row) {
		portfolioService.deleteSection(binder, row.getSection());
	}
	
	private void doConfirmMoveToTrashBinder(UserRequest ureq) {
		if(guardModalController(moveBinderToTrashCtrl)) return;
		
		BinderStatistics stats = portfolioService.getBinderStatistics(binder);
		moveBinderToTrashCtrl = new ConfirmMoveBinderToTrashController(ureq, getWindowControl(), stats);
		listenTo(moveBinderToTrashCtrl);
		
		String title = translate("delete.binder");
		cmc = new CloseableModalController(getWindowControl(), null, moveBinderToTrashCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMoveBinderToTrash() {
		binder = portfolioService.getBinderByKey(binder.getKey());
		binder.setBinderStatus(BinderStatus.deleted);
		binder = portfolioService.updateBinder(binder);
		dbInstance.commit();
		showInfo("delete.binder.success");
	}
	
	private void doConfirmRestore(UserRequest ureq) {
		String title = translate("restore.binder.confirm.title");
		String text = translate("restore.binder.confirm.descr", new String[]{ StringHelper.escapeHtml(binder.getTitle()) });
		confirmRestoreBinderCtrl = activateYesNoDialog(ureq, title, text, confirmRestoreBinderCtrl);
	}
	
	private void doRestore() {
		binder = portfolioService.getBinderByKey(binder.getKey());
		binder.setBinderStatus(BinderStatus.open);
		binder = portfolioService.updateBinder(binder);
		dbInstance.commit();
		showInfo("restore.binder.success");
	}
	
	private void doConfirmDeleteBinder(UserRequest ureq) {
		if(guardModalController(moveBinderToTrashCtrl)) return;
		
		BinderStatistics stats = portfolioService.getBinderStatistics(binder);
		deleteBinderCtrl = new ConfirmDeleteBinderController(ureq, getWindowControl(), stats);
		listenTo(deleteBinderCtrl);
		
		String title = translate("delete.binder");
		cmc = new CloseableModalController(getWindowControl(), null, deleteBinderCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteBinder() {
		portfolioService.deleteBinder(binder);
		showInfo("delete.binder.success");
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
			BinderOnePageController printCtrl = new BinderOnePageController(lureq, lwControl, binder, ExtendedMediaRenderingHints.toPrint(), true);
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, printCtrl);
			layoutCtr.addDisposableChildController(printCtrl); // dispose controller on layout dispose
			return layoutCtr;				
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(ctrlCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr);
	}
	
	public class PageRow {
		
		private final Page page;
		private Link openLink;
		private Link commentLink;
		private PageUserStatus userInfosStatus;

		public PageRow(Page page) {
			this.page = page;
		}
		
		public Long getKey() {
			return page.getKey();
		}
		
		public Page getPage() {
			return page;
		}
		
		public String getStatusCss() {
			return page.getPageStatus() == null
					? PageStatus.draft.statusClass() : page.getPageStatus().statusClass();
		}
		
		public String getStatusIconCss() {
			return page.getPageStatus() == null
					? PageStatus.draft.iconClass() : page.getPageStatus().iconClass();
		}

		public String getI18nKeyStatus() {
			return page.getPageStatus() == null
					? PageStatus.draft.i18nKey() : page.getPageStatus().i18nKey();			
		}
		
		public PageUserStatus getUserInfosStatus() {
			return userInfosStatus;
		}

		public void setUserInfosStatus(PageUserStatus userInfosStatus) {
			this.userInfosStatus = userInfosStatus;
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
		private Link upSectionLink;
		private Link downSectionLink;
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
		
		public String getStatusCss() {
			return section.getSectionStatus() == null
					? SectionStatus.notStarted.statusClass() : section.getSectionStatus().statusClass();
		}
		
		public String getStatusIconCss() {
			return section.getSectionStatus() == null
					? SectionStatus.notStarted.iconClass() : section.getSectionStatus().iconClass();
		}
		
		public String getI18nKeyStatus() {
			return section.getSectionStatus() == null
					? SectionStatus.notStarted.i18nKey() : section.getSectionStatus().i18nKey();			
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
	
	private static class TOCSection {
		
		private final Section section;
		
		public TOCSection(Section section) {
			this.section = section;
		}

		@Override
		public int hashCode() {
			return section.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof TOCSection) {
				TOCSection tocs = (TOCSection)obj;
				return section.equals(tocs.section);
			}
			return false;
		}
	}
	
	private class FinishCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
        	// Get context
        	PortfolioImportEntriesContext context = (PortfolioImportEntriesContext) runContext.get(PortfolioImportEntriesContext.CONTEXT_KEY);
        	
        	// Import pages into section
        	for (PortfolioElementRow page : context.getSelectedPortfolioEntries()) {
        		portfolioService.appendNewPage(getIdentity(), page.getTitle(), page.getSummary(), page.getImageUrl(), page.getPage().getImageAlignment(), context.getCurrentSection(), page.getPage());
        	}
        	
            // Fire event
            return StepsMainRunController.DONE_MODIFIED;
        }
    }
    
    private static class CancelCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            return Step.NOSTEP;
        }
    }
}
