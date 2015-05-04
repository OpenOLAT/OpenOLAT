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
package org.olat.ims.qti.questionimport;

import java.util.ArrayList;
import java.util.List;

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
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Question;

/**
 * 
 * Initial date: 24.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewQuestionController extends StepFormBasicController {
	
	private final boolean lastStep;
	private final ItemsPackage importedItems;
	
	public OverviewQuestionController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form rootForm, ItemsPackage importedItems, boolean lastStep) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.lastStep = lastStep;
		this.importedItems = importedItems;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.hasError.i18n(), Cols.hasError.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT,
				new BooleanCellRenderer(
						new CSSIconFlexiCellRenderer("o_icon_failed"),
						new CSSIconFlexiCellRenderer("o_icon_accept"))
		));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.type.i18n(), Cols.type.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title.i18n(), Cols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.points.i18n(), Cols.points.ordinal()));
		
		ItemsTableDataModel model = new ItemsTableDataModel(importedItems.getItems(), columnsModel);
		uifactory.addTableElement(getWindowControl(), "overviewTable", model, getTranslator(), formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(lastStep) {
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		} else {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
	
	private class ItemsTableDataModel extends DefaultFlexiTableDataModel<ItemAndMetadata> {
		private FlexiTableColumnModel columnModel;
		
		public ItemsTableDataModel(List<ItemAndMetadata> options, FlexiTableColumnModel columnModel) {
			super(options, columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			ItemAndMetadata importedItem = getObject(row);
			Item item = importedItem.getItem();
			switch(Cols.values()[col]) {
				case hasError: return importedItem.isHasError();
				case type: {
					String typeLabel;
					int type = item.getQuestion().getType();
					switch(type) {
						case Question.TYPE_SC: typeLabel = translate("item.type.sc"); break;
						case  Question.TYPE_MC: typeLabel = translate("item.type.mc"); break;
						case Question.TYPE_FIB: typeLabel = translate("item.type.fib"); break;
						default: { typeLabel = "??"; }
					}
					return typeLabel;
				}
				case title: return item.getTitle();
				case points: {
					Float points;
					Question question = item.getQuestion();
					int type = question.getType();
					if(type == Question.TYPE_SC || type == Question.TYPE_MC) {
						if(question.isSingleCorrect()) {
							points = question.getSingleCorrectScore();
						} else {
							points = question.getMaxValue();
						}
					} else if(type == Question.TYPE_FIB) {
						points = question.getMaxValue();
					} else {
						points = null;
					}
					return points;
				}
				default: return item;
			}
		}

		@Override
		public ItemsTableDataModel createCopyWithEmptyList() {
			return new ItemsTableDataModel(new ArrayList<ItemAndMetadata>(), columnModel);
		}
	}
	
	public static enum Cols {
		hasError("table.header.status"),
		type("table.header.type"),
		title("table.header.title"),
		points("table.header.points");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
}
