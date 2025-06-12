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
	
	private final int titleSize;
	
	private final boolean withScopes;
	private boolean withFilterPresetPending;
	private boolean withFilterPresetClosed;
	private boolean withFilterPresetRelevant;
	private boolean withFilterPresetWithoutTeachers;

	private boolean withAllMineSwitch;
	private boolean showMineAsDefault;

	private Visibility withRollCall = Visibility.SHOW;
	private Visibility withExternalRef = Visibility.SHOW;
	private Visibility withCurriculum = Visibility.SHOW;
	private Visibility withRepositoryEntry = Visibility.SHOW;
	private Visibility withLocation = Visibility.SHOW;
	private Visibility withCompulsoryPresence = Visibility.SHOW;
	private Visibility withNumberOfParticipants = Visibility.SHOW;
	private Visibility withNumberOfLectures = Visibility.SHOW;
	private Visibility withExam = Visibility.SHOW;
	private Visibility withOnlineMeeting = Visibility.SHOW;
	private Visibility withEdit = Visibility.SHOW;
	
	private boolean withDetailsParticipantsGroups = true;
	private boolean withDetailsRepositoryEntry = true;
	private boolean withDetailsExam = true;
	private boolean withDetailsUnits = true;
	private boolean withDetailsExternalRef = true;
	
	private boolean withinCurriculums = false;
	
	private final String prefsId;
	
	private LectureListRepositoryConfig(String prefsId, int titleSize, boolean withScopes, boolean withFilterPresetRelevant,
			boolean withFilterPresetPending, boolean withFilterPresetClosed) {
		this.prefsId = prefsId;
		this.titleSize = titleSize;
		this.withScopes = withScopes;
		this.withFilterPresetRelevant = withFilterPresetRelevant;
		this.withFilterPresetPending = withFilterPresetPending;
		this.withFilterPresetClosed = withFilterPresetClosed;
	}
	
	public static final LectureListRepositoryConfig repositoryEntryAdminConfig(String prefsId) {
		return new LectureListRepositoryConfig(prefsId, 3, false, true, false, false);
	}
	
	public static final LectureListRepositoryConfig repositoryEntryToolConfig(String prefsId) {
		return new LectureListRepositoryConfig(prefsId, 3, false, true, true, true);
	}

	public static final LectureListRepositoryConfig curriculumConfig(String prefsId) {
		return new LectureListRepositoryConfig(prefsId, 2, true, false, false, false);
	}
	
	public static final LectureListRepositoryConfig curriculumElementConfig(String prefsId) {
		return new LectureListRepositoryConfig(prefsId, 3, false, true, true, true);
	}
	
	public static final LectureListRepositoryConfig coachingConfig(String prefsId) {
		return new LectureListRepositoryConfig(prefsId, 0, true, true, true, true);
	}
	
	public String getPrefsId() {
		return prefsId;
	}
	
	public int getTitleSize() {
		return titleSize;
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
	
	public LectureListRepositoryConfig withFilterPresetPending(boolean presetEnabled) {
		this.withFilterPresetPending = presetEnabled;
		return this;
	}

	public boolean withFilterPresetClosed() {
		return withFilterPresetClosed;
	}
	
	public LectureListRepositoryConfig withFilterPresetClosed(boolean presetEnabled) {
		this.withFilterPresetClosed = presetEnabled;
		return this;
	}

	public boolean withFilterPresetWithoutTeachers() {
		return withFilterPresetWithoutTeachers;
	}

	public LectureListRepositoryConfig withFilterPresetWithoutTeachers(boolean presetEnabled) {
		this.withFilterPresetWithoutTeachers = presetEnabled;
		return this;
	}

	public Visibility withRollCall() {
		return withRollCall;
	}

	public LectureListRepositoryConfig withRollCall(Visibility rollCallEnabled) {
		this.withRollCall = rollCallEnabled;
		return this;
	}
	
	public boolean withAllMineSwitch() {
		return withAllMineSwitch;
	}
	
	public boolean showMineAsDefault() {
		return showMineAsDefault;
	}

	public LectureListRepositoryConfig withAllMineSwitch(boolean allMineSwitch, boolean mineAsDefault) {
		this.withAllMineSwitch = allMineSwitch;
		this.showMineAsDefault = mineAsDefault;
		return this;
	}

	public Visibility withExternalRef() {
		return withExternalRef;
	}

	public LectureListRepositoryConfig withExternalRef(Visibility externalRef) {
		this.withExternalRef = externalRef;
		return this;
	}

	public Visibility withCurriculum() {
		return withCurriculum;
	}

	public LectureListRepositoryConfig withCurriculum(Visibility curriculum) {
		this.withCurriculum = curriculum;
		return this;
	}

	public Visibility withRepositoryEntry() {
		return withRepositoryEntry;
	}

	public LectureListRepositoryConfig withRepositoryEntry(Visibility withRepositoryEntry) {
		this.withRepositoryEntry = withRepositoryEntry;
		return this;
	}

	public Visibility withLocation() {
		return withLocation;
	}

	public LectureListRepositoryConfig withLocation(Visibility withLocation) {
		this.withLocation = withLocation;
		return this;
	}

	public Visibility withCompulsoryPresence() {
		return withCompulsoryPresence;
	}

	public LectureListRepositoryConfig withCompulsoryPresence(Visibility withCompulsoryPresence) {
		this.withCompulsoryPresence = withCompulsoryPresence;
		return this;
	}
	
	public Visibility withNumberOfParticipants() {
		return withNumberOfParticipants;
	}

	public LectureListRepositoryConfig withNumberOfParticipants(Visibility withNumberOfParticipants) {
		this.withNumberOfParticipants = withNumberOfParticipants;
		return this;
	}
	
	public Visibility withNumberOfLectures() {
		return withNumberOfLectures;
	}

	public LectureListRepositoryConfig withNumberOfLectures(Visibility withNumberOfLectures) {
		this.withNumberOfLectures = withNumberOfLectures;
		return this;
	}

	public Visibility withExam() {
		return withExam;
	}

	public LectureListRepositoryConfig withExam(Visibility withExam) {
		this.withExam = withExam;
		return this;
	}

	public Visibility withOnlineMeeting() {
		return withOnlineMeeting;
	}

	public LectureListRepositoryConfig withOnlineMeeting(Visibility withOnlineMeeting) {
		this.withOnlineMeeting = withOnlineMeeting;
		return this;
	}

	public Visibility withEdit() {
		return withEdit;
	}

	/**
	 * 
	 * @param withEdit true to set visibility of the edit column, fi true, security callback will be checked
	 * @return Itself
	 */
	public LectureListRepositoryConfig withEdit(Visibility withEdit) {
		this.withEdit = withEdit;
		return this;
	}

	public boolean withDetailsParticipantsGroups() {
		return withDetailsParticipantsGroups;
	}

	public LectureListRepositoryConfig withDetailsParticipantsGroups(boolean withDetailsParticipantsGroups) {
		this.withDetailsParticipantsGroups = withDetailsParticipantsGroups;
		return this;
	}
	
	public boolean withDetailsRepositoryEntry() {
		return withDetailsRepositoryEntry;
	}

	public LectureListRepositoryConfig withDetailsRepositoryEntry(boolean withDetailsRepositoryEntry) {
		this.withDetailsRepositoryEntry = withDetailsRepositoryEntry;
		return this;
	}

	public boolean withDetailsExam() {
		return withDetailsExam;
	}

	public LectureListRepositoryConfig withDetailsExam(boolean withDetailsExam) {
		this.withDetailsExam = withDetailsExam;
		return this;
	}

	public boolean withDetailsUnits() {
		return withDetailsUnits;
	}

	public LectureListRepositoryConfig withDetailsUnits(boolean withDetailsUnits) {
		this.withDetailsUnits = withDetailsUnits;
		return this;
	}

	public boolean withDetailsExternalRef() {
		return withDetailsExternalRef;
	}

	public LectureListRepositoryConfig withDetailsExternalRef(boolean withDetailsExternalRef) {
		this.withDetailsExternalRef = withDetailsExternalRef;
		return this;
	}
	
	public boolean withinCurriculums() {
		return withinCurriculums;
	}

	public LectureListRepositoryConfig withinCurriculums(boolean withinCurriculums) {
		this.withinCurriculums = withinCurriculums;
		return this;
	}



	public enum Visibility {
		NO,
		HIDE,
		SHOW
	}
}
