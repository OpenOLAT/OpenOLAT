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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
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
	
	private VFSLeaf tmpFile;
	private VFSContainer tmpContainer;

	private final VelocityContainer proposalLayout;

	private Size movieSize;
	private static final int STEP = 24;
	private final boolean hasProposals;
	
	@Autowired
	private MovieService movieService;
	@Autowired
	private VideoManager videoManager;

	public VideoPosterSelectionForm(UserRequest ureq, WindowControl wControl,
			OLATResource videoResource, VideoMeta videoMetadata) {
		super(ureq, wControl);
		
		proposalLayout = createVelocityContainer("video_poster_proposal");
		// posters are generated in tmp. 
		tmpContainer = new LocalFolderImpl(new File(WebappHelper.getTmpDir(), "poster_" + UUID.randomUUID()));
		
		VFSLeaf videoFile;
		if(StringHelper.containsNonWhitespace(videoMetadata.getUrl())) {
			videoFile = videoManager.downloadTmpVideo(videoResource, videoMetadata);
			tmpFile = videoFile;// delete temporary file
			if(videoMetadata.getVideoFormat() == VideoFormat.m3u8) {
				movieSize = movieService.getSize(videoFile, VideoFormat.mp4.name());
			}
		} else {
			videoFile = videoManager.getMasterVideoFile(videoResource);
			if(videoMetadata.getVideoFormat() == VideoFormat.mp4) {
				movieSize = movieService.getSize(videoFile, VideoFormat.mp4.name());
			}
		}
		
		List<String> proposals = generatePosterProposals(videoFile);
		proposalLayout.contextPut("proposals", proposals);
		hasProposals = !proposals.isEmpty();
		
		if(!proposals.isEmpty()) {
			String mediaUrl = registerMapper(ureq, new PosterMapper());
			proposalLayout.contextPut("mediaUrl", mediaUrl);
		}
		putInitialPanel(proposalLayout);
	}
	
	@Override
	protected void doDispose() {
		// cleanup tmp file
		if(tmpContainer != null) {
			tmpContainer.deleteSilently();
			tmpContainer = null;
		}
		if(tmpFile != null && tmpFile.exists()) {
			tmpFile.deleteSilently();
		}
        super.doDispose();
	}
	
	public boolean hasProposals() {
		return hasProposals;
	}
	
	private List<String> generatePosterProposals(VFSLeaf videoFile) {
		long frames = videoManager.getVideoFrameCount(videoFile);
		
		long framesStepping = frames / 7;
		if(framesStepping == 0) {
			framesStepping = 256;
		}
		long maxAdjust = framesStepping / STEP;
		
		int proposalCounter = 0;
		List<String> generatedPosters = new ArrayList<>();
		
		a_a:
		for (int currentFrame = 0; currentFrame <= frames && generatedPosters.size() < 8; currentFrame += framesStepping) {
			try {
				int adjust = 0;
				String fileName = FILENAME_PREFIX_PROPOSAL_POSTER + (proposalCounter++) + FILENAME_POSTFIX_JPG;
				VFSLeaf posterProposal = tmpContainer.createChildLeaf(fileName);

				boolean imgBlack = true;
				for(int i=0; i<maxAdjust && imgBlack; i++) {
					imgBlack = videoManager.getFrameWithFilter(videoFile, movieSize, (currentFrame+adjust), frames, posterProposal);
					
					if (currentFrame + STEP <= frames) {
						adjust += STEP;
					} else {
						adjust -= STEP;
					}
					// set lower bound to avoid endless loop
					if (currentFrame + adjust < 0) {
						// if all poster images are mostly black just take current frame
						if(videoManager.getFrame(videoFile, currentFrame, posterProposal)) {
							break;
						} else {
							break a_a;
						}
					}
				} 

				Link button = LinkFactory.createButton(fileName, proposalLayout, this);
				button.setCustomEnabledLinkCSS("o_video_poster_selct");
				button.setCustomDisplayText(translate("poster.select"));
				button.setUserObject(fileName);
				generatedPosters.add(fileName);
			} catch (Exception | AssertionError e) {
				logError("Error while creating poster images for video: " + videoFile, e);
			}
		}
		
		return generatedPosters;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link button = (Link) source;
			VFSItem posterFile = tmpContainer.resolve((String)button.getUserObject());
			if (posterFile instanceof VFSLeaf) {
				fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, posterFile));
			}
		}
	}
	
	private class PosterMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(relPath != null && relPath.contains("..") && !relPath.endsWith(FILENAME_POSTFIX_JPG)) { 
				return new ForbiddenMediaResource();
			}
			VFSItem mediaFile = tmpContainer.resolve(relPath);
			if (mediaFile instanceof VFSLeaf){
				return new VFSMediaResource((VFSLeaf)mediaFile);
			}
	 		return new NotFoundMediaResource();
		}
	}
}