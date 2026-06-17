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

import org.olat.user.IdentitySelectionSource;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public final class ToDoTaskMemberConfig {

	// readOnly: not editable, always displayed
	// disabled: not editable, displayed only if more than "me" is selected
	public enum MemberSelection { editable, readOnly, disabled }

	private final MemberSelection selection;
	private final boolean multiSelection;
	private final boolean mandatory;
	private final IdentitySelectionSource source;

	private ToDoTaskMemberConfig(MemberSelection selection, boolean multiSelection,
			boolean mandatory, IdentitySelectionSource source) {
		this.selection = selection;
		this.multiSelection = multiSelection;
		this.mandatory = mandatory;
		this.source = source;
	}

	public static ToDoTaskMemberConfig disabled(IdentitySelectionSource source, boolean mandatory) {
		return new ToDoTaskMemberConfig(MemberSelection.disabled, true, mandatory, source);
	}

	public static ToDoTaskMemberConfig readOnly(IdentitySelectionSource source, boolean mandatory) {
		return new ToDoTaskMemberConfig(MemberSelection.readOnly, true, mandatory, source);
	}

	public static ToDoTaskMemberConfig editable(IdentitySelectionSource source, boolean mandatory) {
		return new ToDoTaskMemberConfig(MemberSelection.editable, true, mandatory, source);
	}

	public static ToDoTaskMemberConfig editableSingle(IdentitySelectionSource source, boolean mandatory) {
		return new ToDoTaskMemberConfig(MemberSelection.editable, false, mandatory, source);
	}

	public MemberSelection getSelection() {
		return selection;
	}

	public boolean isMultiSelection() {
		return multiSelection;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public IdentitySelectionSource getSource() {
		return source;
	}

}
