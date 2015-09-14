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
package org.olat.core.id.context;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date:  26 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@Service("historyModule")
public class HistoryModule extends AbstractSpringModule {

	private static final String RESUME_ENABLED_PROP = "resume.enabled";
	private static final String RESUME_ENABLED_DEFAULT_PROP = "resume.enabled.default";

	@Value("${history.resume.enabled:true}")
	private boolean resumeEnabled;
	@Value("${history.resume.enabled.default:ondemand}")
	private String resumeDefaultSetting;
	
	@Autowired
	public HistoryModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String resumeObj = getStringPropertyValue(RESUME_ENABLED_PROP, true);
		if(StringHelper.containsNonWhitespace(resumeObj)) {
			resumeEnabled = "true".equals(resumeObj);
		}
		
		String resumeDefSettings = getStringPropertyValue(RESUME_ENABLED_DEFAULT_PROP, true);
		if(StringHelper.containsNonWhitespace(resumeDefSettings)) {
			resumeDefaultSetting = resumeDefSettings;
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	public boolean isResumeEnabled() {
		return resumeEnabled;
	}
	
	public String getResumeDefaultSetting() {
		return resumeDefaultSetting;
	}

	public void setResumeDefaultSetting(String resumeDefaultSetting) {
		this.resumeDefaultSetting = resumeDefaultSetting;
	}
}
