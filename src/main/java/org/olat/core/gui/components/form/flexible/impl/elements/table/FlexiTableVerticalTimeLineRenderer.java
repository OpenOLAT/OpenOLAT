/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate.Data;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 26 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableVerticalTimeLineRenderer extends AbstractFlexiCustomRenderer {
	
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
				wrapperCss = ftE.getCssDelegate().getWrapperCssClass(FlexiTableRendererType.verticalTimeLine);
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
			sb.append(" o_vertical_timeline o_rendertype_timeline\">");
			
			//render body
			String tableCss = null;
			if(ftE.getCssDelegate() != null) {
				tableCss = ftE.getCssDelegate().getTableCssClass(FlexiTableRendererType.verticalTimeLine);
			}
			if(!StringHelper.containsNonWhitespace(tableCss)) {
				tableCss = "o_table_body container-fluid";
			}
			sb.append("<ol class='o_vertical_timeline_rows ").append(tableCss).append("'>");
			renderBody(renderer, sb, ftC, ubu, translator, renderResult);
			sb.append("</ol>");
	
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
	protected void renderBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		
		String id = ftC.getFormDispatchId();
		FlexiTableElementImpl ftE = ftC.getFormItem();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		
		int firstRow = ftE.getFirstRow();
		int maxRows = ftE.getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);
		
		if(firstRow == 0 && ftE.getZeroRowItem() != null) {
			String rowIdPrefix = "frow_" + id + "-";
			renderZeroRow(renderer, target, ftC, rowIdPrefix, ubu, translator, renderResult);
		}

		String rowIdPrefix = "row_" + id + "-";
		int currentYear = 0;
		ZonedDateTime currentDate = null;
		final Locale locale = translator.getLocale();
		for (int i = firstRow; i < lastRow; i++) {	
			if(dataModel.isRowLoaded(i)) {
				Object row = dataModel.getObject(i);
				if(row instanceof FlexiTableTimeLineRow item) {
					ZonedDateTime itemDate = item.getDate();
					if(currentYear != itemDate.getYear()) {
						// close year list and the day list before opening a new one
						if(currentYear != 0) {
							target.append("</ol></li></ol></li>");
						}
						target.append("<li class='o_vertical_timeline_year'><h3>").append(itemDate.getYear()).append("</h3>")
						      .append("<ol>");
					}
					
					if(currentDate == null || !DateUtils.isSameDay(currentDate, itemDate)) {
						// close the day list only if the year has not close it already
						if(currentDate != null && currentYear == itemDate.getYear()) {
							target.append("</ol></li>");
						}
						target.append("<li class='o_vertical_timeline_row'>");
						renderDay(target, itemDate, locale);
						target.append("<ol>");
					}
					
					currentDate = itemDate;
					currentYear = itemDate.getYear();
					
					// row will wrap the <li>
					renderRow(renderer, target, ftC, rowIdPrefix, i, ubu, translator, renderResult);
				}
			}
		}
		
		if(firstRow < lastRow) {
			target.append("</li></ol></li></ol>");
		}
	}
	
	private void renderDay(StringOutput target, ZonedDateTime date, Locale locale) {
		String month = date.getMonth().getDisplayName(TextStyle.SHORT, locale);
		String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, locale);
		int dayOfMonth = date.getDayOfMonth();
		target.append("<span class=\"o_vertical_timeline_day\">")
		      .append("<span class=\"o_vertical_timeline_day_name\">").append(dayName).append("</span>")
		      .append("<span class=\"o_vertical_timeline_day_number\">").append(dayOfMonth).append("</span>")
		      .append("<span class=\"o_vertical_timeline_month_name\">").append(month).append("</span>")
		      .append("</span>");
	}

	@Override
	protected void renderRow(Renderer renderer, StringOutput sb, FlexiTableComponent ftC, String rowIdPrefix,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult) {
		final FlexiTableElementImpl ftE = ftC.getFormItem();
		final Form theForm = ftE.getRootForm();
		
		sb.append("<li class='");
		
		FlexiTableCssDelegate delegate = ftC.getFormItem().getCssDelegate();
		if(delegate != null) {
			String cssClass = delegate.getRowCssClass(FlexiTableRendererType.verticalTimeLine, row);
			if (cssClass == null) {
				sb.append("o_vertical_timeline_item");
			} else {
				sb.append(cssClass);				
			}
		} else {
			sb.append("o_vertical_timeline_item");
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
		
		sb.append("<div class='o_vertical_timeline_item_path'>")
		  .append("<div class=\"o_vertical_timeline_path_top\"></div>")
		  .append("<div class=\"o_vertical_timeline_path_center\">&nbsp;</div>")
		  .append("<div class=\"o_vertical_timeline_path_bottom\"></div>")
		  .append("</div>");

		sb.append("<div class='o_vertical_timeline_item_content'>");
		renderRowContent(renderer, sb, ftC, rowIdPrefix, row, ubu, translator, renderResult);
		
		if(ftE.hasDetailsRenderer()) {
			Object rowObject = ftE.getTableDataModel().getObject(row);
			if (ftE.getComponentDelegate() != null && ftE.getComponentDelegate().isDetailsRow(row, rowObject)) {
				boolean expanded = ftE.isDetailsExpended(row);
				if(expanded) {
					VelocityContainer container = ftE.getDetailsRenderer();
					container.contextPut("row", rowObject);
					
					FlexiTableComponentDelegate cmpDelegate = ftE.getComponentDelegate();
					if(cmpDelegate != null) {
						Iterable<Component> cmps = cmpDelegate.getComponents(row, rowObject);
						if(cmps != null) {
							for(Component cmp:cmps) {
								container.put(cmp.getComponentName(), cmp);
							}
						}
					}
					
					sb.append("<div class=\"o_vertical_timeline_item_details\">")
					  .append("<div class=\"o_vertical_timeline_item_details_container\">");
					container.getHTMLRendererSingleton().render(renderer, sb, container, ubu, translator, renderResult, null);
					container.contextRemove("row");
					container.setDirty(false);
					sb.append("</div></div>");
				}
				
				String collapseIcon = expanded ? "o_icon_details_collaps": "o_icon_details_expand";
				String collapseText = expanded ? translator.translate("form.details.collapse") : translator.translate("form.details.expand");
				sb.append("<button class=\"btn btn-default o_button_details\" onclick=\"");
				sb.append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, false, false, false,
						new NameValuePair("tt-details", Integer.toString(row))));
				sb.append(";");
				sb.append(" return false;\" draggable=\"false\">");
				sb.append("<i class='o_icon o_icon-lg ").append(collapseIcon).append("'> </i>");
				sb.append("<span class='sr-only'>").append(collapseText).append("</span>");
				sb.append("</button>");
			}
		}
		
		sb.append("</div>");

		sb.append("</li>");
	}
}
