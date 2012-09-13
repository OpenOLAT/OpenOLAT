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

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.AbstractBusinessGroupListController;
import org.olat.group.ui.main.BGAccessControlledCellRenderer;
import org.olat.group.ui.main.BGResourcesCellRenderer;
import org.olat.group.ui.main.BusinessGroupNameCellRenderer;
import org.olat.group.ui.main.BusinessGroupTableModelWithType.Cols;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseBusinessGroupListController extends AbstractBusinessGroupListController {
	
	private final RepositoryEntry re;
	private final Link createGroup;
	private final Link addGroup;
	
	private SelectBusinessGroupController selectController;
	
	public CourseBusinessGroupListController(UserRequest ureq, WindowControl wControl, RepositoryEntry re) {
		super(ureq, wControl, "group_list");
		this.re = re;
		
		createGroup = LinkFactory.createButton("group.create", mainVC, this);
		mainVC.put("createGroup", createGroup);
		addGroup = LinkFactory.createButton("group.add", mainVC, this);
		mainVC.put("addGroup", addGroup);
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
		groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_LAUNCH, "action", translate("table.header.edit")));
		groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_LAUNCH, "action", translate("table.header.remove")));
		return 11;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == createGroup) {
			doCreate(ureq, getWindowControl(), re);
		} else if (source == addGroup) {
			doSelectGroups(ureq);
		} else {
			super.event(ureq, source, event);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof BusinessGroupSelectionEvent) {
			BusinessGroupSelectionEvent selectionEvent = (BusinessGroupSelectionEvent)event;
			List<BusinessGroup> selectedGroups = selectionEvent.getGroups();
			cmc.deactivate();
			cleanUpPopups();
			addGroupsToCourse(selectedGroups);
		} else {
			super.event(ureq, source, event);
		}
	}
	
	@Override
	protected void cleanUpPopups() {
		super.cleanUpPopups();
		removeAsListenerAndDispose(selectController);
		selectController = null;
	}

	protected void doSelectGroups(UserRequest ureq) {
		removeAsListenerAndDispose(selectController);
		selectController = new SelectBusinessGroupController(ureq, getWindowControl());
		listenTo(selectController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectController.getInitialComponent(), true, translate("select.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void addGroupsToCourse(List<BusinessGroup> groups) {
		List<RepositoryEntry> resources = Collections.singletonList(re);
		businessGroupService.addResourcesTo(groups, resources);
		reloadModel();
		mainVC.setDirty(true);
	}

	@Override
	protected void reloadModel() {
		updateTableModel(new SearchBusinessGroupParams(), false);
	}

	@Override
	protected OLATResource getResource() {
		return re.getOlatResource();
	}
}