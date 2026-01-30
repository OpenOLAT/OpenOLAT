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
package org.olat.modules.certificationprogram.ui;

import java.time.LocalDateTime;

import org.olat.modules.certificationprogram.CertificationProgramLog;
import org.olat.modules.certificationprogram.CertificationProgramLogAction;

/**
 * 
 * Initial date: 6 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramLogRow {
	
	private String object;
	private String message;
	private String originalValue;
	private String newValue;
	private String actor;
	private Long actorKey;
	private Long memberKey;
	
	private final CertificationProgramActivityLogContext context;
	private final CertificationProgramLog certificationProgramLog;

	public CertificationProgramLogRow(CertificationProgramLog certificationProgramLog, CertificationProgramActivityLogContext context,
			String object, Long memberKey, String message, String actor, Long actorKey) {
		this.actor = actor;
		this.object = object;
		this.message = message;
		this.context = context;
		this.actorKey = actorKey;
		this.memberKey = memberKey;
		this.certificationProgramLog = certificationProgramLog;
	}
	
	public LocalDateTime getCreationDate() {
		return certificationProgramLog.getCreationDate();
	}
	
	public CertificationProgramLogAction getAction() {
		return certificationProgramLog.getAction();
	}

	public String getObject() {
		return object;
	}

	public String getMessage() {
		return message;
	}

	public String getActor() {
		return actor;
	}

	public String getOriginalValue() {
		return originalValue;
	}

	public void setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public CertificationProgramActivityLogContext getContext() {
		return context;
	}

	public Long getActorKey() {
		return actorKey;
	}

	public Long getMemberKey() {
		return memberKey;
	}
}
