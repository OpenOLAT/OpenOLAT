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
package org.olat.modules.bigbluebutton.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingWithReference;

/**
 * 
 * Initial date: 7 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonRecordingRow {
	
	private FormLink toolsLink;
	private FormLink publishLink;
	private final boolean published;
	private BigBlueButtonRecordingWithReference recordingReference;
	
	public BigBlueButtonRecordingRow(BigBlueButtonRecordingWithReference recordingReference, boolean published) {
		this.recordingReference = recordingReference;
		this.published = published;
	}
	
	public String getName() {
		return recordingReference.getRecording().getName();
	}
	
	public String getType() {
		return recordingReference.getRecording().getType();
	}
	
	public Date getStart() {
		return recordingReference.getRecording().getStart();
	}
	
	public Date getEnd() {
		return recordingReference.getRecording().getEnd();
	}
	
	public BigBlueButtonRecording getRecording() {
		return recordingReference.getRecording();
	}
	
	public BigBlueButtonRecordingReference getReference() {
		return recordingReference.getReference();
	}

	public boolean isPublished() {
		return published;
	}

	public FormLink getPublishLink() {
		return publishLink;
	}

	public void setPublishLink(FormLink publishLink) {
		this.publishLink = publishLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
