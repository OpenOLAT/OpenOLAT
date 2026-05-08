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
package org.olat.modules.todo.ui;

import org.olat.core.gui.components.util.SelectionValues;

/**
 * Initial date: 2026-05-08<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public final class ToDoTaskDateConfig {

	public enum DateSelection { absoluteOnly, absoluteOrRelative }

	private final DateSelection selection;
	private final SelectionValues relativeRefs;
	private final SelectionValues sameDayRefs;
	private final ToDoDateResolver resolver;

	private ToDoTaskDateConfig(DateSelection selection, SelectionValues relativeRefs,
			SelectionValues sameDayRefs, ToDoDateResolver resolver) {
		this.selection = selection;
		this.relativeRefs = relativeRefs;
		this.sameDayRefs = sameDayRefs;
		this.resolver = resolver;
	}

	public static ToDoTaskDateConfig absoluteOnly() {
		return new ToDoTaskDateConfig(DateSelection.absoluteOnly, null, null, null);
	}

	/**
	 * @param relativeRefs refs shown when unit != SAME_DAY; key = ref string constant,
	 *                     value = translated label (include any suffix like "of the execution period")
	 * @param sameDayRefs  refs shown when unit == SAME_DAY; same key/value convention
	 * @param resolver     converts a (refKey, unit, value) triple to an absolute date
	 */
	public static ToDoTaskDateConfig absoluteOrRelative(SelectionValues relativeRefs,
			SelectionValues sameDayRefs, ToDoDateResolver resolver) {
		return new ToDoTaskDateConfig(DateSelection.absoluteOrRelative, relativeRefs, sameDayRefs, resolver);
	}

	public DateSelection getSelection() {
		return selection;
	}

	public SelectionValues getRelativeRefs() {
		return relativeRefs;
	}

	public SelectionValues getSameDayRefs() {
		return sameDayRefs;
	}

	public ToDoDateResolver getResolver() {
		return resolver;
	}

}
