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
package org.olat.core.gui.components.form.flexible.impl.elements.table;


import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Render the table as a long HTML table
 * @author Christian Guretzki
 */
class FlexiTableClassicRenderer extends AbstractFlexiTableRenderer implements ComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		super.render(renderer, target, source, ubu, translator, renderResult, args);
	}

	@Override
	protected void renderHeaderSort(StringOutput sb, FlexiTableComponent ftC, FlexiColumnModel fcm, int colPos, Translator translator) {
		// sort is not defined
		if (!fcm.isSortable() || fcm.getSortKey() == null) return;
		
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		
		
		Boolean asc = null;
		String sortKey = fcm.getSortKey();
		SortKey[] orderBy = ftE.getOrderBy();
		if(orderBy != null && orderBy.length > 0) {
			for(int i=orderBy.length; i-->0; ) {
				if(sortKey.equals(orderBy[i].getKey())) {
					asc = new Boolean(orderBy[i].isAsc());
				}
			}
		}

		Form theForm = ftE.getRootForm();
		if(asc == null) {
			sb.append("<a class='o_sorting' href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("sort", sortKey), new NameValuePair("asc", "asc")))
			  .append("\"><i class='o_icon o_icon_sort'></i></a>");
		} else if(asc.booleanValue()) {
			sb.append("<a class='o_sorting_asc' href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("sort", sortKey), new NameValuePair("asc", "desc")))
			  .append("\"><i class='o_icon o_icon_sort_asc'></a>");
		} else {
			sb.append("<a class='o_sorting_desc' href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("sort", sortKey), new NameValuePair("asc", "asc")))
			  .append("\"><i class='o_icon o_icon_sort_desc'></a>");
		}
	}
}