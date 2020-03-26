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
package org.olat.modules.bigbluebutton.model;

import java.util.Date;

import org.olat.modules.bigbluebutton.BigBlueButtonRecording;

/**
 * 
 * Initial date: 23 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonRecordingImpl implements BigBlueButtonRecording {
	
	private final String recordId;
	private final String url;
	private final String type;
	private final String name;
	private final String meetingId;
	
	private final Date start;
	private final Date end;
	
	private BigBlueButtonRecordingImpl(String recordId, String name, String meetingId, Date start, Date end, String url, String type) {
		this.recordId = recordId;
		this.url = url;
		this.type = type;
		this.start = start;
		this.end = end;
		this.name = name;
		this.meetingId = meetingId;
	}
	
	public static BigBlueButtonRecording valueOf(String recordId, String name, String meetingId, Date start, Date end, String url, String type) {
		return new BigBlueButtonRecordingImpl(recordId, name, meetingId, start, end, url, type);
	}
	
	@Override
	public String getRecordId() {
		return recordId;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getMeetingId() {
		return meetingId;
	}

	@Override
	public Date getStart() {
		return start;
	}

	@Override
	public Date getEnd() {
		return end;
	}
}
