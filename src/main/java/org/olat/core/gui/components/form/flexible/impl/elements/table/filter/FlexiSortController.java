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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 27 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiSortController extends FormBasicController {
	
	private int count = 0;
	private final SortKey selectedSortKey;
	private final FlexiTableElementImpl tableEl;
	
	public FlexiSortController(UserRequest ureq, WindowControl wControl,
			FlexiTableElementImpl tableEl, SortKey selectedSortKey) {
		super(ureq, wControl, "sort_setting");
		this.tableEl = tableEl;
		this.selectedSortKey = selectedSortKey;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<FlexiTableSort> sorts = tableEl.getSorts();
		List<Object> sortLinks = new ArrayList<>(sorts.size());
		for(FlexiTableSort sort:sorts) {
			if(FlexiTableSort.SPACER.equals(sort)) {
				sortLinks.add("DIVIDER");
			} else {
				String id = "sort_lnk_" + (count++);
				FormLink link = uifactory.addFormLink(id, sort.getLabel(), null, formLayout, Link.LINK | Link.NONTRANSLATED);
				link.setUserObject(sort);
				if(selectedSortKey != null && selectedSortKey.getKey().equals(sort.getSortKey().getKey())) {
					if(selectedSortKey.isAsc()) {
						link.setIconLeftCSS("o_icon o_icon_sort_asc");
					} else {
						link.setIconLeftCSS("o_icon o_icon_sort_desc");
					}
				}
				sortLinks.add(link);
			}
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("sorters", sortLinks);
		}
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink && ((FormLink)source).getUserObject() instanceof FlexiTableSort) {
			FlexiTableSort sort = (FlexiTableSort)((FormLink)source).getUserObject();
			doSelectSortKey(ureq, sort);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSelectSortKey(UserRequest ureq, FlexiTableSort sort) {
		boolean asc = true;
		if(selectedSortKey != null && selectedSortKey.getKey().equals(sort.getSortKey().getKey())) {
			asc = !selectedSortKey.isAsc();
		}
		SortKey sortKey = new SortKey(sort.getSortKey().getKey(), asc);
		fireEvent(ureq, new CustomizeSortEvent(sortKey));
	}
}
