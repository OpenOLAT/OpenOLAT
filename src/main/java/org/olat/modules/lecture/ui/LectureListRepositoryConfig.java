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
package org.olat.modules.lecture.ui;

/**
 * 
 * Initial date: 7 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LectureListRepositoryConfig {
	
	private final boolean withScopes;
	private final boolean withFilterPresetPending;
	private final boolean withFilterPresetClosed;
	private final boolean withFilterPresetRelevant;
	
	private LectureListRepositoryConfig(boolean withScopes, boolean withFilterPresetRelevant,
			boolean withFilterPresetPending, boolean withFilterPresetClosed) {
		this.withScopes = withScopes;
		this.withFilterPresetRelevant = withFilterPresetRelevant;
		this.withFilterPresetPending = withFilterPresetPending;
		this.withFilterPresetClosed = withFilterPresetClosed;
	}
	
	public static final LectureListRepositoryConfig repositoryEntryConfig() {
		return new LectureListRepositoryConfig(false, true, true, true);
	}

	public static final LectureListRepositoryConfig curriculumConfig() {
		return new LectureListRepositoryConfig(true, false, false, false);
	}
	
	public static final LectureListRepositoryConfig curriculumElementConfig() {
		return new LectureListRepositoryConfig(false, true, true, true);
	}

	public boolean withScopes() {
		return withScopes;
	}
	
	public boolean withFilterPresetRelevant() {
		return withFilterPresetRelevant;
	}

	public boolean withFilterPresetPending() {
		return withFilterPresetPending;
	}

	public boolean withFilterPresetClosed() {
		return withFilterPresetClosed;
	}
}
