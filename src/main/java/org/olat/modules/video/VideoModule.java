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
package org.olat.modules.video;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.video.site.VideoSite;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.VideoHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * V
 *
 * Initial date: 23.2.2016<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoModule extends AbstractSpringModule {

	private static final Logger log = Tracing.createLoggerFor(VideoModule.class);

	public static final Integer DEFAULT_RESOLUTION = Integer.valueOf(720);
	
	private static final String VIDEO_ENABLED = "video.enabled";
	private static final String VIDEOCOURSENODE_ENABLED = "video.coursenode.enabled";
	private static final String VIDEOTRANSCODING_ENABLED = "video.transcoding.enabled";
	private static final String VIDEOTRANSCODING_LOCAL = "video.transcoding.local";
	private static final String TRANSCODING_RESOLUTIONS = "video.transcoding.resolutions";
	private static final String PREFERRED_RESOLUTION = "video.transcoding.resolution.preferred";

	@Value("${video.enabled:true}")
	private boolean enabled;
	@Value("${video.coursenode.enabled:true}")
	private boolean coursenodeEnabled;
	@Value("${video.marker.styles}")
	private String markersStyles;
	// transcoding related configuration
	@Value("${video.transcoding.enabled:false}")
	private boolean transcodingEnabled;
	@Value("${video.transcoding.local:true}")
	private boolean transcodingLocal;
	@Value("${video.transcoding.resolutions}")
	private String transcodingResolutions;
	@Value("${video.transcoding.taskset.cpuconfig}")
	private String transcodingTasksetConfig;
	@Value("${video.transcoding.dir}")
	private String transcodingDir;
	@Value("${video.transcoding.resolution.preferred}")
	private String transcodingPreferredResolutionConf;
	@Value("${video.transcoding.profile}")
	private String transcodingProfile;
	
	private int[] transcodingResolutionsArr; //  1080, 720, 480
	private Integer preferredDefaultResolution;// 720

	@Autowired
	private VideoHandler videoHandler;

	@Autowired
	public VideoModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	protected void initDefaultProperties() {
		if(StringHelper.containsNonWhitespace(transcodingResolutions)) {
			try {
				String[] resolutions = transcodingResolutions.split(",");
				int[] resolutionInts = new int[resolutions.length];
				for(int i=resolutions.length; i-->0; ) {
					resolutionInts[i] = Integer.parseInt(resolutions[i]);
				}
				transcodingResolutionsArr = resolutionInts;
			} catch (NumberFormatException e) {
				log.error("Cannot parse transcoding resolutions", e);
			}
		}
		if(StringHelper.containsNonWhitespace(transcodingPreferredResolutionConf)) {
			try {
				preferredDefaultResolution = Integer.valueOf(transcodingPreferredResolutionConf);
			} catch (NumberFormatException e) {
				log.error("Cannot parse property video.transcoding.resolution.preferred::" + transcodingPreferredResolutionConf, e);
			}
		}
		super.initDefaultProperties();
	}

	@Override
	public void init() {
		RepositoryHandlerFactory.registerHandler(this.videoHandler, 43);
		
		String enabledObj = getStringPropertyValue(VIDEO_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}

		String enabledCoursenodeObj = getStringPropertyValue(VIDEOCOURSENODE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledCoursenodeObj)) {
			coursenodeEnabled = "true".equals(enabledCoursenodeObj);
		}

		String enabledTranscodingObj = getStringPropertyValue(VIDEOTRANSCODING_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledTranscodingObj)) {
			transcodingEnabled = "true".equals(enabledTranscodingObj);
		}

		String localTranscodingObj = getStringPropertyValue(VIDEOTRANSCODING_LOCAL, true);
		if(StringHelper.containsNonWhitespace(localTranscodingObj)) {
			transcodingLocal = "true".equals(localTranscodingObj);
		}
		
		String localPreferredResolutionObj = getStringPropertyValue(PREFERRED_RESOLUTION, true);
		if(StringHelper.containsNonWhitespace(localPreferredResolutionObj)) {
			preferredDefaultResolution =  getIntPropertyValue(PREFERRED_RESOLUTION);
		}
		
		// clean setting of injected config
		setVideoTranscodingProfile(this.transcodingProfile);
		

		log.info("video.enabled={}", isEnabled());
		log.info("video.coursenode.enabled={}", isCoursenodeEnabled());
		log.info("video.transcoding.enabled={}", isTranscodingEnabled());
		log.info("video.transcoding.resolutions={}", Arrays.toString(getTranscodingResolutions()));
		log.info("video.transcoding.resolution.preferred={}", getPreferredDefaultResolution());
		log.info("video.transcoding.taskset.cpuconfig={}", getTranscodingTasksetConfig());
		log.info("video.transcoding.local={}", isTranscodingLocal());
		log.info("video.transcoding.profile={}", getVideoTranscodingProfile());

		// Register video site for activation in top navigation
		NewControllerFactory.getInstance().addContextEntryControllerCreator(VideoSite.class.getSimpleName(),
				new SiteContextEntryControllerCreator(VideoSite.class));

	}
	
	public List<String> getMarkerStyles() {
		return stylesToList(markersStyles);
	}
	
	private List<String> stylesToList(String styles) {
		List<String> styleList = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(styles)) {
			String[] styleArr = styles.split("[,]");
			for(String style:styleArr) {
				if(StringHelper.containsNonWhitespace(style)) {
					styleList.add(style);
				}
			}
		}
		return styleList;
	}

	/**
	 * The values represent the target height of the transcoded video, 1080 for
	 * 1080p video size etc. This config can only be set in
	 * olat.localproperties, see "video.transcoding.resolutions"
	 * 
	 * @return Array of transcoding resolutions.
	 */
	public int[] getTranscodingResolutions() {
		return transcodingResolutionsArr;
	}
	
	/**
	 * set the active resolutions for the transcoding process
	 */
	public void setTranscodingResolutions(int[] resolutions){
		this.transcodingResolutionsArr = resolutions;
		String resArr = Arrays.toString(this.transcodingResolutionsArr);
		resArr.replace("[", "");
		resArr.replace("]", "");
		resArr.replace(" ", "");
		setStringProperty(TRANSCODING_RESOLUTIONS, resArr, true);
	}
	
	/**
	 * Use this resolution if the user has no own preferred resolution
	 * @return
	 */
	public Integer getPreferredDefaultResolution() {
		return preferredDefaultResolution;
	}

	/**
	 * Set the system default resolution
	 * @param 
	 */
	public void setPreferedTranscodingResolution(Integer value) {
		preferredDefaultResolution = value;
		setIntProperty(PREFERRED_RESOLUTION, preferredDefaultResolution, true);
	}
	/**
	 * The base container where the transcoded videos are stored. This config can only be set in
	 * olat.localproperties, see "video.transcoding.dir"
	 * @return
	 */
	public VFSContainer getTranscodingBaseContainer() {
		if (transcodingDir != null) {
			File base = new File(transcodingDir);
			if (base.exists() || base.mkdirs()) {
				return new LocalFolderImpl(base);
			}
		} 
		if (transcodingEnabled) {
			log.error("Error, no valid transcoding dir. Disabling transcoding. video.transcoding.dir={}", transcodingDir);
			// only disable variable, don't store it in persisted properties
			transcodingEnabled = false;
		}
		return null;
	}
	
	/**
	 * @return null to indicate that taskset is disabled or the -c options to control the number of cores, e.g. "0,1"
	 */
	public String getTranscodingTasksetConfig() {
		if (StringHelper.containsNonWhitespace(transcodingTasksetConfig)) {
			return transcodingTasksetConfig.trim();			
		} else {
			return null;
		}
	}


	@Override
	protected void initFromChangedProperties() {
		init();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(VIDEO_ENABLED, Boolean.toString(enabled), true);
	}


	public boolean isCoursenodeEnabled() {
		return (coursenodeEnabled && enabled);
	}


	public void setCoursenodeEnabled(boolean coursenodeEnabled) {
		this.coursenodeEnabled = coursenodeEnabled;
		setStringProperty(VIDEOCOURSENODE_ENABLED, Boolean.toString(this.coursenodeEnabled), true);
	}


	public boolean isTranscodingEnabled() {
		return (transcodingEnabled && enabled);
	}

	public void setTranscodingEnabled(boolean transcodingEnabled) {
		this.transcodingEnabled = transcodingEnabled;
		setStringProperty(VIDEOTRANSCODING_ENABLED, Boolean.toString(transcodingEnabled), true);
	}

	public boolean isTranscodingLocal() {		
		return isTranscodingEnabled() && transcodingLocal;
	}

	public void setTranscoding(boolean transcodingLocal) {
		this.transcodingLocal = transcodingLocal;
		setStringProperty(VIDEOTRANSCODING_LOCAL, Boolean.toString(transcodingLocal), true);
	}
	
	public void setVideoTranscodingProfile(String profile) {
		if (StringHelper.containsNonWhitespace(profile)) {
			if (profile.equals("Fast")) {
				this.transcodingProfile = "Fast";
				return;
			} else if (profile.equals("Very Fast")) {
				this.transcodingProfile = "Very Fast";
				return;
			}
		}
		this.transcodingProfile = "Fast"; // default
	}
	
	public String getVideoTranscodingProfile() {
		return this.transcodingProfile;
	}

}
