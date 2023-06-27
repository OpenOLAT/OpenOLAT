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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.NewControllerFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.modules.ceditor.Assignment;
import org.olat.modules.ceditor.Category;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageImageAlign;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.PageStatus;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.PageUserInformations;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.event.ClosePageEvent;
import org.olat.modules.portfolio.ui.event.DonePageEvent;
import org.olat.modules.portfolio.ui.event.EditPageMetadataEvent;
import org.olat.modules.portfolio.ui.event.PublishEvent;
import org.olat.modules.portfolio.ui.event.ReopenPageEvent;
import org.olat.modules.portfolio.ui.event.RevisionEvent;
import org.olat.modules.portfolio.ui.event.ToggleEditPageEvent;
import org.olat.modules.portfolio.ui.model.UserAssignmentInfos;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageMetadataController extends FormBasicController {
	
	public static final int PICTURE_WIDTH = 970 * 2;	// max width for large images: 1294 * 75% , x2 for high res displays
	public static final int PICTURE_HEIGHT = 300 * 2 ; 	// max size for large images, see CSS, x2 for high res displays

	private FormToggle editLink;
	private FormLink editMetaDataLink;
	private FormLink publishButton;
	private FormLink revisionButton;
	private FormLink closeButton;
	private FormLink reopenButton;
	private FormLink bookmarkButton;
	private FormLink sharedWithLink;
	private SingleSelection statusEl;
	
	private CloseableModalController cmc;
	private SharedWithController sharedWithCtrl;
	private CategoriesEditController categoriesEditCtr;
	private CompetencesEditController competencesEditCtrl;
	private ConfirmClosePageController confirmClosePageCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	private final Page page;
	private PageUserInformations pageUserInfos;
	private final List<Assignment> assignments;
	private final BinderSecurityCallback secCallback;
	
	private final PageSettings pageSettings;
	private final boolean taxonomyLinkingEnabled;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PageService pageService;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	
	public PageMetadataController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback,
			Page page, PageSettings pageSettings, boolean openInEditMode) {
		super(ureq, wControl, "page_meta");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.page = page;
		this.secCallback = secCallback;
		this.pageSettings = pageSettings;
		taxonomyLinkingEnabled = portfolioV2Module.isTaxonomyLinkingReady() && pageSettings.isWithTaxonomy();
		assignments = portfolioService.getSectionsAssignments(page, null);
		if(secCallback.canBookmark() || secCallback.canPageUserInfosStatus()) {
			pageUserInfos = portfolioService.getPageUserInfos(page, getIdentity(), PageUserStatus.inProcess);
		}
		
		initForm(ureq);
		if(openInEditMode) {
			editLink.toggleOn();
		} else {
			editLink.toggleOff();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			initFormUserInfos(layoutCont);
			initFormMetadata(layoutCont, ureq);
			initFormAssignments(layoutCont, ureq);
			initFormTaxonomyCompetences(layoutCont);
			initFormStatusAndButtons(layoutCont);
		}
	}

	private void initFormUserInfos(FormItemContainer formLayout) {
		if(secCallback.canBookmark() && pageSettings.isWithBookmarks()) {
			bookmarkButton = uifactory.addFormLink("bookmark", "", null, formLayout, Link.NONTRANSLATED);
			bookmarkButton.setIconLeftCSS("o_icon o_icon-lg o_icon_bookmark");
			bookmarkButton.setElementCssClass("o_sel_pf_bookmark_entry");
			updateBookmarkIcon();
		}
		
		if(secCallback.canPageUserInfosStatus()) {
			syncUserInfosStatus();
			
			String[] keys = new String[] {
					PageUserStatus.incoming.name(), PageUserStatus.inProcess.name(), PageUserStatus.done.name()
				};
			String[] values = new String[] {
					translate("status.user.incoming"), translate("status.user.inProcess"), translate("status.user.done")
				};
			statusEl = uifactory.addDropdownSingleselect("user.status", "page.status", formLayout, keys, values, null);
			statusEl.setDomReplacementWrapperRequired(false);
			statusEl.addActionListener(FormEvent.ONCHANGE);
			if(pageUserInfos != null && pageUserInfos.getStatus() != null) {
				statusEl.select(pageUserInfos.getStatus().name(), true);
			} else {
				statusEl.select(keys[0], true);
			}
		}
	}
	
	private void initFormTaxonomyCompetences(FormLayoutContainer formLayout) {
		formLayout.contextPut("isCompetencesEnabled", taxonomyLinkingEnabled);
	}
	
	private void syncUserInfosStatus() {
		PageStatus status = page.getPageStatus();
		PageUserStatus userStatus = pageUserInfos.getStatus();
		if((status == PageStatus.inRevision || status == PageStatus.published)
				&& (userStatus == null || userStatus == PageUserStatus.incoming)) {
			pageUserInfos.setStatus(PageUserStatus.inProcess);
		} else if((status == PageStatus.closed || status == PageStatus.deleted)
				&& (userStatus != PageUserStatus.done)) {
			pageUserInfos.setStatus(PageUserStatus.done);
		}
		pageUserInfos.setRecentLaunch(new Date());
		pageUserInfos = portfolioService.updatePageUserInfos(pageUserInfos);
	}
	
	private void initFormMetadata(FormLayoutContainer formLayout, UserRequest ureq) {
		Set<Identity> owners = new HashSet<>();
		boolean isOwnedByViewer;
		
		if(page.getSection() != null && page.getSection().getBinder() != null) {
			owners.addAll(portfolioService.getMembers(page.getSection().getBinder(), ContentRoles.owner.name()));
		}
		
		owners.addAll(portfolioService.getMembers(page, ContentRoles.owner.name()));
		isOwnedByViewer = owners.contains(getIdentity());
		
		StringBuilder ownerSb = new StringBuilder();
		for(Identity owner:owners) {
			if(ownerSb.length() > 0) ownerSb.append(", ");
			ownerSb.append(userManager.getUserDisplayName(owner));
		}
		formLayout.contextPut("owners", ownerSb.toString());
		formLayout.contextPut("pageTitle", page.getTitle());
		formLayout.contextPut("pageSummary", page.getSummary());
		formLayout.contextPut("status", page.getPageStatus());
		formLayout.contextPut("statusIconCss", page.getPageStatus() == null ? PageStatus.draft.iconClass() : page.getPageStatus().iconClass());
		formLayout.contextPut("statusCssClass", page.getPageStatus() == null ? PageStatus.draft.statusClass() : page.getPageStatus().statusClass());
		
		int sharedWith = isOwnedByViewer ? portfolioService.countSharedPageBody(page) - 1 : -1;
		if(sharedWith > 0) {
			String sharedWithString = String.valueOf(sharedWith) + " " + translate("page.body.shared.with." + (sharedWith == 1 ? "entry" : "entries"));
			sharedWithLink = uifactory.addFormLink("sharedWithLink", sharedWithString, null, formLayout, Link.NONTRANSLATED);
			formLayout.contextPut("sharedWith", String.valueOf(sharedWith));
			PageStatus syntheticStatus = page.getBody().getSyntheticStatusEnum();
			if(syntheticStatus == PageStatus.published || syntheticStatus == PageStatus.closed) {
				formLayout.contextPut("syntheticStatusCss", syntheticStatus.iconClass());
				formLayout.contextPut("syntheticStatusTooltip", translate("status." + syntheticStatus.name()));
				formLayout.contextPut("syntheticStatus", translate("synthetic.status." + syntheticStatus.name()));
			}
		}
		
		if(pageSettings.isWithCategories()) {
			List<Category> categories = portfolioService.getCategories(page);
			if (secCallback.canEditCategories(page)) {
				// editable categories
				categoriesEditCtr = new CategoriesEditController(ureq, getWindowControl(), mainForm, categories);
				listenTo(categoriesEditCtr);
				formLayout.add("pageCategoriesCtr", categoriesEditCtr.getInitialFormItem());
			} else {
				// read-only categories
				List<String> categoryNames = new ArrayList<>(categories.size());
				for(Category category:categories) {
					categoryNames.add(category.getName());
				}
				formLayout.contextPut("pageCategories", categoryNames);
			}
		} else {
			formLayout.contextPut("pageCategories", List.of());
		}
		
		if (taxonomyLinkingEnabled) {
			if (secCallback.canEditCompetences(page)) {
				// editable categories
				competencesEditCtrl = new CompetencesEditController(ureq, getWindowControl(), mainForm, page);
				listenTo(competencesEditCtrl);
				formLayout.add("pageCompetencesCtrl", competencesEditCtrl.getInitialFormItem());		
			} else {
				// read-only categories
				List<TaxonomyCompetence> competences = pageService.getRelatedCompetences(page, true);
				List<String> competencyNames = new ArrayList<>(competences.size());
				for(TaxonomyCompetence competence:competences) {
					competencyNames.add(TaxonomyUIFactory.translateDisplayName(getTranslator(), competence.getTaxonomyLevel()));
				}
				formLayout.contextPut("pageCompetences", competencyNames);
			}
		}
		
		formLayout.contextPut("lastModified", page.getLastModified());
		
		if(StringHelper.containsNonWhitespace(page.getImagePath())) {
			File posterImage = pageService.getPosterImage(page);
			if(page.getImageAlignment() == PageImageAlign.background) {
				String mapperThumbnailUrl = registerCacheableMapper(ureq, "page-meta", new PageImageMapper(posterImage));
				formLayout.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
				formLayout.contextPut("imageAlign", PageImageAlign.background.name());
				formLayout.contextPut("imageName", posterImage.getName());
			} else {
				// alignment is right
				ImageFormItem imageCmp = new ImageFormItem(ureq.getUserSession(), "poster");
				imageCmp.setMedia(posterImage);
				imageCmp.setMaxWithAndHeightToFitWithin(PICTURE_WIDTH, PICTURE_HEIGHT);
				formLayout.add("poster", imageCmp);
				formLayout.contextPut("imageAlign", page.getImageAlignment() == null ? PageImageAlign.right.name() : page.getImageAlignment().name());
			}
		} else {
			formLayout.contextPut("imageAlign", "none");
		}
	}
	
	private void initFormAssignments(FormLayoutContainer formLayout, UserRequest ureq) {
		boolean needMapper = false;
		
		List<UserAssignmentInfos> assignmentInfos = new ArrayList<>(assignments.size());
		for(Assignment assignment:assignments) {
			List<File> documents = null;

			File storage = fileStorage.getAssignmentDirectory(assignment);
			if(storage != null && storage.exists()) {
				File[] files = storage.listFiles(SystemFileFilter.DIRECTORY_FILES);
				if(files != null && files.length > 0) {
					documents = Arrays.<File>asList(files);
					if(!documents.isEmpty()) {
						needMapper = true;
					}
				}
			}

			UserAssignmentInfos infos = new UserAssignmentInfos(assignment, documents);
			assignmentInfos.add(infos);
		}
		
		formLayout.contextPut("assignments", assignmentInfos);
		if(needMapper) {
			String mapperUri = registerCacheableMapper(ureq, "assigment-" + page.getKey(), new DocumentMapper());
			formLayout.contextPut("mapperUri", mapperUri);
		}
	}

	private void initFormStatusAndButtons(FormLayoutContainer layoutCont) {
		if(page.getSection() != null && page.getSection().getBinder() != null) {
			layoutCont.contextPut("statusEnabled", Boolean.TRUE);
			
			PageStatus pageStatus = page.getPageStatus();
			if(pageStatus == null) {
				pageStatus = PageStatus.draft;
			}
			String status = translate("status." + pageStatus.name());
			layoutCont.contextPut("pageStatus", status);

			if(secCallback.canPublish(page)) {
				publishButton = uifactory.addFormLink("publish", "publish", null, flc, Link.BUTTON_SMALL);
				publishButton.setIconLeftCSS("o_icon o_icon_publish o_icon-fw");
				publishButton.setElementCssClass("o_sel_pf_publish_entry");
			}
			if(secCallback.canRevision(page)) {
				revisionButton = uifactory.addFormLink("revision.page", "revision.page", null, flc, Link.BUTTON_SMALL);
				revisionButton.setIconLeftCSS("o_icon o_icon_rejected o_icon-fw");
				revisionButton.setElementCssClass("o_sel_pf_revision_entry");
				revisionButton.setTitle("revision.page.title");
			}
			if(secCallback.canClose(page)) {
				closeButton = uifactory.addFormLink("close.page", "close.page", null, flc, Link.BUTTON_SMALL);
				closeButton.setIconLeftCSS("o_icon o_icon_status_done o_icon-fw");
				closeButton.setElementCssClass("o_sel_pf_close_entry");
				closeButton.setTitle("close.page.title");
			}
			if(secCallback.canReopen(page)) {
				reopenButton = uifactory.addFormLink("reopen.page", "reopen.page", null, flc, Link.BUTTON_SMALL);
				reopenButton.setIconLeftCSS("o_icon o_icon_redo o_icon-fw");
				reopenButton.setElementCssClass("o_sel_pf_reopen_entry");
			}
		}

		editLink = uifactory.addToggleButton("edit.page", "edit.page.toggle", translate("on"), translate("off"), flc);
		editLink.setElementCssClass("o_sel_page_edit");
		editLink.setUserObject(Boolean.FALSE);
		editLink.setVisible(page.isEditable() && secCallback.canEditPage(page));
		
		editMetaDataLink = uifactory.addFormLink("edit.page.meta", "edit.page.metadata", null, flc, Link.BUTTON_SMALL);
		editMetaDataLink.setElementCssClass("o_sel_pf_edit_metadata_page");
		editMetaDataLink.setIconLeftCSS("o_icon o_ico-lg o_icon_edit_metadata");
		editMetaDataLink.setVisible(page.isEditable() && secCallback.canEditPageMetadata(page, assignments));
	}

	public void updateEditLink(boolean edit) {
		//TODO toogle
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(publishButton == source) {
			fireEvent(ureq, new PublishEvent());
		} else if(revisionButton == source) {
			fireEvent(ureq, new RevisionEvent());
		} else if(closeButton == source) {
			fireEvent(ureq, new ClosePageEvent());
		} else if(reopenButton == source) {
			fireEvent(ureq, new ReopenPageEvent());
		} else if(bookmarkButton == source) {
			toogleBookmark();
		} else if(editLink == source) {
			fireEvent(ureq, new ToggleEditPageEvent());
		} else if(editMetaDataLink == source) {
			fireEvent(ureq, new EditPageMetadataEvent());
		} else if (sharedWithLink == source) {
			doShare(ureq, sharedWithLink.getFormDispatchId());
		} else if(statusEl == source) {
			if(statusEl.isOneSelected()) {
				doChangeUserStatus(ureq, PageUserStatus.valueOfWithDefault(statusEl.getSelectedKey()));
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == categoriesEditCtr) {
			if (event == Event.CHANGED_EVENT) {				
				portfolioService.updateCategories(page, categoriesEditCtr.getUpdatedCategories());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == competencesEditCtrl) {
			if (event == Event.CHANGED_EVENT) {				
				pageService.linkCompetences(page, getIdentity(), competencesEditCtrl.getUpdatedCompetences());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(confirmClosePageCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event instanceof ClosePageEvent) {
				fireEvent(ureq, event);
			}
		} else if(sharedWithCtrl == source) {
			if(event == Event.CLOSE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmClosePageCtrl);
		removeAsListenerAndDispose(sharedWithCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		confirmClosePageCtrl = null;
		sharedWithCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}
	
	private void toogleBookmark() {
		pageUserInfos.setMark(!pageUserInfos.isMark());
		pageUserInfos = portfolioService.updatePageUserInfos(pageUserInfos);
		updateBookmarkIcon();
	}
	
	private void updateBookmarkIcon() {
		if(pageUserInfos.isMark()) {
			bookmarkButton.setIconLeftCSS("o_icon o_icon-lg o_icon_bookmark");
		} else {
			bookmarkButton.setIconLeftCSS("o_icon o_icon-lg o_icon_bookmark_add");
		}
	}
	
	private void doChangeUserStatus(UserRequest ureq, PageUserStatus newStatus) {
		pageUserInfos.setStatus(newStatus);
		pageUserInfos = portfolioService.updatePageUserInfos(pageUserInfos);
		
		if(newStatus == PageUserStatus.done) {
			fireEvent(ureq, new DonePageEvent());
		}
	}
	
	private void doShare(UserRequest ureq, String dispatchId) {
		sharedWithCtrl = new SharedWithController(ureq, getWindowControl());
		listenTo(sharedWithCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), sharedWithCtrl.getInitialComponent(),
				dispatchId, "", true, "", new CalloutSettings(false));
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	public class DocumentMapper implements Mapper {
		
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(relPath.startsWith("/")) {
				relPath = relPath.substring(1, relPath.length());
			}
			int index = relPath.indexOf('/');
			if(index > 0) {
				String assignmentKey = relPath.substring(0, index);
				
				int indexName = relPath.indexOf('/');
				if(indexName > 0) {
					String filename = relPath.substring(indexName + 1);
					
					File storage = null;
					for(Assignment assignment:assignments) {
						if(assignmentKey.equals(assignment.getKey().toString())) {
							storage = fileStorage.getAssignmentDirectory(assignment);
						}
					}
					if(storage != null) {
						File[] documents = storage.listFiles();
						for(File document:documents) {
							if(filename.equalsIgnoreCase(document.getName())) {
								return new FileMediaResource(document);
							}
						}
					}
				}
			}
			return null;
		}
	}
	
	public class PageImageMapper implements Mapper {
		
		private final File posterImage;
		
		public PageImageMapper(File posterImage) {
			this.posterImage = posterImage;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			return new FileMediaResource(posterImage);
		}
	}
	
	public class SharedWithController extends BasicController {
		
		public SharedWithController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			
			VelocityContainer mainVC = createVelocityContainer("shared_body_callout");
			
			List<String> sharingPagesIds = new ArrayList<>();
			List<Page> sharedPages = portfolioService.getPagesSharingSameBody(page);
			sharedPages.stream()
				.filter(sharedPage -> !sharedPage.equals(page))
				.forEach(sharedPage -> {
					// Should not happen, only in case of NPE due to old / incomplete data
					try {
						String linkId = "shared_page_" + sharedPage.getKey();
						String linkName = getLink(sharedPage);
						Link shareingPageLink = LinkFactory.createLink(linkId, sharedPage.getKey().toString(), "open_shared_page", linkName, getTranslator(), mainVC, this, Link.LINK + Link.NONTRANSLATED);
						shareingPageLink.setIconLeftCSS("o_icon o_icon_fw o_icon_pf_page");
						shareingPageLink.setUserObject(sharedPage);
						mainVC.put(linkId, shareingPageLink);
						sharingPagesIds.add(linkId);
					} catch (Exception e) {
						logError("", e);
					}
			});
			mainVC.contextPut("pages", sharingPagesIds);
			
			putInitialPanel(mainVC);
		}
		
		private String getLink(Page sharedPage) {
			StringBuilder sb = new StringBuilder();
			if(sharedPage.getSection() != null) {
				Section section = sharedPage.getSection();
				if(section.getBinder() != null) {
					sb.append(sharedPage.getSection().getBinder().getTitle())
					  .append(" / ");
				}
				sb.append(sharedPage.getSection().getTitle())
				  .append(" / ");
			}
			sb.append(sharedPage.getTitle());
			return sb.toString();
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			 if (source instanceof Link link && link.getCommand().equals("open_shared_page") && link.getUserObject() instanceof Page sharedPaged) {
				 fireEvent(ureq, Event.CLOSE_EVENT);
				 doOpen(ureq, sharedPaged);
			 }
		}
		
		private void doOpen(UserRequest ureq, Page sharedPage) {
			Long identityKey = getIdentity().getKey();
			Long binderKey = sharedPage.getSection().getBinder().getKey();
			Long pageKey = sharedPage.getKey();
			String businessPath = "[HomeSite:" + identityKey + "][PortfolioV2:0][MyBinders:0][Binder:" + binderKey + "][Toc:0][Entry:" + pageKey + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}
}
