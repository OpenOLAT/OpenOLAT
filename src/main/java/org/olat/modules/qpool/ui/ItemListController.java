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
package org.olat.modules.qpool.ui;

import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.ui.events.QItemViewEvent;

/**
 * 
 * Initial date: 16.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ItemListController extends AbstractItemListController {
	
	private FormLink selectLink;
	
	public ItemListController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback secCallback, QuestionItemsSource source) {
		super(ureq, wControl, secCallback, source, "qti-select");
		
		initForm(ureq);
	}
	
	public ItemListController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback secCallback, QuestionItemsSource source,
			String restrictToFormat, List<QItemType> excludeTypes) {
		super(ureq, wControl, secCallback, source, restrictToFormat, excludeTypes, "qti-select");
		
		initForm(ureq);
	}
	
	@Override
	protected void initButtons(UserRequest ureq, FormItemContainer formLayout) {
		getItemsTable().setMultiSelect(true);
		selectLink = uifactory.addFormLink("select-to-import", "select", null, formLayout, Link.BUTTON);
	}

	@Override
	protected void initActionColumns(FlexiTableColumnModel columnsModel) {
		DefaultFlexiColumnModel selectCol = new DefaultFlexiColumnModel("select", translate("select"), "select-item");
		selectCol.setExportable(false);
		columnsModel.addFlexiColumnModel(selectCol);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectLink == source) {
			Set<Integer> selections = getItemsTable().getMultiSelectedIndex();
			if(!selections.isEmpty()) {
				List<QuestionItemView> items = getItemViews(selections);
				fireEvent(ureq, new QItemViewEvent("select-item", items));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	public int updateList() {
		return 0;
	}
	
	@Override
	protected void doSelect(UserRequest ureq, ItemRow row) {
		if(row == null) {
			showWarning("error.select.one");
		} else {
			fireEvent(ureq, new QItemViewEvent("select-item", row));
		}
	}
}
