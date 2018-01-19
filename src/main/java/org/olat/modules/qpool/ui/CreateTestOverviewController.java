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
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;

/**
 * 
 * Initial date: 19.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateTestOverviewController extends FormBasicController {

	private final ExportFormatOptions format;
	private QItemDataModel itemsModel;

	public CreateTestOverviewController(UserRequest ureq, WindowControl wControl, List<QuestionItemShort> items,
			ExportFormatOptions format) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.format = format;
		initForm(ureq);
		itemsModel.setObjects(items);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "export.overview.accept", Cols.accept.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT,
				new BooleanCellRenderer(
						new CSSIconFlexiCellRenderer("o_icon_accept"),
						new CSSIconFlexiCellRenderer("o_icon_failed"))
		));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("general.title", Cols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("technical.format", Cols.format.ordinal()));
		itemsModel = new QItemDataModel(columnsModel, format);
		uifactory.addTableElement(getWindowControl(), "shares", itemsModel, getTranslator(), formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("create.test", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	public String getResourceTypeFormat() {
		return format.getResourceTypeFormat();
	}

	public List<QuestionItemShort> getExportableQuestionItems() {
		List<QuestionItemShort> exportableItems = new ArrayList<>(itemsModel.getRowCount());
		for(int i=0; i<itemsModel.getRowCount(); i++) {
			if(Boolean.TRUE.equals(itemsModel.getValueAt(i, Cols.accept.ordinal()))) {
				exportableItems.add(itemsModel.getObject(i));
			}
		}
		return exportableItems;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

	private static class QItemDataModel extends DefaultFlexiTableDataModel<QuestionItemShort> {
		private final ExportFormatOptions format;
		private final QuestionPoolModule qpoolModule;

		public QItemDataModel(FlexiTableColumnModel columnModel, ExportFormatOptions format) {
			super(columnModel);
			this.format = format;
			qpoolModule = CoreSpringFactory.getImpl(QuestionPoolModule.class);
		}

		@Override
		public QItemDataModel createCopyWithEmptyList() {
			return new QItemDataModel(getTableColumnModel(), format);
		}

		@Override
		public Object getValueAt(int row, int col) {
			QuestionItemShort share = getObject(row);
			switch(Cols.values()[col]) {
				case accept:{
					String itemFormat = share.getFormat();
					QPoolSPI itemProvider = qpoolModule.getQuestionPoolProvider(itemFormat);
					if(itemProvider != null && itemProvider.getTestExportFormats().contains(format)) {
						return Boolean.TRUE;
					}
					return Boolean.FALSE;	
				} 
				case title: return share.getTitle();
				case format: return share.getFormat();
				default : {
					return share;
				}
			}
		}
	}
	
	private enum Cols {
		accept,
		title,
		format
	}
}
