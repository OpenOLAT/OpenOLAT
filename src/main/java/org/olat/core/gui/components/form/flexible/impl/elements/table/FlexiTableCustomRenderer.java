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


import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate.Data;
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
class FlexiTableCustomRenderer extends AbstractFlexiCustomRenderer {

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
	protected void renderRow(Renderer renderer, StringOutput sb, FlexiTableComponent ftC, String rowIdPrefix,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult) {
		sb.append("<div class='");
		
		FlexiTableCssDelegate delegate = ftC.getFormItem().getCssDelegate();
		if(delegate != null) {
			String cssClass = delegate.getRowCssClass(FlexiTableRendererType.custom, row);
			if (cssClass == null) {
				sb.append("o_table_row row");
			} else {
				sb.append(cssClass);				
			}
		} else {
			sb.append("o_table_row row");
		}
		sb.append("'");
		if(delegate != null) {
			List<Data> dataAttributes = delegate.getRowDataAttributes(row);
			if(dataAttributes != null) {
				for(Data dataAttribute:dataAttributes) {
					sb.append(" data-").append(dataAttribute.name()).append("=\"").append(dataAttribute.value()).append("\"");
				}
			}
		}
		sb.append(">");
		
		FlexiTableComponentDelegate compDelegate = ftC.getFormItem().getComponentDelegate();
		if (compDelegate != null && compDelegate.isRowClickEnabled()) {
			NameValuePair pair = new NameValuePair("tt-row-clicked", Integer.toString(row));
			String jsCode = FormJSHelper.getXHRFnCallFor(ftC.getFormItem().getRootForm(), ftC.getFormDispatchId(), 1, false, true, true, pair);
			sb.append("<a class=\"o_row_link ");
			sb.append(compDelegate.getRowClickCss(), StringHelper.containsNonWhitespace(compDelegate.getRowClickCss()));
			sb.append("\" href=\"javascript:;\" onclick=\"").append(jsCode).append("; return false;\"");
			sb.append(FormJSHelper.triggerClickOnKeyDown(compDelegate.isRowClickButton()));
			sb.append(" draggable=\"false\">");
		}
		
		renderRowContent(renderer, sb, ftC, rowIdPrefix, row, ubu, translator, renderResult);
		
		if (compDelegate != null && compDelegate.isRowClickEnabled()) {
			sb.append("</a>");
		}
		
		sb.append("</div>");
	}
}