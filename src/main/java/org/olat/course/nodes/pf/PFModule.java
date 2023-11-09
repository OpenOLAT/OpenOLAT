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
package org.olat.course.nodes.pf;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Module containing default config values for PFCourseNode
 * Initial date: Nov 01, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class PFModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String CONFIG_KEY_PF_ENABLED = "participant.folder.enabled";
	public static final String CONFIG_KEY_PARTICIPANTBOX = "participant.folder.participantbox";
	public static final String CONFIG_KEY_COACHBOX = "participant.folder.coachbox";
	public static final String CONFIG_KEY_ALTERFILE = "participant.folder.alterfile";
	public static final String CONFIG_KEY_LIMITCOUNT = "participant.folder.limitcount";
	public static final String CONFIG_KEY_FILECOUNT = "participant.folder.filecount";

	@Value("${participant.folder.enabled:true}")
	private boolean enabled;
	@Value("${participant.folder.participantbox:true}")
	private boolean hasParticipantBox;
	@Value("${participant.folder.coachbox:true}")
	private boolean hasCoachBox;
	@Value("${participant.folder.alterfile:true}")
	private boolean canAlterFile;
	@Value("${participant.folder.limitcount:false}")
	private boolean canLimitCount;
	@Value("${participant.folder.filecount:0}")
	private int fileCount;

	public PFModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		setDefaultProperties();
		updateProperties();
	}

	public void updateProperties() {
		String enabledObj;

		enabledObj = getStringPropertyValue(CONFIG_KEY_PF_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_PARTICIPANTBOX, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasParticipantBox = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_COACHBOX, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			hasCoachBox = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_ALTERFILE, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			canAlterFile = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_KEY_LIMITCOUNT, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			canLimitCount = "true".equals(enabledObj);
		}
		fileCount = getIntPropertyValue(CONFIG_KEY_FILECOUNT, 0);
	}

	public void setDefaultProperties() {
		setStringPropertyDefault(CONFIG_KEY_PARTICIPANTBOX, hasParticipantBox ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_COACHBOX, hasCoachBox ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_ALTERFILE, canAlterFile ? "true" : "false");
		setStringPropertyDefault(CONFIG_KEY_LIMITCOUNT, canLimitCount ? "true" : "false");
		setIntPropertyDefault(CONFIG_KEY_FILECOUNT, 0);
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(CONFIG_KEY_PF_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean hasParticipantBox() {
		return hasParticipantBox;
	}

	public void setHasParticipantBox(boolean hasParticipantBox) {
		this.hasParticipantBox = hasParticipantBox;
		setStringProperty(CONFIG_KEY_PARTICIPANTBOX, Boolean.toString(hasParticipantBox), true);
	}

	public boolean hasCoachBox() {
		return hasCoachBox;
	}

	public void setHasCoachBox(boolean hasCoachBox) {
		this.hasCoachBox = hasCoachBox;
		setStringProperty(CONFIG_KEY_COACHBOX, Boolean.toString(hasCoachBox), true);
	}

	public boolean canAlterFile() {
		return canAlterFile;
	}

	public void setCanAlterFile(boolean canAlterFile) {
		this.canAlterFile = canAlterFile;
		setStringProperty(CONFIG_KEY_ALTERFILE, Boolean.toString(canAlterFile), true);
	}

	public boolean canLimitCount() {
		return canLimitCount;
	}

	public void setCanLimitCount(boolean canLimitCount) {
		this.canLimitCount = canLimitCount;
		setStringProperty(CONFIG_KEY_LIMITCOUNT, Boolean.toString(canLimitCount), true);
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
		setIntProperty(CONFIG_KEY_FILECOUNT, fileCount, true);
	}

	public void resetProperties() {
		removeProperty(CONFIG_KEY_PARTICIPANTBOX, true);
		removeProperty(CONFIG_KEY_COACHBOX, true);
		removeProperty(CONFIG_KEY_ALTERFILE, true);
		removeProperty(CONFIG_KEY_LIMITCOUNT, true);
		removeProperty(CONFIG_KEY_FILECOUNT, true);
		updateProperties();
	}
}
