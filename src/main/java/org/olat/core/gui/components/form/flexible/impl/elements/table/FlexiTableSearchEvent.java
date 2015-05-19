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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.List;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * 
 * Initial date: 11.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableSearchEvent extends FormEvent {

	private static final long serialVersionUID = -1977791683080030187L;
	public static final String SEARCH = "ftSearch";
	public static final String RESET = "ftResetSearch";

	private final String search;
	private final List<String> condQueries;

	public FlexiTableSearchEvent(FormItem source, String search, List<String> condQueries, int action) {
		super(SEARCH, source, action);
		this.search = search;
		this.condQueries = condQueries;
	}
	
	public FlexiTableSearchEvent(FormItem source, int action) {
		super(RESET, source, action);
		this.search = null;
		this.condQueries = null;
	}

	public String getSearch() {
		return search;
	}

	public List<String> getCondQueries() {
		return condQueries;
	}
}
