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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.image.ImageComponent;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageImageAlign;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserInformations;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
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
public class PageMetadataController extends BasicController {
	
	public static final int PICTURE_WIDTH = 970 * 2;	// max width for large images: 1294 * 75% , x2 for high res displays
	public static final int PICTURE_HEIGHT = 300 * 2 ; 	// max size for large images, see CSS, x2 for high res displays

	private Link editLink;
	private Link editMetaDataLink;
	private Link publishButton;
	private Link revisionButton;
	private Link closeButton;
	private Link reopenButton;
	private Link bookmarkButton;
	private Link sharedWithLink;
	
	private ImageComponent imageCmp;
	private String mapperThumbnailUrl;
	private VelocityContainer mainVC;
	
	private CloseableModalController cmc;
	private UserInfosStatusController userStatusCtrl;
	private CategoriesEditController categoriesEditCtr;
	private CompetencesEditController competencesEditCtrl;
	private ConfirmClosePageController confirmClosePageCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	private final Page page;
	private PageUserInformations pageUserInfos;
	private final List<Assignment> assignments;
	private final BinderSecurityCallback secCallback;
	
	private final boolean taxonomyLinkingEnabled;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioFileStorage fileStorage;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	
	public PageMetadataController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback,
			Page page, boolean openInEditMode) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.page = page;
		this.secCallback = secCallback;
		taxonomyLinkingEnabled = portfolioV2Module.isTaxonomyLinkingReady();
		assignments = portfolioService.getSectionsAssignments(page, null);
		if(secCallback.canBookmark() || secCallback.canPageUserInfosStatus()) {
			pageUserInfos = portfolioService.getPageUserInfos(page, getIdentity(), PageUserStatus.inProcess);
		}

		mainVC = createVelocityContainer("page_meta");
		
		initUserInfos(ureq);
		initMetadata(ureq);
		initAssignments(ureq);
		initStatus();
		initTaxonomyCompetences();
		editLink(!openInEditMode);
		editMetaDataLink();
		putInitialPanel(mainVC);
	}
	
	private void initUserInfos(UserRequest ureq) {
		if(secCallback.canBookmark()) {
			bookmarkButton = LinkFactory.createLink("bookmark", "bookmark", "bookmark", null, getTranslator(), mainVC, this, Link.NONTRANSLATED);
			bookmarkButton.setIconLeftCSS("o_icon o_icon-lg o_icon_bookmark");
			bookmarkButton.setElementCssClass("o_sel_pf_bookmark_entry");
			updateBookmarkIcon();
		}
		if(secCallback.canPageUserInfosStatus()) {
			syncUserInfosStatus();
			userStatusCtrl = new UserInfosStatusController(ureq, getWindowControl());
			listenTo(userStatusCtrl);
			mainVC.put("userStatus", userStatusCtrl.getInitialComponent());
		}
	}
	
	private void initTaxonomyCompetences() {
		mainVC.contextPut("isCompetencesEnabled", taxonomyLinkingEnabled);
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
	
	private void initMetadata(UserRequest ureq) {
		Set<Identity> owners = new HashSet<>();
		boolean isOwnedByViewer;
		
		if(page.getSection() != null && page.getSection().getBinder() != null) {
			owners.addAll(portfolioService.getMembers(page.getSection().getBinder(), PortfolioRoles.owner.name()));
		}
		
		owners.addAll(portfolioService.getMembers(page, PortfolioRoles.owner.name()));
		isOwnedByViewer = owners.contains(ureq.getUserSession().getIdentity());
		
		StringBuilder ownerSb = new StringBuilder();
		for(Identity owner:owners) {
			if(ownerSb.length() > 0) ownerSb.append(", ");
			ownerSb.append(userManager.getUserDisplayName(owner));
		}
		mainVC.contextPut("owners", ownerSb.toString());
		mainVC.contextPut("pageTitle", page.getTitle());
		mainVC.contextPut("pageSummary", page.getSummary());
		mainVC.contextPut("status", page.getPageStatus());
		mainVC.contextPut("statusIconCss", page.getPageStatus() == null ? PageStatus.draft.iconClass() : page.getPageStatus().iconClass());
		mainVC.contextPut("statusCssClass", page.getPageStatus() == null ? PageStatus.draft.statusClass() : page.getPageStatus().statusClass());
		
		
		int sharedWith = isOwnedByViewer ? portfolioService.countSharedPageBody(page) - 1 : -1;
		if(sharedWith > 0) {
			String sharedWithString = String.valueOf(sharedWith) + " " + translate("page.body.shared.with." + (sharedWith == 1 ? "entry" : "entries"));
			sharedWithLink = LinkFactory.createLink("sharedWithLink", "sharedWithLink", "showSharedPages", sharedWithString, getTranslator(), mainVC, this, Link.NONTRANSLATED);
			mainVC.contextPut("sharedWith", String.valueOf(sharedWith));
			PageStatus syntheticStatus = page.getBody().getSyntheticStatusEnum();
			if(syntheticStatus == PageStatus.published || syntheticStatus == PageStatus.closed) {
				mainVC.contextPut("syntheticStatusCss", syntheticStatus.iconClass());
				mainVC.contextPut("syntheticStatusTooltip", translate("status." + syntheticStatus.name()));
				mainVC.contextPut("syntheticStatus", translate("synthetic.status." + syntheticStatus.name()));
			}
		}

		List<Category> categories = portfolioService.getCategories(page);
		if (secCallback.canEditCategories(page)) {
			// editable categories
			categoriesEditCtr = new CategoriesEditController(ureq, getWindowControl(), categories);
			listenTo(categoriesEditCtr);
			mainVC.put("pageCategoriesCtr", categoriesEditCtr.getInitialComponent());			
		} else {
			// read-only categories
			List<String> categoryNames = new ArrayList<>(categories.size());
			for(Category category:categories) {
				categoryNames.add(category.getName());
			}
			mainVC.contextPut("pageCategories", categoryNames);
		}
		
		if (taxonomyLinkingEnabled) {
			if (secCallback.canEditCompetences(page)) {
				// editable categories
				competencesEditCtrl = new CompetencesEditController(ureq, getWindowControl(), page);
				listenTo(competencesEditCtrl);
				mainVC.put("pageCompetencesCtrl", competencesEditCtrl.getInitialComponent());			
			} else {
				// read-only categories
				List<TaxonomyCompetence> competences = portfolioService.getRelatedCompetences(page, true);
				List<String> competencyNames = new ArrayList<>(competences.size());
				for(TaxonomyCompetence competence:competences) {
					competencyNames.add(TaxonomyUIFactory.translateDisplayName(getTranslator(), competence.getTaxonomyLevel()));
				}
				mainVC.contextPut("pageCompetences", competencyNames);
			}
		}
		
		mainVC.contextPut("lastModified", page.getLastModified());
		
		if(StringHelper.containsNonWhitespace(page.getImagePath())) {
			File posterImage = portfolioService.getPosterImage(page);
			if(page.getImageAlignment() == PageImageAlign.background) {
				mapperThumbnailUrl = registerCacheableMapper(ureq, "page-meta", new PageImageMapper(posterImage));
				mainVC.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
				mainVC.contextPut("imageAlign", PageImageAlign.background.name());
				mainVC.contextPut("imageName", posterImage.getName());
			} else {
				// alignment is right
				imageCmp = new ImageComponent(ureq.getUserSession(), "poster");
				imageCmp.setMedia(posterImage);
				imageCmp.setMaxWithAndHeightToFitWithin(PICTURE_WIDTH, PICTURE_HEIGHT);
				mainVC.put("poster", imageCmp);
				mainVC.contextPut("imageAlign", page.getImageAlignment() == null ? PageImageAlign.right.name() : page.getImageAlignment().name());
			}
		} else {
			mainVC.contextPut("imageAlign", "none");
		}
	}
	
	private void initAssignments(UserRequest ureq) {
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
		
		mainVC.contextPut("assignments", assignmentInfos);
		if(needMapper) {
			String mapperUri = registerCacheableMapper(ureq, "assigment-" + page.getKey(), new DocumentMapper());
			mainVC.contextPut("mapperUri", mapperUri);
		}
	}

	private void initStatus() {
		if(page.getSection() != null && page.getSection().getBinder() != null) {
			mainVC.contextPut("statusEnabled", Boolean.TRUE);
			
			PageStatus pageStatus = page.getPageStatus();
			if(pageStatus == null) {
				pageStatus = PageStatus.draft;
			}
			String status = translate("status." + pageStatus.name());
			mainVC.contextPut("pageStatus", status);

			if(secCallback.canPublish(page)) {
				publishButton = LinkFactory.createButtonSmall("publish", mainVC, this);
				publishButton.setIconLeftCSS("o_icon o_icon_publish o_icon-fw");
				publishButton.setElementCssClass("o_sel_pf_publish_entry");
			}
			if(secCallback.canRevision(page)) {
				revisionButton = LinkFactory.createButtonSmall("revision.page", mainVC, this);
				revisionButton.setIconLeftCSS("o_icon o_icon_rejected o_icon-fw");
				revisionButton.setElementCssClass("o_sel_pf_revision_entry");
				revisionButton.setTitle("revision.page.title");
			}
			if(secCallback.canClose(page)) {
				closeButton = LinkFactory.createButtonSmall("close.page", mainVC, this);
				closeButton.setIconLeftCSS("o_icon o_icon_status_done o_icon-fw");
				closeButton.setElementCssClass("o_sel_pf_close_entry");
				closeButton.setTitle("close.page.title");
			}
			if(secCallback.canReopen(page)) {
				reopenButton = LinkFactory.createButtonSmall("reopen.page", mainVC, this);
				reopenButton.setIconLeftCSS("o_icon o_icon_redo o_icon-fw");
				reopenButton.setElementCssClass("o_sel_pf_reopen_entry");
			}
		}
	}
	
	protected Link editLink(boolean edit) {
		if(page.isEditable()) {
			if(editLink == null) {
				editLink = LinkFactory.createButtonSmall("edit.page", mainVC, this);
				editLink.setElementCssClass("o_sel_pf_edit_page");
				mainVC.put("edit.page", editLink);
			}
			if(edit) {
				editLink.setCustomDisplayText(translate("edit.page.meta"));
				editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_off");
			} else {
				editLink.setCustomDisplayText(translate("edit.page.close"));
				editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_on");
			}
			editLink.setVisible(secCallback.canEditPage(page));
		}
		
		return editLink;
	}
	
	protected Link editMetaDataLink() {
		if(page.isEditable() && secCallback.canEditPageMetadata(page, assignments)) {
			if(editMetaDataLink == null) {
				editMetaDataLink = LinkFactory.createButtonSmall("edit.page.metadata", mainVC, this);
				editMetaDataLink.setElementCssClass("o_sel_pf_edit_metadata_page");
				editMetaDataLink.setIconLeftCSS("o_icon o_ico-lg o_icon_edit_metadata");
				mainVC.put("edit.page.meta", editMetaDataLink);
			}
			
			editLink.setVisible(secCallback.canEditPage(page));
		}
		return editLink;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
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
			removeAsListenerAndDispose(calloutCtrl);

			CalloutSettings settings = new CalloutSettings(false);
			VelocityContainer sharedBodyPagesList = createVelocityContainer("shared_body_callout");
			
			List<String> sharingPagesIds = new ArrayList<>();
			portfolioService.getPagesSharingSameBody(page).stream().forEach(sharingPage -> {
				if (!sharingPage.equals(page)) {
					// Should not happen, only in case of NPE due to old / incomplete data
					try {
						Link shareingPageLink = LinkFactory.createLink("shared_page_" + sharingPage.getKey().toString(), sharingPage.getKey().toString(), "open_shared_page", sharingPage.getSection().getBinder().getTitle() + " / " + sharingPage.getSection().getTitle() + " / " + sharingPage.getTitle(), (Translator) null, sharedBodyPagesList, this, Link.LINK + Link.NONTRANSLATED);
						shareingPageLink.setIconLeftCSS("o_icon o_icon_fw o_icon_pf_page");
						shareingPageLink.setUserObject(sharingPage);
						sharedBodyPagesList.put("shared_page_" + sharingPage.getKey().toString(), shareingPageLink);
						sharingPagesIds.add("shared_page_" + sharingPage.getKey().toString());
					} catch (Exception e) {}
				}
			});
			sharedBodyPagesList.contextPut("pages", sharingPagesIds);
		

			calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),sharedBodyPagesList, sharedWithLink.getDispatchID(), "", true, "", settings);
			listenTo(calloutCtrl);
			calloutCtrl.activate();
		} else if (source instanceof Link) {
			if (((Link) source).getCommand().equals("open_shared_page")) {
				calloutCtrl.deactivate();
				cleanUp();
				
				Page sharedPage = (Page) ((Link) source).getUserObject();
				String identityKey = ureq.getUserSession().getIdentity().getKey().toString();
				String binderKey = sharedPage.getSection().getBinder().getKey().toString();
				String pageKey = sharedPage.getKey().toString();
				String businessPath = "[HomeSite:" + identityKey + "][PortfolioV2:0][MyBinders:0][Binder:" + binderKey + "][Toc:0][Entry:" + pageKey + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
		}
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
				portfolioService.linkCompetences(page, getIdentity(), competencesEditCtrl.getUpdatedCompetences());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(confirmClosePageCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event instanceof ClosePageEvent) {
				fireEvent(ureq, event);
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmClosePageCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		confirmClosePageCtrl = null;
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
	
	private class UserInfosStatusController extends FormBasicController {
		
		private SingleSelection statusEl;
		
		public UserInfosStatusController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "page_meta_user_status");
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
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
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(statusEl == source) {
				if(statusEl.isOneSelected()) {
					PageUserStatus selectedStatus = PageUserStatus
							.valueOfWithDefault(statusEl.getSelectedKey());
					doChangeUserStatus(ureq, selectedStatus);
				}
			} 
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
