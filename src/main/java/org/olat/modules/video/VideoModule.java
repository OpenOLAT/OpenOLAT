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

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * V
 *
 * Initial date: 23.2.2016<br>
 * @author dfakae, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoModule extends AbstractSpringModule {

	private static final String VIDEO_ENABLED = "video.enabled";
	private static final String VIDEOCOURSENODE_ENABLED = "video.coursenode.enabled";
	private static final String VIDEOTRANSCODING_ENABLED = "video.transcoding.enabled";
	@Value("${video.transcoding.provider:handbrake}")
	private String transcodingProvider;

	@Value("${video.enabled:true}")
	private boolean enabled;
	@Value("${video.coursenode.enabled:true}")
	private boolean coursenodeEnabled;
	@Value("${video.transcoding.enabled:false}")
	private boolean transcodingEnabled;


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
}
