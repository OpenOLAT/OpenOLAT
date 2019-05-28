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
package org.olat.course.nodes.livestream;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LiveStreamModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String LIVE_STREAM_ENABLED = "live.stream.enabled";
	public static final String LIVE_STREAM_BUFFER_BEFORE_MIN = "live.stream.buffer.before.min";
	public static final String LIVE_STREAM_BUFFER_AFTER_MIN = "live.stream.buffer.after.min";

	@Value("${live.stream.enabled:false}")
	private boolean enabled;
	@Value("${live.stream.buffer.before.min:5}")
	private int bufferBeforeMin;
	@Value("${live.stream.buffer.after.min:5}")
	private int bufferAfterMin;
			
	@Autowired
	public LiveStreamModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(LIVE_STREAM_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String bufferBeforeMinObj = getStringPropertyValue(LIVE_STREAM_BUFFER_BEFORE_MIN, true);
		if(StringHelper.containsNonWhitespace(bufferBeforeMinObj)) {
			bufferAfterMin = Integer.parseInt(bufferBeforeMinObj);
		}

		String bufferAfterMinObj = getStringPropertyValue(LIVE_STREAM_BUFFER_AFTER_MIN, true);
		if(StringHelper.containsNonWhitespace(bufferAfterMinObj)) {
			bufferAfterMin = Integer.parseInt(bufferAfterMinObj);
		}
	}
	
	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(LIVE_STREAM_ENABLED, Boolean.toString(enabled), true);
	}

	public int getBufferBeforeMin() {
		return bufferBeforeMin;
	}

	public void setBufferBeforeMin(int bufferBeforeMin) {
		this.bufferBeforeMin = bufferBeforeMin;
		setStringProperty(LIVE_STREAM_BUFFER_BEFORE_MIN, Integer.toString(bufferBeforeMin), true);
	}

	public int getBufferAfterMin() {
		return bufferAfterMin;
	}

	public void setBufferAfterMin(int bufferAfterMin) {
		this.bufferAfterMin = bufferAfterMin;
		setStringProperty(LIVE_STREAM_BUFFER_BEFORE_MIN, Integer.toString(bufferAfterMin), true);
	}

}
