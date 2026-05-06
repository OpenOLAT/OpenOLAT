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

import org.olat.modules.todo.ToDoContext;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public final class ToDoTaskContextConfig {
	
	public enum ContextSelection {
		off, dropdown, picker
	}

	private final ContextSelection selection;
	private final Collection<ToDoContext> availableContexts;
	private final ToDoContext currentContext;
	private final ToDoTaskContextPicker picker;

	private ToDoTaskContextConfig(ContextSelection selection, Collection<ToDoContext> availableContexts,
			ToDoContext currentContext, ToDoTaskContextPicker picker) {
		this.selection = selection;
		this.availableContexts = availableContexts;
		this.currentContext = currentContext;
		this.picker = picker;
	}

	public static ToDoTaskContextConfig off(ToDoContext currentContext) {
		return new ToDoTaskContextConfig(ContextSelection.off, List.of(), currentContext, null);
	}

	public static ToDoTaskContextConfig dropdown(Collection<ToDoContext> availableContexts,
			ToDoContext currentContext) {
		return new ToDoTaskContextConfig(ContextSelection.dropdown, availableContexts, currentContext, null);
	}

	public static ToDoTaskContextConfig picker(ToDoTaskContextPicker picker, ToDoContext currentContext) {
		return new ToDoTaskContextConfig(ContextSelection.picker, List.of(), currentContext, picker);
	}

	public ContextSelection getSelection() {
		return selection;
	}

	public Collection<ToDoContext> getAvailableContexts() {
		return availableContexts;
	}

	public ToDoContext getCurrentContext() {
		return currentContext;
	}

	public ToDoTaskContextPicker getPicker() {
		return picker;
	}

}
