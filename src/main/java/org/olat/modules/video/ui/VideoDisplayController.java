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
import java.util.Map;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ReadOnlyCommentsSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
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
	private VideoModule videoModule;
	@Autowired
	private VideoManager videoManager;

	private UserCommentsAndRatingsController commentsAndRatingCtr;
	private VelocityContainer mainVC;
	
	// User preferred resolution, stored in GUI prefs
	private Integer userPreferredResolution = null;
	
	private RepositoryEntry entry;
	private String descriptionText;
	private String mediaRepoBaseUrl;
	private VideoMeta videoMetadata;

	public VideoDisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean autoWidth) {
		this(ureq, wControl, entry, false, false, false, true, null, false, autoWidth, null, false);
	}
	
	public VideoDisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		this(ureq, wControl, entry, false, false, false, true, null, false, false, null, false);
	}

	public VideoDisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			Boolean autoplay, Boolean showComments, Boolean showRating, Boolean showTitleAndDescription, String OresSubPath,
			boolean customDescription, boolean autoWidth, String descriptionText, boolean readOnly) {
		super(ureq, wControl);
		this.entry = entry;
		this.descriptionText = (customDescription ? this.descriptionText = descriptionText : null);
		
		mainVC = createVelocityContainer("video_run");
		putInitialPanel(mainVC);
		
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
		VFSContainer mediaContainer = handler.getMediaContainer(entry);
		if(mediaContainer != null) {
			mediaRepoBaseUrl = registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()));
		}
		initMediaElementJs();
				
		VFSLeaf video = videoManager.getMasterVideoFile(entry.getOlatResource());
		if(video != null) {
			videoMetadata = videoManager.getVideoMetadata(entry.getOlatResource());
			if(autoWidth){
				mainVC.contextPut("height", 480);
				mainVC.contextPut("width", "100%");
			} else if(videoMetadata != null) {
				mainVC.contextPut("height", videoMetadata.getHeight());
				mainVC.contextPut("width", videoMetadata.getWidth());
			} else {
				mainVC.contextPut("height", 480);
				mainVC.contextPut("width", 640);
			}

			// Load users preferred version from GUI prefs
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			userPreferredResolution = (Integer) guiPrefs.get(VideoDisplayController.class, GUIPREF_KEY_PREFERRED_RESOLUTION);
			if (userPreferredResolution == null) {
				userPreferredResolution = videoModule.getPreferredDefaultResolution();
			}

			mainVC.contextPut("autoplay", autoplay);
	
			if ((showComments || showRating) && !ureq.getUserSession().getRoles().isGuestOnly()) {
				CommentAndRatingSecurityCallback ratingSecCallback = readOnly ? new ReadOnlyCommentsSecurityCallback() : new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
				commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(),entry.getOlatResource(), OresSubPath , ratingSecCallback,showComments, showRating, true);
				if (showComments) {					
					commentsAndRatingCtr.expandComments(ureq);
				}
				listenTo(commentsAndRatingCtr);				
				mainVC.put("commentsAndRating", commentsAndRatingCtr.getInitialComponent());
			}
			mainVC.contextPut("showTitleAndDescription", showTitleAndDescription);

			// Finally load the video, transcoded versions and tracks
			loadVideo(ureq, video);
		}
	}
	
	public VideoMeta getVideoMetadata() {
		return videoMetadata;
	}
	
	public void setTimeUpdateListener(boolean enable) {
		mainVC.contextPut("listenTimeUpdate", enable);
	}
	
	private void initMediaElementJs() {
		// load mediaelementjs player, speed and sourcechooser pluginss
		String[] cssPath;
		String[] jsCodePath;
		if(Settings.isDebuging()) {
			cssPath = new String[] {
					StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/features/source-chooser/source-chooser.css"),
					StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/features/speed/speed.css"),
					StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.css")
				};
			jsCodePath = new String[] {
					"movie/mediaelementjs/mediaelement-and-player.js",
					"movie/mediaelementjs/features/source-chooser/source-chooser.js",
					"movie/mediaelementjs/features/speed/speed.js"
				};
		} else {
			cssPath = new String[] {
					StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/features/source-chooser/source-chooser.css"),
					StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/features/speed/speed.css"),
					StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.min.css")
				};
			jsCodePath = new String[] {
					"movie/mediaelementjs/mediaelement-and-player.min.js",
					"movie/mediaelementjs/features/source-chooser/source-chooser.min.js",
					"movie/mediaelementjs/features/speed/speed.min.js"
				};
		}
		
		JSAndCSSComponent mediaelementjs = new JSAndCSSComponent("mediaelementjs", jsCodePath ,cssPath);
		mainVC.put("mediaelementjs", mediaelementjs);
	}
	
	public String getVideoElementId() {
		return mainVC.getDispatchID();
	}

	/**
	 * Reload the video, e.g. when new captions or transcoded versions are available
	 * @param ureq
	 * @param currentTime The start time in seconds (optional)
	 */
	protected void reloadVideo(UserRequest ureq) {
		//load video as VFSLeaf
		VFSLeaf video = videoManager.getMasterVideoFile(entry.getOlatResource());
		loadVideo(ureq, video);
		mainVC.contextPut("addForceReload", "?t=" + CodeHelper.getRAMUniqueID());
	}
	
	/**
	 * Reload video poster when the video poster has been exchanged
	 */
	protected void reloadVideoPoster() {
		// Check for null-value posters
		VFSLeaf poster = videoManager.getPosterframe(entry.getOlatResource());
		mainVC.contextPut("usePoster", Boolean.valueOf(poster != null && poster.getSize() > 0));
		// avoid browser caching of poster resource
		mainVC.contextPut("nocache", "?t=" + CodeHelper.getRAMUniqueID());
	}
	
	/**
	 * Set the text with url rewrite for embedded images, latex...
	 * @param text
	 * @param key
	 */
	private void setText(String text, String key) {
		if(StringHelper.containsNonWhitespace(text)) {
			text = StringHelper.xssScan(text);
			if(mediaRepoBaseUrl != null) {
				text = FilterFactory.getBaseURLToMediaRelativeURLFilter(mediaRepoBaseUrl).filter(text);
			}
			text = Formatter.formatLatexFormulas(text);
		}
		mainVC.contextPut(key, text);
	}

	/**
	 * Internal helper to do the actual video loading, checking for transcoded versions and captions
	 * @param ureq
	 * @param video
	 */
	private void loadVideo(UserRequest ureq, VFSLeaf video) {
		mainVC.contextPut("title", entry.getDisplayname());
		String desc = (descriptionText != null ? descriptionText : entry.getDescription());
		setText(desc, "description");
		String authors = entry.getAuthors();
		mainVC.contextPut("authors", (StringHelper.containsNonWhitespace(authors) ? authors : null));

		if(video != null) {
			// get resolution of master video resource 
			Size masterResolution = videoManager.getVideoResolutionFromOLATResource(entry.getOlatResource());
			String masterTitle = videoManager.getDisplayTitleForResolution(masterResolution.getHeight(), getTranslator());
			String masterSize = " (" + Formatter.formatBytes(videoManager.getVideoMetadata(entry.getOlatResource()).getSize()) + ")";
			boolean addMaster = true;
			// Mapper for Video
			String masterMapperId = "master-" + entry.getOlatResource().getResourceableId();
			String masterUrl = registerCacheableMapper(ureq, masterMapperId, new VideoMediaMapper(videoManager.getMasterContainer(entry.getOlatResource())));
			mainVC.contextPut("masterUrl", masterUrl);
			// Mapper for versions specific because not in same base as the resource itself
			String transcodingMapperId = "transcoding-" + entry.getOlatResource().getResourceableId();
			VFSContainer transcodedContainer = videoManager.getTranscodingContainer(entry.getOlatResource());
			String transcodedUrl = registerCacheableMapper(ureq, transcodingMapperId, new VideoMediaMapper(transcodedContainer));
			mainVC.contextPut("transcodedUrl", transcodedUrl);
			
			// Add transcoded versions
			List<VideoTranscoding> videos = videoManager.getVideoTranscodings(entry.getOlatResource());
			List<VideoTranscoding> readyToPlayVideos = new ArrayList<>();
			List<String> displayTitles = new ArrayList<>();
			int preferredAvailableResolution = 0;
						
			for (VideoTranscoding videoTranscoding : videos) {
				if (videoTranscoding.getStatus() == VideoTranscoding.TRANSCODING_STATUS_DONE) {
					readyToPlayVideos.add(videoTranscoding);
					// Check if at least one has equal height, else use master as resource
					addMaster &= videoTranscoding.getHeight() < masterResolution.getHeight();
					// Use the users preferred resolution or the next higher resolution
					if (videoTranscoding.getResolution() >= userPreferredResolution.intValue()) {
						preferredAvailableResolution = readyToPlayVideos.size() - 1;
					}
					// Calculate title. Standard title for standard resolution, original title if not standard resolution
					String title = videoManager.getDisplayTitleForResolution(videoTranscoding.getResolution(), getTranslator());
					displayTitles.add(title);
				}
			}
			mainVC.contextPut("addMaster", addMaster);			
			mainVC.contextPut("masterTitle", masterTitle + masterSize);
			mainVC.contextPut("videos", readyToPlayVideos);
			mainVC.contextPut("displayTitles", displayTitles);
			mainVC.contextPut("useSourceChooser", Boolean.valueOf(readyToPlayVideos.size() > 1));
			mainVC.contextPut(GUIPREF_KEY_PREFERRED_RESOLUTION, preferredAvailableResolution);
			// Check for null-value posters
			VFSLeaf poster = videoManager.getPosterframe(entry.getOlatResource());
			mainVC.contextPut("usePoster", Boolean.valueOf(poster != null && poster.getSize() > 0));
			
			// Load the track from config
			Map<String, String> trackfiles = new HashMap<String, String>();
			Map<String, VFSLeaf> configTracks = videoManager.getAllTracks(entry.getOlatResource());
			for (HashMap.Entry<String, VFSLeaf> track : configTracks.entrySet()) {
				trackfiles.put(track.getKey(), track.getValue().getName());
			}
			mainVC.contextPut("trackfiles",trackfiles);			
			
			// Load video chapter if available
			mainVC.contextPut("hasChapters", videoManager.hasChapters(entry.getOlatResource()));		
			
			// Add duration without preloading video
			String duration = entry.getExpenditureOfWork();
			if (!StringHelper.containsNonWhitespace(duration)) {
				duration = "00:00";
			}
			mainVC.contextPut("duration", duration);					
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
				String duration = ureq.getHttpReq().getParameter("duration");
				String src = ureq.getHttpReq().getParameter("src");
				//logDebug(cmd + " " + currentTime + " " + duration + " " + src, null);
				switch(cmd) {
					case "play":
						fireEvent(ureq, new VideoEvent(VideoEvent.PLAY, currentTime, duration));
						break;
					case "pause":
						fireEvent(ureq, new VideoEvent(VideoEvent.PAUSE, currentTime, duration));
						break;
					case "seeked":
						fireEvent(ureq, new VideoEvent(VideoEvent.SEEKED, currentTime, duration));					
						break;
					case "ended":
						fireEvent(ureq, new VideoEvent(VideoEvent.ENDED, currentTime, duration));
						break;
					case "timeupdate":
						fireEvent(ureq, new VideoEvent(VideoEvent.TIMEUPDATE, currentTime, duration));
						break;
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
