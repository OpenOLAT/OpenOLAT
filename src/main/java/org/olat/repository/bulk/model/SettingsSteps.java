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
package org.olat.repository.bulk.model;

import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.olat.repository.bulk.SettingsBulkEditable;

/**
 * 
 * Initial date: 17 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SettingsSteps {
	
	public enum Step {
		steps,
		metadata,
		taxonomy,
		organisation,
		authorRights,
		execution,
		toolbar,
		options,
		overview
	}
	
	private static final List<Step> DEFAULT_STEPS = List.of(Step.steps, Step.overview);
	public static final int SELECTABLE_STEPS_SIZE = Step.values().length - DEFAULT_STEPS.size();
	private static final Set<SettingsBulkEditable> EDITABLES_METADATA = Set.of(
			SettingsBulkEditable.authors,
			SettingsBulkEditable.educationalType,
			SettingsBulkEditable.mainLanguage,
			SettingsBulkEditable.expenditureOfWork,
			SettingsBulkEditable.license);
	private static final Set<SettingsBulkEditable> EDITABLES_TAXONOMY = Set.of(
			SettingsBulkEditable.taxonomyLevelsAdd,
			SettingsBulkEditable.taxonomyLevelsRemove);
	private static final Set<SettingsBulkEditable> EDITABLES_ORAGANISATION = Set.of(
			SettingsBulkEditable.organisationsAdd,
			SettingsBulkEditable.organisationsRemove);
	private static final Set<SettingsBulkEditable> EDITABLES_AUTHOR_RIGHTS = Set.of(
			SettingsBulkEditable.authorRightReference,
			SettingsBulkEditable.authorRightCopy,
			SettingsBulkEditable.authorRightDownload);
	private static final Set<SettingsBulkEditable> EDITABLES_EXECUTION = Set.of(
			SettingsBulkEditable.location,
			SettingsBulkEditable.lifecycleType);
	private static final Set<SettingsBulkEditable> EDITABLES_TOOLBAR = Set.of(
			SettingsBulkEditable.toolSearch,
			SettingsBulkEditable.toolCalendar,
			SettingsBulkEditable.toolParticipantList,
			SettingsBulkEditable.toolParticipantInfo,
			SettingsBulkEditable.toolEmail,
			SettingsBulkEditable.toolTeams,
			SettingsBulkEditable.toolBigBlueButton,
			SettingsBulkEditable.toolBigBlueButtonModeratorStartsMeeting,
			SettingsBulkEditable.toolZoom,
			SettingsBulkEditable.toolBlog,
			SettingsBulkEditable.toolWiki,
			SettingsBulkEditable.toolForum,
			SettingsBulkEditable.toolDocuments,
			SettingsBulkEditable.toolChat);
	
	public static Set<SettingsBulkEditable> getEditables(Step step) {
		switch (step) {
		case metadata: return EDITABLES_METADATA;
		case taxonomy: return EDITABLES_TAXONOMY;
		case organisation: return EDITABLES_ORAGANISATION;
		case authorRights: return EDITABLES_AUTHOR_RIGHTS;
		case execution: return EDITABLES_EXECUTION;
		case toolbar: return EDITABLES_TOOLBAR;
		default:
			return Set.of();
		}
	}
	
	private NavigableSet<Step> steps = new TreeSet<>();
	
	public void reset() {
		steps.clear();
		steps.addAll(DEFAULT_STEPS);
	}
	
	public void add(Step step) {
		steps.add(step);
	}
	
	public Step getNext(Step step) {
		if (step != null && steps.contains(step)) {
			Step next = steps.higher(step);
			if (next != null) {
				return next;
			}
		}
		return Step.overview;
	}
	
	public boolean contains(Step step) {
		return steps.contains(step);
	}

	public Object size() {
		return steps.size();
	}
	
	public Stream<Step> stream() {
		return steps.stream();
	}

}
