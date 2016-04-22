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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.modules.video.model.VideoQualityVersion;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoDisplayController extends BasicController {

	@Autowired
	private MovieService movieService;
	@Autowired
	VideoManager videoManager;

	private UserCommentsAndRatingsController commentsAndRatingCtr;
	private VelocityContainer mainVC;
	public static final Event ENDED_EVENT = new Event("videoEnded");


	public VideoDisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean autoWidth) {
		this(ureq, wControl, entry, false, false, false, null, false, autoWidth, null);
	}
	
	public VideoDisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		this(ureq, wControl, entry, false, false, false, null, false, false, null);
	}

	public VideoDisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Boolean autoplay, Boolean showComments, Boolean showRating, String OresSubPath, boolean customDescription, boolean autoWidth, String descriptionText) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("video_run");
		mainVC.contextPut("displayName", entry.getDisplayname());
		
		StringOutput sb = new StringOutput(50);
		Renderer.renderStaticURI(sb, "movie/mediaelementjs/mediaelementplayer.min.css");
		String cssPath = sb.toString();
		JSAndCSSComponent mediaelementjs = new JSAndCSSComponent("mediaelementjs", new String[] { "movie/mediaelementjs/mediaelement-and-player.min.js" },new String[] {cssPath});
		
		mainVC.put("mediaelementjs", mediaelementjs);
		putInitialPanel(mainVC);
		
		//load video as VFSLeaf
		VFSLeaf video = videoManager.getMasterVideoFile(entry.getOlatResource());
		if(video != null) {
			String filename = video.getName();
			mainVC.contextPut("filename", filename);
			String lowerFilename = filename.toLowerCase();
			String cssClass = CSSHelper.createFiletypeIconCssClassFor(lowerFilename);
			mainVC.contextPut("cssClass", cssClass);

			String extension = FileUtils.getFileSuffix(filename);
			String mediaUrl = registerMapper(ureq, new VideoMediaMapper(video.getParentContainer().getParentContainer()));
			mainVC.contextPut("movie", filename);
			mainVC.contextPut("mediaUrl", mediaUrl);
			
			Size realSize = movieService.getSize(video, extension);
			if(autoWidth){
				mainVC.contextPut("height", 480);
				mainVC.contextPut("width", "100%");
			} else if(realSize != null) {
				mainVC.contextPut("height", realSize.getHeight());
				mainVC.contextPut("width", realSize.getWidth());
			} else {
				mainVC.contextPut("height", 480);
				mainVC.contextPut("width", 640);
			}

			// Add versions
			List<VideoQualityVersion> videos = videoManager.getQualityVersions(entry.getOlatResource());
			mainVC.contextPut("videos", videos);
			
			//FIXME: ???? load tracks from config
			HashMap<String, String> trackfiles = new HashMap<String, String>();
			for(String lang : Locale.getISOLanguages()){
				VFSLeaf track = videoManager.getTrack(entry.getOlatResource(), lang);
				if(track != null){
					trackfiles.put(lang, track.getName());
				}
			}
			mainVC.contextPut("trackfiles",trackfiles);

			if (showComments || showRating) {
				CommentAndRatingSecurityCallback ratingSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
				commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(),entry.getOlatResource(), OresSubPath , ratingSecCallback,showComments, showRating, true);
				listenTo(commentsAndRatingCtr);				
				mainVC.put("commentsAndRating", commentsAndRatingCtr.getInitialComponent());
			}

			mainVC.contextPut("autoplay", autoplay);

			if (customDescription) {
				mainVC.contextPut("description", descriptionText);
			} else {
				mainVC.contextPut("description", videoManager.getDescription(entry.getOlatResource()));
			}
		}
	}

	@Override
	protected void doDispose() {

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == mainVC){
			String cmd = event.getCommand();
			if (StringHelper.containsNonWhitespace(cmd)) {
				String currentTime = ureq.getHttpReq().getParameter("currentTime");
				switch(cmd) {
					case "play":
						fireEvent(ureq, new VideoEvent(VideoEvent.PLAY, currentTime));					
					case "pause":
						fireEvent(ureq, new VideoEvent(VideoEvent.PAUSE, currentTime));
					case "seeked":
						fireEvent(ureq, new VideoEvent(VideoEvent.SEEKED, currentTime));					
					case "ended":
						fireEvent(ureq, new VideoEvent(VideoEvent.ENDED, currentTime));
				}
			}
		}
	}

}
