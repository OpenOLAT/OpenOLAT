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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.SearchElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * Fired by a {@link SearchElement}.
 *
 * Initial date: 2026-08-25<br>
 * @author uhensler, https://www.frentix.com
 */
public class SearchFormEvent extends FormEvent {

	private static final long serialVersionUID = 7691234098123456789L;

	public static final String SEARCH = "search-element-search";
	public static final String RESET = "search-element-reset";

	private final String searchText;

	public SearchFormEvent(String command, FormItem source, String searchText) {
		super(command, source, FormEvent.ONCHANGE);
		this.searchText = searchText;
	}

	public String getSearchText() {
		return searchText;
	}

}
