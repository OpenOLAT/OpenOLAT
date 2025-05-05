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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
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
	private static final String PARAMETER_NAME_ORG = "org_name_";

	private static final int PAGE_SIZE = 50;

	private FormLink applyButton;
	private TextElement quickSearchEl;
	private FormLink resetQuickSearchButton;

	private FormLink loadMoreLink;

	private Set<Long> selectedKeys;
	private final List<OrgSelectorElementImpl.OrgRow> orgRows;
	private List<OrgUIRow> orgUIRows;
	private int maxUnselectedRows = PAGE_SIZE;

	public record OrgUIRow(Long key, String path, String displayPath, String title, String location, String numberOfElements, boolean checked) {}

	public OrgSelectorController(UserRequest ureq, WindowControl wControl, List<OrgSelectorElementImpl.OrgRow> orgRows,
								 Set<Long> selectedKeys, boolean multipleSelection) {
		super(ureq, wControl, "org_selector");

		this.selectedKeys = selectedKeys;
		this.orgRows = orgRows;

		buildUIRows();

		flc.contextPut("inputType", multipleSelection ? "checkbox" : "radio");

		initForm(ureq);
		doResetQuickSearch(ureq);
	}

	private void buildUIRows() {
		orgUIRows = orgRows.stream().map(this::mapToOrgUIRow).sorted(Comparator.comparing(OrgUIRow::path)).toList();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initSearchLine(formLayout);

		loadMoreLink = uifactory.addFormLink("selector.load.more", formLayout, Link.LINK);
		loadMoreLink.setIconLeftCSS("o_icon o_icon_load_more");
		
		applyButton = uifactory.addFormLink("apply", formLayout, Link.BUTTON_SMALL);
		applyButton.setElementCssClass("o_sel_org_apply");
		applyButton.setPrimary(true);
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

	private OrgUIRow mapToOrgUIRow(OrgSelectorElementImpl.OrgRow orgRow) {
		Long key = orgRow.key();
		String path = orgRow.path();
		String displayPath = orgRow.displayPath();
		String title = orgRow.title();
		String location = orgRow.location();
		String numberOfElements = orgRow.numberOfElements() > 1 ? Integer.toString(orgRow.numberOfElements()) : "";
		boolean checked = selectedKeys.contains(key);
		return new OrgUIRow(key, path, displayPath, title, location, numberOfElements, checked);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (applyButton == source) {
			doApply(ureq);
		} else if (quickSearchEl == source) {
			doQuickSearch(ureq);
		} else if (resetQuickSearchButton == source) {
			doResetQuickSearch(ureq);			
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
		doApply(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doApply(UserRequest ureq) {
		HashSet<Long> selectedKeys = new HashSet<>();
		for (String name : ureq.getParameterSet()) {
			if (name.startsWith(PARAMETER_NAME_ORG)) {
				String keyString = name.substring(PARAMETER_NAME_ORG.length());
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
		buildUIRows();

		loadMoreLink.setVisible(false);

		List<OrgUIRow> selectedOrgs = orgUIRows.stream().filter(row -> selectedKeys.contains(row.key)).toList();
		flc.contextPut("selectedOrgs", selectedOrgs);

		quickSearchEl.getComponent().setDirty(false);

		String searchFieldValue = quickSearchEl.getValue().toLowerCase();
		List<OrgUIRow> orgs = orgUIRows.stream()
				.filter(row -> filter(row, searchFieldValue))
				.toList();

		if (orgs.size() > maxUnselectedRows) {
			orgs = orgs.subList(0, maxUnselectedRows);
			loadMoreLink.setVisible(true);
		}

		flc.contextPut("orgs", orgs);
		
		loadMoreLink.getComponent().setDirty(true);
	}
	
	private boolean filter(OrgUIRow orgUIRow, String searchFieldValue) {
		if (!StringHelper.containsNonWhitespace(searchFieldValue)) {
			return true;
		}
		if (orgUIRow.title.toLowerCase().contains(searchFieldValue)) {
			return true;
		}
		if (orgUIRow.path.toLowerCase().contains(searchFieldValue)) {
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
