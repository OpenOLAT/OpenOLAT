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
package org.olat.modules.todo.ui;

import java.util.Collection;
import java.util.List;

import org.olat.core.id.Identity;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public final class ToDoTaskMemberConfig {

	// read-only: not editable, always displayed
	// disabled: not editable, displayed if at least one but not me selected
	public enum MemberSelection { search, candidatesSingle, candidates, readOnly, disabled }

	private final MemberSelection selection;
	private final Collection<Identity> candidates;
	private final ToDoTaskMemberSearchProvider searchProvider;
	private final boolean mandatory;

	private ToDoTaskMemberConfig(MemberSelection selection, Collection<Identity> candidates,
			ToDoTaskMemberSearchProvider searchProvider, boolean mandatory) {
		this.selection = selection;
		this.candidates = candidates;
		this.searchProvider = searchProvider;
		this.mandatory = mandatory;
	}

	public static ToDoTaskMemberConfig disabled() {
		return new ToDoTaskMemberConfig(MemberSelection.disabled, List.of(), null, true);
	}

	public static ToDoTaskMemberConfig readOnly() {
		return new ToDoTaskMemberConfig(MemberSelection.readOnly, List.of(), null, true);
	}

	public static ToDoTaskMemberConfig search(ToDoTaskMemberSearchProvider searchProvider) {
		return new ToDoTaskMemberConfig(MemberSelection.search, List.of(), searchProvider, true);
	}

	public static ToDoTaskMemberConfig search(Collection<Identity> candidates,
			ToDoTaskMemberSearchProvider searchProvider) {
		return new ToDoTaskMemberConfig(MemberSelection.search, candidates, searchProvider, true);
	}

	public static ToDoTaskMemberConfig candidates(Collection<Identity> candidates) {
		return new ToDoTaskMemberConfig(MemberSelection.candidates, candidates, null, true);
	}

	public static ToDoTaskMemberConfig candidatesSingle(Collection<Identity> candidates) {
		return new ToDoTaskMemberConfig(MemberSelection.candidatesSingle, candidates, null, true);
	}

	public ToDoTaskMemberConfig notMandatory() {
		return new ToDoTaskMemberConfig(selection, candidates, searchProvider, false);
	}

	public MemberSelection getSelection() {
		return selection;
	}

	public Collection<Identity> getCandidates() {
		return candidates;
	}

	public ToDoTaskMemberSearchProvider getSearchProvider() {
		return searchProvider;
	}

	public boolean isMandatory() {
		return mandatory;
	}

}
