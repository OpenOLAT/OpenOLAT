/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a synthetic status to build the status filter in UI
 * 
 * Initial date: 1 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CoachedParticipantStatus {
	open("o_process_status_active", "msg.status.active"),
	waiting("o_process_status_waiting", "msg.status.waiting"),
	expired("o_process_status_expired", "msg.status.expired"),
	late("o_process_status_late", "msg.status.late"),
	done("o_process_status_done", "msg.status.done"),
	revisionAvailable("o_process_status_active", "msg.status.active"),
	solutionAvailable("o_process_status_done", "msg.status.available"),
	notAvailable("o_process_status_notavailable", "msg.status.not.available");
	
	private final String i18nKey;
	private final String cssClass;
	
	private CoachedParticipantStatus(String cssClass, String i18nKey) {
		this.i18nKey = i18nKey;
		this.cssClass = cssClass;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public String cssClass() {
		return cssClass;
	}
	
	public static List<CoachedParticipantStatus> toList(List<String> values) {
		if(values == null || values.isEmpty()) return new ArrayList<>();
		
		List<CoachedParticipantStatus> statusList = new ArrayList<>(values.size());
		for(String value:values) {
			CoachedParticipantStatus status = secureValueOf(value, null);
			if(status != null) {
				statusList.add(status);
			}
		}
		return statusList;
	}
	
	public static CoachedParticipantStatus secureValueOf(String val, CoachedParticipantStatus defaultValue) {
		for(CoachedParticipantStatus status:values()) {
			if(status.name().equals(val)) {
				return status;
			}
		}
		return defaultValue;
	}

}
