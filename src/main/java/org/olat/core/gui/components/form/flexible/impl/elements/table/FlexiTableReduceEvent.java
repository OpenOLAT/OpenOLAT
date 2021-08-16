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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableReduceEvent extends FormEvent {

	private static final long serialVersionUID = -4123918266379346895L;
	
	public static final String SEARCH = "ftSearch";
	public static final String QUICK_SEARCH = "ftQuickSearch";
	public static final String QUICK_SEARCH_KEY_SELECTION = "ftQuickSearchSelectKey";
	public static final String FILTER = "ftFilter";
	public static final String EXTENDED_FILTER = "ftEXTENDEDFILTER";

	private final String search;
	private final List<FlexiTableFilter> filters;

	public FlexiTableReduceEvent(String cmd, FormItem source, String search,
			List<FlexiTableFilter> filters, int action) {
		super(cmd, source, action);
		this.search = search;
		this.filters = filters;
	}
	
	public FlexiTableReduceEvent(FormItem source, int action) {
		super(RESET, source, action);
		this.search = null;
		this.filters = null;
	}

	public String getSearch() {
		return search;
	}

	public List<FlexiTableFilter> getFilters() {
		return filters;
	}
}
