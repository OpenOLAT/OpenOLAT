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
package org.olat.modules.qpool.ui.metadata;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItem2Pool;
import org.olat.modules.qpool.ui.AbstractItemListController;
import org.olat.modules.qpool.ui.QuestionsController;

/**
 * 
 * Initial date: 20.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PoolsMetadataController extends FormBasicController {
	
	private static final String CSS_ICON_READONLY = AbstractItemListController.CSS_ICON_READONLY;
	private static final String CSS_ICON_READWRITE = AbstractItemListController.CSS_ICON_READWRITE;
	
	private PoolInfosDataModel poolInfosModel;
	private FlexiTableElement poolInfosTable;
	
	private QPoolService qpoolService;

	public PoolsMetadataController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));

		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel poolInfosColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		poolInfosColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "share.editable", 0,
			false, null, FlexiColumnModel.ALIGNMENT_LEFT, new BooleanCellRenderer(
					new CSSIconFlexiCellRenderer(CSS_ICON_READWRITE),
					new CSSIconFlexiCellRenderer(CSS_ICON_READONLY))
		));
		poolInfosColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("pool.name", 1));
		poolInfosModel = new PoolInfosDataModel(poolInfosColumnsModel);
		poolInfosTable = uifactory.addTableElement(getWindowControl(), "details_pools", poolInfosModel, getTranslator(), formLayout);
		poolInfosTable.setCustomizeColumns(false);
		poolInfosTable.setEmptyTableMessageKey("sharing.pools.empty.table");
	}
	
	public void setItem(QuestionItem item) {
		List<QuestionItem2Pool> poolInfos = qpoolService.getPoolInfosByItem(item);
		poolInfosModel.setObjects(poolInfos);
		poolInfosTable.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private static class PoolInfosDataModel extends  DefaultFlexiTableDataModel<QuestionItem2Pool> {
	
		public PoolInfosDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		@Override
		public DefaultFlexiTableDataModel<QuestionItem2Pool> createCopyWithEmptyList() {
			return new PoolInfosDataModel(getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			QuestionItem2Pool info = getObject(row);
			if(col == 0) {
				return Boolean.valueOf(info.isEditable());
			}
			return info.getPoolName();
		}
	}
}

