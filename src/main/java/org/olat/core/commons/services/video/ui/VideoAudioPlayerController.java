/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.video.ui;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
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
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.video.VideoFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 July 2022<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class VideoAudioPlayerController extends BasicController {

	private Access access;
	
	@Autowired
	private DocEditorService docEditorService;

	private final VelocityContainer videoAudioPlayerVC;

	private final boolean passPlayerEventsToParent;

	public VideoAudioPlayerController(UserRequest ureq, WindowControl wControl, DocEditorConfigs configs, Access access,
									  boolean showAudioVisualizer) {
		this(ureq, wControl, configs.getVfsLeaf(), null, false, true,
				showAudioVisualizer);
		this.access = access;
	}

	public VideoAudioPlayerController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsMedia,
									  String streamingVideoUrl, boolean minimalControls, boolean autoplay,
									  boolean showAudioVisualizer) {
		this(ureq, wControl, vfsMedia, streamingVideoUrl, minimalControls, autoplay, showAudioVisualizer,
				false);
	}

	public VideoAudioPlayerController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsMedia,
									  String streamingVideoUrl, boolean minimalControls, boolean autoplay,
									  boolean showAudioVisualizer, boolean passPlayerEventsToParent) {
		super(ureq, wControl);
		this.passPlayerEventsToParent = passPlayerEventsToParent;
		videoAudioPlayerVC = createVelocityContainer("video_audio_player");
		videoAudioPlayerVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID

		videoAudioPlayerVC.contextPut("minimalControls", minimalControls);
		videoAudioPlayerVC.contextPut("autoplay", autoplay);
		videoAudioPlayerVC.contextPut("passPlayerEventsToParent", passPlayerEventsToParent);

		videoAudioPlayerVC.put("mediaelementjs", mediaElementPlayerAndPlugins());

		if (vfsMedia != null) {
			VFSMetadata metaData = vfsMedia.getMetaInfo();
			String mapperId = Long.toString(CodeHelper.getUniqueIDFromString(vfsMedia.getRelPath()));
			VFSMediaMapper mediaMapper = new VFSMediaMapper(vfsMedia);
			boolean useMaster = metaData != null && metaData.isInTranscoding();
			mediaMapper.setUseMaster(useMaster);
			String url = registerCacheableMapper(ureq, mapperId, mediaMapper);
			videoAudioPlayerVC.contextPut("mediaUrl", url + "/" + vfsMedia.getName());

			if (metaData != null) {
				videoAudioPlayerVC.contextPut("mediaTitle", metaData.getTitle());
			}
			videoAudioPlayerVC.contextPut("contentType", WebappHelper.getMimeType(vfsMedia.getName()));
			videoAudioPlayerVC.contextPut("showVisualizer", showAudioVisualizer && isAudio(vfsMedia.getName()));
		}

		if (streamingVideoUrl != null) {
			VideoFormat videoFormat = VideoFormat.valueOfUrl(streamingVideoUrl);
			if (videoFormat != null) {
				videoAudioPlayerVC.contextPut("mediaUrl", adjustStreamingVideoUrl(streamingVideoUrl, videoFormat));
				videoAudioPlayerVC.contextPut("contentType", videoFormat.mimeType());
			}
		}

		putInitialPanel(videoAudioPlayerVC);
	}

	private boolean isAudio(String fileName) {
		return WebappHelper.getMimeType(fileName).startsWith("audio");
	}

	private JSAndCSSComponent mediaElementPlayerAndPlugins() {
		List<String> cssPath = new ArrayList<>();
		cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/features/speed/speed.css"));
		List<String> jsCodePath = new ArrayList<>();
		jsCodePath.add("js/jquery/ui/jquery-ui-1.13.2.dnd.resize.slider.min.js");
		if(Settings.isDebuging()) {
			cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.css"));
			jsCodePath.add("movie/mediaelementjs/mediaelement-and-player.js");
			jsCodePath.add("movie/mediaelementjs/features/speed/speed.js");
		} else {
			cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.min.css"));
			jsCodePath.add("movie/mediaelementjs/mediaelement-and-player.min.js");
			jsCodePath.add("movie/mediaelementjs/features/speed/speed.min.js");
		}
		jsCodePath.add("movie/mediaelementjs/renderers/vimeo.js");
		jsCodePath.add("movie/mediaelementjs/features/visualizer/o_visualizer.js");

		return new JSAndCSSComponent("mediaelementjs",
				jsCodePath.toArray(new String[0]),
				cssPath.toArray(new String[0]));
	}

	private String adjustStreamingVideoUrl(String streamingVideoUrl, VideoFormat videoFormat) {
		if (VideoFormat.vimeo.equals(videoFormat)) {
			URIBuilder uriBuilder;
			try {
				uriBuilder = new URIBuilder(streamingVideoUrl);
				uriBuilder.setParameter("controls", "0");
				if (uriBuilder.getPathSegments().size() == 2) {
					uriBuilder.setPath(uriBuilder.getPathSegments().get(0));
				}
				return uriBuilder.build().toString();
			} catch (URISyntaxException e) {
				logError("", e);
			}
		}
		return streamingVideoUrl;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("close".equals(event.getCommand())) {
			// User clicked on the margin. This event must close the lightbox.
			fireEvent(ureq, Event.CLOSE_EVENT);
		} else if (source == videoAudioPlayerVC) {
			if (passPlayerEventsToParent) {
				fireEvent(ureq, event);
			}
		}
	}

	@Override
	protected void doDispose() {
		deleteAccess();
		super.doDispose();
	}
	
	private void deleteAccess() {
		docEditorService.deleteAccess(access);
	}

	public String getMediaElementId() {
		return videoAudioPlayerVC.getDispatchID();
	}
}
