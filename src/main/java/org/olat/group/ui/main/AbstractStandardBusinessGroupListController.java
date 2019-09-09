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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;

/**
 * 
 * Initial date: 29.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractStandardBusinessGroupListController extends AbstractBusinessGroupListController {

	public AbstractStandardBusinessGroupListController(UserRequest ureq, WindowControl wControl, String page, String prefsKey) {
		super(ureq, wControl, page, prefsKey);
	}
	
	public AbstractStandardBusinessGroupListController(UserRequest ureq, WindowControl wControl, String page,
			boolean showAdminTools, boolean startExtendedSearch, String prefsKey, Object userObject) {
		super(ureq, wControl, page, showAdminTools, startExtendedSearch, false, prefsKey, userObject);
	}

	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		//mark
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18n(), Cols.mark.ordinal(),
				true, Cols.mark.name()));
		//group name
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		//id and reference
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18n(), Cols.key.ordinal(), true, Cols.key.name()));
		if(groupModule.isManagedBusinessGroups()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId.i18n(), Cols.externalId.ordinal(),
					true, Cols.externalId.name()));
		}
		//description
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.description.i18n(), Cols.description.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		//courses
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.resources.i18n(), Cols.resources.ordinal(),
				true, Cols.resources.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGResourcesCellRenderer(flc)));
		//access
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(),
				true, Cols.accessTypes.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGAccessControlledCellRenderer()));
		//launch dates
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.createionDate.i18n(), Cols.createionDate.ordinal(),
				true, Cols.createionDate.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.firstTime.i18n(), Cols.firstTime.ordinal(),
				true, Cols.firstTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lastTime.i18n(), Cols.lastTime.ordinal(),
				true, Cols.lastTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage.i18n(), Cols.lastUsage.ordinal(),
				true, Cols.lastUsage.name()));
		//roles
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.role.i18n(), Cols.role.ordinal(),
				true, Cols.role.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGRoleCellRenderer(getLocale())));
		
		DefaultFlexiColumnModel tutorColumnModel = new DefaultFlexiColumnModel(false, Cols.tutorsCount.i18n(),
				Cols.tutorsCount.ordinal(), true, Cols.tutorsCount.name());
		tutorColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		tutorColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(tutorColumnModel);
		
		DefaultFlexiColumnModel participantsColumnModel = new DefaultFlexiColumnModel(false,
				Cols.participantsCount.i18n(), Cols.participantsCount.ordinal(), true, Cols.participantsCount.name());
		participantsColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		participantsColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(participantsColumnModel);
		
		DefaultFlexiColumnModel waitingListColumnModel = new DefaultFlexiColumnModel(false,
				Cols.waitingListCount.i18n(), Cols.waitingListCount.ordinal(), true, Cols.waitingListCount.name());
		waitingListColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		waitingListColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(waitingListColumnModel);
		
		//actions
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.allowLeave.i18n(), Cols.allowLeave.ordinal(), TABLE_ACTION_LEAVE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.leave"), TABLE_ACTION_LEAVE), null)));
		return columnsModel;
	}
}
