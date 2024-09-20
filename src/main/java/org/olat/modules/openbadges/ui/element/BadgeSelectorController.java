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
package org.olat.modules.openbadges.ui.element;

import java.io.Serial;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
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
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-09-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeSelectorController extends FormBasicController {
	private static final String PARAMETER_KEY_BADGE_SELECTION = "bsel_";

	private final RepositoryEntry entry;
	private final Set<Long> availableKeys;
	private Set<Long> selectedKeys;
	private final String mediaUrl;

	private FormLink openBrowserButton;
	private FormLink applyButton;
	private FormLink searchButton;
	private TextElement searchFieldEl;
	private FormLink searchResetButton;
	private List<Row> rows;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public record Row(Long key, String image, Size size, String statusString, String title, String version) {}

	public BadgeSelectorController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
								   Set<Long> availableKeys, Set<Long> selectedKeys) {
		super(ureq, wControl, "badge_selector",
				Util.createPackageTranslator(OpenBadgesUIFactory.class, ureq.getLocale()));
		this.entry = entry;
		this.availableKeys = availableKeys;
		this.selectedKeys = selectedKeys;

		mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchButton = uifactory.addFormLink("searchButton", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		searchButton.setElementCssClass("o_indicate_search");
		searchButton.setIconLeftCSS("o_icon o_icon_search");
		searchButton.setEnabled(false);
		searchButton.setDomReplacementWrapperRequired(false);

		searchFieldEl = uifactory.addTextElement("searchField", null, 32, "", formLayout);
		searchFieldEl.setPlaceholderKey("enter.search.term", null);
		searchFieldEl.setDomReplacementWrapperRequired(false);
		searchFieldEl.addActionListener(FormEvent.ONKEYUP);
		searchFieldEl.setFocus(true);

		searchResetButton = uifactory.addFormLink("searchResetButton", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		searchResetButton.setElementCssClass("o_reset_search");
		searchResetButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		searchResetButton.setDomReplacementWrapperRequired(false);

		openBrowserButton = uifactory.addFormLink("badge.selector.open.browser", formLayout, Link.BUTTON_SMALL);
		applyButton = uifactory.addFormLink("apply", formLayout, Link.BUTTON_SMALL);
		applyButton.setPrimary(true);
	}

	private void loadModel() {
		rows = openBadgesManager.getBadgeClassesWithSizes(entry).stream()
				.filter(bce -> availableKeys.contains(bce.badgeClass().getKey()))
				.map(this::row).toList();
		List<Row> selectedRows = rows.stream().filter(row -> selectedKeys.contains(row.key)).toList();
		List<Row> unselectedRows = rows.stream().filter(row -> !selectedKeys.contains(row.key)).toList();

		flc.contextPut("selectedRows", selectedRows);
		flc.contextPut("unselectedRows", unselectedRows);
		flc.contextPut("mediaUrl", mediaUrl);
		flc.contextPut("nbSelected", selectedRows.size());
	}

	private Row row(OpenBadgesManager.BadgeClassWithSize badgeClassWithSize) {
		BadgeClass badgeClass = badgeClassWithSize.badgeClass();
		Size size = badgeClassWithSize.fitIn(40, 40);
		String statusString = getTranslator().translate("class.status." + badgeClass.getStatus().name());
		return new Row(badgeClass.getKey(), badgeClass.getImage(), size, statusString, badgeClass.getName(),
				badgeClass.getVersion());
	}

	private void setSelectedKeys() {
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (openBrowserButton == source) {
			setSelectedKeys();
			fireEvent(ureq, OPEN_BROWSER_EVENT);
		} else if (applyButton == source) {
			doApply(ureq);
		} else if (searchFieldEl == source) {
			doSearch();
		} else if (searchResetButton == source) {
			doResetSearch();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == openBrowserButton) {
			super.propagateDirtinessToContainer(source, fe);
		}
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
			if (name.startsWith(PARAMETER_KEY_BADGE_SELECTION)) {
				String badgeKeyString = name.substring(PARAMETER_KEY_BADGE_SELECTION.length());
				selectedKeys.add(Long.parseLong(badgeKeyString));
			}
		}
		this.selectedKeys = selectedKeys;
		fireEvent(ureq, new BadgesSelectedEvent(selectedKeys));
	}

	private void doSearch() {
		updateRows();
	}

	private void doResetSearch() {
		searchFieldEl.setValue("");
		updateRows();
	}

	private void updateRows() {
		String searchFieldValue = searchFieldEl.getValue().toLowerCase();
		List<Row> unselectedRows = rows.stream()
				.filter(row -> !selectedKeys.contains(row.key))
				.filter(row -> row.title.toLowerCase().contains(searchFieldValue))
				.toList();
		flc.contextPut("unselectedRows", unselectedRows);
	}

	public static final Event OPEN_BROWSER_EVENT = new Event("open.browser");

	private class BadgeClassMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
			if (classFileLeaf != null) {
				return new VFSMediaResource(classFileLeaf);
			}
			return new NotFoundMediaResource();
		}
	}

	public static class BadgesSelectedEvent extends Event {

		@Serial
		private static final long serialVersionUID = -7523245830075971768L;

		private final Set<Long> keys;

		public BadgesSelectedEvent(Set<Long> keys) {
			super("badges-selected");
			this.keys = keys;
		}

		public Set<Long> getKeys() {
			return keys;
		}
	}
}
