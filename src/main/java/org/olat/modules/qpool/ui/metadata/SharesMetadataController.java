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
import org.olat.modules.qpool.QuestionItem2Resource;
import org.olat.modules.qpool.ui.AbstractItemListController;
import org.olat.modules.qpool.ui.QuestionsController;

/**
 * 
 * Initial date: 20.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SharesMetadataController extends FormBasicController {
	
	private static final String CSS_ICON_READONLY = AbstractItemListController.CSS_ICON_READONLY;
	private static final String CSS_ICON_READWRITE = AbstractItemListController.CSS_ICON_READWRITE;
	
	private SharesDataModel sharesModel;
	private FlexiTableElement sharesTable;
	
	private QPoolService qpoolService;

	public SharesMetadataController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));

		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel sharesColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		sharesColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "share.editable", 0,
			false, null, FlexiColumnModel.ALIGNMENT_LEFT, new BooleanCellRenderer(
					new CSSIconFlexiCellRenderer(CSS_ICON_READWRITE),
					new CSSIconFlexiCellRenderer(CSS_ICON_READONLY))
		));
		sharesColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("pool.name", 1));
		sharesModel = new SharesDataModel(sharesColumnsModel);
		sharesTable = uifactory.addTableElement(getWindowControl(), "details_shares", sharesModel, getTranslator(), formLayout);
		sharesTable.setCustomizeColumns(false);
		sharesTable.setEmptyTableMessageKey("sharing.shares.empty.table");
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public void setItem(QuestionItem item) {
		List<QuestionItem2Resource> sharedResources = qpoolService.getSharedResourceInfosByItem(item);
		sharesModel.setObjects(sharedResources);
		sharesTable.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private static class SharesDataModel extends DefaultFlexiTableDataModel<QuestionItem2Resource> {

		public SharesDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public DefaultFlexiTableDataModel<QuestionItem2Resource> createCopyWithEmptyList() {
			return new SharesDataModel(getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			QuestionItem2Resource share = getObject(row);
			if(col == 0) {
				return Boolean.valueOf(share.isEditable());
			}
			return share.getName();
		}
	}

}

