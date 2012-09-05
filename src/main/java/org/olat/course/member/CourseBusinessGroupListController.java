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
package org.olat.course.member;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.AbstractBusinessGroupListController;
import org.olat.group.ui.main.BGAccessControlledCellRenderer;
import org.olat.group.ui.main.BGResourcesCellRenderer;
import org.olat.group.ui.main.BusinessGroupNameCellRenderer;
import org.olat.group.ui.main.BusinessGroupTableModelWithType.Cols;
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseBusinessGroupListController extends AbstractBusinessGroupListController {
	
	private final OLATResource resource;
	
	public CourseBusinessGroupListController(UserRequest ureq, WindowControl wControl, OLATResource resource) {
		super(ureq, wControl, "group_list");
		this.resource = resource;
	}

	@Override
	protected void initButtons(UserRequest ureq) {
		initButtons(ureq, true);
		groupListCtr.setMultiSelect(true);
		groupListCtr.addMultiSelectAction("table.duplicate", TABLE_ACTION_DUPLICATE);
		groupListCtr.addMultiSelectAction("table.merge", TABLE_ACTION_MERGE);
		groupListCtr.addMultiSelectAction("table.users.management", TABLE_ACTION_USERS);
		groupListCtr.addMultiSelectAction("table.config", TABLE_ACTION_CONFIG);
		groupListCtr.addMultiSelectAction("table.email", TABLE_ACTION_EMAIL);
	}

	@Override
	protected int initColumns() {
		CustomCssCellRenderer nameRenderer = new BusinessGroupNameCellRenderer();
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, nameRenderer));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.key.i18n(), Cols.key.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.description.i18n(), Cols.description.ordinal(), null, getLocale()));
		CustomCellRenderer resourcesRenderer = new BGResourcesCellRenderer(this, mainVC, getTranslator());
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.resources.i18n(), Cols.resources.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, resourcesRenderer));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.tutorsCount.i18n(), Cols.tutorsCount.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.participantsCount.i18n(), Cols.participantsCount.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.freePlaces.i18n(), Cols.freePlaces.ordinal(), TABLE_ACTION_LAUNCH, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.waitingListCount.i18n(), Cols.waitingListCount.ordinal(), null, getLocale()));
		CustomCellRenderer acRenderer = new BGAccessControlledCellRenderer();
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(), null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, acRenderer));
		groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_LAUNCH, "action", translate("table.header.work")));
		groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_LAUNCH, "action", translate("table.header.remove")));
		return 11;
	}

	@Override
	protected void reloadModel() {
		updateTableModel(new SearchBusinessGroupParams(), false);
	}

	@Override
	protected OLATResource getResource() {
		return resource;
	}
}