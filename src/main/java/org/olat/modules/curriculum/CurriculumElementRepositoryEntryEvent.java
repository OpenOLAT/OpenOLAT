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
package org.olat.modules.curriculum;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 9 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRepositoryEntryEvent extends MultiUserEvent {

	private static final long serialVersionUID = 4794247345720510725L;
	
	public static final String REPOSITORY_ENTRY_ADDED = "curriculum.element.repository.entry.added.event";
	
	private final Long curriculumElementKey;
	private final Long entryKey;
	
	public CurriculumElementRepositoryEntryEvent(String command, Long repositoryEntries, Long entryKey) {
		super(command);
		this.curriculumElementKey = repositoryEntries;
		this.entryKey = entryKey;
	}

	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}

	public Long getEntryKey() {
		return entryKey;
	}
}
