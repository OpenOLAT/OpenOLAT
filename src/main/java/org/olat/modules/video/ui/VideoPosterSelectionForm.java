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


import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import org.jcodec.common.FileChannelWrapper;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoPosterSelectionForm extends BasicController {
	private static final String FILENAME_POSTFIX_JPG = ".jpg";
	private static final String FILENAME_PREFIX_PROPOSAL_POSTER = "proposalPoster";
	
	private VFSContainer tmpContainer;

	@Autowired
	private VideoManager videoManager;
	private VelocityContainer proposalLayout = createVelocityContainer("video_poster_proposal");

	private Map<String, String> generatedPosters = new HashMap<String, String>();

	public VideoPosterSelectionForm(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);
		
		// posters are generated in tmp. 
		File tmp = new File(System.getProperty("java.io.tmpdir"), CodeHelper.getGlobalForeverUniqueID());
		tmp.mkdirs();
		tmpContainer = new LocalFolderImpl(tmp);
		
		long duration =1000;

		File videoFile = videoManager.getVideoFile(videoResource);
		RandomAccessFile accessFile;
		try {
			accessFile = new RandomAccessFile(videoFile,"r");
			FileChannel ch = accessFile.getChannel();
			FileChannelWrapper in = new FileChannelWrapper(ch);
			MP4Demuxer demuxer1 = new MP4Demuxer(in);
			duration = demuxer1.getVideoTrack().getFrameCount();
		} catch (Exception e) {
			logError("Error while accessing master video::" + videoFile.getAbsolutePath(), e);
		}

		long firstThirdDuration = duration/7;
		for (int x = 0; x <= duration; x += firstThirdDuration) {
			try {
				String fileName = FILENAME_PREFIX_PROPOSAL_POSTER + x + FILENAME_POSTFIX_JPG;
				VFSLeaf posterProposal = tmpContainer.createChildLeaf(fileName);
				videoManager.getFrame(videoResource, x, posterProposal);
				VideoMediaMapper mediaMapper = new VideoMediaMapper(tmpContainer);
				String mediaUrl = registerMapper(ureq, mediaMapper);
				String serverUrl = Settings.createServerURI();
				proposalLayout.contextPut("serverUrl", serverUrl);

				Link button = LinkFactory.createButton(String.valueOf(x), proposalLayout, this);
				button.setCustomEnabledLinkCSS("o_video_poster_selct");
				button.setCustomDisplayText(translate("poster.select"));
				button.setUserObject(fileName);
				
				generatedPosters.put(mediaUrl + "/" + fileName,	String.valueOf(x));
			} catch (Exception e) {
				logError("Error while creating poster images for video::" + videoFile.getAbsolutePath(), e);
			}
		}
		proposalLayout.contextPut("pics", generatedPosters);

		putInitialPanel(proposalLayout);
	}

	@Override
	protected void doDispose() {
		// cleanup tmp file
		if (tmpContainer != null) {
			tmpContainer.delete();
			tmpContainer = null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link button = (Link) source;
			VFSLeaf posterFile = (VFSLeaf)tmpContainer.resolve((String)button.getUserObject());
			if (posterFile != null) {
				fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, posterFile));
			}
		}
	}
}