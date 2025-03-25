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
package org.olat.user.ui.organisation.element;

import java.io.Serial;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 2025-03-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrgSelectorController extends FormBasicController {
	private static final String PARAMETER_KEY_ORG_SELECTION = "org_sel_";

	private static final int PAGE_SIZE = 50;

	private FormLink selectButton;
	private TextElement quickSearchEl;
	private FormLink resetQuickSearchButton;
	private StaticTextElement selectionNoneEl;
	private StaticTextElement selectionNumEl;

	private FormLink loadMoreLink;

	private Set<Long> selectedKeys;
	private final boolean multipleSelection;
	private final boolean liveUpdate;
	private final List<Row> rows;
	private int maxUnselectedRows = PAGE_SIZE;

	public record Row(Long key, String path, String title, String location, int size) {}

	public OrgSelectorController(UserRequest ureq, WindowControl wControl, List<OrgSelectorElementImpl.OrgRow> orgRows,
								 Set<Long> selectedKeys, boolean multipleSelection, boolean liveUpdate) {
		super(ureq, wControl, "org_selector");

		this.selectedKeys = selectedKeys;
		this.multipleSelection = multipleSelection;
		this.liveUpdate = liveUpdate;

		rows = orgRows.stream().map(this::row).toList();

		initForm(ureq);
		doResetQuickSearch(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initSearchLine(formLayout);

		selectionNoneEl = uifactory.addStaticTextElement("selector.selection.none",
				"selector.selection", translate("selector.selection.none"), formLayout);

		selectionNumEl = uifactory.addStaticTextElement("selector.selection.num",
				"selector.selection.num", "", formLayout);

		loadMoreLink = uifactory.addFormLink("selector.load.more", formLayout, Link.LINK);
		loadMoreLink.setIconLeftCSS("o_icon o_icon_load_more");
		
		selectButton = uifactory.addFormLink("select", formLayout, Link.BUTTON_SMALL);
		selectButton.setPrimary(true);
	}

	private void initSearchLine(FormItemContainer formLayout) {
		FormLink quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setElementCssClass("o_indicate_search");
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setEnabled(false);
		quickSearchButton.setDomReplacementWrapperRequired(false);
		quickSearchButton.setTitle(translate("search"));

		quickSearchEl = uifactory.addTextElement("quickSearch", null, 32, "", formLayout);
		quickSearchEl.setPlaceholderKey("enter.search.term", null);
		quickSearchEl.setElementCssClass("o_quick_search");
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.addActionListener(FormEvent.ONKEYUP);
		quickSearchEl.setFocus(true);
		quickSearchEl.setAriaLabel(translate("enter.search.term"));

		resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		resetQuickSearchButton.setElementCssClass("o_reset_search");
		resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		resetQuickSearchButton.setDomReplacementWrapperRequired(false);
		resetQuickSearchButton.setTitle(translate("reset"));
	}

	private Row row(OrgSelectorElementImpl.OrgRow orgRow) {
		Long key = orgRow.key();
		String path = orgRow.path();
		String title = orgRow.title();
		String location = orgRow.location();
		int size = 1;
		return new Row(key, path, title, location, size);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (selectButton == source) {
			doSelect(ureq);
		} else if (quickSearchEl == source) {
			doQuickSearch(ureq);
		} else if (resetQuickSearchButton == source) {
			doResetQuickSearch(ureq);			
		} else if (flc == source && liveUpdate) {
			String unselect = ureq.getParameter("unselect");
			String select = ureq.getParameter("select");
			if (unselect != null) {
				Long orgKey = Long.valueOf(unselect);
				if (selectedKeys.contains(orgKey)) {
					selectedKeys.remove(orgKey);
					updateUI();
				}
			} else if (select != null) {
				Long orgKey = Long.valueOf(select);
				if (!selectedKeys.contains(orgKey)) {
					if (multipleSelection) {
						selectedKeys.add(orgKey);
					} else {
						selectedKeys = new HashSet<>();
						selectedKeys.add(orgKey);
					}
					updateUI();
				}
			}
		} else if (loadMoreLink == source) {
			doLoadMore(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSelect(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doSelect(UserRequest ureq) {
		HashSet<Long> selectedKeys = new HashSet<>();
		for (String name : ureq.getParameterSet()) {
			if (name.startsWith(PARAMETER_KEY_ORG_SELECTION)) {
				String keyString = name.substring(PARAMETER_KEY_ORG_SELECTION.length());
				selectedKeys.add(Long.parseLong(keyString));
			}
		}
		this.selectedKeys = selectedKeys;
		fireEvent(ureq, new OrgsSelectedEvent(selectedKeys));
	}
	
	private void doQuickSearch(UserRequest ureq) {
		maxUnselectedRows = PAGE_SIZE;
		updateUI();
		
		fireEvent(ureq, RESIZED_EVENT);
	}
	
	private void doResetQuickSearch(UserRequest ureq) {
		maxUnselectedRows = PAGE_SIZE;
		quickSearchEl.setValue("");
		updateUI();
		
		fireEvent(ureq, RESIZED_EVENT);
	}
	
	private void doLoadMore(UserRequest ureq) {
		maxUnselectedRows += PAGE_SIZE;	
		updateUI();

		fireEvent(ureq, RESIZED_EVENT);
	}
	
	private void updateUI() {
		loadMoreLink.setVisible(false);

		List<Row> selectedRows = rows.stream().filter(row -> selectedKeys.contains(row.key)).toList();
		flc.contextPut("selectedRows", selectedRows);

		selectionNoneEl.setVisible(selectedKeys.isEmpty());
		selectionNumEl.setVisible(!selectedKeys.isEmpty());
		if (multipleSelection) {
			selectionNumEl.setLabel("selector.selection.num", new String[] { String.valueOf(selectedKeys.size()) });
		} else {
			selectionNumEl.setLabel("selector.selection", null);
		}

		quickSearchEl.getComponent().setDirty(false);

		String searchFieldValue = quickSearchEl.getValue().toLowerCase();
		List<Row> unselectedRows = rows.stream()
				.filter(row -> !selectedKeys.contains(row.key))
				.filter(row -> filter(row, searchFieldValue))
				.toList();

		if (unselectedRows.size() > maxUnselectedRows) {
			unselectedRows = unselectedRows.subList(0, maxUnselectedRows);
			loadMoreLink.setVisible(true);
		}

		flc.contextPut("unselectedRows", unselectedRows);
		
		loadMoreLink.getComponent().setDirty(true);
	}
	
	private boolean filter(Row row, String searchFieldValue) {
		if (!StringHelper.containsNonWhitespace(searchFieldValue)) {
			return true;
		}
		if (row.title.toLowerCase().contains(searchFieldValue)) {
			return true;
		}
		if (row.path.toLowerCase().contains(searchFieldValue)) {
			return true;
		}
		return false;
	}
	
	public static class OrgsSelectedEvent extends Event {

		@Serial
		private static final long serialVersionUID = -2594865379910201039L;

		private final Set<Long> keys;

		public OrgsSelectedEvent(Set<Long> keys) {
			super("orgs-selected");
			this.keys = keys;
		}

		public Set<Long> getKeys() {
			return keys;
		}
	}
	
	public static final Event RESIZED_EVENT = new Event("org-selector-resized");
}
