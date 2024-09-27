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

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2024-09-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseSelectorController extends FormBasicController {

	private static final int MAX_RESULTS = 50;

	private FormLink browserButton;
	private FormLink applyButton;
	private TextElement quickSearchEl;
	private FormLink resetQuickSearchButton;

	private MultipleSelectionElement selectionEl;
	private StaticTextElement resultsNoneEl;
	private MultipleSelectionElement resultsEl;
	private StaticTextElement resultsMoreEl;

	private final Set<RepositoryEntry> visibleCourses;
	private final Set<Long> currentSelectionKeys;
	private Set<String> selectionKeys;

	public CourseSelectorController(UserRequest ureq, WindowControl wControl, Set<RepositoryEntry> visibleCourses,
									Set<Long> currentSelectionKeys) {
		super(ureq, wControl, "course_selector",
				Util.createPackageTranslator(OpenBadgesUIFactory.class, ureq.getLocale()));
		this.visibleCourses = visibleCourses;
		this.currentSelectionKeys = currentSelectionKeys;
		initForm(ureq);
		updateSelectionUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initSearchLine(formLayout);

		Set<RepositoryEntry> currentCourses = visibleCourses.stream()
				.filter(course -> currentSelectionKeys.contains(course.getKey()))
				.collect(Collectors.toSet());
		SelectionValues selectedKV = createCoursesKV(currentCourses);
		selectionEl = uifactory.addCheckboxesVertical("course.selector.selection", formLayout, selectedKV.keys(),
				selectedKV.values(), 1);
		selectionEl.setHorizontallyAlignedCheckboxes(true);
		selectionEl.setEscapeHtml(false);
		selectionKeys = new HashSet<>(selectionEl.getKeys());
		selectionEl.setVisible(!selectionEl.getKeys().isEmpty());
		if (selectionEl.isVisible()) {
			selectionKeys.forEach(key -> selectionEl.select(key, true));
		}

		StaticTextElement selectionNoneEl = uifactory.addStaticTextElement("course.selector.selection.none",
				"course.selector.selection", translate("course.selector.selection.none"), formLayout);
		selectionNoneEl.setVisible(selectionEl.getKeys().isEmpty());

		resultsNoneEl = uifactory.addStaticTextElement("course.selector.results.none",
				"course.selector.results", translate("course.selector.results.none"), formLayout);
		resultsNoneEl.setVisible(false);

		resultsEl = uifactory.addCheckboxesVertical("course.selector.results", formLayout, emptyStrings(),
				emptyStrings(), 1);
		resultsEl.setHorizontallyAlignedCheckboxes(true);
		resultsEl.setEscapeHtml(false);
		resultsEl.setVisible(false);

		resultsMoreEl = uifactory.addStaticTextElement("course.selector.results.more", null,
				translate("course.selector.results.more", String.valueOf(MAX_RESULTS)), formLayout);
		resultsMoreEl.setVisible(false);

		browserButton = uifactory.addFormLink("course.selector.browser", formLayout, Link.BUTTON_SMALL);
		applyButton = uifactory.addFormLink("course.selector.apply", formLayout, Link.BUTTON_SMALL);
		applyButton.setPrimary(true);
	}

	private void initSearchLine(FormItemContainer formLayout) {
		FormLink quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null,
				formLayout, Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setElementCssClass("o_indicate_search");
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setEnabled(false);
		quickSearchButton.setDomReplacementWrapperRequired(false);

		quickSearchEl = uifactory.addTextElement("quickSearch", null, 32, "", formLayout);
		quickSearchEl.setPlaceholderKey("enter.search.term", null);
		quickSearchEl.setElementCssClass("o_quick_search");
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.addActionListener(FormEvent.ONKEYUP);
		quickSearchEl.setFocus(true);

		resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		resetQuickSearchButton.setElementCssClass("o_reset_search");
		resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		resetQuickSearchButton.setDomReplacementWrapperRequired(false);
	}

	private SelectionValues createCoursesKV(Collection<RepositoryEntry> courses) {
		SelectionValues selectedSV = new SelectionValues();
		for (RepositoryEntry course : courses) {
			StringBuilder sb = new StringBuilder();
			sb.append("<div class=\"o_badge_course_selector_option\">");
			sb.append("<div class=\"o_nowrap\">");
			sb.append(StringHelper.escapeHtml(course.getDisplayname()));
			sb.append(" &middot; ");
			sb.append(course.getKey().toString());
			sb.append("</div>");
			sb.append("</div>");
			selectedSV.add(entry(course.getKey().toString(), sb.toString()));
		}
		selectedSV.sort(SelectionValues.VALUE_ASC);
		return selectedSV;
	}

	private void updateSelectionUI() {
		setSelectedKeys();
		updateSelectionNumUI();
		selectionKeys.forEach(key -> selectionEl.select(key, true));
	}

	private void setSelectedKeys() {
		selectionKeys = new HashSet<>(selectionEl.getSelectedKeys());
	}

	private void updateSelectionNumUI() {
		if (selectionKeys.isEmpty()) {
			selectionEl.setLabel("course.selector.selection", null);
		} else {
			selectionEl.setLabel("course.selector.selection.num", new String[] { String.valueOf(selectionKeys.size()) });
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (browserButton == source) {
			setSelectedKeys();
			fireEvent(ureq, BROWSE_EVENT);
		} else if (applyButton == source) {
			doApply(ureq);
		} else if (quickSearchEl == source) {
			doQuickSearch();
		} else if (resetQuickSearchButton == source) {
			doResetQuickSearch();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == browserButton || source == selectionEl) {
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
		Set<Long> keys = new HashSet<>();

		for (String key : selectionEl.getSelectedKeys()) {
			keys.add(Long.valueOf(key));
		}

		if (resultsEl.isVisible()) {
			for (String key : resultsEl.getSelectedKeys()) {
				keys.add(Long.valueOf(key));
			}
		}

		fireEvent(ureq, new ApplyEvent(keys));
	}

	private void doQuickSearch() {
		resultsNoneEl.setVisible(false);
		resultsMoreEl.setVisible(false);

		String searchText = quickSearchEl.getValue().toLowerCase();
		quickSearchEl.getComponent().setDirty(false);

		if (StringHelper.containsNonWhitespace(searchText)) {
			resultsEl.setLabel("course.selector.results", new String[] { searchText });
			Set<RepositoryEntry> courses = searchCourses(searchText);
			if (!courses.isEmpty()) {
				SelectionValues resultsKV = createCoursesKV(courses);
				boolean cropped = false;
				if (resultsKV.size() > MAX_RESULTS) {
					resultsKV.cropEnd(MAX_RESULTS - 1);
					cropped = true;
				}
				resultsEl.setKeysAndValues(resultsKV.keys(), resultsKV.values());
				resultsEl.setVisible(true);
				if (cropped) {
					resultsMoreEl.setVisible(true);
				}
			} else {
				resultsEl.setKeysAndValues(emptyStrings(), emptyStrings());
				resultsEl.setVisible(false);
			}
		} else {
			resultsEl.setKeysAndValues(emptyStrings(), emptyStrings());
			resultsEl.setVisible(false);
		}

		resultsNoneEl.getComponent().setDirty(true);
		resultsEl.getComponent().setDirty(true);
		resultsMoreEl.getComponent().setDirty(true);
	}

	private Set<RepositoryEntry> searchCourses(String searchText) {
		String lowerSearchText = searchText.toLowerCase();
		return visibleCourses.stream()
				.filter(course -> searchCourse(course, lowerSearchText))
				.collect(Collectors.toSet());
	}

	private boolean searchCourse(RepositoryEntry course, String searchText) {
		if (course.getDisplayname().toLowerCase().contains(searchText)) {
			return true;
		}

		if (course.getExternalRef() != null) {
			if (course.getExternalRef().toLowerCase().contains(searchText)) {
				return true;
			}
		}
		if (Long.toString(course.getKey()).contains(searchText)) {
			return true;
		}
		return false;
	}

	private void doResetQuickSearch() {
		quickSearchEl.setValue("");
		resultsNoneEl.setVisible(false);
		resultsEl.setVisible(false);
		resultsMoreEl.setVisible(false);
	}

	public static final Event BROWSE_EVENT = new Event("course-selector-browse");
	public static class ApplyEvent extends Event {

		@Serial
		private static final long serialVersionUID = -5932890792859728660L;

		private final Set<Long> keys;

		public ApplyEvent(Set<Long> keys) {
			super("course-selector-apply");
			this.keys = keys;
		}

		public Set<Long> getKeys() {
			return keys;
		}
	}
}
