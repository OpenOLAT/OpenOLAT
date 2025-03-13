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
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.launcher.StaticCurriculumElementHandler;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumElementSelectionController;

/**
 * 
 * Initial date: 12 Mar 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogLauncherStaticCurriculumElementEditController extends AbstractLauncherEditController {
	
	private FormLayoutContainer ceCont;
	private FormLink addElementLink;
	private FlexiTableElement tableEl;
	private StaticLauncherDataModel dataModel;
	
	private CloseableModalController cmc;
	private CurriculumElementSelectionController selectCtrl;
	
	private final StaticCurriculumElementHandler handler;
	private final List<CurriculumElement> curriculumElements;

	public CatalogLauncherStaticCurriculumElementEditController(UserRequest ureq, WindowControl wControl,
			StaticCurriculumElementHandler handler, CatalogLauncher catalogLauncher) {
		super(ureq, wControl, handler, catalogLauncher);
		this.handler = handler;
		this.curriculumElements = handler.getCurriculumElements(getCatalogLauncher());
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer generalCont) {
		String page = velocity_root + "/launcher_static_curriculum_element.html";
		ceCont = FormLayoutContainer.createCustomFormLayout("static", getTranslator(), page);
		ceCont.setRootForm(mainForm);
		ceCont.setLabel("launcher.staticce.elements", null);
		generalCont.add(ceCont);
		
		addElementLink = uifactory.addFormLink("launcher.staticce.elements.add", ceCont, Link.BUTTON);
		addElementLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StaticLauncherCols.upDown));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StaticLauncherCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StaticLauncherCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StaticLauncherCols.delete));
		
		dataModel = new StaticLauncherDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), ceCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}

	private void updateModel() {
		List<StaticLauncherRow> rows = new ArrayList<>(curriculumElements.size());
		for (int i = 0; i < curriculumElements.size(); i++) {
			CurriculumElement curriculumElement = curriculumElements.get(i);
			StaticLauncherRow row = new StaticLauncherRow(curriculumElement);
			
			ExternalLink elementNameLink = new ExternalLink("open_" + i);
			elementNameLink.setName(curriculumElement.getDisplayName());
			elementNameLink.setTarget("_blank");
			String businessPath = "[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + row.getCurriculumElement().getKey() + "]";
			List<ContextEntry> ceList = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
			String url = BusinessControlFactory.getInstance().getAsAuthURIString(ceList, true);
			elementNameLink.setUrl(url);
			row.setElementNameLink(elementNameLink);
			
			UpDown upDown = UpDownFactory.createUpDown("up_down_" + i, UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
			upDown.setUserObject(row);
			if (i == 0) {
				upDown.setTopmost(true);
			}
			if (i == curriculumElements.size() - 1) {
				upDown.setLowermost(true);
			} 
			row.setUpDown(upDown);
			
			FormLink deleteLink = uifactory.addFormLink("delete_" + i, "delete", "delete", null, ceCont, Link.LINK);
			deleteLink.setUserObject(row);
			row.setDeleteLink(deleteLink);
			
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected String getConfig() {
		return handler.getConfig(curriculumElements);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == selectCtrl) {
			if (event == Event.DONE_EVENT) {
				curriculumElements.addAll(selectCtrl.getSelectedElements());
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(selectCtrl);
		removeAsListenerAndDispose(cmc);
		selectCtrl = null;
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
		if(source == addElementLink) {
			doSelectResource(ureq);
		} else if (source instanceof FormLink link) {
			if (link.getCmd().equals("delete")) {
				Object userObject = source.getUserObject();
				if (userObject instanceof StaticLauncherRow row) {
					doDeleteElement(row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSelectResource(UserRequest ureq) {
		guardModalController(selectCtrl);
		
		selectCtrl = new CurriculumElementSelectionController(ureq, getWindowControl());
		listenTo(selectCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectCtrl.getInitialComponent(),
				translate("launcher.staticce.elements.add"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doMove(StaticLauncherRow row, Direction direction) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		int index = curriculumElements.indexOf(curriculumElement);
		if (index > 0) {
			int swapIndex = Direction.UP == direction? index - 1: index + 1;
			Collections.swap(curriculumElements, index, swapIndex);
			updateModel();
		}
	}

	protected void doDeleteElement(StaticLauncherRow row) {
		Long key = row.getCurriculumElement().getKey();
		curriculumElements.removeIf(re -> re.getKey().equals(key));
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
				case id: return row.getCurriculumElement().getKey();
				case name: return row.getElementNameLink();
				case delete: return row.getDeleteLink();
				default: return null;
			}
		}
	}
	
	public enum StaticLauncherCols implements FlexiColumnDef {
		upDown("table.header.updown"),
		id("launcher.staticce.id"),
		name("launcher.staticce.title"),
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
		
		private final CurriculumElement curriculumElement;
		private ExternalLink elementNameLink;
		private UpDown upDown;
		private FormLink deleteLink;
		
		public StaticLauncherRow(CurriculumElement curriculumElement) {
			this.curriculumElement = curriculumElement;
		}

		public ExternalLink getElementNameLink() {
			return elementNameLink;
		}

		public void setElementNameLink(ExternalLink elementNameLink) {
			this.elementNameLink = elementNameLink;
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

		public CurriculumElement getCurriculumElement() {
			return curriculumElement;
		}
		
	}
}
