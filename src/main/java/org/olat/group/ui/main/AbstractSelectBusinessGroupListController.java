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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;

/**
 * Used for the business groups selection popup.
 * 
 * Initial date: 29.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractSelectBusinessGroupListController extends AbstractBusinessGroupListController {
	
	public AbstractSelectBusinessGroupListController(UserRequest ureq, WindowControl wControl, String page, String prefsKey) {
		super(ureq, wControl, page, prefsKey);
	}
	
	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		//
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

		//stats
		DefaultFlexiColumnModel tutorColumnModel = new DefaultFlexiColumnModel(true, Cols.tutorsCount.i18n(),
				Cols.tutorsCount.ordinal(), true, Cols.tutorsCount.name());
		tutorColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		tutorColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(tutorColumnModel);
		
		DefaultFlexiColumnModel participantsColumnModel = new DefaultFlexiColumnModel(true,
				Cols.participantsCount.i18n(), Cols.participantsCount.ordinal(), true, Cols.participantsCount.name());
		participantsColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		participantsColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(participantsColumnModel);
		
		DefaultFlexiColumnModel freePlacesColumnModel = new DefaultFlexiColumnModel(true, Cols.freePlaces.i18n(),
				Cols.freePlaces.ordinal(), true, Cols.freePlaces.name(), FlexiColumnModel.ALIGNMENT_LEFT,
				new TextFlexiCellRenderer(EscapeMode.none));
		freePlacesColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		freePlacesColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(freePlacesColumnModel);
		
		DefaultFlexiColumnModel waitingListColumnModel = new DefaultFlexiColumnModel(true,
				Cols.waitingListCount.i18n(), Cols.waitingListCount.ordinal(), true, Cols.waitingListCount.name());
		waitingListColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		waitingListColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(waitingListColumnModel);
		
		//actions
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), TABLE_ACTION_SELECT));
		return columnsModel;
	}

	@Override
	protected List<BGTableItem> searchTableItems(BusinessGroupQueryParams params) {
		List<StatisticsBusinessGroupRow> rows = businessGroupService.findBusinessGroupsForSelection(params, getIdentity());
		List<BGTableItem> items = new ArrayList<>(rows.size());
		for(StatisticsBusinessGroupRow row:rows) {
			FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			
			BGTableItem item = new BGTableItem(row, markLink, Boolean.FALSE, Boolean.FALSE);
			item.setNumOfOwners(row.getNumOfCoaches());
			item.setNumOfParticipants(row.getNumOfParticipants());
			item.setNumWaiting(row.getNumWaiting());
			item.setNumOfPendings(row.getNumPending());
			items.add(item);
		}
		return items;
	}
}
