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
package org.olat.modules.qpool.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.ui.events.QPoolSelectionEvent;
import org.olat.modules.qpool.ui.metadata.MetadatasController;

/**
 * 
 * Select the list of pools
 * 
 * Initial date: 15.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PoolsController extends FormBasicController {
	
	private FormLink selectButton;
	
	private Object userObject;
	private PoolDataModel model;
	private FlexiTableElement poolTable;
	
	private final QPoolService qpoolService;
	
	public PoolsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "pools");
		setTranslator(Util.createPackageTranslator(MetadatasController.class, ureq.getLocale(), getTranslator()));
		
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.id.i18nKey(), Cols.id.ordinal(), true, "key"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.publicPool.i18nKey(), Cols.publicPool.ordinal(),
				true, "publicPool", FlexiColumnModel.ALIGNMENT_LEFT,
				new BooleanCellRenderer(
						new CSSIconFlexiCellRenderer("o_icon_pool_public"),
						new CSSIconFlexiCellRenderer("o_icon_pool_private"))
		));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18nKey(), Cols.name.ordinal(), true, "name"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select-pool"));

		model = new PoolDataModel(columnsModel, getTranslator());
		poolTable = uifactory.addTableElement(getWindowControl(), "pools", model, getTranslator(), formLayout);
		poolTable.setMultiSelect(true);
		poolTable.setRendererType(FlexiTableRendererType.classic);
		reloadModel(ureq);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		selectButton = uifactory.addFormLink("select", buttonsCont, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void reloadModel(UserRequest ureq) {
		List<Pool> pools = qpoolService.getPools(getIdentity(), ureq.getUserSession().getRoles());
		model.setObjects(pools);
		poolTable.reset();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == poolTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select-pool".equals(se.getCommand())) {
					Pool row = model.getObject(se.getIndex());
					fireEvent(ureq, new QPoolSelectionEvent(Collections.singletonList(row)));
				}
			}
		} else if(source == selectButton) {
			Set<Integer> selectIndexes = poolTable.getMultiSelectedIndex();
			if(!selectIndexes.isEmpty()) {
				List<Pool> rows = new ArrayList<>(selectIndexes.size());
				for(Integer index:selectIndexes) {
					Pool row = model.getObject(index.intValue());
					rows.add(row);
				}
				fireEvent(ureq, new QPoolSelectionEvent(rows));	
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	protected void doShare() {
		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private enum Cols {
		id("pool.key"),
		publicPool("pool.public"),
		name("pool.name");
		
		private final String i18nKey;
	
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	private static class PoolDataModel implements FlexiTableDataModel<Pool>, TableDataModel<Pool> {
	
		private List<Pool> rows;
		private FlexiTableColumnModel columnModel;
		private final Translator translator;
		
		public PoolDataModel(FlexiTableColumnModel columnModel, Translator translator) {
			this.columnModel = columnModel;
			this.translator = translator;
		}
		
		@Override
		public FlexiTableColumnModel getTableColumnModel() {
			return columnModel;
		}
	
		@Override
		public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
			this.columnModel = tableColumnModel;
		}

		@Override
		public boolean isSelectable(int row) {
			return true;
		}

		@Override
		public int getRowCount() {
			return rows == null ? 0 : rows.size();
		}
		
		@Override
		public boolean isRowLoaded(int row) {
			return rows != null && row < rows.size();
		}
	
		@Override
		public Pool getObject(int row) {
			return rows.get(row);
		}
	
		@Override
		public void setObjects(List<Pool> objects) {
			rows = new ArrayList<>(objects);
		}
	
		@Override
		public int getColumnCount() {
			return columnModel.getColumnCount();
		}
		
		@Override
		public PoolDataModel createCopyWithEmptyList() {
			return new PoolDataModel(columnModel, translator);
		}
	
		@Override
		public Object getValueAt(int row, int col) {
			Pool item = getObject(row);
			switch(Cols.values()[col]) {
				case id: return item.getKey();
				case publicPool: return Boolean.valueOf(item.isPublicPool());
				case name: return item.getName();
				default: return "";
			}
		}
	}
}
