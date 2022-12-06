/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.core.gui.components.table;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class TableRenderer extends DefaultComponentRenderer {

	protected static final String TABLE_MULTISELECT_GROUP = "tb_ms";
	
	private static final Logger log = Tracing.createLoggerFor(TableRenderer.class);


	@Override
	public void renderComponent(final Renderer renderer, final StringOutput target, final Component source, final URLBuilder ubu, final Translator translator, final RenderResult renderResult,
			final String[] args) {

		Table table = (Table) source;

		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();

		int rows = table.getRowCount();
		int cols = table.getColumnCount();
		boolean selRowUnSelectable = table.isSelectedRowUnselectable();
		// the really selected rowid (from the tabledatamodel)
		int selRowId = table.getSelectedRowId();

		int resultsPerPage = table.getResultsPerPage();
		Integer currentPageId = table.getCurrentPageId();
		boolean usePageing;
		int startRowId = 0;
		int endRowId = rows;
		// initalize pageing
		if (table.isPageingEnabled() && currentPageId != null && !table.isShowAllSelected()) {
			startRowId = ((currentPageId.intValue() - 1) * resultsPerPage);
			endRowId = startRowId + resultsPerPage;
			if (endRowId > rows) {
				endRowId = rows;
			}
			usePageing = true;
		} else {
			startRowId = 0;
			endRowId = rows;
			usePageing = false;
		}

		// Render table wrapper and table

		String formName = renderMultiselectForm(target, source, ubu, iframePostEnabled);
		String scrollableWrapperId = "o_scroll_" + table.getDispatchID();
		target.append("<div class=\"o_table_wrapper\" id=\"o_table_wrapper_").append(table.hashCode()).append("\">")
			  .append("<div class='o_scrollable_wrapper' id=\"").append(scrollableWrapperId).append("\"><div class='o_scrollable'>")
		      .append("<table id=\"o_table").append(table.hashCode()).append("\" class=\"o_table table table-striped table-condensed table-hover")
		      .append(" table-bordered", table.isDisplayTableGrid())
		      .append("\">");		
		appendHeaderLinks(target, translator, table, cols, iframePostEnabled, ubu);
		appendDataRows(renderer, target, ubu, table, iframePostEnabled, cols, selRowUnSelectable, selRowId, startRowId, endRowId);
		target.append("</table></div></div><div class='o_table_footer'>");
		appendTablePageing(target, translator, table, rows, resultsPerPage, currentPageId, usePageing, iframePostEnabled, ubu);
		appendMultiselectFormActions(target, formName, translator, table);
		target.append("</div></div>")
		// lastly close multiselect
	         .append("</form>");
		// Initialize the scrolling overflow indicator code
		target.append("<script>o_initScrollableOverflowIndicator('").append(scrollableWrapperId).append("');</script>");
		appendViewportResizeJsFix(target, source, rows, usePageing);
	}

	private void appendViewportResizeJsFix(final StringOutput target, final Component source, int rows, boolean usePageing) {
		// JS code to resize table to browser view port and display scrollbars in the table
		// when not in pageing mode and more than 1000 results are shown.
		// This prevents a very strange overflow problem in FF that makes all
		// entries after the 1023 entry or even the entire table unreadable.
		// Comment CDATA section to make it work with prototype's stripScripts method !
		if (!usePageing && rows > 1000) {
			target.append("<script>/* <![CDATA[ */\n ")
			      .append("jQuery(function() { jQuery('#o_table_wrapper").append(source.hashCode()).append("').height(o_viewportHeight()/3*2);});")
			      .append("/* ]]> */\n</script>");
		}
	}

	private void appendMultiselectFormActions(StringOutput target, String formName, Translator translator, Table table) {
		// add multiselect form actions
		List<TableMultiSelect> multiSelectActions = table.getMultiSelectActions();
		if (multiSelectActions.size() > 0) {
			target.append("<div class=\"o_button_group\">");
			for (TableMultiSelect action: multiSelectActions) {
				String multiSelectActionIdentifer = action.getAction();
				String value;
				if(action.getI18nKey() != null) {
					value = StringHelper.escapeHtml(translator.translate(action.getI18nKey()));
				} else {
					value = action.getLabel();
				}
	
				target.append("<button type=\"button\" name=\"").append(multiSelectActionIdentifer)
				      .append("\" class=\"btn btn-default\" onclick=\"o_TableMultiActionEvent('").append(formName).append("','").append(multiSelectActionIdentifer).append("');\"><span>").append(value).append("</span></button> ");
			}
			target.append("</div>");
		}
	}

	private void appendTablePageing(StringOutput target, Translator translator, Table table, int rows,
			int resultsPerPage, Integer currentPageId,  boolean usePageing, boolean ajaxEnabled, URLBuilder ubu) {
		if (usePageing && (rows > resultsPerPage)) {
			int pageid = currentPageId.intValue();
			// paging bug OLAT-935 part missing second page, or missing last page due rounding issues.
			int maxpageid = (int) Math.ceil(((double) rows / (double) resultsPerPage));
			target.append("<div class='o_table_pagination'><ul class='pagination'>");

			String formName = "tb_ms_" + table.hashCode();
			appendTablePageingBackLink(target, formName, pageid);
			appendPageNumberLinks(target, formName, pageid, maxpageid, ubu);
			appendTablePageingNextLink(target, formName, rows, resultsPerPage, pageid);
			appendTablePageingShowallLink(target, translator, table, ajaxEnabled, ubu);
			
			target.append("</ul></div>");
		}
		if (table.isShowAllSelected() && (rows > resultsPerPage)) {
			target.append("<div class='o_table_pagination'><ul class='pagination'><li>")
			      .append("<a class=\"").append("btn btn-sm btn-default").append("\" ");	
			
			ubu.buildHrefAndOnclick(target, ajaxEnabled,
					new NameValuePair(Table.FORM_CMD, Table.COMMAND_PAGEACTION),
					new NameValuePair(Table.FORM_PARAM, Table.COMMAND_SHOW_PAGES))
				.append(">")
			    .append(translator.translate("table.showpages")).append("</a>")
			    .append("</li><ul></div>");
		}

	}

	private void appendTablePageingShowallLink(StringOutput sb, Translator translator, Table table, boolean ajaxEnabled, URLBuilder ubu) {
		if (table.isShowAllLinkEnabled()) {
			sb.append("<li><a ");
			ubu.buildHrefAndOnclick(sb, ajaxEnabled,
					new NameValuePair(Table.FORM_CMD,Table.COMMAND_PAGEACTION),
					new NameValuePair(Table.FORM_PARAM, Table.COMMAND_PAGEACTION_SHOWALL))
			   .append(">").append(translator.translate("table.showall")).append("</a></li>");
		}
	}

	private void appendTablePageingNextLink(StringOutput target, String formName, int rows, int resultsPerPage, int pageid) {
		boolean enabled = ((pageid * resultsPerPage) < rows);
		target.append("<li").append(" class='disabled'", !enabled).append("><a ");
		if(enabled) {
			target.append(" href=\"javascript:;\" onclick=\"o_XHRSubmit('")
	              .append(formName).append("','").append(Table.FORM_CMD).append("','").append(Table.COMMAND_PAGEACTION)
	              .append("','").append(Table.FORM_PARAM).append("','").append(Table.COMMAND_PAGEACTION_FORWARD).append("'); return false;\"");
		} else {
			target.append("href=\"javascript:;\"");
		}		
		target.append(">&raquo;").append("</a></li>");
	}

	private void appendTablePageingBackLink(StringOutput target, String formName, int pageid) {
		boolean enabled = pageid > 1;
		target.append("<li").append(" class='disabled'", !enabled).append("><a ");
		if(enabled) {
			target.append(" href=\"javascript:;\" onclick=\"o_XHRSubmit('")
		          .append(formName).append("','").append(Table.FORM_CMD).append("','").append(Table.COMMAND_PAGEACTION)
		          .append("','").append(Table.FORM_PARAM).append("','").append(Table.COMMAND_PAGEACTION_BACKWARD).append("'); return false;\"");
		} else {
			target.append("href=\"javascript:;\"");
		}
		target.append(">&laquo;").append("</a>");
	}

	private void appendSelectDeselectAllButtons(StringOutput target, Translator translator, Table table, boolean iframePostEnabled, URLBuilder ubu) {
			String formID = renderMultiselectForm(target, table, ubu, iframePostEnabled);
		if (table.isMultiSelect() && !table.isMultiSelectAsDisabled()) {
			// Nothing is checked - check all
			target.append("<div class='o_table_checkall'><a id='").append(formID).append("_dsa' href=\"javascript:o_table_toggleCheck('")
				.append(formID).append("',false);o_table_updateCheckAllMenu('")
				.append(formID).append("',true,false);")
				.append("\" title=\"").append(translator.translate("uncheckall")).append("\"")
				.append(" style='display:none'")
				.append("><i class='o_icon o_icon-lg o_icon_check_on' aria-hidden='true'> </i></a>");
			// Everything is checked - uncheck all
			target.append("<a id='").append(formID).append("_sa' href=\"javascript:o_table_toggleCheck('")
				.append(formID).append("',true);o_table_updateCheckAllMenu('")
				.append(formID).append("',false,true);")
				.append("\" title=\"").append(translator.translate("checkall")).append("\"")
				.append("><i class='o_icon o_icon-lg o_icon_check_off' aria-hidden='true'> </i></a></div>");
		} else {
			target.append("<div title=\"").append(translator.translate("table.header.multiselect")).append("\">")
				.append("<i class='o_icon o_icon-lg o_icon_check_disabled text-muted' aria-hidden='true'> </i></div>");
		}
	}

	private void appendDataRows(final Renderer renderer, final StringOutput target, final URLBuilder ubu, Table table, boolean iframePostEnabled, int cols, boolean selRowUnSelectable, int selRowId,
			int startRowId, int endRowId) {
		target.append("<tbody>");
		long startRowLoop = 0;
		if (log.isDebugEnabled()) {
			startRowLoop = System.currentTimeMillis();
		}
		for (int i = startRowId; i < endRowId; i++) {
			String cssClass = "";
			// the position of the selected row in the tabledatamodel
			int currentPosInModel = table.getSortedRow(i);
			boolean isMark = selRowUnSelectable && (selRowId == currentPosInModel);

			TableDataModel<?> model = table.getTableDataModel();
			if (model instanceof TableDataModelWithMarkableRows) {
				TableDataModelWithMarkableRows<?> markableModel = (TableDataModelWithMarkableRows<?>) model;
				String rowCss = markableModel.getRowCssClass(currentPosInModel);
				if (rowCss != null) {
					cssClass += " " + rowCss;
				}
			}

			target.append("<tr class=\"").append(cssClass).append("\">");
			appendSingleDataRow(renderer, target, ubu, table, iframePostEnabled, cols, i, currentPosInModel, isMark);
			target.append("</tr>");
		}
		if (log.isDebugEnabled()) {
			long durationRowLoop = System.currentTimeMillis() - startRowLoop;
			log.debug("Perf-Test: render.durationRowLoop takes " + durationRowLoop);
		}

		// end of table table
		target.append("</tbody>");
	}

	private void appendSingleDataRow(final Renderer renderer, final StringOutput target, final URLBuilder ubu, Table table, final boolean iframePostEnabled, final int cols, final int i,
			final int currentPosInModel, final boolean isMark) {
		String cssClass;
		for (int j = 0; j < cols; j++) {
			ColumnDescriptor cd = table.getColumnDescriptor(j);
			int alignment = cd.getAlignment();
			cssClass = (alignment == ColumnDescriptor.ALIGNMENT_LEFT ? "text-left" : (alignment == ColumnDescriptor.ALIGNMENT_RIGHT ? "text-right" : "text-center"));
			target.append("<td class=\"").append(cssClass);
			if (isMark) {
				target.append(" o_table_marked");
			}
			target.append("\">");
			String action = cd.getAction(i);
			if (action != null) {
				try(StringOutput so = new StringOutput(100)) {
					cd.renderValue(so, i, renderer);
					appendSingleDataRowActionColumn(target, ubu, table, iframePostEnabled, i, currentPosInModel, j, cd, action, so.toString());
				} catch(IOException e) {
					log.error("", e);
				}
			} else {
				cd.renderValue(target, i, renderer);
			}
			target.append("</td>");
		}
	}

	private void appendSingleDataRowActionColumn(StringOutput target, URLBuilder ubu, Table table, boolean ajaxEnabled, int i, int currentPosInModel, int j,
			ColumnDescriptor cd, String action, String renderval) {
		// If we have actions on the table rows, we just submit traditional style (not via form.submit())
		// Note that changes in the state of multiselects will not be reflected in the model.
		target.append("<a ");
		if (cd.isPopUpWindowAction()) {
			// render as popup window
			target.append("href=\"javascript:{var win=window.open('");
			ubu.buildURI(target, new String[] { Table.COMMANDLINK_ROWACTION_CLICKED, Table.COMMANDLINK_ROWACTION_ID }, new String[] { String.valueOf(currentPosInModel), action }); // url
			target.append("','tw_").append(i + "_" + j); // javascript window name
			target.append("','");
			String popUpAttributes = cd.getPopUpWindowAttributes();
			if (popUpAttributes != null) {
				target.append(popUpAttributes);
			}
			target.append("');win.focus();}\">");
		} else {
			// render in same window
			ubu.buildHrefAndOnclick(target, null, ajaxEnabled, !table.isSuppressDirtyFormWarning(), true,
					new NameValuePair(Table.COMMANDLINK_ROWACTION_CLICKED, currentPosInModel),
					new NameValuePair(Table.COMMANDLINK_ROWACTION_ID, action)).append(">");
		}
		target.append(renderval).append("</a>");
	}

	private void appendHeaderLinks(final StringOutput target, final Translator translator, Table table, int cols, boolean ajaxEnabled, URLBuilder ubu) {
		if (!table.isDisplayTableHeader()) return;
		target.append("<thead><tr>");
		for (int i = 0; i < cols; i++) {
			ColumnDescriptor cd = table.getColumnDescriptor(i);
			String header;
			if (cd.translateHeaderKey()) {
				header = translator.translate(cd.getHeaderKey());
			} else {
				header = cd.getHeaderKey();
			}

			int alignment = cd.getHeaderAlignment();
			String cssHeaderClass = (alignment == ColumnDescriptor.ALIGNMENT_LEFT ? "text-left" : (alignment == ColumnDescriptor.ALIGNMENT_RIGHT ? "text-right" : "text-center"));
			target.append("<th class='").append(cssHeaderClass).append("'>");
			// header either a link or not
			if (cd instanceof MultiSelectColumnDescriptor) {
				appendSelectDeselectAllButtons(target, translator, table, ajaxEnabled, ubu);
			} else if (table.isSortingEnabled() && cd.isSortingAllowed()) {
				target.append("<a class='o_orderby' ");
				ubu.buildHrefAndOnclick(target, ajaxEnabled,
						new NameValuePair(Table.FORM_CMD, Table.COMMAND_SORTBYCOLUMN),
						new NameValuePair(Table.FORM_PARAM, i)).append(">")
				      .append(header)
				      .append("</a>");
			} else {
				target.append(header);
			}
			target.append("</th>");
		}
		target.append("</tr></thead>");
	}

	private String renderMultiselectForm(final StringOutput target, final Component source, final URLBuilder ubu, final boolean iframePostEnabled) {
		String formName = "tb_ms_" + source.hashCode();
		target.append("<form method=\"post\" name=\"");
		target.append(formName);
		target.append("\" action=\"");
		ubu.buildURI(target, null, null, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		target.append("\" id=\"").append(formName).append("\"");
		if (iframePostEnabled) {
			target.append(" onsubmit=\"o_XHRSubmit('").append(formName).append("');\">");
		} else {
			target.append(" onsubmit=\"o_beforeserver();\">");
		}
		target.append("<input id=\"o_mai_").append(formName).append("\" type=\"hidden\" name=\"multi_action_identifier\" value=\"\"").append(" />");
		return formName;
	}

	/**
	 * @param target
	 * @param ubu
	 * @param pageid
	 * @param maxpageid
	 */
	private void appendPageNumberLinks(StringOutput target, String formName, int pageid, int maxpageid, URLBuilder ubu) {
		if (maxpageid < 12) {
			addPageNumberLinksForSimpleCase(target, formName, pageid, maxpageid);
		} else {
			int powerOf10 = String.valueOf(maxpageid).length() - 1;
			int maxStepSize = (int) Math.pow(10, powerOf10);
			int stepSize = (int) Math.pow(10, String.valueOf(pageid).length() - 1);
			boolean isStep = false;
			int useEveryStep = 3;
			int stepCnt = 0;
			boolean isNear = false;
			int nearleft = 5;
			int nearright = 5;
			if (pageid < nearleft) {
				nearleft = pageid;
				nearright += (nearright - nearleft);
			} else if (pageid > (maxpageid - nearright)) {
				nearright = maxpageid - pageid;
				nearleft += (nearleft - nearright);
			}
			for (int i = 1; i <= maxpageid; i++) {
				// adapt stepsize if needed
				stepSize = adaptStepsizeIfNeeded(pageid, maxStepSize, stepSize, i);
	
				isStep = ((i % stepSize) == 0);
				if (isStep) {
					stepCnt++;
					isStep = isStep && (stepCnt % useEveryStep == 0);
				}
				isNear = (i > (pageid - nearleft) && i < (pageid + nearright));
				if (i == 1 || i == maxpageid || isStep || isNear) {
					appendPagenNumberLink(target, formName, pageid, i);
				}
			}
		}
	}

	private void appendPagenNumberLink(StringOutput target, String formName, int pageid, int i) {
		target.append("<li").append(" class='active'", pageid == i).append("><a href=\"#\" onclick=\"o_XHRSubmit('")
		      .append(formName).append("','").append(Table.FORM_CMD).append("','").append(Table.COMMAND_PAGEACTION)
		      .append("','").append(Table.FORM_PARAM).append("','").append(i).append("'); return false;\">")
		      .append(i).append("</a></li>");
	}

	private int adaptStepsizeIfNeeded(final int pageid, final int maxStepSize, final int stepSize, final int i) {
		int newStepSize = stepSize;
		if (i < pageid && stepSize > 1 && ((pageid - i) / stepSize == 0)) {
			newStepSize = stepSize / 10;
		} else if (i > pageid && stepSize < maxStepSize && ((i - pageid) / stepSize == 9)) {
			newStepSize = stepSize * 10;
		}
		return newStepSize;
	}

	private void addPageNumberLinksForSimpleCase(StringOutput target, String formName, int pageid, int maxpageid) {
		for (int i = 1; i <= maxpageid; i++) {
			appendPagenNumberLink(target, formName, pageid, i);
		}
	}
}