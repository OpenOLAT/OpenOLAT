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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageImageAlign;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.ui.event.ClosePageEvent;
import org.olat.modules.portfolio.ui.event.PublishEvent;
import org.olat.modules.portfolio.ui.event.ReopenPageEvent;
import org.olat.modules.portfolio.ui.event.RevisionEvent;
import org.olat.modules.portfolio.ui.model.UserAssignmentInfos;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageMetadataController extends BasicController {
	
	public static final int PICTURE_WIDTH = 570;
	public static final int PICTURE_HEIGHT = (PICTURE_WIDTH / 3) * 2;
	
	private Link publishButton, revisionButton, closeButton, reopenButton;
	private ImageComponent imageCmp;
	private String mapperThumbnailUrl;
	private VelocityContainer mainVC;
	
	private final Page page;
	private final List<Assignment> assignments;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioFileStorage fileStorage;
	
	public PageMetadataController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback, Page page) {
		super(ureq, wControl);
		this.page = page;
		this.secCallback = secCallback;
		assignments = portfolioService.getAssignments(page);

		mainVC = createVelocityContainer("page_meta");
		
		initMetadata(ureq);
		initAssignments(ureq);
		initStatus();
		putInitialPanel(mainVC);
	}
	
	private void initMetadata(UserRequest ureq) {
		Set<Identity> owners = new HashSet<>();
		if(page.getSection() != null && page.getSection().getBinder() != null) {
			owners.addAll(portfolioService.getMembers(page.getSection().getBinder(), PortfolioRoles.owner.name()));
		}
		owners.addAll(portfolioService.getMembers(page, PortfolioRoles.owner.name()));
		
		StringBuilder ownerSb = new StringBuilder();
		for(Identity owner:owners) {
			if(ownerSb.length() > 0) ownerSb.append(", ");
			ownerSb.append(userManager.getUserDisplayName(owner));
		}
		mainVC.contextPut("owners", ownerSb.toString());
		mainVC.contextPut("pageTitle", page.getTitle());
		mainVC.contextPut("pageSummary", page.getSummary());
		mainVC.contextPut("status", page.getPageStatus());
		mainVC.contextPut("statusCss", page.getPageStatus() == null ? PageStatus.draft.cssClass() : page.getPageStatus().cssClass());
		
		mainVC.contextPut("lastPublicationDate", page.getLastPublicationDate());

		List<Category> categories = portfolioService.getCategories(page);
		List<String> categoryNames = new ArrayList<>(categories.size());
		for(Category category:categories) {
			categoryNames.add(category.getName());
		}
		mainVC.contextPut("pageCategories", categoryNames);
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
			if(storage != null) {
				documents = Arrays.<File>asList(storage.listFiles());
				if(documents.size() > 0) {
					needMapper = true;
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
			}
			if(secCallback.canClose(page)) {
				closeButton = LinkFactory.createButtonSmall("close.page", mainVC, this);
				closeButton.setIconLeftCSS("o_icon o_icon_status_done o_icon-fw");
				closeButton.setElementCssClass("o_sel_pf_close_entry");
			}
			if(secCallback.canReopen(page)) {
				reopenButton = LinkFactory.createButtonSmall("reopen.page", mainVC, this);
				reopenButton.setIconLeftCSS("o_icon o_icon_redo o_icon-fw");
				reopenButton.setElementCssClass("o_sel_pf_reopen_entry");
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
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
		}
	}
	
	public class DocumentMapper implements Mapper {
		
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(relPath.startsWith("/")) {
				relPath = relPath.substring(1, relPath.length());
			}
			int index = relPath.indexOf("/");
			if(index > 0) {
				String assignmentKey = relPath.substring(0, index);
				
				int indexName = relPath.indexOf("/");
				if(indexName > 0) {
					String filename = relPath.substring(indexName + 1);
					
					File storage = null;
					for(Assignment assignment:assignments) {
						if(assignmentKey.equals(assignment.getKey().toString())) {
							storage = fileStorage.getAssignmentDirectory(assignment);
						}
					}
					
					File[] documents = storage.listFiles();
					for(File document:documents) {
						if(filename.equalsIgnoreCase(document.getName())) {
							return new FileMediaResource(document);
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
}
