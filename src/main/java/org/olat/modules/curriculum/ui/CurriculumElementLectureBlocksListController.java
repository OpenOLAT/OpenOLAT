/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementLectureBlocksTableModel.BlockCols;
import org.olat.modules.curriculum.ui.component.LocationCellRenderer;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.ConfirmDeleteLectureBlockController;
import org.olat.modules.lecture.ui.EditLectureBlockController;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementLectureBlocksListController extends FormBasicController {

	private static final String TOOLS_CMD = "tools";
	
	private FormLink addLectureButton;
	private FlexiTableElement tableEl;
	private CurriculumElementLectureBlocksTableModel tableModel;
	
	private final CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	
	private int count = 0;
	private boolean lectureManagementManaged;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private EditLectureBlockController addLectureCtrl;
	private EditLectureBlockController editLectureCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmDeleteLectureBlockController deleteLectureBlockCtrl;
	
	
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementLectureBlocksListController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "lectureblocks", Util.createPackageTranslator(LectureListRepositoryController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!lectureManagementManaged && secCallback.canNewLectureBlock()) { 
			addLectureButton = uifactory.addFormLink("add.lecture", formLayout, Link.BUTTON);
			addLectureButton.setIconLeftCSS("o_icon o_icon_add");
			addLectureButton.setElementCssClass("o_sel_curriculum_add_lecture");
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.date,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.startTime,
				new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.endTime,
				new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.location,
				new LocationCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.status,
				new LectureBlockStatusCellRenderer(getTranslator())));
		
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(BlockCols.tools);
		toolsCol.setExportable(false);
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new CurriculumElementLectureBlocksTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editLectureCtrl == source || addLectureCtrl == source
				|| deleteLectureBlockCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(editLectureCtrl);
		removeAsListenerAndDispose(addLectureCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		editLectureCtrl = null;
		addLectureCtrl = null;
		cmc = null;
	}
	
	private void loadModel() {
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(curriculumElement);
		List<CurriculumElementLectureBlockRow> rows = new ArrayList<>(lectureBlocks.size());
		for(LectureBlock block:lectureBlocks) {
			CurriculumElementLectureBlockRow row = forgeRow(block);
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CurriculumElementLectureBlockRow forgeRow(LectureBlock block) {
		CurriculumElementLectureBlockRow row = new CurriculumElementLectureBlockRow(block);
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++count), TOOLS_CMD, "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fw o_icon-lg");
		toolsLink.setTitle(translate("action.more"));
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		
		return row;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addLectureButton == source) {
			doAddLectureBlock(ureq);
		} else if(tableEl == source) {
			//
		} else if(source instanceof FormLink link && link.getUserObject() instanceof CurriculumElementLectureBlockRow row) {
			if("tools".equals(link.getCmd())) {
				doOpenTools(ureq, row, link);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddLectureBlock(UserRequest ureq) {
		if(guardModalController(editLectureCtrl) || !secCallback.canNewLectureBlock()) return;
		
		CurriculumElement element = curriculumService.getCurriculumElement(curriculumElement);
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
	
		addLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), element, entries);
		listenTo(addLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), addLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditLectureBlock(UserRequest ureq) {
		if(guardModalController(editLectureCtrl) || !secCallback.canNewLectureBlock()) return;
		
		CurriculumElement element = curriculumService.getCurriculumElement(curriculumElement);
		editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), element, null);
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), editLectureCtrl.getInitialComponent(), true, translate("edit.lecture"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, CurriculumElementLectureBlockRow row) {
		List<LectureBlock> blocks = Collections.singletonList(row.getLectureBlock());
		deleteLectureBlockCtrl = new ConfirmDeleteLectureBlockController(ureq, getWindowControl(), blocks);
		listenTo(deleteLectureBlockCtrl);
		
		String title = translate("delete.lectures.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteLectureBlockCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementLectureBlockRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final CurriculumElementLectureBlockRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumElementLectureBlockRow row) {
			super(ureq, wControl);
			this.row = row;

			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();
			addLink("details.delete", "delete", "o_icon o_icon-fw o_icon_delete_item", links);

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String cmd, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link link) {
				String cmd = link.getCommand();
				if("delete".equals(cmd)) {
					doConfirmDelete(ureq, row);
				}
			}
		}
		
	}
}
