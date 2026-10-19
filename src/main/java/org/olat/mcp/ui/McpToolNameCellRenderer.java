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
package org.olat.mcp.ui;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.ui.McpToolTreeTableModel.ToolSelection;
import org.olat.mcp.ui.McpToolTreeTableModel.ToolState;

/**
 * Renders the technical name of a granted tool, a hint if the module of the
 * tool is switched off, and a dash with a text for screen readers if the
 * object has no granted tool in the column. The text for screen readers
 * tells a tool which is not granted from a missing tool. In the editable
 * view the checkbox of the tool comes right before its name, the name is
 * the visible part of the label of the checkbox. The row of a pack stays
 * empty in the read-only view.
 *
 * Initial date: 12 Oct 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public class McpToolNameCellRenderer implements FlexiCellRenderer {

	private final Translator translator;

	public McpToolNameCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(cellValue instanceof ToolSelection selection) {
			McpToolProvider provider = selection.tool();
			MultipleSelectionElement checkbox = selection.checkbox();
			target.append("<span class='o_mcp_tool_select ").append(checkbox.getElementCssClass())
			      .append(" o_disabled", !provider.isAvailable()).append("'>");
			if(renderer == null) {
				renderToolName(target, provider);
			} else {
				// the label of the checkbox shows the name of the tool
				renderCheckbox(renderer, target, source, ubu, trans, checkbox);
				renderUnavailable(target, provider);
			}
			target.append("</span>");
		} else if(cellValue instanceof McpToolProvider provider) {
			renderToolName(target, provider);
		} else if(cellValue instanceof ToolState state) {
			String i18nKey = state == ToolState.notGranted ? "tools.not.granted" : "tools.none";
			target.append("<span class='o_muted' aria-hidden='true'>-</span><span class='sr-only'>")
			      .append(StringHelper.escapeHtml(translator.translate(i18nKey)))
			      .append("</span>");
		}
	}

	private void renderToolName(StringOutput target, McpToolProvider provider) {
		target.append("<code>").append(StringHelper.escapeHtml(provider.getToolName())).append("</code>");
		renderUnavailable(target, provider);
	}

	private void renderUnavailable(StringOutput target, McpToolProvider provider) {
		if(!provider.isAvailable()) {
			target.append(" <span class='o_muted o_xsmall'>")
			      .append(StringHelper.escapeHtml(translator.translate("tools.unavailable")))
			      .append("</span>");
		}
	}

	/**
	 * Renders the checkbox like the table renders a form item in a cell, but
	 * without the wrapper of the layout "tablecell" (a block fieldset) to keep
	 * the checkbox and the name of the tool on one line.
	 */
	private void renderCheckbox(Renderer renderer, StringOutput target, FlexiTableComponent source,
			URLBuilder ubu, Translator trans, MultipleSelectionElement checkbox) {
		FlexiTableElementImpl tableEl = source.getFormItem();
		checkbox.setTranslator(trans);
		if(tableEl.getRootForm() != checkbox.getRootForm()) {
			checkbox.setRootForm(tableEl.getRootForm());
		}
		tableEl.addFormItem(checkbox);
		Component cmp = checkbox.getComponent();
		cmp.getHTMLRendererSingleton().render(renderer, target, cmp, ubu, trans, null, new String[] { "nolayout" });
		cmp.setDirty(false);
	}
}
