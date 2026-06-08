/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.roommanagement.ui;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractFlexiTableRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * External FlexiTable renderer that replaces the row area with a FullCalendarElement.
 * The FlexiTable toolbar (filter tabs, search, entry count, renderer-type toggle) is
 * still rendered by {@link AbstractFlexiTableRenderer#renderHeaders}.
 * <p>
 * Initial date: 5 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomCalendarRenderer extends AbstractFlexiTableRenderer {

	static final String CALENDAR_ITEM_NAME = "roomCalendar";

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFormItem();
		String id = ftC.getFormDispatchId();

		renderHeaders(renderer, sb, ftE, ubu, translator, renderResult, args);

		sb.append("<div class=\"o_table_wrapper o_table_flexi o_rendertype_user o_rendertype_calendar\">");
		sb.append("<div class=\"o_table_body\">");
		renderBody(renderer, sb, ftC, ubu, translator, renderResult);
		sb.append("</div>");
		sb.append("</div>");

		setHeadersRendered(ftE);

		if (source.isEnabled()) {
			FormJSHelper.appendFlexiFormDirty(sb, ftE.getRootForm(), id);
		}
	}

	@Override
	protected void renderBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		Form theForm = ftC.getFormItem().getRootForm();
		FormItem calendarItem = theForm.getFormItemContainer().getFormComponent(CALENDAR_ITEM_NAME);
		if (calendarItem != null && calendarItem.isVisible()) {
			// Use Renderer.render() so the infrastructure adds the <div id="o_c{id}"> wrapper
			// that FullCalendarComponentRenderer's init script expects (jQuery('#o_c{id}')).
			// Use this instead of renderFormItem(), which bypasses this wrapper and the calendar cannot initialize.
			Component cmp = calendarItem.getComponent();
			renderer.render(target, cmp, null);
			cmp.setDirty(false);
		}
	}

	@Override
	protected void renderUserOptions(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu,
			Translator translator, RenderResult renderResult) {
		//
	}

	@Override
	protected void renderHeaders(StringOutput target, FlexiTableComponent ftC, Translator translator) {
		//
	}

	@Override
	protected void renderZeroRow(Renderer renderer, StringOutput target, FlexiTableComponent ftC, String rowIdPrefix,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		//
	}

	@Override
	protected void renderRow(Renderer renderer, StringOutput target, FlexiTableComponent ftC, String rowIdPrefix,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult) {
		//
	}

	@Override
	protected void renderFooter(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		//
	}
}
