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
package org.olat.course.nodes.practice.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.PracticeResourceInfos;
import org.olat.course.nodes.practice.ui.SharedResourceTableModel.SharedResourceCols;
import org.olat.course.nodes.practice.ui.renders.PracticeResourceIconFlexiCellRenderer;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedResourceChooserController extends FormBasicController {
	
	private final Roles roles;
	
	private FormLink selectButton;
	private FlexiTableElement tableEl;
	private SharedResourceTableModel tableModel;
	
	private final PracticeCourseNode courseNode;
	private final RepositoryEntry courseEntry;
	private final List<PracticeResourceInfos> currentResources;
	
	@Autowired
	private QPoolService qPoolService;
	@Autowired
	private PracticeService practiceResourceService;
	
	public SharedResourceChooserController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, PracticeCourseNode courseNode, List<PracticeResourceInfos> resources) {
		super(ureq, wControl, "shares");
		this.courseNode = courseNode;
		this.courseEntry = courseEntry;
		this.currentResources = resources;
		roles = ureq.getUserSession().getRoles();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SharedResourceCols.id));
		DefaultFlexiColumnModel iconCol = new DefaultFlexiColumnModel(SharedResourceCols.icon,
				new PracticeResourceIconFlexiCellRenderer());
		iconCol.setHeaderLabel("&nbsp;");
		iconCol.setHeaderTooltip(translate(SharedResourceCols.icon.i18nHeaderKey()));
		columnsModel.addFlexiColumnModel(iconCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedResourceCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));

		tableModel = new SharedResourceTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(false);
		
		selectButton = uifactory.addFormLink("select", formLayout, Link.BUTTON);
		tableEl.addBatchButton(selectButton);
	}
	
	private void loadModel() {
		List<SharedResourceRow> rows = new ArrayList<>();
		
		List<QuestionItemCollection> collections = qPoolService.getCollections(getIdentity());
		for(QuestionItemCollection collection:collections) {
			if(!isInUse(collection)) {
				rows.add(new SharedResourceRow(collection));
			}
		}

		List<Pool> pools = qPoolService.getPools(getIdentity(), roles).stream()
				.sorted(Comparator.comparing(Pool::getName))
				.collect(Collectors.toList());
		for(Pool pool:pools) {
			if(!isInUse(pool)) {
				rows.add(new SharedResourceRow(pool));
			}
		}

		List<BusinessGroup> businessGroups = qPoolService.getResourcesWithSharedItems(getIdentity()).stream()
				.sorted(Comparator.comparing(BusinessGroup::getName))
				.collect(Collectors.toList());
		for(BusinessGroup businessGroup:businessGroups) {
			if(!isInUse(businessGroup)) {
				rows.add(new SharedResourceRow(businessGroup));
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private boolean isInUse(QuestionItemCollection collection) {
		return currentResources.stream()
				.anyMatch(resource -> resource.getResource().getItemCollection() != null
						&& resource.getResource().getItemCollection().equals(collection));
	}
	
	private boolean isInUse(Pool pool) {
		return currentResources.stream()
				.anyMatch(resource -> resource.getResource().getPool() != null
						&& resource.getResource().getPool().equals(pool));
	}
	
	private boolean isInUse(BusinessGroup businessGroup) {
		return currentResources.stream()
				.anyMatch(resource -> resource.getResource().getResourceShare() != null
						&& resource.getResource().getResourceShare().equals(businessGroup.getResource()));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent
					&& ("select".equals(event.getCommand()) || FlexiTableElement.ROW_SELECT_EVENT.equals(event.getCommand()))) {
				SelectionEvent se = (SelectionEvent)event;
				doSelect(tableModel.getObject(se.getIndex()));
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(selectButton == source) {
			if(doMultiSelect()) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private boolean doMultiSelect() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		if(selectedIndexes.isEmpty()) {
			showWarning("warning.choose.atleast");
			return false;
		}
		
		for(Integer selectedIndex:selectedIndexes) {
			SharedResourceRow row = tableModel.getObject(selectedIndex.intValue());
			if(row != null) {
				doSelect(row);
			}
		}
		return true;
	}
	
	private void doSelect(SharedResourceRow row) {
		if(row.getCollection() != null) {
			practiceResourceService.createResource(courseEntry, courseNode.getIdent(), row.getCollection());
		} else if(row.getPool() != null) {
			practiceResourceService.createResource(courseEntry, courseNode.getIdent(), row.getPool());
		} else if(row.getBusinessGroup() != null) {
			practiceResourceService.createResource(courseEntry, courseNode.getIdent(), row.getBusinessGroup().getResource());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
