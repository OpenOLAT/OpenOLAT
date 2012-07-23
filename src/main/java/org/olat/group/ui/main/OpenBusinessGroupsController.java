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

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.BusinessGroupTableModelWithType.Cols;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenBusinessGroupsController extends AbstractBusinessGroupListController {
	private static final String TABLE_ACTION_LEAVE = "bgTblLeave";
	private static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	private static final String TABLE_ACTION_ACCESS = "bgTblAccess";
	
	private final OpenBusinessGroupSearchController searchController;
	
	private final Link allOpenLink, searchOpenLink;
	private final SegmentViewComponent segmentView;


	public OpenBusinessGroupsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "open");
		
		//segment view
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		allOpenLink = LinkFactory.createLink("opengroups.all", mainVC, this);
		segmentView.addSegment(allOpenLink, true);
		searchOpenLink = LinkFactory.createLink("opengroups.search", mainVC, this);
		segmentView.addSegment(searchOpenLink, false);

		//search controller
		searchController = new OpenBusinessGroupSearchController(ureq, wControl);
		listenTo(searchController);
		mainVC.put("search", searchController.getInitialComponent());
		searchController.getInitialComponent().setVisible(false);
		
		updateOpenGroupModel();
	}
	
	@Override
	protected int initColumns() {
		CustomCellRenderer markRenderer = new BGMarkCellRenderer(this, mainVC, getTranslator());
		groupListCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor(Cols.mark.i18n(), Cols.resources.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, markRenderer));
		CustomCellRenderer acRenderer = new BGAccessControlledCellRenderer();
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(), null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, acRenderer));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH, getLocale()));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.description.i18n(), Cols.description.ordinal(), null, getLocale()));
		CustomCellRenderer resourcesRenderer = new BGResourcesCellRenderer(this, mainVC, getTranslator());
		groupListCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor(Cols.resources.i18n(), Cols.resources.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, resourcesRenderer));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.lastUsage.i18n(), Cols.lastUsage.ordinal(), null, getLocale()));
		CustomCellRenderer roleRenderer = new BGRoleCellRenderer(getTranslator());
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.role.i18n(), Cols.role.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, roleRenderer));
		groupListCtr.addColumnDescriptor(false, new BooleanColumnDescriptor(Cols.allowLeave.i18n(), Cols.allowLeave.ordinal(), TABLE_ACTION_LEAVE, translate("table.header.leave"), null));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.accessControlLaunch.i18n(), Cols.accessControlLaunch.ordinal(), TABLE_ACTION_ACCESS, getLocale()));
		return 7;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == allOpenLink) {
					updateOpenGroupModel();
				} else if (clickedLink == searchOpenLink){
					updateSearchGroupModel(null);
				}
				searchController.getInitialComponent().setVisible(clickedLink == searchOpenLink);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == searchController) {
			if(event instanceof SearchEvent) {
				updateSearchGroupModel((SearchEvent)event);
			}
		}
		super.event(ureq, source, event);
	}

	private void updateSearchGroupModel(SearchEvent event) {
		if(event == null) {
			groupListModel.setEntries(Collections.<BGTableItem>emptyList());
			groupListCtr.modelChanged();
			return;
		}
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setName(event.getName());
		params.setDescription(event.getDescription());
		params.setOwnerName(event.getOwnerName());
		params.setPublicGroups(Boolean.TRUE);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		updateTableModel(groups, false);
	}
	
	private void updateOpenGroupModel() {
		//find all accessible business groups
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setPublicGroups(Boolean.TRUE);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		updateTableModel(groups, false);
	}
}