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
package org.olat.ims.qti21.questionimport;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;

/**
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewQuestionController extends StepFormBasicController {
	
	private final boolean lastStep;
	private final AssessmentItemsPackage importedItems;
	
	public OverviewQuestionController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form rootForm, AssessmentItemsPackage importedItems, boolean lastStep) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.lastStep = lastStep;
		this.importedItems = importedItems;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.hasError.i18nHeaderKey(), Cols.hasError.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT,
				new BooleanCellRenderer(
						new CSSIconFlexiCellRenderer("o_icon_failed"),
						new CSSIconFlexiCellRenderer("o_icon_accept"))
		));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.points));
		
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
	
	private class ItemsTableDataModel extends DefaultFlexiTableDataModel<AssessmentItemAndMetadata> {
		private FlexiTableColumnModel columnModel;
		
		public ItemsTableDataModel(List<AssessmentItemAndMetadata> options, FlexiTableColumnModel columnModel) {
			super(options, columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			AssessmentItemAndMetadata importedItem = getObject(row);
			AssessmentItemBuilder itemBuilder = importedItem.getItemBuilder();
			switch(Cols.values()[col]) {
				case hasError: return importedItem.isHasError();
				case type: return getTypeLabel(itemBuilder.getQuestionType());
				case title: return itemBuilder.getTitle();
				case points: {
					ScoreBuilder score = itemBuilder.getMaxScoreBuilder();
					if(score == null) {
						return null;
					}
					return score.getScore();
				}
				default: return itemBuilder.getAssessmentItem();
			}
		}
		
		private String getTypeLabel(QTI21QuestionType type) {
			switch(type) {
				case sc: return translate("item.type.sc");
				case mc: return translate("item.type.mc");
				case fib: return translate("item.type.fib");
				case numerical: return translate("item.type.numerical");
				case kprim: return translate("item.type.kprim");
				case essay: return translate("item.type.essay");
				case match: return translate("item.type.match");
				case matchdraganddrop: return translate("item.type.matchdraganddrop");
				case matchtruefalse: return translate("item.type.matchtruefalse");
				default: return "??";
			}
		}

		@Override
		public ItemsTableDataModel createCopyWithEmptyList() {
			return new ItemsTableDataModel(new ArrayList<>(), columnModel);
		}
	}
	
	public enum Cols implements FlexiColumnDef {
		hasError("table.header.status"),
		type("table.header.type"),
		title("table.header.title"),
		points("table.header.points");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}	
		
		@Override
		public String i18nHeaderKey() {
			return i18n;
		}
	}
}
