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
package org.olat.core.gui.components.form.flexible.impl.elements.table;


import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class FlexiTableCustomRenderer extends AbstractFlexiTableRenderer implements ComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FlexiTableComponent ftC = (FlexiTableComponent)source;
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();

		String id = ftC.getFormDispatchId();
		sb.append("<div class=\"b_table_wrapper b_floatscrollbox\">");
		renderHeaderButtons(renderer, sb, ftE, ubu, translator, renderResult, args);

		//render body

		sb.append("<div class='b_table_row'>");
		renderBody(renderer, sb, ftC, ubu, translator, renderResult);
		sb.append("</div>");

		renderFooterButtons(renderer, sb, ftC, ubu, translator, renderResult, args);
		sb.append("</div>");
		
		//source
		if (source.isEnabled()) {
			sb.append(FormJSHelper.getJSStartWithVarDeclaration(id));
			sb.append(FormJSHelper.getSetFlexiFormDirty(ftE.getRootForm(), id));
			sb.append(FormJSHelper.getJSEnd());
		}
	}

	@Override
	protected void renderHeaders(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		//do nothing
	}
	
	@Override
	protected void renderHeaderSort(StringOutput target, FlexiTableComponent ftC, FlexiColumnModel fcm,
			int colPos, Translator translator) {
		//do nothing
	}

	@Override
	protected void renderRow(Renderer renderer, StringOutput sb, FlexiTableComponent ftC, String rowIdPrefix,
			int row, int rows, URLBuilder ubu, Translator translator, RenderResult renderResult) {
		sb.append("<div class='b_clearfix b_table_row'>");

		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		VelocityContainer container = ftE.getRowRenderer();
		
		Object rowObject = ftE.getTableDataModel().getObject(row);
		container.contextPut("row", rowObject);
		
		FlexiTableColumnModel columnsModel = ftE.getTableDataModel().getTableColumnModel();
		int numOfCols = columnsModel.getColumnCount();
		//link to the table element the form elements in the data model	
		for (int j = 0; j<numOfCols; j++) {
			FlexiColumnModel fcm = columnsModel.getColumnModel(j);
			int columnIndex = fcm.getColumnIndex();
			Object cellValue = columnIndex >= 0 ? dataModel.getValueAt(row, columnIndex) : null;
			if (cellValue instanceof FormItem) {
				FormItem formItem = (FormItem)cellValue;
				formItem.setTranslator(translator);
				if(ftE.getRootForm() != formItem.getRootForm()) {
					formItem.setRootForm(ftE.getRootForm());
				}
				ftE.addFormItem(formItem);
				container.put(formItem.getComponent().getComponentName(), formItem.getComponent());
			}
		}

		container.getHTMLRendererSingleton().render(renderer, sb, container, ubu, translator, renderResult, null);
		container.contextRemove("row");

		sb.append("</div>");
	}
}