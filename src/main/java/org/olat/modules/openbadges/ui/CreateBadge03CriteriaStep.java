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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.MarkdownElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.course.core.manager.CourseElementDAO;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.criteria.CourseElementPassedCondition;
import org.olat.modules.openbadges.criteria.CourseElementScoreCondition;
import org.olat.modules.openbadges.criteria.CoursePassedCondition;
import org.olat.modules.openbadges.criteria.CourseScoreCondition;
import org.olat.modules.openbadges.criteria.CoursesPassedCondition;
import org.olat.modules.openbadges.criteria.GlobalBadgesEarnedCondition;
import org.olat.modules.openbadges.criteria.LearningPathProgressCondition;
import org.olat.modules.openbadges.criteria.OtherBadgeEarnedCondition;
import org.olat.modules.openbadges.criteria.Symbol;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge03CriteriaStep extends BasicStep {
	public CreateBadge03CriteriaStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		setI18nTitleAndDescr("form.award.criteria", null);
		setNextStep(new CreateBadge04SummaryStep(ureq, createBadgeClassContext));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadge03CriteriaForm(ureq, wControl, form, runContext, FormBasicController.LAYOUT_CUSTOM, "criteria_step");
	}

	private static class CreateBadge03CriteriaForm extends StepFormBasicController {

		private SingleSelection newRule;
		private ArrayList<Condition> conditions;

		@Autowired
		private OpenBadgesManager openBadgesManager;
		@Autowired
		private CourseElementDAO courseElementDao;
		@Autowired
		private RepositoryManager repositoryManager;;

		public class Condition {
			private final String id;
			private final StaticTextElement andTextEl;
			private final SingleSelection conditionDropdown;
			private final SingleSelection symbolDropdown;
			private final SingleSelection badgesDropdown;
			private final SingleSelection courseElementsDropdown;
			private final MultipleSelectionElement coursesDropdown;
			private final MultipleSelectionElement globalBadgesDropdown;
			private final TextElement valueEl;
			private final StaticTextElement unitEl;
			private final FormLink deleteLink;

			Condition(String id, BadgeCondition badgeCondition, FormItemContainer formLayout, boolean showAndLabel) {
				this.id = id;

				andTextEl = uifactory.addStaticTextElement("form.criteria.condition.and." + id, null,
						translate("form.criteria.condition.and"), formLayout);
				andTextEl.setVisible(showAndLabel);

				conditionDropdown = uifactory.addDropdownSingleselect("form.condition." + id, null,
						formLayout, conditionsKV.keys(), conditionsKV.values());
				if (conditionDropdown.containsKey(badgeCondition.getKey())) {
					conditionDropdown.select(badgeCondition.getKey(), true);
				}
				conditionDropdown.setVisible(true);
				conditionDropdown.addActionListener(FormEvent.ONCHANGE);
				conditionDropdown.setUserObject(this);

				symbolDropdown = uifactory.addDropdownSingleselect("form.condition.symbol." + id, null,
						formLayout, symbolsKV.keys(), symbolsKV.values());
				symbolDropdown.addActionListener(FormEvent.ONCHANGE);

				badgesDropdown = uifactory.addDropdownSingleselect("form.condition.badges." + id, null,
						formLayout, badgesKV.keys(), badgesKV.values());
				badgesDropdown.addActionListener(FormEvent.ONCHANGE);

				courseElementsDropdown = uifactory.addDropdownSingleselect("form.condition.courseElements." + id, null,
						formLayout, courseElementsKV.keys(), courseElementsKV.values());
				courseElementsDropdown.addActionListener(FormEvent.ONCHANGE);

				coursesDropdown = uifactory.addCheckboxesDropdown("form.condition.courses." + id, null,
						formLayout, coursesKV.keys(), coursesKV.values());
				coursesDropdown.addActionListener(FormEvent.ONCHANGE);

				globalBadgesDropdown = uifactory.addCheckboxesDropdown("form.condition.global.badges.earned." + id, null,
						formLayout, globalBadgesKV.keys(), globalBadgesKV.values());
				globalBadgesDropdown.addActionListener(FormEvent.ONCHANGE);

				valueEl = uifactory.addTextElement("form.condition.value." + id, "", 32,
						"", formLayout);

				unitEl = uifactory.addStaticTextElement("form.condition.unit." + id, null,
						"", formLayout);

				deleteLink = uifactory.addFormLink("delete." + id, "delete." + id, "", "",
						formLayout, Link.BUTTON | Link.NONTRANSLATED);
				deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
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
				if (badgeCondition instanceof GlobalBadgesEarnedCondition) {
					globalBadgesDropdown.setVisible(true);
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
					case GlobalBadgesEarnedCondition.KEY -> globalBadgesDropdown.setVisible(true);
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
			public MultipleSelectionElement getGlobalBadgesDropdown() {
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
							globalBadgesDropdown.getSelectedKeys().stream().map(Long::parseLong).toList()
					);
					default -> null;
				};
			}
		}

		private static final String KEY_AUTOMATIC = "automatic";
		private static final String KEY_MANUAL = "manual";
		private final String[] awardProcedureKeys;
		private final String[] awardProcedureValues;
		private final String[] awardProcedureDescriptions;
		private final SelectionValues conditionsKV;
		private final SelectionValues symbolsKV;
		private final SelectionValues badgesKV;
		private final SelectionValues courseElementsKV;
		private final SelectionValues coursesKV;
		private final SelectionValues globalBadgesKV;

		private CreateBadgeClassWizardContext createContext;
		private MarkdownElement descriptionEl;
		private SingleSelection awardProcedureCards;

		public CreateBadge03CriteriaForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout, String customLayoutPageName) {
			super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			awardProcedureKeys = new String[] { KEY_AUTOMATIC, KEY_MANUAL };
			awardProcedureValues = new String[] {
					translate("form.award.procedure.automatic"),
					translate("form.award.procedure.manual")
			};
			awardProcedureDescriptions = new String[] {
					translate("form.award.procedure.automatic.description"),
					translate("form.award.procedure.manual.description")
			};

			badgesKV = new SelectionValues();
			if (createContext.isCourseBadge()) {
				openBadgesManager.getBadgeClasses(createContext.getBadgeClass().getEntry()).stream()
						.filter((badgeClass) -> !badgeClass.getUuid().equals(createContext.getBadgeClass().getUuid()))
						.forEach((badgeClass) -> badgesKV.add(SelectionValues.entry(badgeClass.getUuid(), badgeClass.getName())));
			}

			courseElementsKV = new SelectionValues();
			if (createContext.isCourseBadge()) {
				createContext.assessableCourseNodes().stream()
						.filter(courseNode -> !STCourseNode.TYPE.equals(courseNode.getType()))
						.forEach(courseNode -> courseElementsKV.add(SelectionValues.entry(courseNode.getIdent(), courseNode.getShortName())));
			}

			coursesKV = new SelectionValues();
			if (createContext.isGlobalBadge()) {
				SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(getIdentity(), Roles.administratorRoles());
				params.setResourceTypes(Collections.singletonList("CourseModule"));
				repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true).stream()
						.filter(re -> RepositoryEntryStatusEnum.published.equals(re.getEntryStatus()))
						.forEach(re -> coursesKV.add(SelectionValues.entry(re.getOlatResource().getKey().toString(), re.getDisplayname())));
			}

			globalBadgesKV = new SelectionValues();
			if (createContext.isGlobalBadge()) {
				openBadgesManager.getBadgeClasses(null).stream()
						.filter(badgeClass -> !badgeClass.getKey().equals(createContext.getBadgeClass().getKey()))
						.map((badgeClass) -> SelectionValues.entry(Long.toString(badgeClass.getKey()), badgeClass.getName()))
						.forEach(globalBadgesKV::add);
			}

			conditionsKV = new SelectionValues();
			if (createContext.isCourseBadge()) {
				conditionsKV.add(SelectionValues.entry(CoursePassedCondition.KEY, translate("form.criteria.condition.course.passed")));
				conditionsKV.add(SelectionValues.entry(CourseScoreCondition.KEY, translate("form.criteria.condition.course.score")));
				if (createContext.isLearningPath()) {
					conditionsKV.add(SelectionValues.entry(LearningPathProgressCondition.KEY, translate("form.criteria.condition.learning.path.progress")));
				}
				if (!badgesKV.isEmpty()) {
					conditionsKV.add(SelectionValues.entry(OtherBadgeEarnedCondition.KEY, translate("form.criteria.condition.otherBadgeEarned")));
				}
				if (!courseElementsKV.isEmpty()) {
					conditionsKV.add(SelectionValues.entry(CourseElementPassedCondition.KEY, translate("form.criteria.condition.course.element.passed")));
					conditionsKV.add(SelectionValues.entry(CourseElementScoreCondition.KEY, translate("form.criteria.condition.course.element.score")));
				}
			}

			if (createContext.isGlobalBadge()) {
				if (!globalBadgesKV.isEmpty()) {
					conditionsKV.add(SelectionValues.entry(GlobalBadgesEarnedCondition.KEY, translate("form.criteria.condition.global.badges.earned")));
				}
				if (!coursesKV.isEmpty()) {
					conditionsKV.add(SelectionValues.entry(CoursesPassedCondition.KEY, translate("form.criteria.condition.courses.passed")));
				}
			}

			symbolsKV = new SelectionValues();
			symbolsKV.add(SelectionValues.entry(Symbol.greaterThan.name(), Symbol.greaterThan.getSymbolString()));
			symbolsKV.add(SelectionValues.entry(Symbol.greaterThanOrEqual.name(), Symbol.greaterThanOrEqual.getSymbolString()));
			symbolsKV.add(SelectionValues.entry(Symbol.equals.name(), Symbol.equals.getSymbolString()));
			symbolsKV.add(SelectionValues.entry(Symbol.lessThanOrEqual.name(), Symbol.lessThanOrEqual.getSymbolString()));
			symbolsKV.add(SelectionValues.entry(Symbol.lessThan.name(), Symbol.lessThan.getSymbolString()));

			initForm(ureq);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == awardProcedureCards) {
				boolean awardAutomatically = KEY_AUTOMATIC.equals(awardProcedureCards.getSelectedKey());
				flc.contextPut("awardAutomatically", awardAutomatically);
			} else if (source.getUserObject() instanceof Condition condition) {
				if (source == condition.getConditionDropdown()) {
					condition.updateVisibilities();
					setVelocityConditions();
				} else if (source == condition.getDeleteLink()) {
					conditions.remove(condition);
					setVelocityConditions();
				}
			} else if (source == newRule) {
				doAddCondition();
			}
			super.formInnerEvent(ureq, source, event);
		}

		private void setVelocityConditions() {
			flc.contextPut("conditions", conditions);

			SelectionValues newConditionsKV = new SelectionValues();
			newConditionsKV.addAll(conditionsKV);

			Set<String> unusedBadgeKeys = new HashSet<>(Set.of(badgesKV.keys()));
			Set<String> subIdentsNotUsedInPassedCondition = new HashSet<>(Set.of(courseElementsKV.keys()));
			for (Condition condition : conditions) {
				BadgeCondition badgeCondition = condition.asBadgeCondition();
				if (badgeCondition instanceof OtherBadgeEarnedCondition otherBadgeEarnedCondition) {
					unusedBadgeKeys.remove(otherBadgeEarnedCondition.getBadgeClassUuid());
					if (unusedBadgeKeys.isEmpty()) {
						newConditionsKV.remove(badgeCondition.getKey());
					}
				} else if (badgeCondition instanceof CourseElementPassedCondition courseElementPassedCondition) {
					subIdentsNotUsedInPassedCondition.remove(courseElementPassedCondition.getSubIdent());
					if (subIdentsNotUsedInPassedCondition.isEmpty()) {
						newConditionsKV.remove(badgeCondition.getKey());
					}
				} else if (badgeCondition instanceof CourseElementScoreCondition) {
					// allow more than one course element score condition
				} else if (badgeCondition instanceof CourseScoreCondition) {
					// allow more than one course score condition
				} else {
					newConditionsKV.remove(badgeCondition.getKey());
				}
			}

			newRule.setVisible(!newConditionsKV.isEmpty());
			newRule.setKeysAndValues(newConditionsKV.keys(), newConditionsKV.values(), null);
		}

		private void doAddCondition() {
			if (!newRule.isOneSelected()) {
				return;
			}
			String key = newRule.getSelectedKey();
			BadgeCondition newBadgeCondition = switch (key) {
				case CoursePassedCondition.KEY -> new CoursePassedCondition();
				case CourseScoreCondition.KEY -> new CourseScoreCondition(Symbol.greaterThan, 1);
				case LearningPathProgressCondition.KEY -> new LearningPathProgressCondition(Symbol.greaterThan, 50);
				case OtherBadgeEarnedCondition.KEY -> new OtherBadgeEarnedCondition(getUnusedBadgeKey());
				case CourseElementPassedCondition.KEY -> new CourseElementPassedCondition(getSubIdentsNotUsedInPassedCondition());
				case CourseElementScoreCondition.KEY -> new CourseElementScoreCondition(getSubIdentsNotUsedInScoreCondition(), Symbol.greaterThan, 1);
				case CoursesPassedCondition.KEY -> new CoursesPassedCondition();
				case GlobalBadgesEarnedCondition.KEY -> new GlobalBadgesEarnedCondition();
				default -> null;
			};
			if (newBadgeCondition != null) {
				String id = Long.toString(conditions.size());
				Condition condition = new Condition(id, newBadgeCondition, flc, !conditions.isEmpty());
				conditions.add(condition);
				setVelocityConditions();
			}
		}

		private String getUnusedBadgeKey() {
			Set<String> unusedBadgeKeys = new HashSet<>(Set.of(badgesKV.keys()));
			for (Condition condition : conditions) {
				if (condition.asBadgeCondition() instanceof OtherBadgeEarnedCondition otherBadgeEarnedCondition) {
					unusedBadgeKeys.remove(otherBadgeEarnedCondition.getBadgeClassUuid());
				}
			}
			return unusedBadgeKeys.isEmpty() ? null : unusedBadgeKeys.iterator().next();
		}

		private String getSubIdentsNotUsedInPassedCondition() {
			Set<String> unusedSubIdents = new HashSet<>(Set.of(courseElementsKV.keys()));
			for (Condition condition : conditions) {
				if (condition.asBadgeCondition() instanceof CourseElementPassedCondition courseElementPassedCondition) {
					unusedSubIdents.remove(courseElementPassedCondition.getSubIdent());
				}
			}
			return unusedSubIdents.isEmpty() ? null : unusedSubIdents.iterator().next();
		}

		private String getSubIdentsNotUsedInScoreCondition() {
			Set<String> unusedSubIdents = new HashSet<>(Set.of(courseElementsKV.keys()));
			for (Condition condition : conditions) {
				if (condition.asBadgeCondition() instanceof CourseElementScoreCondition courseElementScoreCondition) {
					unusedSubIdents.remove(courseElementScoreCondition.getSubIdent());
				}
			}
			return unusedSubIdents.isEmpty() ? null : unusedSubIdents.iterator().next();
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);

			boolean awardAutomatically = KEY_AUTOMATIC.equals(awardProcedureCards.getSelectedKey());

			newRule.clearError();
			if (awardAutomatically) {
				if (conditions.isEmpty()) {
					newRule.setErrorKey("alert");
					allOk &= false;
				}
			}

			descriptionEl.clearError();
			if (!StringHelper.containsNonWhitespace(descriptionEl.getValue())) {
				descriptionEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}

			return allOk;
		}

		@Override
		protected void formNext(UserRequest ureq) {
			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();
			boolean awardAutomatically = KEY_AUTOMATIC.equals(awardProcedureCards.getSelectedKey());
			badgeCriteria.setDescriptionWithScan(descriptionEl.getValue());
			badgeCriteria.setAwardAutomatically(awardAutomatically);
			badgeCriteria.setConditions(conditions.stream().map(Condition::asBadgeCondition).collect(Collectors.toList()));

			String xml = BadgeCriteriaXStream.toXml(badgeCriteria);
			createContext.getBadgeClass().setCriteria(xml);

			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			flc.contextPut("showAwardProcedure", true);

			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();
			boolean awardAutomatically = badgeCriteria.isAwardAutomatically();
			flc.contextPut("awardAutomatically", awardAutomatically);

			uifactory.addStaticTextElement("form.criteria.summary.explanation", null,
					translate("form.criteria.summary.explanation"), formLayout);
			descriptionEl = uifactory.addMarkdownElement("form.criteria.description",
					"form.criteria.description", badgeCriteria.getDescriptionWithScan(), formLayout);
			descriptionEl.setElementCssClass("o_sel_badge_criteria_summary o_badge_criteria_description");
			descriptionEl.setMandatory(true);
			uifactory.addStaticTextElement("form.award.procedure.description", null,
					translate("form.award.procedure.description"), formLayout);

			awardProcedureCards = uifactory.addCardSingleSelectHorizontal("form.award.procedure", formLayout, awardProcedureKeys,
					awardProcedureValues, awardProcedureDescriptions, null);
			awardProcedureCards.addActionListener(FormEvent.ONCHANGE);
			if (awardAutomatically) {
				awardProcedureCards.select(KEY_AUTOMATIC, true);
			} else {
				awardProcedureCards.select(KEY_MANUAL, true);
			}

			if (awardAutomatically && !badgeCriteria.getConditions().isEmpty()) {
				uifactory.addStaticTextElement("form.criteria.condition.met", null,
						translate("form.criteria.condition.met"), formLayout);
			}

			newRule = uifactory.addDropdownSingleselect("form.condition.new", null,
					formLayout, conditionsKV.keys(), conditionsKV.values());
			newRule.enableNoneSelection(translate("form.criteria.new.rule"));
			newRule.addActionListener(FormEvent.ONCHANGE);

			buildConditionsFromContext(formLayout);
		}

		private void buildConditionsFromContext(FormItemContainer formLayout) {
			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			List<BadgeCondition> badgeConditions = badgeCriteria.getConditions(
					Set.of(conditionsKV.keys()),
					Set.of(courseElementsKV.keys()));
			conditions = new ArrayList<>();
			for (int i = 0; i < badgeConditions.size(); i++) {
				BadgeCondition badgeCondition = badgeConditions.get(i);
				Condition condition = new Condition(Integer.toString(i), badgeCondition, formLayout, i > 0);
				conditions.add(condition);
			}
			setVelocityConditions();
		}
	}
}
