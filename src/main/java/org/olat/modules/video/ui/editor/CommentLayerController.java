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
package org.olat.modules.video.ui.editor;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoComments;
import org.olat.modules.video.VideoManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-03-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CommentLayerController extends BasicController {
	private final VelocityContainer mainVC;
	private final Link closeLink;
	private final RepositoryEntry repositoryEntry;
	private VideoComments comments;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DisplayPortraitManager portraitManager;


	public CommentLayerController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;

		mainVC = createVelocityContainer("comment_layer");

		closeLink = LinkFactory.createToolLink("close", "", this, "o_icon o_icon_lg o_icon_close");
		closeLink.setElementCssClass("o_video_comment_close");
		mainVC.put("close", closeLink);

		mainVC.contextPut("showComment", false);

		putInitialPanel(mainVC);

		loadComments();

		MapperKey avatarMapperKey = mapperService.register(null, "avatars-members", new UserAvatarMapper(false));
		mainVC.contextPut("avatarBaseURL", avatarMapperKey.getUrl());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (closeLink == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	public void loadComments() {
		comments = videoManager.loadComments(repositoryEntry.getOlatResource());
	}

	public void setComment(UserRequest ureq, String commentId) {
		if (comments == null) {
			return;
		}
		comments.getComments().stream().filter(c -> c.getId().equals(commentId)).findFirst().ifPresent(c -> {
			mainVC.contextPut("showComment", true);
			mainVC.contextPut("color", c.getColor());
			BaseSecurity manager = BaseSecurityManager.getInstance();
			Identity identity = manager.findIdentityByName(c.getAuthor());
			if (identity != null) {
				Long identityKey = identity.getKey();
				String displayName = userManager.getUserDisplayName(identity);
				mainVC.contextPut("name", displayName);
				if (portraitManager.getSmallPortraitResource(identityKey) != null) {
					mainVC.contextPut("avatarKey", identityKey);
				} else {
					mainVC.contextRemove("avatarKey");
				}
			} else {
				mainVC.contextRemove("name");
				mainVC.contextRemove("avatarKey");
			}
			if (StringHelper.containsNonWhitespace(c.getText())) {
				mainVC.contextPut("text", c.getText());
			} else {
				mainVC.contextRemove("text");
			}
			mainVC.remove("video");
			if (StringHelper.containsNonWhitespace(c.getFileName())) {
				VFSContainer masterContainer = videoManager.getCommentMediaContainer(repositoryEntry.getOlatResource());
				VFSLeaf vfsVideo = (VFSLeaf) masterContainer.resolve(c.getFileName());
				if (vfsVideo != null) {
					VideoAudioPlayerController videoAudioPlayerController = new VideoAudioPlayerController(ureq,
							getWindowControl(), vfsVideo, null, true, false);
					mainVC.put("video", videoAudioPlayerController.getInitialComponent());
				}
			} else if (StringHelper.containsNonWhitespace(c.getUrl())) {
				VideoAudioPlayerController videoAudioPlayerController = new VideoAudioPlayerController(ureq,
						getWindowControl(), null, c.getUrl(), true, false);
				mainVC.put("video", videoAudioPlayerController.getInitialComponent());
			}
		});
	}

	public void hideComment() {
		mainVC.contextPut("showComment", false);
	}
}
