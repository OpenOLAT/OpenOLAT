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
package org.olat.modules.catalog.ui.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.launcher.StaticHandler;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositorySearchController.Can;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherStaticEditController extends AbstractLauncherEditController {

	private FormLayoutContainer reCont;
	private FormLink addResourceLink;
	private FlexiTableElement tableEl;
	private StaticLauncherDataModel dataModel;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController selectResouceCtrl;
	
	private final StaticHandler handler;
	private final List<RepositoryEntry> repositoryEntries;

	public CatalogLauncherStaticEditController(UserRequest ureq, WindowControl wControl, StaticHandler handler, CatalogLauncher catalogLauncher) {
		super(ureq, wControl, handler, catalogLauncher);
		this.handler = handler;
		this.repositoryEntries = handler.getRepositoryEntries(getCatalogLauncher());
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout) {
		String page = velocity_root + "/launcher_static.html";
		reCont = FormLayoutContainer.createCustomFormLayout("static", getTranslator(), page);
		reCont.setRootForm(mainForm);
		reCont.setLabel("launcher.static.resources", null);
		formLayout.add(reCont);
		
		addResourceLink = uifactory.addFormLink("launcher.static.resources.add", reCont, Link.BUTTON);
		addResourceLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StaticLauncherCols.upDown));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StaticLauncherCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StaticLauncherCols.delete));
		
		dataModel = new StaticLauncherDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), reCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}

	private void updateModel() {
		List<StaticLauncherRow> rows = new ArrayList<>(repositoryEntries.size());
		for (int i = 0; i < repositoryEntries.size(); i++) {
			RepositoryEntry repositoryEntry = repositoryEntries.get(i);
			StaticLauncherRow row = new StaticLauncherRow(repositoryEntry);
			
			UpDown upDown = UpDownFactory.createUpDown("up_down_" + row.getRepositoryEntry().getKey(), UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
			upDown.setUserObject(row);
			if (i == 0) {
				upDown.setTopmost(true);
			}
			if (i == repositoryEntries.size() - 1) {
				upDown.setLowermost(true);
			} 
			row.setUpDown(upDown);
			
			FormLink deleteLink = uifactory.addFormLink("delete_" + row.getRepositoryEntry().getKey(), "delete", "delete", null, reCont, Link.LINK);
			deleteLink.setUserObject(row);
			row.setDeleteLink(deleteLink);
			
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected String getConfig() {
		return handler.getConfig(repositoryEntries);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == selectResouceCtrl) {
			if (event.equals(ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) ||
				event.equals(ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED)) {
				repositoryEntries.addAll(selectResouceCtrl.getSelectedEntries());
				updateModel();
			}
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(selectResouceCtrl);
		removeAsListenerAndDispose(cmc);
		selectResouceCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent && source instanceof UpDown) {
			UpDownEvent ude = (UpDownEvent) event;
			UpDown upDown = (UpDown)source;
			Object userObject = upDown.getUserObject();
			if (userObject instanceof StaticLauncherRow) {
				StaticLauncherRow row = (StaticLauncherRow)userObject;
				doMove(row, ude.getDirection());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addResourceLink) {
			doSelectResource(ureq);
		} else if (source instanceof FormLink) {
			if (((FormLink) source).getCmd().equals("delete")) {
				Object userObject = source.getUserObject();
				if (userObject instanceof StaticLauncherRow) {
					StaticLauncherRow row = (StaticLauncherRow)userObject;
					doDeleteResource(row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelectResource(UserRequest ureq) {
		guardModalController(selectResouceCtrl);
		
		selectResouceCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, null, null, null,
				translate("launcher.static.resources.add"), false, false, true, false, true, false, Can.all);
		listenTo(selectResouceCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectResouceCtrl.getInitialComponent(), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doMove(StaticLauncherRow row, Direction direction) {
		RepositoryEntry repositoryEntry = row.getRepositoryEntry();
		int index = repositoryEntries.indexOf(repositoryEntry);
		if (index > 0) {
			int swapIndex = Direction.UP == direction? index - 1: index + 1;
			Collections.swap(repositoryEntries, index, swapIndex);
			updateModel();
		}
	}

	protected void doDeleteResource(StaticLauncherRow row) {
		Long key = row.getRepositoryEntry().getKey();
		repositoryEntries.removeIf(re -> re.getKey().equals(key));
		updateModel();
	}
	
	public static class StaticLauncherDataModel extends DefaultFlexiTableDataModel<StaticLauncherRow> {
		
		private static final StaticLauncherCols[] COLS = StaticLauncherCols.values();
		
		public StaticLauncherDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			StaticLauncherRow performanceClassRow = getObject(row);
			return getValueAt(performanceClassRow, col);
		}

		private Object getValueAt(StaticLauncherRow row, int col) {
			switch(COLS[col]) {
				case upDown: return row.getUpDown();
				case name: return row.getRepositoryEntry().getDisplayname();
				case delete: return row.getDeleteLink();
				default: return null;
			}
		}
	}
	
	public enum StaticLauncherCols implements FlexiColumnDef {
		upDown("table.header.updown"),
		name("launcher.static.display.name"),
		delete("delete");
		
		private final String i18nKey;
		
		private StaticLauncherCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
	
	public static final class StaticLauncherRow {
		
		private final RepositoryEntry repositoryEntry;
		private UpDown upDown;
		private FormLink deleteLink;
		
		public StaticLauncherRow(RepositoryEntry repositoryEntry) {
			this.repositoryEntry = repositoryEntry;
		}

		public UpDown getUpDown() {
			return upDown;
		}

		public void setUpDown(UpDown upDown) {
			this.upDown = upDown;
		}

		public FormLink getDeleteLink() {
			return deleteLink;
		}

		public void setDeleteLink(FormLink deleteLink) {
			this.deleteLink = deleteLink;
		}

		public RepositoryEntry getRepositoryEntry() {
			return repositoryEntry;
		}
		
	}

}
