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
package org.olat.course.nodes.video;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * peekviewcontroller of videomodule
 * 
 * @author dfakae, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoPeekviewController  extends BasicController implements Controller{
	
	private final static String EVENT_RUN = "run";
	
	private final Long repositoryEntryKey;
	private final String nodeId;
	private VelocityContainer peekviewVC;

	@Autowired
	private VideoManager videoManager;

	public VideoPeekviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry videoEntry,
			Long repositoryEntryKey, String nodeId) {
		super(ureq, wControl);

		this.repositoryEntryKey = repositoryEntryKey;
		this.nodeId = nodeId;

		// show empty screen when video is not available or in deleted state
		if (videoEntry == null) {
			EmptyStateConfig emptyState = EmptyStateConfig.builder()
					.withIconCss("o_icon_video")
					.withMessageI18nKey(VideoEditController.NLS_ERROR_VIDEOREPOENTRYMISSING)
					.build();
			EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
			emptyStateCmp.setTranslator(getTranslator());
			putInitialPanel(emptyStateCmp);
			return;
		} else if (RepositoryEntryStatusEnum.deleted == videoEntry.getEntryStatus()
				|| RepositoryEntryStatusEnum.trash == videoEntry.getEntryStatus()) {
			EmptyStateConfig emptyState = EmptyStateConfig.builder()
					.withIconCss("o_icon_video")
					.withIndicatorIconCss("o_icon_deleted")
					.withMessageI18nKey(VideoEditController.NLS_ERROR_VIDEOREPOENTRYDELETED)
					.build();
			EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
			emptyStateCmp.setTranslator(getTranslator());
			putInitialPanel(emptyStateCmp);
			return;
		}

		peekviewVC = createVelocityContainer("peekview");
		VFSContainer posterFolder = videoManager.getMasterContainer(videoEntry.getOlatResource());
		String masterMapperId = "master-" + videoEntry.getOlatResource().getResourceableId();
		String mediaUrl = registerCacheableMapper(ureq, masterMapperId, new VideoMediaMapper(posterFolder));
		peekviewVC.contextPut("mediaUrl", mediaUrl);
		peekviewVC.contextPut("nodeLink", posterFolder);
		peekviewVC.contextPut("run", EVENT_RUN);
		putInitialPanel(peekviewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == peekviewVC && EVENT_RUN.equals(event.getCommand())) {
			String businessPath = "[RepositoryEntry:" + repositoryEntryKey + "][CourseNode:" + nodeId + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());	
		}
	}

}