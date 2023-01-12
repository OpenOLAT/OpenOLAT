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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.impl.FormDecorator;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class FlexiTableCustomRenderer extends AbstractFlexiTableRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FlexiTableComponent ftC = (FlexiTableComponent)source;
		FlexiTableElementImpl ftE = ftC.getFormItem();
		String id = ftC.getFormDispatchId();

		renderHeaders(renderer, sb, ftE, ubu, translator, renderResult, args);
		
		if (ftE.getTableDataModel().getRowCount() == 0 && StringHelper.containsNonWhitespace(ftE.getEmtpyTableMessageKey())) {
			renderEmptyState(renderer, sb, ubu, translator, renderResult, ftE);			
		
		} else {
			//render wrapper
			String wrapperCss = null;
			if(ftE.getCssDelegate() != null) {
				wrapperCss = ftE.getCssDelegate().getWrapperCssClass(FlexiTableRendererType.custom);
			}
			if(!StringHelper.containsNonWhitespace(wrapperCss)) {
				wrapperCss = "o_table_wrapper o_table_flexi";
			}
			sb.append("<div class=\"").append(wrapperCss)
			  .append(" o_table_edit", ftE.isEditMode());
			String css = ftE.getElementCssClass();
			if (css != null) {
				sb.append(" ").append(css);
			}
			sb.append(" o_rendertype_custom\">");
			
			//render body
			String tableCss = null;
			if(ftE.getCssDelegate() != null) {
				tableCss = ftE.getCssDelegate().getTableCssClass(FlexiTableRendererType.custom);
			}
			if(!StringHelper.containsNonWhitespace(tableCss)) {
				tableCss = "o_table_body container-fluid";
			}
			sb.append("<div class='").append(tableCss).append("'>");
			renderBody(renderer, sb, ftC, ubu, translator, renderResult);
			sb.append("</div>");
	
			renderTreeButtons(sb, ftC, translator);
			if(ftE.getDefaultPageSize() > 0) {
				renderPagesLinks(sb, ftC, translator);
			}
			sb.append("</div>");
		}
		
		setHeadersRendered(ftE);
		
		//source
		if (source.isEnabled()) {
			FormJSHelper.appendFlexiFormDirty(sb, ftE.getRootForm(), id);
		}
	}

	@Override
	protected void renderHeaders(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		//do nothing
	}
	
	@Override
	protected void renderUserOptions(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu,
			Translator translator, RenderResult renderResult) {
		//
	}

	@Override
	protected void renderRow(Renderer renderer, StringOutput sb, FlexiTableComponent ftC, String rowIdPrefix,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult) {
		sb.append("<div class='");
		if(ftC.getFormItem().getCssDelegate() != null) {
			String cssClass = ftC.getFormItem().getCssDelegate().getRowCssClass(FlexiTableRendererType.custom, row);
			if (cssClass == null) {
				sb.append("o_table_row row");
			} else {
				sb.append(cssClass);				
			}
		} else {
			sb.append("o_table_row row");
		}
		sb.append("'>");

		FlexiTableElementImpl ftE = ftC.getFormItem();
		VelocityContainer container = ftE.getRowRenderer();
		container.contextPut("f", new FormDecorator(ftE.getRootForm()));

		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		Object rowObject = ftE.getTableDataModel().getObject(row);
		container.contextPut("row", rowObject);
		container.contextPut("rowIndex", row);
		
		FlexiTableColumnModel columnsModel = ftE.getTableDataModel().getTableColumnModel();
		int numOfCols = columnsModel.getColumnCount();
		//link to the table element the form elements in the data model	
		for (int j = 0; j<numOfCols; j++) {
			FlexiColumnModel fcm = columnsModel.getColumnModel(j);
			int columnIndex = fcm.getColumnIndex();
			Object cellValue = columnIndex >= 0 ? dataModel.getValueAt(row, columnIndex) : null;
			if (cellValue instanceof FormItem) {
				FormItem formItem = (FormItem)cellValue;
				addFormItem(formItem, ftE, container, translator);
			} else if (cellValue instanceof FormItemCollection) {
				FormItemCollection collection = (FormItemCollection)cellValue;
				for (FormItem formItem : collection.getFormItems()) {
					addFormItem(formItem, ftE, container, translator);
				}
			}
		}
		
		FlexiTableComponentDelegate cmpDelegate = ftE.getComponentDelegate();
		if(cmpDelegate != null) {
			Iterable<Component> cmps = cmpDelegate.getComponents(row, rowObject);
			if(cmps != null) {
				for(Component cmp:cmps) {
					container.put(cmp.getComponentName(), cmp);
				}
			}
		}
		
		if(dataModel instanceof FlexiTreeTableDataModel) {
			boolean hasChildren = ((FlexiTreeTableDataModel<?>)dataModel).hasChildren(row);
			container.contextPut("hasChildren", hasChildren);
			if(hasChildren) {
				container.contextPut("isOpen", ((FlexiTreeTableDataModel<?>)dataModel).isOpen(row));
			}
		}

		container.getHTMLRendererSingleton().render(renderer, sb, container, ubu, translator, renderResult, null);
		container.contextRemove("openCloseLink");
		container.contextRemove("hasChildren");
		container.contextRemove("rowIndex");
		container.contextRemove("isOpen");
		container.contextRemove("row");
		container.contextRemove("f");
		container.setDirty(false);

		sb.append("</div>");
	}

	private void addFormItem(FormItem formItem, FlexiTableElementImpl ftE, VelocityContainer container,
			Translator translator) {
		formItem.setTranslator(translator);
		if(ftE.getRootForm() != formItem.getRootForm()) {
			formItem.setRootForm(ftE.getRootForm());
		}
		ftE.addFormItem(formItem);
		container.put(formItem.getComponent().getComponentName(), formItem.getComponent());
	}

	@Override
	protected void renderFooter(Renderer renderer, StringOutput target, FlexiTableComponent ftC, URLBuilder ubu,
			Translator translator, RenderResult renderResult) {
		//
	}
}