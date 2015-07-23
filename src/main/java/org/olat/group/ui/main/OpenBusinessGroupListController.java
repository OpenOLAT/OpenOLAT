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
package org.olat.group.ui.main;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.BusinessGroupFlexiTableModel.Cols;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenBusinessGroupListController extends AbstractBusinessGroupListController {
	
	public OpenBusinessGroupListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
		super(ureq, wControl, "group_list", prefsKey);
	}
	
	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		searchCtrl.enableRoles(false);
	}
	
	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18n(), Cols.key.ordinal(),
				true, Cols.key.name()));
		if(groupModule.isManagedBusinessGroups()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId.i18n(), Cols.externalId.ordinal(),
					true, Cols.externalId.name()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.description.i18n(), Cols.description.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.resources.i18n(), Cols.resources.ordinal(),
				true, Cols.resources.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGResourcesCellRenderer(flc)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.freePlaces.i18n(), Cols.freePlaces.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.freePlaces.name(), new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(),
				true, Cols.accessTypes.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGAccessControlledCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.role.i18n(), Cols.role.ordinal(),
				true, Cols.role.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGRoleCellRenderer(getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.accessControlLaunch.i18n(), Cols.accessControlLaunch.ordinal()));
		
		return columnsModel;
	}
	
	@Override
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {	
		if(businessGroupService.isIdentityInBusinessGroup(getIdentity(), group)) {
			super.doLaunch(ureq, group);
		} else {
			String businessPath = "[GroupCard:" + group.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}
	
	@Override
	protected SearchBusinessGroupParams getSearchParams(SearchEvent event) {
		SearchBusinessGroupParams params = event.convertToSearchBusinessGroupParams(getIdentity());
		params.setPublicGroups(Boolean.TRUE);
		return params;
	}

	@Override
	protected SearchBusinessGroupParams getDefaultSearchParams() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setPublicGroups(Boolean.TRUE);
		return params;
	}
}