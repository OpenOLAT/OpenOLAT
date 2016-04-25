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
import java.util.Arrays;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
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

	private static final String VIDEO_ENABLED = "video.enabled";
	private static final String VIDEOCOURSENODE_ENABLED = "video.coursenode.enabled";
	private static final String VIDEOTRANSCODING_ENABLED = "video.transcoding.enabled";

	@Value("${video.enabled:true}")
	private boolean enabled;
	@Value("${video.coursenode.enabled:true}")
	private boolean coursenodeEnabled;
	// transcoding related configuration
	@Value("${video.transcoding.enabled:false}")
	private boolean transcodingEnabled;
	@Value("${video.transcoding.resolutions}")
	private int[] transcodingResolutions;
	@Value("${video.transcoding.taskset.cpuconfig}")
	private String transcodingTasksetConfig;
	@Value("${video.transcoding.dir}")
	private String transcodingDir;
	
	
	@Bean
	public static ConversionService conversionService() {
		// needed to create the transcodingResolutions (int[]) property by spring
	    return new DefaultFormattingConversionService();
	}
	
	private static final OLog log = Tracing.createLoggerFor(VideoModule.class);

	@Autowired
	public VideoModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
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

		log.info("video.enabled=" + isEnabled());
		log.info("video.coursenode.enabled=" + isCoursenodeEnabled());
		log.info("video.transcoding.enabled=" + isTranscodingEnabled());
		log.info("video.transcoding.resolutions=" + Arrays.toString(getTranscodingResolutions()));
		log.info("video.transcoding.taskset.cpuconfig=" + getTranscodingTasksetConfig());
	}

	/**
	 * The values represent the target height of the transcoded video, 1080 for
	 * 1080p video size etc. This config can only be set in
	 * olat.localproperties, see "video.transcoding.resolutions"
	 * 
	 * @return Array of transcoding resolutions.
	 */
	public int[] getTranscodingResolutions() {
		return transcodingResolutions;
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
			log.error("Error, no valid transcoding dir. Disabling transcoding. video.transcoding.dir=" + transcodingDir);
			// only disable variabe, don't store it in persisted properties
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
		//TODO: check all video resources if there are missing versions
	}
}
