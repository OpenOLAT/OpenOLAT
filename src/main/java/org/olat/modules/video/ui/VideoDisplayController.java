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
import org.olat.core.util.prefs.Preferences;
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

	private static final String GUIPREF_KEY_PREFERRED_RESOLUTION = "preferredResolution";
	@Autowired
	private MovieService movieService;
	@Autowired
	VideoManager videoManager;

	private UserCommentsAndRatingsController commentsAndRatingCtr;
	private VelocityContainer mainVC;
	
	// User preferred resolution, stored in GUI prefs
	private Integer userPreferredResolution = null;
	
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

		// load mediaelementjs player and sourcechooser plugin
		StringOutput sb = new StringOutput(50);
		Renderer.renderStaticURI(sb, "movie/mediaelementjs/mediaelementplayer.min.css");
		String[] cssPath = new String[] {sb.toString()};
		String[] jsCodePath= new String[] { "movie/mediaelementjs/mediaelement-and-player.min.js", "movie/mediaelementjs/features/oo-mep-feature-sourcechooser.js" };
		JSAndCSSComponent mediaelementjs = new JSAndCSSComponent("mediaelementjs", jsCodePath ,cssPath);
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

			// Load users preferred version from GUI prefs
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			userPreferredResolution = (Integer) guiPrefs.get(VideoDisplayController.class, GUIPREF_KEY_PREFERRED_RESOLUTION);
			if (userPreferredResolution == null) {
				// default value if not yet stored: 720p videos
				userPreferredResolution = new Integer(720);
			}

			// Add versions
			List<VideoQualityVersion> videos = videoManager.getQualityVersions(entry.getOlatResource());
			List<VideoQualityVersion> readyToPlayVideos = new ArrayList<>();
			int preferredAvailableResolution = 0;
						
			for (VideoQualityVersion videoVersion : videos) {
				if (videoVersion.getTranscodingStatus() == VideoQualityVersion.TRANSCODING_STATUS_DONE) {
					readyToPlayVideos.add(videoVersion);
					// Use the users preferred resolution or the next higher resolution
					if (videoVersion.getResolution() >= userPreferredResolution.intValue()) {
						preferredAvailableResolution = readyToPlayVideos.size() - 1;
					}
				}
			}
			mainVC.contextPut("videos", readyToPlayVideos);
			mainVC.contextPut("useSourceChooser", Boolean.valueOf(readyToPlayVideos.size() > 1));
			mainVC.contextPut(GUIPREF_KEY_PREFERRED_RESOLUTION, preferredAvailableResolution);
			
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
				String src = ureq.getHttpReq().getParameter("src");
				logDebug(cmd + " " + currentTime + " " + src, null);				
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
				updateGUIPreferences(ureq, src);
			}
		}
	}

	/**
	 * Update the users preferred resolution in the GUI prefs from the given video URL
	 * @param ureq
	 * @param src
	 */
	private void updateGUIPreferences(UserRequest ureq, String src) {
		if (src != null) {
			int start = src.lastIndexOf("/");
			if (start != -1) {
				String video = src.substring(start + 1);
				int end = video.indexOf("video");
				if (end > 0) { // dont's save master videos
					String resolution = video.substring(0, end);
					try {
						int res = Integer.parseInt(resolution.trim());
						if (userPreferredResolution == null || userPreferredResolution.intValue() != res) {
							// update GUI prefs, reload first
							Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
							userPreferredResolution = (Integer) guiPrefs.get(VideoDisplayController.class, GUIPREF_KEY_PREFERRED_RESOLUTION);
							guiPrefs.putAndSave(VideoDisplayController.class, GUIPREF_KEY_PREFERRED_RESOLUTION, Integer.valueOf(res));
						}							
					} catch (NumberFormatException e) {
						// ignore, do nothing
						logDebug("Error parsing the users preferred resolution from url::" + src, null);
					}
				}
			}
			
		}
	}

}
