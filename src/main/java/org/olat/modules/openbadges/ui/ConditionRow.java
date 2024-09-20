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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.CourseElementPassedCondition;
import org.olat.modules.openbadges.criteria.CourseElementScoreCondition;
import org.olat.modules.openbadges.criteria.CoursePassedCondition;
import org.olat.modules.openbadges.criteria.CourseScoreCondition;
import org.olat.modules.openbadges.criteria.CoursesPassedCondition;
import org.olat.modules.openbadges.criteria.GlobalBadgesEarnedCondition;
import org.olat.modules.openbadges.criteria.LearningPathProgressCondition;
import org.olat.modules.openbadges.criteria.OtherBadgeEarnedCondition;
import org.olat.modules.openbadges.criteria.Symbol;
import org.olat.modules.openbadges.ui.element.BadgeSelectorElement;
import org.olat.modules.openbadges.ui.element.BadgeSelectorElementImpl;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2024-09-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ConditionRow {
	private final String id;
	private final StaticTextElement andTextEl;
	private final SingleSelection conditionDropdown;
	private final SingleSelection symbolDropdown;
	private final SingleSelection badgesDropdown;
	private final SingleSelection courseElementsDropdown;
	private final MultipleSelectionElement coursesDropdown;
	private final BadgeSelectorElement globalBadgesDropdown;
	private final TextElement valueEl;
	private final StaticTextElement unitEl;
	private final FormLink deleteLink;

	public record ConditionRowContext(RepositoryEntry entry, SelectionValues badgesKV, SelectionValues coursesKV,
									  SelectionValues courseElementsKV, SelectionValues globalBadgesKV,
									  SelectionValues conditionsKV, SelectionValues symbolsKV, String mediaUrl) {}

	public ConditionRow(UserRequest ureq, String id, BadgeCondition badgeCondition, FormItemContainer formLayout, boolean showAndLabel,
						Translator translator, ConditionRowContext context) {
		this.id = id;

		FormUIFactory uifactory = FormUIFactory.getInstance();
		andTextEl = uifactory.addStaticTextElement("form.criteria.condition.and." + id, null,
				translator.translate("form.criteria.condition.and"), formLayout);
		andTextEl.setVisible(showAndLabel);

		conditionDropdown = uifactory.addDropdownSingleselect("form.condition." + id, null,
				formLayout, context.conditionsKV.keys(), context.conditionsKV.values());
		if (conditionDropdown.containsKey(badgeCondition.getKey())) {
			conditionDropdown.select(badgeCondition.getKey(), true);
		}
		conditionDropdown.setVisible(true);
		conditionDropdown.addActionListener(FormEvent.ONCHANGE);
		conditionDropdown.setUserObject(this);

		symbolDropdown = uifactory.addDropdownSingleselect("form.condition.symbol." + id, null,
				formLayout, context.symbolsKV.keys(), context.symbolsKV.values());
		symbolDropdown.addActionListener(FormEvent.ONCHANGE);

		badgesDropdown = uifactory.addDropdownSingleselect("form.condition.badges." + id, null,
				formLayout, context.badgesKV.keys(), context.badgesKV.values());
		badgesDropdown.addActionListener(FormEvent.ONCHANGE);

		courseElementsDropdown = uifactory.addDropdownSingleselect("form.condition.courseElements." + id, null,
				formLayout, context.courseElementsKV.keys(), context.courseElementsKV.values());
		courseElementsDropdown.addActionListener(FormEvent.ONCHANGE);

		coursesDropdown = uifactory.addCheckboxesDropdown("form.condition.courses." + id, null,
				formLayout, context.coursesKV.keys(), context.coursesKV.values());
		coursesDropdown.addActionListener(FormEvent.ONCHANGE);

		globalBadgesDropdown = new BadgeSelectorElementImpl(ureq, formLayout.getRootForm().getWindowControl(),
				"form.condition.global.badges." + id, context.entry, context.globalBadgesKV, context.mediaUrl);
		formLayout.add(globalBadgesDropdown);
		globalBadgesDropdown.addActionListener(FormEvent.ONCHANGE);

		valueEl = uifactory.addTextElement("form.condition.value." + id, "", 32,
				"", formLayout);

		unitEl = uifactory.addStaticTextElement("form.condition.unit." + id, null,
				"", formLayout);

		deleteLink = uifactory.addFormLink("delete." + id, "delete." + id, "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED);
		deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
		deleteLink.setTitle(translator.translate("delete"));
		deleteLink.setUserObject(this);

		symbolDropdown.setVisible(false);
		badgesDropdown.setVisible(false);
		courseElementsDropdown.setVisible(false);
		coursesDropdown.setVisible(false);
		globalBadgesDropdown.setVisible(false);
		valueEl.setVisible(false);
		unitEl.setVisible(false);

		if (badgeCondition instanceof CourseScoreCondition courseScoreCondition) {
			symbolDropdown.setVisible(true);
			symbolDropdown.select(courseScoreCondition.getSymbol().name(), true);

			valueEl.setVisible(true);
			valueEl.setValue(Double.toString(courseScoreCondition.getValue()));

			unitEl.setVisible(true);
			unitEl.setValue("Pt.");
		}

		if (badgeCondition instanceof LearningPathProgressCondition learningPathProgressCondition) {
			symbolDropdown.setVisible(true);
			symbolDropdown.select(learningPathProgressCondition.getSymbol().name(), true);

			valueEl.setVisible(true);
			valueEl.setValue(Double.toString(learningPathProgressCondition.getValue()));

			unitEl.setVisible(true);
			unitEl.setValue("%");
		}

		if (badgeCondition instanceof OtherBadgeEarnedCondition otherBadgeCondition) {
			badgesDropdown.setVisible(true);
			if (badgesDropdown.containsKey(otherBadgeCondition.getBadgeClassUuid())) {
				badgesDropdown.select(otherBadgeCondition.getBadgeClassUuid(), true);
			}
		}

		if (badgeCondition instanceof CourseElementPassedCondition courseElementPassedCondition) {
			courseElementsDropdown.setVisible(true);
			if (courseElementsDropdown.containsKey(courseElementPassedCondition.getSubIdent())) {
				courseElementsDropdown.select(courseElementPassedCondition.getSubIdent(), true);
			}
		}
		if (badgeCondition instanceof CourseElementScoreCondition courseElementScoreCondition) {
			courseElementsDropdown.setVisible(true);
			if (courseElementsDropdown.containsKey(courseElementScoreCondition.getSubIdent())) {
				courseElementsDropdown.select(courseElementScoreCondition.getSubIdent(), true);
			}

			symbolDropdown.setVisible(true);
			symbolDropdown.select(courseElementScoreCondition.getSymbol().name(), true);

			valueEl.setVisible(true);
			valueEl.setValue(Double.toString(courseElementScoreCondition.getValue()));

			unitEl.setVisible(true);
			unitEl.setValue("Pt.");
		}
		if (badgeCondition instanceof CoursesPassedCondition coursesPassedCondition) {
			coursesDropdown.setVisible(true);
			for (Long courseResourceKey : coursesPassedCondition.getCourseResourceKeys()) {
				if (coursesDropdown.getKeys().contains(courseResourceKey.toString())) {
					coursesDropdown.select(courseResourceKey.toString(), true);
				}
			}
		}
		if (badgeCondition instanceof GlobalBadgesEarnedCondition globalBadgesEarnedCondition) {
			globalBadgesDropdown.setVisible(true);
			globalBadgesDropdown.setSelection(globalBadgesEarnedCondition.getBadgeClassKeys());
		}
	}

	public void updateVisibilities() {
		String conditionKey = conditionDropdown.getSelectedKey();
		symbolDropdown.setVisible(false);
		badgesDropdown.setVisible(false);
		courseElementsDropdown.setVisible(false);
		coursesDropdown.setVisible(false);
		globalBadgesDropdown.setVisible(false);
		valueEl.setVisible(false);
		unitEl.setVisible(false);
		switch (conditionKey) {
			case CoursePassedCondition.KEY -> {
				//
			}
			case CourseScoreCondition.KEY, LearningPathProgressCondition.KEY -> {
				symbolDropdown.setVisible(true);
				valueEl.setVisible(true);
				unitEl.setVisible(true);
			}
			case CourseElementScoreCondition.KEY -> {
				courseElementsDropdown.setVisible(true);
				symbolDropdown.setVisible(true);
				valueEl.setVisible(true);
				unitEl.setVisible(true);
			}
			case OtherBadgeEarnedCondition.KEY -> badgesDropdown.setVisible(true);
			case CourseElementPassedCondition.KEY -> courseElementsDropdown.setVisible(true);
			case CoursesPassedCondition.KEY -> coursesDropdown.setVisible(true);
			case GlobalBadgesEarnedCondition.KEY -> {
				globalBadgesDropdown.setVisible(true);
			}
		}
	}

	public String getId() {
		return id;
	}

	/**
	 *  Used in template
	 */
	public StaticTextElement getAndTextEl() {
		return andTextEl;
	}

	public SingleSelection getConditionDropdown() {
		return conditionDropdown;
	}

	/**
	 *  Used in template
	 */
	public SingleSelection getSymbolDropdown() {
		return symbolDropdown;
	}

	/**
	 * Used in template
	 */
	public SingleSelection getBadgesDropdown() {
		return badgesDropdown;
	}

	/**
	 * Used in template
	 */
	public SingleSelection getCourseElementsDropdown() {
		return courseElementsDropdown;
	}

	/**
	 * Used in template
	 */
	public MultipleSelectionElement getCoursesDropdown() {
		return coursesDropdown;
	}

	/**
	 * Used in template
	 */
	public BadgeSelectorElement getGlobalBadgesDropdown() {
		return globalBadgesDropdown;
	}

	public TextElement getValueEl() {
		return valueEl;
	}

	/**
	 *  Used in template
	 */
	public StaticTextElement getUnitEl() {
		return unitEl;
	}

	public FormLink getDeleteLink() {
		return deleteLink;
	}

	public BadgeCondition asBadgeCondition() {
		return switch (conditionDropdown.getSelectedKey()) {
			case CoursePassedCondition.KEY -> new CoursePassedCondition();
			case CourseScoreCondition.KEY -> new CourseScoreCondition(
					Symbol.valueOf(symbolDropdown.isOneSelected() ? symbolDropdown.getSelectedKey() : symbolDropdown.getKeys()[0]),
					StringHelper.containsNonWhitespace(valueEl.getValue()) ? Double.parseDouble(valueEl.getValue()) : 0
			);
			case LearningPathProgressCondition.KEY -> new LearningPathProgressCondition(
					Symbol.valueOf(symbolDropdown.isOneSelected() ? symbolDropdown.getSelectedKey() : symbolDropdown.getKeys()[0]),
					StringHelper.containsNonWhitespace(valueEl.getValue()) ? Double.parseDouble(valueEl.getValue()) : 0
			);
			case OtherBadgeEarnedCondition.KEY -> new OtherBadgeEarnedCondition(
					badgesDropdown.isOneSelected() ? badgesDropdown.getSelectedKey() : badgesDropdown.getKeys()[0]
			);
			case CourseElementPassedCondition.KEY -> new CourseElementPassedCondition(
					courseElementsDropdown.isOneSelected() ? courseElementsDropdown.getSelectedKey() : courseElementsDropdown.getKeys()[0]
			);
			case CourseElementScoreCondition.KEY -> new CourseElementScoreCondition(
					courseElementsDropdown.isOneSelected() ? courseElementsDropdown.getSelectedKey() : courseElementsDropdown.getKeys()[0],
					Symbol.valueOf(symbolDropdown.isOneSelected() ? symbolDropdown.getSelectedKey() : symbolDropdown.getKeys()[0]),
					StringHelper.containsNonWhitespace(valueEl.getValue()) ? Double.parseDouble(valueEl.getValue()) : 0
			);
			case CoursesPassedCondition.KEY -> new CoursesPassedCondition(
					coursesDropdown.getSelectedKeys().stream().map(Long::parseLong).toList()
			);
			case GlobalBadgesEarnedCondition.KEY -> new GlobalBadgesEarnedCondition(
					globalBadgesDropdown.getSelection().stream().toList()
			);
			default -> null;
		};
	}
}
