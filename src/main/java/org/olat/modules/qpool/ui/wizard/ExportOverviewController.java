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
package org.olat.modules.qpool.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ui.QuestionsController;

/**
 * 
 * Initial date: 20.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportOverviewController extends StepFormBasicController {

	private final ExportFormatOptions format;
	private QItemDataModel itemsModel;
	
	public ExportOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));

		format = (ExportFormatOptions)runContext.get("format");
	
		initForm(ureq);
		
		@SuppressWarnings("unchecked")
		List<QuestionItemShort> items = (List<QuestionItemShort>)runContext.get("items");
		itemsModel.setObjects(items);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "exportable", Cols.exportable.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT,
				new BooleanCellRenderer(
						new CSSIconFlexiCellRenderer("o_icon_accept"),
						new CSSIconFlexiCellRenderer("o_icon_failed"))
		));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("general.title", Cols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("technical.format", Cols.format.ordinal()));
		itemsModel = new QItemDataModel(columnsModel, format);
		uifactory.addTableElement(getWindowControl(), "shares", itemsModel, getTranslator(), formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<QuestionItemShort> exportableItems = new ArrayList<>(itemsModel.getRowCount());
		for(int i=0; i<itemsModel.getRowCount(); i++) {
			if(Boolean.TRUE.equals(itemsModel.getValueAt(i, Cols.exportable.ordinal()))) {
				exportableItems.add(itemsModel.getObject(i));
			}
		}
		addToRunContext("itemsToExport", exportableItems);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
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
		public Object getValueAt(int row, int col) {
			QuestionItemShort share = getObject(row);
			switch(Cols.values()[col]) {
				case exportable:{
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
		exportable,
		title,
		format
	}
}