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
package org.olat.modules.video.ui;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This site implements a YouTube stile video library for self-study. 
 * 
 * Initial date: 08.05.2016<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class VideoListingController extends BasicController implements Activateable2 {

	private final TooledStackedPanel toolbarPanel;
	private final VelocityContainer listingVC;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	
	VideoListingController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel) {		
		super(ureq, wControl);
		this.toolbarPanel = toolbarPanel;
		this.listingVC = createVelocityContainer("video_listing");
		putInitialPanel(listingVC);
		doInitVideosListing(ureq);
	}

	/**
	 * Helper to implement the initialization of the datamodel and the view
	 * @param ureq
	 */
	private void doInitVideosListing(UserRequest ureq) {
		List<String> types = new ArrayList<String>();
		types.add(VideoFileResource.TYPE_NAME);
		//TODO: refactor to flexi table with custom renderer and search field, sort by views/name/date, paging etc. 
		
		List<RepositoryEntry> videoEntries = repositoryManager.queryByTypeLimitAccess(ureq.getIdentity(), types, ureq.getUserSession().getRoles());
		listingVC.contextPut("videoEntries", videoEntries);		
		
		String imgUrl = registerMapper(ureq, new Mapper() {
			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				if (StringHelper.containsNonWhitespace(relPath)) {
					int start = relPath.lastIndexOf("/");
					if (start != -1) {
						relPath = relPath.substring(start+1);
						long id = Long.parseLong(relPath);
						RepositoryEntry videoResource = repositoryManager.lookupRepositoryEntry(id);
						VFSLeaf imageFile = repositoryManager.getImage(videoResource);
						MediaResource res = new VFSMediaResource(imageFile);
						return res;
					}
				}
				return new NotFoundMediaResource("Image for resource ID::" + relPath + " not found");
			}
		});
		listingVC.contextPut("imgUrl", imgUrl);		
	}

	/**
	 * Launch a single video and add to breadcrumb
	 * @param ureq
	 * @param id the video resource ID
	 */
	private void doShowVideo(UserRequest ureq, Long id) {
		RepositoryEntry videoEntry = repositoryManager.lookupRepositoryEntry(id);
		if (repositoryManager.isAllowed(ureq, videoEntry).canLaunch()) {
			VideoDisplayController videoDisplayCtr = new VideoDisplayController(ureq, getWindowControl(), videoEntry, true, true, true, null, false, true, null);
			listenTo(videoDisplayCtr);
			toolbarPanel.pushController(videoEntry.getDisplayname(), videoDisplayCtr);
			// Update launch counter
			repositoryService.incrementLaunchCounter(videoEntry);
			// Update business path and URL
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(videoEntry);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ce.getOLATResourceable()));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
			addToHistory(ureq, bwControl);			
		}

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source.equals(listingVC) && "view".equals(event.getCommand())) {
			String id = ureq.getParameter("id");
			if (StringHelper.isLong(id)) {
				Long repoId = Long.valueOf(id);
				doShowVideo(ureq, repoId);
			}
		}
	}
	

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		// cleanup existing video
		if (toolbarPanel.getBreadCrumbs().size() > 1) {
			toolbarPanel.popUpToRootController(ureq);
		}
		if(entries == null || entries.isEmpty()) {
			// nothing to do, defautl video listing
			return;
		} else {
			Long id = entries.get(0).getOLATResourceable().getResourceableId();
			doShowVideo(ureq, id);			
		}
	}
	
	@Override
	protected void doDispose() {
		// controllers auto-disposed by basic controller
	}

}
