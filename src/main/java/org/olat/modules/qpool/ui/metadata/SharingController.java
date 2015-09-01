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
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItem2Pool;
import org.olat.modules.qpool.QuestionItem2Resource;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 16.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharingController extends FormBasicController {
	
	private PoolInfosDataModel poolInfosModel;
	private FlexiTableElement poolInfosTable;
	private AuthorDataModel authorsModel;
	private FlexiTableElement authorsTable;
	private SharesDataModel sharesModel;
	private FlexiTableElement sharesTable;
	
	private QPoolService qpoolService;

	public SharingController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl, "sharing");
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));

		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("sharing");
		
		//list of pools
		FlexiTableColumnModel poolInfosColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		poolInfosColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "share.editable", 0,
			false, null, FlexiColumnModel.ALIGNMENT_LEFT, new BooleanCellRenderer(
				new CSSIconFlexiCellRenderer("o_readwrite"),
				new CSSIconFlexiCellRenderer("o_readonly"))
		));
		poolInfosColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("pool.name", 1));
		poolInfosModel = new PoolInfosDataModel(poolInfosColumnsModel);
		poolInfosTable = uifactory.addTableElement(getWindowControl(), "pools", poolInfosModel, getTranslator(), formLayout);
		poolInfosTable.setCustomizeColumns(false);
		
		//list of authors
		FlexiTableColumnModel authorsColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		authorsColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("sharing.author.name", 0));
		authorsModel = new AuthorDataModel(authorsColumnsModel);
		authorsTable = uifactory.addTableElement(getWindowControl(), "authors", authorsModel, getTranslator(), formLayout);
		authorsTable.setCustomizeColumns(false);

		//list of groups
		FlexiTableColumnModel sharesColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		sharesColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "share.editable", 0,
			false, null, FlexiColumnModel.ALIGNMENT_LEFT, new BooleanCellRenderer(
				new CSSIconFlexiCellRenderer("o_readwrite"),
				new CSSIconFlexiCellRenderer("o_readonly"))
		));
		sharesColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("pool.name", 1));
		sharesModel = new SharesDataModel(poolInfosColumnsModel);
		sharesTable = uifactory.addTableElement(getWindowControl(), "shares", sharesModel, getTranslator(), formLayout);
		sharesTable.setCustomizeColumns(false);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public void setItem(QuestionItem item) {
		//pools informations
		List<QuestionItem2Pool> poolInfos = qpoolService.getPoolInfosByItem(item);
		poolInfosModel.setObjects(poolInfos);
		poolInfosTable.reset();
		flc.contextPut("showPoolInfos", new Boolean(!poolInfos.isEmpty()));
		
		//authors
		List<Identity> authors = qpoolService.getAuthors(item);
		authorsModel.setObjects(authors);
		authorsTable.reset();
		flc.contextPut("showAuthors", new Boolean(!authors.isEmpty()));
		
		//groups
		List<QuestionItem2Resource> sharedResources = qpoolService.getSharedResourceInfosByItem(item);
		sharesModel.setObjects(sharedResources);
		sharesTable.reset();
		flc.contextPut("showShares", new Boolean(!sharedResources.isEmpty()));
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
				return new Boolean(share.isEditable());
			}
			return share.getName();
		}
	}

	private static class AuthorDataModel extends DefaultFlexiTableDataModel<Identity> {
		
		public AuthorDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public DefaultFlexiTableDataModel<Identity> createCopyWithEmptyList() {
			return new AuthorDataModel(getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			Identity id = getObject(row);
			if(col == 0) {
				return UserManager.getInstance().getUserDisplayName(id);
			}
			return null;
		}
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
				return new Boolean(info.isEditable());
			}
			return info.getPoolName();
		}
	}
}
