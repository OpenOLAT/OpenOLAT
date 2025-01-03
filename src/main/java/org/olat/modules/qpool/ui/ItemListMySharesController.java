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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.ui.datasource.EmptyItemsSource;
import org.olat.modules.qpool.ui.datasource.PoolItemsSource;
import org.olat.modules.qpool.ui.datasource.SharedItemsSource;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ItemListMySharesController extends AbstractItemListController {

	private FormLink selectLink;
    private SingleSelection myShareEl;

	private List<Pool> myPools;
	private List<BusinessGroup> myGroups;
	
	@Autowired
	private QuestionPoolModule qpoolModule;

	public ItemListMySharesController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback secCallback,
			String restrictToFormat, List<QItemType> excludeTypes) {
		super(ureq, wControl, secCallback, new EmptyItemsSource(),
				DefaultSearchSettings.itemList(restrictToFormat, excludeTypes, false), "qti-select");
	}
	
	@Override
	protected void initButtons(UserRequest ureq, FormItemContainer formLayout) {
		getItemsTable().setMultiSelect(true);
		selectLink = uifactory.addFormLink("select-to-import", "select", null, formLayout, Link.BUTTON);
		
		Roles roles = ureq.getUserSession().getRoles();
		if(qpoolModule.isPoolsEnabled()) {
			myPools = qpoolService.getPools(getIdentity(), roles);
		} else {
			myPools = Collections.emptyList();
		}

		if(qpoolModule.isSharesEnabled()) {
			myGroups = qpoolService.getResourcesWithSharedItems(getIdentity());
		} else {
			myGroups = Collections.emptyList();
		}

		String[] myShareKeys;
		String[] myShareValues;
		if(myPools.isEmpty() && myGroups.isEmpty()) {
			myShareKeys = new String[1];
			myShareValues = new String[1];
			myShareKeys[0] = "";
			myShareValues[0] = "";
		} else {
			int numOfShares = myPools.size() + myGroups.size();

			myShareKeys = new String[numOfShares];
			myShareValues = new String[numOfShares];
			int pos = 0;
			for (Pool myPool : myPools) {
				myShareKeys[pos] = "pool" + myPool.getKey().toString();
				myShareValues[pos++] = myPool.getName();
			}
			for (BusinessGroup group : myGroups) {
				myShareKeys[pos] = "grou" + group.getKey().toString();
				myShareValues[pos++] = group.getName();
			}
		}

		myShareEl = uifactory.addDropdownSingleselect("source.selector", "my.share", formLayout, myShareKeys, myShareValues, null);
		myShareEl.setDomReplacementWrapperRequired(false);
		myShareEl.addActionListener(FormEvent.ONCHANGE);
		if(myPools.isEmpty() && myGroups.isEmpty()) {
			myShareEl.setEnabled(false);
		} else {
			myShareEl.select(myShareKeys[0], true);
			if(!myPools.isEmpty()) {
				doSelectPool(ureq, myPools.get(0));
			} else if(!myGroups.isEmpty()) {
				doSelectBusinessGroup(ureq, myGroups.get(0));
			}
		}
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
		} else if(myShareEl == source) {
            String selectedKey = myShareEl.getSelectedKey();
            if(selectedKey != null && selectedKey.length() > 4) {
            	String key = selectedKey.substring(4);
				if(StringHelper.isLong(key)) {
					Long resourceKey = Long.parseLong(key);
					if(selectedKey.startsWith("pool")) {
						doSelectPool(ureq, resourceKey);
					} else if(selectedKey.startsWith("grou")) {
						doSelectBusinessGroup(ureq, resourceKey);
					}
				}
            }
        }
		super.formInnerEvent(ureq, source, event);
	}

	private void doSelectPool(UserRequest ureq, Long poolKey) {
		Pool myPool = null;
		for(Pool pool: myPools) {
			if(poolKey.equals(pool.getKey())) {
				myPool = pool;
			}
		}
		doSelectPool(ureq, myPool);
	}
	

	private void doSelectPool(UserRequest ureq, Pool myPool) {
		if(myPool == null) {
			updateSource(new EmptyItemsSource());
		} else {
			PoolItemsSource source = new PoolItemsSource(getIdentity(), ureq.getUserSession().getRoles(), getLocale(), myPool);
			source.getDefaultParams().setFormat(searchSettings.getRestrictToFormat());
			source.getDefaultParams().setExcludedItemTypes(searchSettings.getExcludeTypes());
			updateSource(source);
		}
	}
	
	private void doSelectBusinessGroup(UserRequest ureq, Long businessGroupKey) {
		BusinessGroup myGroup = null;
		for(BusinessGroup group: myGroups) {
			if(businessGroupKey.equals(group.getKey())) {
				myGroup = group;
			}
		}
		doSelectBusinessGroup(ureq, myGroup);
	}
	
	private void doSelectBusinessGroup(UserRequest ureq, BusinessGroup myGroup) {
		if(myGroup == null) {
			updateSource(new EmptyItemsSource());
		} else {
			SharedItemsSource source = new SharedItemsSource(myGroup, getIdentity(), ureq.getUserSession().getRoles(), getLocale(), false);
			source.setRestrictToFormat(searchSettings.getRestrictToFormat());
			source.setExcludedItemTypes(searchSettings.getExcludeTypes());
			updateSource(source);
		}
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
