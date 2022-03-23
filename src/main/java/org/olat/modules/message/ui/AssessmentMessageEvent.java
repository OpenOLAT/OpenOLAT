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
package org.olat.modules.message.ui;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 15 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageEvent extends MultiUserEvent {
	
	private static final long serialVersionUID = -3346767260826513872L;
	
	public static final String READ = "message-read";
	public static final String PUBLISHED = "message-published";
	public static final String DELETED = "message-deleted";
	
	private Long messageKey;
	private String resSubPath;
	private Long repositoryEntryKey;
	
	private Long emitter;
	
	public AssessmentMessageEvent(String name, Long messageKey, Long repositoryEntryKey, String resSubPath, Long emitter) {
		super(name);
		this.repositoryEntryKey = repositoryEntryKey;
		this.resSubPath = resSubPath;
		this.messageKey = messageKey;
		this.emitter = emitter;
	}
	
	public Long getMessageKey() {
		return messageKey;
	}

	public String getResSubPath() {
		return resSubPath;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public Long getEmitter() {
		return emitter;
	}

	public void setEmitter(Long emitter) {
		this.emitter = emitter;
	}
}
