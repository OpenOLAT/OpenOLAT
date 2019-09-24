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

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 15 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TreeNodeFlexiCellRenderer implements FlexiCellRenderer {

	private FlexiCellRenderer labelDelegate = new TextFlexiCellRenderer();
	
	private boolean flatBySearchAndFilter;
	private boolean flatBySort;
	private final String action;
	
	public TreeNodeFlexiCellRenderer() {
		action = "tt-focus";
	}
	
	public TreeNodeFlexiCellRenderer(String action) {
		this.action = action;
	}
	
	public TreeNodeFlexiCellRenderer(FlexiCellRenderer labelDelegate) {
		this.labelDelegate = labelDelegate;
		this.action = null;
	}

	public boolean isFlatBySearchAndFilter() {
		return flatBySearchAndFilter;
	}

	public void setFlatBySearchAndFilter(boolean flatBySearchAndFilter) {
		this.flatBySearchAndFilter = flatBySearchAndFilter;
	}

	public boolean isFlatBySort() {
		return flatBySort;
	}

	public void setFlatBySort(boolean flatBySort) {
		this.flatBySort = flatBySort;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		FlexiTableElementImpl ftE = source.getFlexiTableElement();
		FlexiTreeTableDataModel<?> treeTableModel = ftE.getTreeTableDataModel();
		if(treeTableModel != null) {
			if(isFlat(ftE)) {
				labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
			} else {
				renderIndented(renderer, target, cellValue, row, source, ubu, translator);
			}
		} else {
			labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
		}
	}
	
	private boolean isFlat(FlexiTableElementImpl ftE) {
		return isFlatSearchAndFilter(ftE) || isFlatSort(ftE) ;
	}

	private boolean isFlatSearchAndFilter(FlexiTableElementImpl ftE) {
		return flatBySearchAndFilter
				&& (StringHelper.containsNonWhitespace(ftE.getQuickSearchString()) || isFiltered(ftE.getSelectedFilters()));
	}
	
	private boolean isFlatSort(FlexiTableElementImpl ftE) {
		return flatBySort && isSorted(ftE);
	}

	private boolean isSorted(FlexiTableElementImpl ftE) {
		SortKey[] keys = ftE.getOrderBy();
		return keys != null && keys.length > 0 && keys[0] != null && !"natural".equals(keys[0].getKey());
	}

	private boolean isFiltered(List<FlexiTableFilter> filters) {
		if(filters == null || filters.isEmpty()) return false;
		
		boolean filtered = true;
		for(FlexiTableFilter filter:filters) {
			if(filter.isShowAll()) {
				filtered &= false;
			}
		}
		return filtered;
	}
	
	private void renderIndented(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		FlexiTableElementImpl ftE = source.getFlexiTableElement();
		FlexiTreeTableDataModel<?> treeTableModel = ftE.getTreeTableDataModel();
		
		String id = source.getFormDispatchId();
		Form rootForm = ftE.getRootForm();

		// indentation
		int indentation = treeTableModel.getIndentation(row);
		boolean hasChildren = treeTableModel.hasChildren(row);
		target.append("<div class='o_table_flexi_l").append(indentation)
		      .append(" o_table_flexi_leaf", !hasChildren).append("'>");
		
		if(hasChildren) {
			NameValuePair pair;
			boolean open = treeTableModel.isOpen(row);
			if(open) {
				pair = new NameValuePair("tt-close", Integer.toString(row));
			} else {
				pair = new NameValuePair("tt-open", Integer.toString(row));
			}
			String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, true, true, pair);
			target.append("<a href=\"javascript:").append(jsCode).append(";\"><i class='o_icon o_icon-fw ");
			if(open) {
				target.append("o_icon_close_tree");
			} else {
				target.append("o_icon_open_tree");
			}
			target.append("'> </i></a> ");
		}
		
		if(action == null) {
			labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
		} else {
			NameValuePair pair = new NameValuePair(action, Integer.toString(row));
			String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, false, false, pair);
			target.append("<a href=\"javascript:").append(jsCode).append(";\">");
			labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
			target.append("</a></div>");
		}
	}
}