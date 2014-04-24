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

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class TableRenderer implements ComponentRenderer {

	private static final String CLOSE_HTML_BRACE = "\">";
	private static final String CLOSE_DIV = "</div>";
	private static final String SINGLEQUOTE_CLOSEBRACE_OPEN_TITLE = "');\" title=\"";
	private static final String B_LAST_CHILD = " b_last_child";
	private static final String B_FIRST_CHILD = " b_first_child";
	private static final String A_CLASS = "<a class=\"";
	private static final String HREF_JAVA_SCRIPT_TABLE_FORM_INJECT_COMMAND_AND_SUBMIT = "\" href=\"JavaScript:tableFormInjectCommandAndSubmit('";
	private static final String CLOSE_HREF = "</a>";
	private static final String CLOSE_AND_O2CLICK = "');\" onclick=\"return o2cl();\">";
	private static final String SINGLE_COMMA_SINGLE = "', '";
	private static final String A_HREF = "<a href=\"";
	protected static final String TABLE_MULTISELECT_GROUP = "tb_ms";
	private OLog log = Tracing.createLoggerFor(this.getClass());

	/**
	 * Constructor for TableRenderer. There must be an empty contructor for the Class.forName() call
	 */
	public TableRenderer() {
		super();
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(final Renderer renderer, final StringOutput target, final Component source, final URLBuilder ubu, final Translator translator, final RenderResult renderResult,
			final String[] args) {
		long start = 0;
		if (log.isDebug()) {
			start = System.currentTimeMillis();
		}
		assert (source instanceof Table);
		Table table = (Table) source;

		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();

		String formName = renderMultiselectForm(target, source, ubu, iframePostEnabled);

		int rows = table.getRowCount();
		int cols = table.getColumnCount();
		boolean asc = table.isSortAscending();
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

		// starting real table table
		target.append("<div class=\"o_table_wrapper table-responsive\" id=\"b_overflowscrollbox_").append(table.hashCode()).append("\">");

		target.append("<table id=\"b_table").append(table.hashCode()).append("\" class=\"o_table table table-bordered table-hover").append(CLOSE_HTML_BRACE);
		appendHeaderLinks(target, translator, table, formName, cols, asc);
		appendDataRows(renderer, target, ubu, table, iframePostEnabled, cols, selRowUnSelectable, selRowId, startRowId, endRowId);
		target.append("</table><div class='o_table_footer'>");
		appendSelectDeselectAllButtons(target, translator, table, formName, rows, resultsPerPage);
		appendTablePageing(target, translator, table, formName, rows, resultsPerPage, currentPageId, usePageing);
		appendMultiselectFormActions(target, translator, table);
		target.append("</div></div>");

		appendViewportResizeJsFix(target, source, rows, usePageing);
		
		if (log.isDebug()) {
			long duration = System.currentTimeMillis() - start;
			log.debug("Perf-Test: render takes " + duration);
		}
	}

	private void appendViewportResizeJsFix(final StringOutput target, final Component source, int rows, boolean usePageing) {
		// JS code to resize table to browser view port and display scrollbars in the table
		// when not in pageing mode and more than 1000 results are shown.
		// This prevents a very strange overflow problem in FF that makes all
		// entries after the 1023 entry or even the entire table unreadable.
		// Comment CDATA section to make it work with prototype's stripScripts method !
		if (!usePageing && rows > 1000) {
			target.append("<script type=\"text/javascript\">/* <![CDATA[ */\n ");
			target.append("jQuery(function() { jQuery('#b_overflowscrollbox_").append(source.hashCode()).append("').height(b_viewportHeight()/3*2);});");
			target.append("/* ]]> */\n</script>");
		}
	}

	private void appendMultiselectFormActions(final StringOutput target, final Translator translator, final Table table) {
		// add multiselect form actions
		List<TableMultiSelect> multiSelectActions = table.getMultiSelectActions();
		if (table.isMultiSelect() && multiSelectActions.isEmpty()) {
			throw new OLATRuntimeException(null, "Action key in multiselect table is undefined. Use addMultiSelectI18nAction(\"i18nkey\", \"action\"); to set an action for this multiselect table.",
					null);
		}

		target.append("<div class=\"o_table_buttons\">");
		for (TableMultiSelect action: multiSelectActions) {

			String multiSelectActionIdentifer = action.getAction();
			String value;
			if(action.getI18nKey() != null) {
				value = StringEscapeUtils.escapeHtml(translator.translate(action.getI18nKey()));
			} else {
				value = action.getLabel();
			}

			target.append("<input type=\"submit\" name=\"").append(multiSelectActionIdentifer)
			      .append("\" value=\"").append(value).append("\" class=\"btn btn-default\" />");
		}
		target.append(CLOSE_DIV);
		// add hidden action command placeholders to the form. these will be manipulated when
		// the user clicks on a regular link within the table to e.g. re-sort the columns.
		target.append("<input type=\"hidden\" name=\"cmd\" value=\"\" />")
		      .append("<input type=\"hidden\" name=\"param\" value=\"\" />")
		// close multiselect form
		      .append("</form>");
	}

	private void appendTablePageing(final StringOutput target, final Translator translator, final Table table, final String formName, final int rows, int resultsPerPage, final Integer currentPageId,
			final boolean usePageing) {
		if (usePageing && (rows > resultsPerPage)) {
			int pageid = currentPageId.intValue();
			// paging bug OLAT-935 part missing second page, or missing last page due rounding issues.
			int maxpageid = (int) Math.ceil(((double) rows / (double) resultsPerPage));
			target.append("<div class='o_table_pagination'><ul class='pagination'>");

			appendTablePageingBackLink(target, formName, pageid);
			addPageNumberLinks(target, formName, pageid, maxpageid);
			appendTablePageingNextLink(target, formName, rows, resultsPerPage, pageid);
			appendTablePageingShowallLink(target, translator, table, formName);

			target.append("</ul></div>");

		}
	}

	private void appendTablePageingShowallLink(final StringOutput target, final Translator translator, final Table table, final String formName) {
		if (table.isShowAllLinkEnabled()) {
			target.append("<li>");
			target.append(A_CLASS).append(HREF_JAVA_SCRIPT_TABLE_FORM_INJECT_COMMAND_AND_SUBMIT);
			target.append(formName).append(SINGLE_COMMA_SINGLE).append(Table.COMMAND_PAGEACTION).append(SINGLE_COMMA_SINGLE).append(Table.COMMAND_PAGEACTION_SHOWALL).append(CLOSE_AND_O2CLICK);
			target.append(translator.translate("table.showall")).append("</a></li>");
		}
	}

	private void appendTablePageingNextLink(StringOutput target, String formName, int rows, int resultsPerPage, int pageid) {
		boolean enabled = ((pageid * resultsPerPage) < rows);
		target.append("<li").append(" class='disabled'", !enabled).append("><a href='");
		if(enabled) {
			target.append("JavaScript:tableFormInjectCommandAndSubmit('")
			      .append(formName).append(SINGLE_COMMA_SINGLE).append(Table.COMMAND_PAGEACTION).append(SINGLE_COMMA_SINGLE)
			      .append(Table.COMMAND_PAGEACTION_FORWARD).append(CLOSE_AND_O2CLICK);
		} else {
			target.append("#'>");
		}		
		target.append("&raquo;").append("</a></li>");
	}

	private void appendTablePageingBackLink(StringOutput target, String formName, int pageid) {
		boolean disabled = pageid <= 1;
		target.append("<li").append(" class='disabled'", disabled).append("><a href='");
		if(disabled) {
			target.append("JavaScript:tableFormInjectCommandAndSubmit('")
			      .append(formName).append(SINGLE_COMMA_SINGLE).append(Table.COMMAND_PAGEACTION).append(SINGLE_COMMA_SINGLE)
			      .append(Table.COMMAND_PAGEACTION_BACKWARD).append(CLOSE_AND_O2CLICK);
		} else {
			target.append("#'>");
		}
		target.append("&laquo;").append(CLOSE_HREF);
	}

	private void appendSelectDeselectAllButtons(final StringOutput target, final Translator translator, Table table, String formName, int rows, int resultsPerPage) {
		if (table.isMultiSelect()) {
			target.append("<div class='o_table_checkall input-sm'>");
			target.append("<label class='checkbox-inline'>");
			target.append("<a href='#' onclick=\"javascript:b_table_toggleCheck('").append(formName).append("', true)\">");
			target.append("<input type='checkbox' checked='checked' disabled='disabled' />");
			target.append(translator.translate("checkall"));
			target.append("</a></label>");
			target.append("<label class='checkbox-inline'><a href=\"#\" onclick=\"javascript:b_table_toggleCheck('").append(formName).append("', false)\">");
			target.append("<input type='checkbox' disabled='disabled' />");
			target.append(translator.translate("uncheckall"));
			target.append("</a></label>");
			target.append("</div>");
		}

		if (table.isShowAllSelected() && (rows > resultsPerPage)) {
			target.append("<div class='o_table_pagination'><ul class='pagination'><li>");
			target.append(A_CLASS).append("btn btn-sm btn-default").append(HREF_JAVA_SCRIPT_TABLE_FORM_INJECT_COMMAND_AND_SUBMIT);			
			target.append(formName).append(SINGLE_COMMA_SINGLE).append(Table.COMMAND_PAGEACTION).append(SINGLE_COMMA_SINGLE).append(Table.COMMAND_SHOW_PAGES).append(CLOSE_AND_O2CLICK);
			target.append(translator.translate("table.showpages")).append("</a>");
			target.append("</li><ul></div>");
		}
	}

	private void appendDataRows(final Renderer renderer, final StringOutput target, final URLBuilder ubu, Table table, boolean iframePostEnabled, int cols, boolean selRowUnSelectable, int selRowId,
			int startRowId, int endRowId) {
		String cssClass;
		target.append("<tbody>");
		long startRowLoop = 0;
		if (log.isDebug()) {
			startRowLoop = System.currentTimeMillis();
		}
		int lastVisibleRowId = endRowId - 1;
		for (int i = startRowId; i < endRowId; i++) {
			// the position of the selected row in the tabledatamodel
			int currentPosInModel = table.getSortedRow(i);
			boolean isMark = selRowUnSelectable && (selRowId == currentPosInModel);

			cssClass = defineCssClassDependingOnRow(startRowId, lastVisibleRowId, i);
			// VCRP-16
			TableDataModel<?> model = table.getTableDataModel();
			if (model instanceof TableDataModelWithMarkableRows) {
				TableDataModelWithMarkableRows markableModel = (TableDataModelWithMarkableRows) model;
				String rowCss = markableModel.getRowCssClass(i);
				if (rowCss != null) {
					cssClass += " " + rowCss;
				}
			}

			target.append("<tr class=\"").append(cssClass).append(CLOSE_HTML_BRACE);
			appendSingleDataRow(renderer, target, ubu, table, iframePostEnabled, cols, i, currentPosInModel, isMark);
			target.append("</tr>");
		}
		if (log.isDebug()) {
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
			cssClass = (alignment == ColumnDescriptor.ALIGNMENT_LEFT ? "b_align_normal" : (alignment == ColumnDescriptor.ALIGNMENT_RIGHT ? "b_align_inverse" : "b_align_center"));
			// add css class for first and last column to support older browsers
			if (j == 0) {
				cssClass += B_FIRST_CHILD;
			}
			if (j == cols - 1) {
				cssClass += B_LAST_CHILD;
			}
			target.append("<td class=\"").append(cssClass);
			if (isMark) {
				target.append(" b_table_marked");
			}
			target.append(CLOSE_HTML_BRACE);
			if (j == 0) {
				target.append("<a name=\"b_table\"></a>"); // add once for accessabillitykey
			}
			String action = cd.getAction(i);
			if (action != null) {
				StringOutput so = new StringOutput(100);
				cd.renderValue(so, i, renderer);
				appendSingleDataRowActionColumn(target, ubu, iframePostEnabled, i, currentPosInModel, j, cd, action, so.toString());
			} else {
				cd.renderValue(target, i, renderer);
			}
			target.append("</td>");
		}
	}

	private void appendSingleDataRowActionColumn(final StringOutput target, final URLBuilder ubu, final boolean iframePostEnabled, final int i, final int currentPosInModel, final int j,
			final ColumnDescriptor cd, final String action, final String renderval) {
		// If we have actions on the table rows, we just submit traditional style (not via form.submit())
		// Note that changes in the state of multiselects will not be reflected in the model.
		HrefGenerator hrefG = cd.getHrefGenerator();

		if (hrefG != null) {
			target.append(A_HREF);
			StringOutput link = new StringOutput();
			ubu.buildURI(link, new String[] { Table.COMMANDLINK_ROWACTION_CLICKED, Table.COMMANDLINK_ROWACTION_ID }, new String[] { String.valueOf(currentPosInModel), action }); // url
			target.append(hrefG.generate(currentPosInModel, link.toString()));
			target.append(CLOSE_HTML_BRACE);

		} else if (cd.isPopUpWindowAction()) {
			// render as popup window
			target.append(A_HREF);
			target.append("javascript:{var win=window.open('");
			ubu.buildURI(target, new String[] { Table.COMMANDLINK_ROWACTION_CLICKED, Table.COMMANDLINK_ROWACTION_ID }, new String[] { String.valueOf(currentPosInModel), action }); // url
			target.append("','tw_").append(i + "_" + j); // javascript window
															// name
			target.append("','");
			String popUpAttributes = cd.getPopUpWindowAttributes();
			if (popUpAttributes != null) {
				target.append(popUpAttributes);
			}
			target.append("');win.focus();}\">");
		} else {
			// render in same window
			target.append(A_HREF);
			ubu.buildURI(target, new String[] { Table.COMMANDLINK_ROWACTION_CLICKED, Table.COMMANDLINK_ROWACTION_ID }, new String[] { String.valueOf(currentPosInModel), action },
					iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
			target.append("\"  onclick=\"return o2cl()\"");
			if (iframePostEnabled) {
				ubu.appendTarget(target);
			}
			target.append(">");

		}
		target.append(renderval);
		target.append(CLOSE_HREF);
	}

	private String defineCssClassDependingOnRow(int startRowId, int lastVisibleRowId, int i) {
		String cssClass;
		// use alternating css class
		if (i % 2 == 0) {
			cssClass = "";
		} else {
			cssClass = "b_table_odd";
		}
		// add css class for first and last column to support older browsers
		if (i == startRowId) {
			cssClass += B_FIRST_CHILD;
		}
		if (i == lastVisibleRowId) {
			cssClass += B_LAST_CHILD;
		}
		return cssClass;
	}

	private void appendHeaderLinks(final StringOutput target, final Translator translator, Table table, String formName, int cols, boolean asc) {
		if (table.isDisplayTableHeader()) {
			target.append("<thead><tr>");

			ColumnDescriptor sortedCD = table.getCurrentlySortedColumnDescriptor();
			for (int i = 0; i < cols; i++) {
				ColumnDescriptor cd = table.getColumnDescriptor(i);
				String header;
				if (cd.translateHeaderKey()) {
					header = translator.translate(cd.getHeaderKey());
				} else {
					header = cd.getHeaderKey();
				}

				target.append("<th class=\"");
				// add css class for first and last column to support older browsers
				if (i == 0) {
					target.append(B_FIRST_CHILD);
				}
				if (i == cols - 1) {
					target.append(B_LAST_CHILD);
				}
				target.append(CLOSE_HTML_BRACE);

				// add 'move column left' link (if we are not at the leftmost position)
				if (i != 0 && table.isColumnMovingOffered()) {
					target.append(A_CLASS).append(HREF_JAVA_SCRIPT_TABLE_FORM_INJECT_COMMAND_AND_SUBMIT);
					target.append(formName).append(SINGLE_COMMA_SINGLE + Table.COMMAND_MOVECOLUMN_LEFT + SINGLE_COMMA_SINGLE).append(i).append("');\" class=\"b_table_move_left\" title=\"");
					target.append(StringEscapeUtils.escapeHtml(translator.translate("row.move.left"))).append("\"> <i class='o_icon o_icon_move_left'></i> </a> ");
				}
				// header either a link or not
				if (table.isSortingEnabled() && cd.isSortingAllowed()) {
					target.append(A_CLASS).append(HREF_JAVA_SCRIPT_TABLE_FORM_INJECT_COMMAND_AND_SUBMIT);
					target.append(formName).append(SINGLE_COMMA_SINGLE + Table.COMMAND_SORTBYCOLUMN + SINGLE_COMMA_SINGLE).append(i).append(SINGLEQUOTE_CLOSEBRACE_OPEN_TITLE);
					target.append(StringEscapeUtils.escapeHtml(translator.translate("row.sort"))).append(CLOSE_HTML_BRACE);
					target.append(header);
					target.append(CLOSE_HREF);
				} else {
					target.append(header);
				}
				// mark currently sorted row special
				if (table.isSortingEnabled() && cd == sortedCD) {
					target.append(A_CLASS).append(HREF_JAVA_SCRIPT_TABLE_FORM_INJECT_COMMAND_AND_SUBMIT);
					target.append(formName).append(SINGLE_COMMA_SINGLE + Table.COMMAND_SORTBYCOLUMN + SINGLE_COMMA_SINGLE).append(i).append(SINGLEQUOTE_CLOSEBRACE_OPEN_TITLE);
					target.append(StringEscapeUtils.escapeHtml(translator.translate("row.sort.invert"))).append("\">");
					target.append((asc ? " <i class='o_icon o_icon_sort_asc'></i> " : " <i class='o_icon o_icon_sort_desc'></i> "));
					target.append(CLOSE_HREF);
				}

				// add 'move column right' link (if we are not at the rightmost
				// position)
				if (i != cols - 1 && table.isColumnMovingOffered()) {
					target.append(A_CLASS).append(HREF_JAVA_SCRIPT_TABLE_FORM_INJECT_COMMAND_AND_SUBMIT);
					target.append(formName).append(SINGLE_COMMA_SINGLE + Table.COMMAND_MOVECOLUMN_RIGHT + SINGLE_COMMA_SINGLE).append(i).append("');\" class=\"b_table_move_right\" title=\"");
					target.append(StringEscapeUtils.escapeHtml(translator.translate("row.move.right"))).append("\"> <i class='o_icon o_icon_move_right'></i> </a>");
				}
				target.append("</th>");
			}
			target.append("</tr></thead>");
		}
	}

	private String renderMultiselectForm(final StringOutput target, final Component source, final URLBuilder ubu, final boolean iframePostEnabled) {
		String formName = "tb_ms_" + source.hashCode();
		target.append("<form method=\"post\" name=\"");
		target.append(formName);
		target.append("\" action=\"");
		ubu.buildURI(target, null, null, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		target.append("\" id=\"");
		target.append(formName);
		target.append("\"");
		if (iframePostEnabled) {
			ubu.appendTarget(target);
		}
		target.append(" onsubmit=\"o_beforeserver();\">");
		return formName;
	}

	/**
	 * @param target
	 * @param ubu
	 * @param pageid
	 * @param maxpageid
	 */
	private void addPageNumberLinks(StringOutput target, String formName, int pageid, int maxpageid) {
		if (maxpageid < 12) {
			addPageNumberLinksForSimpleCase(target, formName, pageid, maxpageid);
			return;
		}

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

	private void appendPagenNumberLink(StringOutput target, String formName, int pageid, int i) {
		target.append("<li").append(" class='active'", pageid == i).append("><a ")
		      .append(" href=\"JavaScript:tableFormInjectCommandAndSubmit('")
		      .append(formName).append(SINGLE_COMMA_SINGLE + Table.COMMAND_PAGEACTION + SINGLE_COMMA_SINGLE).append(i).append("');\">")
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

	private void addPageNumberLinksForSimpleCase(final StringOutput target, String formName, int pageid, int maxpageid) {
		for (int i = 1; i <= maxpageid; i++) {
			appendPagenNumberLink(target, formName, pageid, i);
		}
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		//
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		//
	}

}