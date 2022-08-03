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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
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

/**
 * 
 * Initial date: 12 July 2022<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class VideoAudioPlayerController extends BasicController {
	private VelocityContainer videoAudioPlayerVC;

	public VideoAudioPlayerController(UserRequest ureq, WindowControl wControl, DocEditorConfigs configs, Access access) {
		super(ureq, wControl);
		videoAudioPlayerVC = createVelocityContainer("video_audio_player");
		videoAudioPlayerVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		
		// 1) Load mediaelementjs player and plugins
		List<String> cssPath = new ArrayList<>();
		cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/features/speed/speed.css"));
		List<String> jsCodePath = new ArrayList<>();
		jsCodePath.add("js/jquery/ui/jquery-ui-1.11.4.custom.resize.min.js");
		jsCodePath.add("js/jquery/ui/jquery-ui-1.11.4.custom.dnd.min.js");
		if(Settings.isDebuging()) {
			cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.css"));			
			jsCodePath.add("movie/mediaelementjs/mediaelement-and-player.js");
			jsCodePath.add("movie/mediaelementjs/features/speed/speed.js");
		} else {
			cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.min.css"));
			jsCodePath.add("movie/mediaelementjs/mediaelement-and-player.min.js");
			jsCodePath.add("movie/mediaelementjs/features/speed/speed.min.js");
		}		
		JSAndCSSComponent mediaelementjs = new JSAndCSSComponent("mediaelementjs",
				jsCodePath.toArray(new String[jsCodePath.size()]),
				cssPath.toArray(new String[cssPath.size()]));
		videoAudioPlayerVC.put("mediaelementjs", mediaelementjs);		

		// 2) Create mapper and URL for video delivery
		VFSLeaf vfsVideo = configs.getVfsLeaf();
		String mapperId = Long.toString(CodeHelper.getUniqueIDFromString(vfsVideo.getRelPath()));		
		VFSMediaMapper videoMapper = new VFSMediaMapper(vfsVideo);		
		String url = registerCacheableMapper(ureq, mapperId, videoMapper);
		videoAudioPlayerVC.contextPut("videoUrl", url + "/" + vfsVideo.getName());

		// *) Add some metadata
		VFSMetadata metaData = vfsVideo.getMetaInfo();
		if (metaData != null) {
			videoAudioPlayerVC.contextPut("videoTitle", metaData.getTitle());			
		}		
		videoAudioPlayerVC.contextPut("contentType", WebappHelper.getMimeType(vfsVideo.getName()));			
		
		putInitialPanel(videoAudioPlayerVC);
	}

	
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events to dispatch
	}
	
}
