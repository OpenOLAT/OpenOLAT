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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.ui.McpToolTreeTableModel.ToolTreeCols;
import org.olat.mcp.ui.McpToolTreeTableModel.ToolTreeRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A set of tools as one tree table: the packs on the first level, the
 * objects on the second level with the name of the reading and of the
 * changing tool. The read-only view shows only the granted tools, a pack
 * without a granted tool is left out. The editable view shows every tool of
 * the registry with a checkbox before its name, and the pack row has the
 * links "reading tools only" and "all tools". The pack and the object names
 * come from the i18n keys <code>pack.&lt;pack&gt;</code> and
 * <code>object.&lt;object&gt;</code>, the tool names stay technical. The
 * controller is always embedded in the form of its parent.
 *
 * Initial date: 12 Oct 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public class McpToolTreeController extends FormBasicController {

	private static final String[] ON_KEYS = new String[] { "on" };
	private static final String CMD_SELECT_READ = "select_read";
	private static final String CMD_SELECT_ALL = "select_all";

	private FlexiTableElement tableEl;
	private McpToolTreeTableModel tableModel;

	private final boolean editable;
	private final Set<String> tools = new HashSet<>();
	private final Map<String,List<McpToolProvider>> providersByPack = new TreeMap<>();
	private final Map<String,MultipleSelectionElement> checkboxes = new LinkedHashMap<>();
	private final Map<String,FormLink[]> packLinks = new TreeMap<>();
	private int counter = 0;

	@Autowired
	private McpToolRegistry toolRegistry;

	/**
	 * The read-only view.
	 *
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param rootForm The form of the parent controller
	 * @param tools The granted tool names
	 */
	public McpToolTreeController(UserRequest ureq, WindowControl wControl, Form rootForm, Collection<String> tools) {
		this(ureq, wControl, rootForm, tools, false);
	}

	/**
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param rootForm The form of the parent controller
	 * @param tools The granted or selected tool names
	 * @param editable true to show every tool with a checkbox, false for the read-only view
	 */
	public McpToolTreeController(UserRequest ureq, WindowControl wControl, Form rootForm, Collection<String> tools,
			boolean editable) {
		super(ureq, wControl, LAYOUT_CUSTOM, "tool_tree", rootForm);
		this.editable = editable;
		if(tools != null) {
			this.tools.addAll(tools);
		}
		for(McpToolProvider provider:toolRegistry.getProviders()) {
			providersByPack.computeIfAbsent(provider.getPack(), pack -> new ArrayList<>()).add(provider);
		}
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToolTreeCols.object, new TreeNodeFlexiCellRenderer(false)));
		McpToolNameCellRenderer toolRenderer = new McpToolNameCellRenderer(getTranslator());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToolTreeCols.readTool, toolRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToolTreeCols.modifyTool, toolRenderer));

		tableModel = new McpToolTreeTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "tools", tableModel, getTranslator(), formLayout);
		tableEl.setElementCssClass(editable
				? "o_mcp_tool_tree o_mcp_tool_tree_edit o_sel_mcp_tool_tree"
				: "o_mcp_tool_tree o_sel_mcp_tool_tree");
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(false);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_tools")
				.withMessageI18nKey("access.check.tools.none")
				.build());
	}

	/**
	 * Replaces the granted or the selected tools.
	 *
	 * @param newTools The tool names
	 */
	public void setTools(Collection<String> newTools) {
		tools.clear();
		if(newTools != null) {
			tools.addAll(newTools);
		}
		for(Map.Entry<String,MultipleSelectionElement> entry:checkboxes.entrySet()) {
			entry.getValue().select(ON_KEYS[0], tools.contains(entry.getKey()));
		}
		loadModel();
	}

	/**
	 * @return The names of the selected tools with a registered provider in
	 *         the editable view, the granted tools in the read-only view
	 */
	public Set<String> getSelectedTools() {
		if(!editable) {
			return new HashSet<>(tools);
		}
		Set<String> selected = new HashSet<>();
		for(Map.Entry<String,MultipleSelectionElement> entry:checkboxes.entrySet()) {
			if(entry.getValue().isAtLeastSelected(1)) {
				selected.add(entry.getKey());
			}
		}
		return selected;
	}

	private void loadModel() {
		Collator collator = Collator.getInstance(getLocale());
		List<ToolTreeRow> rows = new ArrayList<>();
		for(Map.Entry<String,List<McpToolProvider>> entry:providersByPack.entrySet()) {
			List<McpToolProvider> providers = entry.getValue();
			if(!editable && providers.stream().noneMatch(provider -> tools.contains(provider.getToolName()))) {
				continue;
			}

			ToolTreeRow packRow = new ToolTreeRow(null, translate("pack." + entry.getKey()));
			if(editable) {
				FormLink[] links = getPackLinks(entry.getKey());
				packRow.setLinks(links[0], links[1]);
			}
			Map<String,ToolTreeRow> objectRows = new LinkedHashMap<>();
			for(McpToolProvider provider:providers) {
				String object = provider.getObject();
				// the editable view shows every tool, its checkbox holds the selection
				boolean granted = editable || tools.contains(provider.getToolName());
				MultipleSelectionElement checkbox = editable ? getCheckbox(provider) : null;
				ToolTreeRow objectRow = objectRows.get(object);
				if(objectRow == null) {
					objectRow = new ToolTreeRow(packRow, translate("object." + object));
					objectRows.put(object, objectRow);
				}
				if(!objectRow.setTool(provider, granted, checkbox)) {
					// a second tool with the same object and scope gets a row of its own
					ToolTreeRow extraRow = new ToolTreeRow(packRow, objectRow.getLabel());
					extraRow.setTool(provider, granted, checkbox);
					objectRows.put(object + "_" + provider.getToolName(), extraRow);
				}
			}

			List<ToolTreeRow> children = new ArrayList<>(objectRows.values());
			children.removeIf(row -> row.getReadTool() == null && row.getModifyTool() == null);
			children.sort((r1, r2) -> collator.compare(r1.getLabel(), r2.getLabel()));
			rows.add(packRow);
			rows.addAll(children);
		}

		tableModel.setObjects(rows);
		tableModel.openAll();
		tableEl.reset(true, true, true);
	}

	private FormLink[] getPackLinks(String pack) {
		return packLinks.computeIfAbsent(pack, p -> {
			FormLink readLink = uifactory.addFormLink("read_" + p, CMD_SELECT_READ, "profile.tools.select.read", null, flc, Link.BUTTON_XSMALL);
			readLink.setGhost(true);
			readLink.setUserObject(p);
			readLink.setElementCssClass("o_sel_mcp_tools_read_" + p);
			FormLink allLink = uifactory.addFormLink("all_" + p, CMD_SELECT_ALL, "profile.tools.select.all", null, flc, Link.BUTTON_XSMALL);
			allLink.setGhost(true);
			allLink.setUserObject(p);
			allLink.setElementCssClass("o_sel_mcp_tools_all_" + p);
			return new FormLink[] { readLink, allLink };
		});
	}

	/**
	 * The checkbox of a tool is created once and keeps its state over a
	 * reload of the model. A new checkbox is selected if the tool is in the
	 * set of tools.
	 */
	private MultipleSelectionElement getCheckbox(McpToolProvider provider) {
		return checkboxes.computeIfAbsent(provider.getToolName(), toolName -> {
			// the name of the tool is visible, screen readers read the long label
			String label = "<code aria-hidden='true'>" + StringHelper.escapeHtml(toolName) + "</code>"
					+ "<span class='sr-only'>" + StringHelper.escapeHtml(getToolLabel(provider)) + "</span>";
			MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("tool_" + (++counter), null, flc,
					ON_KEYS, new String[] { label });
			checkbox.setEscapeHtml(false);
			checkbox.setUserObject(provider);
			checkbox.setElementCssClass("o_sel_mcp_tool_" + toolName);
			checkbox.getComponent().setSpanAsDomReplaceable(true);
			checkbox.getComponent().setLayout("nolayout");
			checkbox.select(ON_KEYS[0], tools.contains(toolName));
			return checkbox;
		});
	}

	private String getToolLabel(McpToolProvider provider) {
		String title = translate("object." + provider.getObject());
		String key = provider.getScope().isWrite() ? "profile.tool.label.write" : "profile.tool.label";
		return translate(key, title, provider.getToolName());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link && link.getUserObject() instanceof String pack
				&& providersByPack.containsKey(pack)) {
			if(CMD_SELECT_READ.equals(link.getCmd())) {
				doSelectTools(pack, true);
			} else if(CMD_SELECT_ALL.equals(link.getCmd())) {
				doSelectTools(pack, false);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSelectTools(String pack, boolean readOnly) {
		for(McpToolProvider provider:providersByPack.get(pack)) {
			MultipleSelectionElement checkbox = checkboxes.get(provider.getToolName());
			if(checkbox != null) {
				boolean select = !readOnly || !provider.getScope().isWrite();
				checkbox.select(ON_KEYS[0], select);
			}
		}
		markDirty();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
