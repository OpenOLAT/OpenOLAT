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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ReadOnlyCommentsSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.manager.LicenseTypeDAO;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.PublishingInformations;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.VideoQuestions;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.modules.video.manager.VideoSubtitlesHelper;
import org.olat.modules.video.ui.component.ContinueAtCommand;
import org.olat.modules.video.ui.component.ContinueCommand;
import org.olat.modules.video.ui.event.MarkerMovedEvent;
import org.olat.modules.video.ui.event.MarkerResizedEvent;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.modules.video.ui.question.VideoAssessmentItemController;
import org.olat.modules.video.ui.question.VideoQuestionRowComparator;
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
	private static final int EXPIRATION_TIME = 60 * 60;// one hour
	
	private VideoAssessmentItemController questionCtrl;
	private UserCommentsAndRatingsController commentsAndRatingCtr;
	private CloseableModalController cmc;
	private VideoLicenseInterceptionController licenseController;
	
	
	private final VelocityContainer mainVC;
	private final VelocityContainer markerVC;
	private final Panel markerPanel = new Panel("markerpanes");
	
	// User preferred resolution, stored in GUI prefs
	private Integer userPreferredResolution;
	
	private RepositoryEntry videoEntry;
	private String mediaRepoBaseUrl;
	private VideoMeta videoMetadata;
	private VideoMarkers videoMarkers;
	private VideoQuestions videoQuestions;
	private VideoQuestion backToQuestion;
	
	private final VideoDisplayOptions displayOptions;

	private List<Marker> markers = new ArrayList<>();

	@Autowired
	private VideoModule videoModule;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private LicenseService licenseService;
	
	public VideoDisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry videoEntry) {
		this(ureq, wControl, videoEntry, null, null, VideoDisplayOptions.valueOf(false, false, false, false, true, true, true, videoEntry.getDescription(), false, false, false));
	}
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param videoEntry The repository entry of the video resource
	 * @param entry The entry of the container, the course in most cases
	 * @param courseNode The course node
	 * @param displayOptions A list of options
	 */
	public VideoDisplayController(UserRequest ureq, WindowControl wControl, RepositoryEntry videoEntry, RepositoryEntry entry,
			VideoCourseNode courseNode, VideoDisplayOptions displayOptions) {
		super(ureq, wControl);
		this.videoEntry = videoEntry;
		this.displayOptions = displayOptions;
		
		mainVC = createVelocityContainer("video_run");
		putInitialPanel(mainVC);
		mainVC.put("markers", markerPanel);
		markerVC = createVelocityContainer("video_markers");
		markerVC.getContext().put("videoElementId", getVideoElementId());
		
		questionCtrl = new VideoAssessmentItemController(ureq, getWindowControl(), videoEntry, entry, courseNode,
				getVideoElementId(), displayOptions.isAuthorMode());
		listenTo(questionCtrl);
		
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(videoEntry);
		VFSContainer mediaContainer = handler.getMediaContainer(videoEntry);
		if(mediaContainer != null) {
			mediaRepoBaseUrl = registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()));
		}
		initMediaElementJs();

		videoMetadata = videoManager.getVideoMetadata(videoEntry.getOlatResource());	
		VFSLeaf video = videoManager.getMasterVideoFile(videoEntry.getOlatResource());
		if(videoMetadata != null && videoMetadata.getHeight() != 600 && videoMetadata.getWidth() != 800) {
			// we exclude 800x600 because it's the default (unkown) size and in this case we let the browser estimate the size
			mainVC.contextPut("height", videoMetadata.getHeight());
			mainVC.contextPut("width", videoMetadata.getWidth());
		}
		if(video != null || (videoMetadata != null && StringHelper.containsNonWhitespace(videoMetadata.getUrl()))) {
			// Load users preferred version from GUI prefs
			UserSession usess = ureq.getUserSession();
			Preferences guiPrefs = usess.getGuiPreferences();
			setUserPreferredResolution((Integer)guiPrefs.get(VideoDisplayController.class, GUIPREF_KEY_PREFERRED_RESOLUTION));

			mainVC.contextPut("autoplay", displayOptions.isAutoplay());
	
			if ((displayOptions.isShowComments() || displayOptions.isShowRating()) && !usess.getRoles().isGuestOnly()) {
				CommentAndRatingSecurityCallback ratingSecCallback = displayOptions.isReadOnly()
						? new ReadOnlyCommentsSecurityCallback() : new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
				String subIdent = courseNode == null ? null : courseNode.getIdent();
				PublishingInformations publishingInformations = getPublisher(entry, videoEntry, subIdent);
				// Legacy: comments are always linked to the video resource (it's historic)
				RepositoryEntry commentsEntry = videoEntry;
				// New: use the context rather than the video entry for not leaking comments across contexts
				if (displayOptions.isUseContainerForCommentsAndRatings() && entry != null) {
					commentsEntry = entry;
				}
				commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(), commentsEntry.getOlatResource(), subIdent,
						ratingSecCallback, publishingInformations, displayOptions.isShowComments(), displayOptions.isShowRating(), true);
				if (displayOptions.isShowComments()) {					
					commentsAndRatingCtr.expandComments(ureq);
				}
				listenTo(commentsAndRatingCtr);				
				mainVC.put("commentsAndRating", commentsAndRatingCtr.getInitialComponent());
			}
			mainVC.contextPut("showTitle", displayOptions.isShowTitle());
			mainVC.contextPut("showDescription", displayOptions.isShowDescription());
			mainVC.contextPut("alwaysShowControls", displayOptions.isAlwaysShowControls());
			mainVC.contextPut("clickToPlayPause", displayOptions.isClickToPlayPause());
			mainVC.contextPut("forwardSeekingRestricted", displayOptions.isForwardSeekingRestricted());
			
			initDownloadOptions();
			
			// Finally load the video, transcoded versions and tracks
			if(video != null) {
				loadVideo(ureq, video);
			} else if(videoMetadata != null && StringHelper.containsNonWhitespace(videoMetadata.getUrl())) {
				loadVideo(ureq, videoMetadata.getUrl(), videoMetadata.getVideoFormat());
			}
		}
	}
	
	private PublishingInformations getPublisher(RepositoryEntry entry, RepositoryEntry resourceEntry, String subIdent) {
		OLATResourceable ores = null;
		if(entry != null) {
			ores = entry.getOlatResource();
		} else {
			ores = resourceEntry.getOlatResource();
		}
		if(subIdent == null) {
			subIdent = "";
		}
		
		String businessPath = getWindowControl().getBusinessControl().getAsString();
		SubscriptionContext ctx = new SubscriptionContext(ores.getResourceableTypeName(), ores.getResourceableId(), subIdent);
		PublisherData data = new PublisherData(VideoFileResource.TYPE_NAME, ores.getResourceableId() + "-" + subIdent, businessPath);
		return new PublishingInformations(data, ctx);
	}
	
	public VideoMeta getVideoMetadata() {
		return videoMetadata;
	}
	
	public String getVideoElementId() {
		return mainVC.getDispatchID();
	}

	public void setTimeUpdateListener(boolean enable) {
		mainVC.contextPut("listenTimeUpdate", enable);
	}

	public void setProgressListener(boolean enable) {
		mainVC.contextPut("listenProgress", enable);
	}
	
	public Integer getUserPreferredResolution() {
		if (userPreferredResolution == null) {
			userPreferredResolution = videoModule.getPreferredDefaultResolution();
		}
		if (userPreferredResolution == null)  {
			userPreferredResolution = VideoModule.DEFAULT_RESOLUTION;
		}
		return userPreferredResolution;
	}
	
	public void setUserPreferredResolution(Integer resolution) {
		userPreferredResolution = resolution;
	}
	
	public AssessmentTestSession getCandidateSession() {
		return questionCtrl == null ? null : questionCtrl.getCandidateSession();
	}
	
	private void initMediaElementJs() {
		// load mediaelementjs player, speed and sourcechooser plugins
		List<String> cssPath = new ArrayList<>();
		cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/features/source-chooser/source-chooser.css"));
		cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/features/speed/speed.css"));

		List<String> jsCodePath = new ArrayList<>();
		jsCodePath.add("js/jquery/ui/jquery-ui-1.11.4.custom.resize.min.js");
		jsCodePath.add("js/jquery/ui/jquery-ui-1.11.4.custom.dnd.min.js");
		
		if(Settings.isDebuging()) {
			cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.css"));
			
			jsCodePath.add("movie/mediaelementjs/mediaelement-and-player.js");
			jsCodePath.add("movie/mediaelementjs/features/source-chooser/source-chooser.js");
			jsCodePath.add("movie/mediaelementjs/features/speed/speed.js");
		} else {
			cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.min.css"));

			jsCodePath.add("movie/mediaelementjs/mediaelement-and-player.min.js");
			jsCodePath.add("movie/mediaelementjs/features/source-chooser/source-chooser.min.js");
			jsCodePath.add("movie/mediaelementjs/features/speed/speed.min.js");
		}
		jsCodePath.add("movie/mediaelementjs/features/markers/o_markers.js");
		jsCodePath.add("movie/mediaelementjs/renderers/vimeo.js");
		
		JSAndCSSComponent mediaelementjs = new JSAndCSSComponent("mediaelementjs",
				jsCodePath.toArray(new String[jsCodePath.size()]),
				cssPath.toArray(new String[cssPath.size()]));
		mainVC.put("mediaelementjs", mediaelementjs);
	}

	/**
	 * Reload the video, e.g. when new captions or transcoded versions are available
	 * @param ureq
	 */
	protected void reloadVideo(UserRequest ureq) {
		videoMetadata = videoManager.getVideoMetadata(videoEntry.getOlatResource());
		if(StringHelper.containsNonWhitespace(videoMetadata.getUrl())) {
			loadVideo(ureq, videoMetadata.getUrl(), videoMetadata.getVideoFormat());
		} else {
			//load video as VFSLeaf
			VFSLeaf video = videoManager.getMasterVideoFile(videoEntry.getOlatResource());
			loadVideo(ureq, video);
			mainVC.contextPut("addForceReload", "?t=" + CodeHelper.getRAMUniqueID());
		} 
	}
	
	/**
	 * Reload video poster when the video poster has been exchanged
	 */
	protected void reloadVideoPoster() {
		// Check for null-value posters
		VFSLeaf poster = videoManager.getPosterframe(videoEntry.getOlatResource());
		boolean showPoster = displayOptions.isShowPoster() && poster != null && poster.getSize() > 0;
		mainVC.contextPut("usePoster", Boolean.valueOf(showPoster));
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
		if(video != null) {
			// get resolution of master video resource 
			Size masterResolution = videoManager.getVideoResolutionFromOLATResource(videoEntry.getOlatResource());
			String masterTitle = videoManager.getDisplayTitleForResolution(masterResolution.getHeight(), getTranslator());
			String masterSize = " (" + Formatter.formatBytes(videoManager.getVideoMetadata(videoEntry.getOlatResource()).getSize()) + ")";
			boolean addMaster = true;
			// Mapper for Video
			
			// Mapper for versions specific because not in same base as the resource itself
			String transcodingMapperId = mainVC.getDispatchID() + "-transcoding-" + videoEntry.getOlatResource().getResourceableId();
			VFSContainer transcodedContainer = videoManager.getTranscodingContainer(videoEntry.getOlatResource());
			String transcodedUrl = registerCacheableMapper(ureq, transcodingMapperId,
					new VideoMediaMapper(transcodedContainer), EXPIRATION_TIME);
			mainVC.contextPut("transcodedUrl", transcodedUrl);
			
			// Add transcoded versions
			List<VideoTranscoding> videos = videoManager.getVideoTranscodings(videoEntry.getOlatResource());
			List<VideoTranscoding> readyToPlayVideos = new ArrayList<>();
			List<String> displayTitles = new ArrayList<>();
			int preferredAvailableResolution = 0;
						
			for (VideoTranscoding videoTranscoding : videos) {
				if (videoTranscoding.getStatus() == VideoTranscoding.TRANSCODING_STATUS_DONE) {
					readyToPlayVideos.add(videoTranscoding);
					// Check if at least one has equal height, else use master as resource
					addMaster &= videoTranscoding.getHeight() < masterResolution.getHeight();
					// Use the users preferred resolution or the next higher resolution
					if (videoTranscoding.getResolution() >= getUserPreferredResolution().intValue()) {
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
	
			loadMetadataAndDisplayOptions(ureq);
			loadTracks();
			loadChapters();
			loadMarkers();
		}
	}
	
	private void loadVideo(UserRequest ureq, String url, VideoFormat format) {
		mainVC.contextPut("externalUrlOriginal", url);
		if (VideoFormat.vimeo.equals(format)) {
			URIBuilder uriBuilder;
			try {
				uriBuilder = new URIBuilder(url);
				// Vimeo allows removing the controls when using the payed service. It ignores the parameter
				// for the free service, so it is save to use it. We use the URIBuilder method in case the
				// user already added that parameter or if he uses some other parameters
				uriBuilder.setParameter("controls", "0");
				// Check if this is a Vimeo with the settings "Only people with the private link". In this case 
				// the second parameter needs to be removed for mediaelementjs to work properly.
				// Example: https://vimeo.com/123922956/05ad0e2cbf
				if (uriBuilder.getPathSegments().size() == 2) {
					uriBuilder.setPath(uriBuilder.getPathSegments().get(0));
				}
				url = uriBuilder.build().toString();
			} catch (URISyntaxException e) {
				logWarn("Error while parsing URL from external video source URL::" + url, e);
			}			
		}
	    
		mainVC.contextPut("externalUrl", url);
		mainVC.contextPut("sourceType", format.mimeType());

		loadMetadataAndDisplayOptions(ureq);
		loadTracks();
		loadChapters();
		loadMarkers();
	}
	
	private void loadMetadataAndDisplayOptions(UserRequest ureq) {
		String masterMapperId = mainVC.getDispatchID() + "-master-" + videoEntry.getOlatResource().getResourceableId();
		String masterUrl = registerCacheableMapper(ureq, masterMapperId,
				new VideoMediaMapper(videoManager.getMasterContainer(videoEntry.getOlatResource())), EXPIRATION_TIME);
		mainVC.contextPut("masterUrl", masterUrl);
		
		mainVC.contextPut("title", videoEntry.getDisplayname());
		if(displayOptions.isShowDescription()) {
			setText(displayOptions.getDescriptionText(), "description");
		}
		
		String authors = videoEntry.getAuthors();
		mainVC.contextPut("authors", (StringHelper.containsNonWhitespace(authors) ? authors : null));
		
		mainVC.contextPut("clickToPlayPause", Boolean.valueOf(displayOptions.isClickToPlayPause()));
		
		// Check for null-value posters
		if(displayOptions.isShowPoster()) {
			VFSLeaf poster = videoManager.getPosterframe(videoEntry.getOlatResource());
			mainVC.contextPut("usePoster", Boolean.valueOf(poster != null && poster.getSize() > 0));
		} else {
			mainVC.contextPut("usePoster", Boolean.FALSE);
		}
		
		// Add duration without preloading video
		String duration = videoEntry.getExpenditureOfWork();
		if (!StringHelper.containsNonWhitespace(duration)) {
			duration = "00:00";
		}
		mainVC.contextPut("duration", duration);
	}
	
	private void loadTracks() {
		VideoSubtitlesHelper.upgradeTracks(
				videoManager.getMasterContainer(videoEntry.getOlatResource()), getIdentity());
		Map<String, String> trackfiles = new HashMap<>();
		Map<String, VFSLeaf> configTracks = videoManager.getAllTracks(videoEntry.getOlatResource());
		for (Map.Entry<String, VFSLeaf> track : configTracks.entrySet()) {
			trackfiles.put(track.getKey(), track.getValue().getName());
		}
		mainVC.contextPut("trackfiles",trackfiles);	
	}
	
	private void loadChapters() {
		// Load video chapter if available
		mainVC.contextPut("hasChapters", videoManager.hasChapters(videoEntry.getOlatResource()));	
	}
	
	public List<Marker> loadMarkers() {
		markers.clear();
		
		if(displayOptions.isShowAnnotations()) {
			videoMarkers = videoManager.loadMarkers(videoEntry.getOlatResource());
			if(videoMarkers != null && !videoMarkers.getMarkers().isEmpty()) {
				List<Marker> vcMarkers = toMarkers(videoMarkers.getMarkers());
				markers.addAll(vcMarkers);
			}
		}
		if(displayOptions.isShowQuestions()) {
			videoQuestions = videoManager.loadQuestions(videoEntry.getOlatResource());
			if(videoQuestions != null && !videoQuestions.getQuestions().isEmpty()) {
				List<Marker> vcMarkers = questionsToMarkers(videoQuestions.getQuestions());
				markers.addAll(vcMarkers);
			}
		}
		Collections.sort(markers);
		mainVC.getContext().put("markers", markers);// make it without dirty=true
		return new ArrayList<>(markers);
	}
	
	public List<Marker> toMarkers(List<VideoMarker> vmarkers) {
		List<Marker> vcMarkers = new ArrayList<>();
		if(vmarkers != null && !vmarkers.isEmpty()) {
			for(VideoMarker marker:vmarkers) {
				long start = marker.toSeconds();
				vcMarkers.add(new Marker(marker.getId(), marker.getStyle(), start, "start", true, marker));
				long end = start + marker.getDuration();
				vcMarkers.add(new Marker(marker.getId(), marker.getStyle(), end, "end", false, marker));
			}
		}
		return vcMarkers;
	}
	
	public List<Marker> questionsToMarkers(List<VideoQuestion> questions) {
		List<Marker> vcMarkers = new ArrayList<>();
		if(questions != null && !questions.isEmpty()) {
			for(VideoQuestion question:questions) {
				long start = question.toSeconds();
				vcMarkers.add(new Marker(question.getId(), question.getStyle(), start, "start", true, question));
			}
		}
		return vcMarkers;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == mainVC){
			String cmd = event.getCommand();
			if (StringHelper.containsNonWhitespace(cmd)) {
				processVideoEvents(ureq, cmd);
			}
		} else if(markerVC == source) {
			if("marker_moved".equals(event.getCommand())) {
				doMarkerMoved(ureq);
			} else if("marker_resized".equals(event.getCommand())) {
				doMarkerResized(ureq);
			}
		} else if (source instanceof Link) {
			Link sourceLink = (Link) source;
			if (sourceLink.getCommand().equals("download_video")) {
				VideoTranscoding transcoding = (VideoTranscoding) sourceLink.getUserObject();
				License license = licenseService.loadLicense(videoEntry.getOlatResource());
				
				if (license != null && !license.getLicenseType().getName().equals(LicenseTypeDAO.NO_LICENSE_NAME)) {
					interceptLicense(ureq, license, null, transcoding);
				} else {
					downloadTranscoding(transcoding, ureq);
				}
				
			} else if (sourceLink.getCommand().equals("download_master_video")) {
				VFSLeaf masterFile = (VFSLeaf) sourceLink.getUserObject();
				License license = licenseService.loadLicense(videoEntry.getOlatResource());
				
				if (license != null && !license.getLicenseType().getName().equals(LicenseTypeDAO.NO_LICENSE_NAME)) {
					interceptLicense(ureq, license, masterFile, null);
				} else {
					downloadMasterVidoe(masterFile, ureq);
				}
			}
		}
	}
	
	private void interceptLicense(UserRequest ureq, License license, VFSLeaf masterFile, VideoTranscoding transcoding) {
		licenseController = new VideoLicenseInterceptionController(ureq, getWindowControl(), license, masterFile, transcoding);
		listenTo(licenseController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), licenseController.getInitialComponent());
		listenTo(cmc);
		
		cmc.activate();
	}
	
	private void downloadTranscoding(VideoTranscoding transcoding, UserRequest ureq) {
		VFSItem videoFile = videoManager.getTranscodingContainer(videoMetadata.getVideoResource()).resolve(transcoding.getResolution() + "video.mp4");
		
		if (videoFile != null && videoFile.exists()) {
			VFSMediaResource resource = new VFSMediaResource((VFSLeaf) videoFile);
			resource.setDownloadable(true);
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void downloadMasterVidoe(VFSLeaf vfsLeaf, UserRequest ureq) {
		VFSMediaResource resource = new VFSMediaResource(vfsLeaf);
		resource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void processVideoEvents(UserRequest ureq, String cmd) {
		String currentTime = ureq.getHttpReq().getParameter("currentTime");
		String duration = ureq.getHttpReq().getParameter("duration");
		if(!StringHelper.containsNonWhitespace(videoMetadata.getLength()) && StringHelper.containsNonWhitespace(duration)) {
			updateVideoDuration(duration);
		}
		
		String src = ureq.getHttpReq().getParameter("src");
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
			case "progress":
				fireEvent(ureq, new VideoEvent(VideoEvent.PROGRESS, currentTime, duration));
				break;
			case "marker":
				String markerId = ureq.getParameter("markerId");
				loadMarker(ureq, currentTime, markerId);
				break;
			default: // do nothing
		}
		updateGUIPreferences(ureq, src);
	}
	
	private void updateVideoDuration(String duration) {
		try {
			Long durationInSeconds = Math.round(Double.parseDouble(duration));
			if(durationInSeconds.longValue() > 0l) {
				videoEntry = videoManager.updateVideoMetadata(videoEntry, durationInSeconds, getIdentity());
				videoMetadata = videoManager.getVideoMetadata(videoEntry.getOlatResource());
			}
		} catch (NumberFormatException e) {
			logError("Cannot parse duration: " + duration, e);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(questionCtrl == source) {
			if(event == Event.BACK_EVENT) {
				doGoBackAfterQuestion(questionCtrl.getCurrentQuestion());
			} else if(event == Event.DONE_EVENT) {
				doContinueAfterQuestion();
			}
		} else if (licenseController == source) {
			VideoLicenseAcceptEvent licenseEvent = (VideoLicenseAcceptEvent) event;
			
			if (licenseEvent.getCommand().equals(VideoLicenseAcceptEvent.ACCEPT_LICENSE)) {
				if (licenseEvent.getMasterFile() != null) {
					downloadMasterVidoe(licenseEvent.getMasterFile(), ureq);
				} else if (licenseEvent.getTranscoding() != null) {
					downloadTranscoding(licenseEvent.getTranscoding(), ureq);
				}
			}
			
			cmc.deactivate();
			
			removeAsListenerAndDispose(licenseController);
			removeAsListenerAndDispose(cmc);
			licenseController = null;
			cmc = null;
		} else if (cmc == source) {
			removeAsListenerAndDispose(questionCtrl);
			removeAsListenerAndDispose(licenseController);
			removeAsListenerAndDispose(cmc);
			questionCtrl = null;
			licenseController = null;
			cmc = null;
		}
		super.event(ureq, source, event);
	}
	
	private void doContinueAfterQuestion() {
		markerPanel.setContent(null);

		ContinueCommand cmd = new ContinueCommand(getVideoElementId());
		getWindowControl().getWindowBackOffice().sendCommandTo(cmd);
	}
	
	private void doGoBackAfterQuestion(VideoQuestion question) {
		markerPanel.setContent(null);
		
		List<VideoQuestion> questions = videoQuestions.getQuestions();
		Collections.sort(questions, new VideoQuestionRowComparator());
		
		int pos = questions.indexOf(question);
		
		long time;
		if(pos <= 0) {
			time = 1l;
		} else {
			VideoQuestion previousQuestions = questions.get(pos - 1);
			time = previousQuestions.toSeconds();
			backToQuestion = previousQuestions;
		}
		Command cmd = new ContinueAtCommand(getVideoElementId(), time);
		getWindowControl().getWindowBackOffice().sendCommandTo(cmd);
	}

	private void doMarkerMoved(UserRequest ureq) {
		String markerId = ureq.getParameter("marker_id");
		MarkerMovedEvent event = new MarkerMovedEvent(markerId);
		event.setTop(parseDouble(ureq, "top", 0.0d, 0.98d));
		event.setLeft(parseDouble(ureq, "left", 0.0d, 0.98d));
		fireEvent(ureq, event);
	}
	
	private void doMarkerResized(UserRequest ureq) {
		String markerId = ureq.getParameter("marker_id");
		MarkerResizedEvent event = new MarkerResizedEvent(markerId);
		event.setTop(parseDouble(ureq, "top", 0.0d, 0.98d));
		event.setLeft(parseDouble(ureq, "left", 0.0d, 0.98d));
		event.setWidth(parseDouble(ureq, "width", 10.0d, 0.98d));
		event.setHeight(parseDouble(ureq, "height", 10.0d, 0.98d));
		fireEvent(ureq, event);
	}
	
	private double parseDouble(UserRequest ureq, String name, double def, double max) {
		try {
			double val = Double.parseDouble(ureq.getParameter(name));
			if(val < 0.0d) {
				val = 0.0d;
			} else if(val > max) {
				val = max;
			}
			return val;
		} catch (NumberFormatException e) {
			return def;
		}
	}
	
	public void loadMarker(UserRequest ureq, String currentTime, String markerId) {
		double time = Double.parseDouble(currentTime);
		VideoQuestion questionToPresent = null;
		if(displayOptions.isShowQuestions() && videoQuestions != null && StringHelper.containsNonWhitespace(markerId)) {
			for(VideoQuestion question:videoQuestions.getQuestions()) {
				if(markerId.equals(question.getId())) {
					questionToPresent = question;
				}
			}
		}
		
		if(questionToPresent != null && !questionToPresent.equals(backToQuestion)
				&& questionCtrl.present(ureq, questionToPresent, videoQuestions.getQuestions())) {
			markerPanel.setContent(questionCtrl.getInitialComponent());
		} else {
			List<VideoMarkerWrapper> currentMarkers = new ArrayList<>();
			if(questionToPresent == null || displayOptions.isShowAnnotations() && videoMarkers != null) {
				for(VideoMarker marker:videoMarkers.getMarkers()) {
					long start = marker.toSeconds();
					long end = start + marker.getDuration();
					if(start <= time && time < end) {
						currentMarkers.add(new VideoMarkerWrapper(marker));
					}
				}
			}
			
			backToQuestion = null;
			markerVC.contextPut("markers", currentMarkers);
			markerVC.contextPut("dragMarkers", displayOptions.isDragAnnotations());
			markerPanel.setContent(markerVC);
		}
	}

	/**
	 * Update the users preferred resolution in the GUI prefs from the given video URL
	 * @param ureq
	 * @param src
	 */
	private void updateGUIPreferences(UserRequest ureq, String src) {
		if (src != null) {
			int start = src.lastIndexOf('/');
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
							guiPrefs.commit(VideoDisplayController.class, GUIPREF_KEY_PREFERRED_RESOLUTION, Integer.valueOf(res));
						}							
					} catch (NumberFormatException e) {
						// ignore, do nothing
						logDebug("Error parsing the users preferred resolution from url::" + src);
					}
				}
			}
		}
	}
	
	private void initDownloadOptions() {
		// Only show download for local videos
		if (videoMetadata.getVideoFormat() != null && videoMetadata.getVideoFormat().equals(VideoFormat.mp4) && videoMetadata.isDownloadEnabled()) {
			List<Link> downloadLinks = new ArrayList<>();
			List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodings(videoMetadata.getVideoResource());
			int i = 0;
			
			for(VideoTranscoding videoTranscoding : videoTranscodings){
				if (videoTranscoding.getStatus() < VideoTranscoding.TRANSCODING_STATUS_DONE) {
					break;
				}
				
				String fileSize = "";
				
				if (videoTranscoding.getSize() != 0) {
					fileSize = Formatter.formatBytes(videoTranscoding.getSize());
				} 
				
				String title = videoManager.getDisplayTitleForResolution(videoTranscoding.getResolution(), getTranslator()) + " - " + fileSize;
				
				Link downloadLink = LinkFactory.createLink("download_" + i++, "download_video", getTranslator(), mainVC, this, Link.LINK);
				downloadLink.setCustomDisplayText(title);
				downloadLink.setUserObject(videoTranscoding);
				
				downloadLinks.add(downloadLink);
			}
			
			String masterFileSize = Formatter.formatBytes(videoManager.getMasterVideoFile(videoMetadata.getVideoResource()).getSize());			
			String masterTitle = videoManager.getDisplayTitleForResolution(videoManager.getVideoResolutionFromOLATResource(videoMetadata.getVideoResource()).getHeight(), getTranslator()) + " - " + masterFileSize;
			
			mainVC.contextPut("downloadEnabled", true);
			
			if (downloadLinks.isEmpty()) {
				Link downloadMasterFileLink = LinkFactory.createLink("download_" + i++, "download_master_video", getTranslator(), mainVC, this, Link.BUTTON);
				downloadMasterFileLink.setElementCssClass("pull-right o_video_download_btn");
				downloadMasterFileLink.setCustomDisplayText(translate("download.options") + " - " + masterTitle);
				downloadMasterFileLink.setUserObject(videoManager.getMasterVideoFile(videoMetadata.getVideoResource()));
				
				mainVC.put("downloadOptions", downloadMasterFileLink);
			} else {
				Dropdown downloadDropDown = new Dropdown("downloadOptions", "download.options", true, getTranslator());
				downloadDropDown.setButton(true);
				downloadDropDown.setEmbbeded(true);
				downloadDropDown.setOrientation(DropdownOrientation.right);
				downloadDropDown.setExpandContentHeight(true);
				
				Link downloadMasterFileLink = LinkFactory.createLink("download_" + i++, "download_master_video", getTranslator(), mainVC, this, Link.LINK);
				downloadMasterFileLink.setCustomDisplayText(translate("download.original") + " - " + masterTitle);
				downloadMasterFileLink.setUserObject(videoManager.getMasterVideoFile(videoMetadata.getVideoResource()));
				downloadDropDown.addComponent(downloadMasterFileLink);
				
				for (Link downloadLink : downloadLinks) {
					downloadDropDown.addComponent(downloadLink);
				}
				
				mainVC.put("downloadOptions", downloadDropDown);
			}
		}
	}

	/**
	 * Set and enable the replay feature for a certain position. The position is
	 * defined by the percentage of the video time already watched.
	 * 
	 * @param playProgress Value between 0 (0% watched) and 1 (100% watched) to
	 *                     indicate the progress.
	 */
	public void setPlayProgress(Double playProgress) {
		// only set play position when in between of the video
		if (playProgress > 0d && playProgress < 1d) {
			mainVC.contextPut("playProgress", playProgress);			
			String playPercent = Math.round(playProgress * 100) + "";
			// Enable resume dialog for now only for local videos and not for youtube/vimeo etc.
			mainVC.contextPut("resumeIntro", translate("resume.intro", playPercent));			
			//TODO: make media.duration and currentTime time for external videos work to enable the feature there as well
			// as soon as it works the enableResumeDialog flag can be removed
			mainVC.contextPut("enableResumeDialog", Boolean.valueOf(videoMetadata.getUrl() == null));		
		}
	}

	public void forceReload() {
		mainVC.contextPut("addForceReload", "?t=" + CodeHelper.getRAMUniqueID());
	}

	public void setMode(boolean showQuestions, boolean showAnnotations) {
		displayOptions.setShowQuestions(showQuestions);
		displayOptions.setShowAnnotations(showAnnotations);
	}

	public void clearMarkerLayer() {
		markerPanel.setContent(null);
	}

	public static class VideoMarkerWrapper {
		
		private final VideoMarker marker;
		
		public VideoMarkerWrapper(VideoMarker marker) {
			this.marker = marker;
		}

		public String getId() {
			return marker.getId();
		}

		public String getText() {
			String text = marker.getText();
			TextMode mode = TextMode.guess(text);
			switch(mode) {
				case oneLine: return TextMode.fromOneLine(text);
				case multiLine: return TextMode.fromMultiLine(text);
				default: return text;
			}
		}

		public double getTop() {
			return marker.getTop();
		}

		public double getLeft() {
			return marker.getLeft();
		}

		public double getWidth() {
			return marker.getWidth();
		}

		public double getHeight() {
			return marker.getHeight();
		}

		public String getStyle() {
			return marker.getStyle();
		}
	}
	
	public static class Marker implements Comparable<Marker> {
		
		private final String id;
		private final String color;
		private final long time;
		private final String action;
		private final boolean showInTimeline;
		private final Object userObject;
		
		public Marker(String id, String color, long time, String action, boolean showInTimeline, Object userObject) {
			this.id = id;
			this.color = color;
			this.time = time;
			this.action = action;
			this.showInTimeline = showInTimeline;
			this.userObject = userObject;
		}

		public String getId() {
			return id;
		}

		public String getColor() {
			return color;
		}

		public long getTime() {
			return time;
		}

		public String getAction() {
			return action;
		}
		
		public boolean isShowInTimeline() {
			return showInTimeline;
		}
		
		public Object getUserObject() {
			return userObject;
		}

		@Override
		public int compareTo(Marker o) {
			return Long.compare(time, o.time);
		}

		@Override
		public int hashCode() {
			return id == null ? 78638 : id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof Marker) {
				Marker m = (Marker)obj;
				return id != null && id.equals(m.id);
			}
			return false;
		}
	}
}