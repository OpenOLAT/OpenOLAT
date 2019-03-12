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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 23.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExtendedFilterController extends FormBasicController {
	
	private final List<FlexiTableFilter> filters;
	
	public ExtendedFilterController(UserRequest ureq, WindowControl wControl, List<FlexiTableFilter> filters) {
		super(ureq, wControl, "extended_filters");
		this.filters = new ArrayList<>(filters);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int count = 0;
		List<ExtendedFilter> filterNames = new ArrayList<>(filters.size());
		for(FlexiTableFilter filter:filters) {
			String name = "f-" + (++count);
			if(FlexiTableFilter.SPACER.equals(filter)) {
				filterNames.add(new ExtendedFilter());
			} else {
				FormLink filterLink = uifactory.addFormLink(name, formLayout, Link.LINK | Link.NONTRANSLATED);
				filterLink.setI18nKey(filter.getLabel());
				filterLink.setIconLeftCSS(filter.getIconLeftCSS());
				filterLink.setUserObject(filter);
				filterNames.add(new ExtendedFilter(filter, name));
			}
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("filters", filterNames);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			Object uobject = link.getUserObject();
			if(uobject instanceof FlexiTableFilter) {
				FlexiTableFilter filter = (FlexiTableFilter)uobject;
				if(filter.isShowAll()) {
					for(FlexiTableFilter f:filters) {
						f.setSelected(false);
					}
				} else {
					filter.setSelected(!filter.isSelected());
				}
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
		super.formInnerEvent(ureq, source, event);
	}
	
	public List<FlexiTableFilter> getFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>();
		for(FlexiTableFilter filter:filters) {
			if(filter.isSelected()) {
				selectedFilters.add(filter);
			}
		}
		return selectedFilters;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public static final class ExtendedFilter {
		
		private final FlexiTableFilter filter;
		private final String componentName;
		private final boolean spacer;
		
		public ExtendedFilter() {
			spacer = true;
			filter = null;
			componentName = null;
		}
		
		public ExtendedFilter(FlexiTableFilter filter, String componentName) {
			spacer = false;
			this.filter = filter;
			this.componentName = componentName;
		}
		
		public boolean isSpacer() {
			return spacer;
		}

		public FlexiTableFilter getFilter() {
			return filter;
		}
		
		public boolean isSelected() {
			return filter.isSelected();
		}

		public String getComponentName() {
			return componentName;
		}
	}
}
