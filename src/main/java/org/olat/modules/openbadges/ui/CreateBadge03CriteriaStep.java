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

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.MarkdownElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge03CriteriaStep extends BasicStep {

	private final CreateBadgeClassWizardContext createBadgeClassContext;

	public CreateBadge03CriteriaStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		this.createBadgeClassContext = createBadgeClassContext;
		setI18nTitleAndDescr("form.award.criteria", null);
		setNextStep(new CreateBadge04DetailsStep(ureq, createBadgeClassContext));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		runContext.put(CreateBadgeClassWizardContext.KEY, createBadgeClassContext);
		return new CreateBadgeCriteriaForm(ureq, wControl, form, runContext, FormBasicController.LAYOUT_CUSTOM, "criteria_step");
	}

	private static class CreateBadgeCriteriaForm extends StepFormBasicController {

		private SingleSelection newRule;
		private ArrayList<ConditionRow> conditionRows;

		@Autowired
		private OpenBadgesManager openBadgesManager;
		@Autowired
		private RepositoryManager repositoryManager;;

		private static final String KEY_AUTOMATIC = "automatic";
		private static final String KEY_MANUAL = "manual";
		private final String[] awardProcedureKeys;
		private final String[] awardProcedureValues;
		private final String[] awardProcedureDescriptions;

		private ConditionRow.ConditionRowContext conditionContext;

		private CreateBadgeClassWizardContext createContext;
		private MarkdownElement descriptionEl;
		private SingleSelection awardProcedureCards;

		public CreateBadgeCriteriaForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout, String customLayoutPageName) {
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

			buildConditionContext(ureq);

			initForm(ureq);
		}

		private void buildConditionContext(UserRequest ureq) {
			SelectionValues badgesKV = new SelectionValues();
			if (createContext.isCourseBadge()) {
				openBadgesManager.getBadgeClasses(createContext.getBadgeClass().getEntry()).stream()
						.filter((badgeClass) -> !badgeClass.getUuid().equals(createContext.getBadgeClass().getUuid()))
						.forEach((badgeClass) -> badgesKV.add(SelectionValues.entry(badgeClass.getUuid(), badgeClass.getName())));
			}

			SelectionValues courseElementsKV = new SelectionValues();
			if (createContext.isCourseBadge()) {
				createContext.assessableCourseNodes().stream()
						.filter(courseNode -> !STCourseNode.TYPE.equals(courseNode.getType()))
						.forEach(courseNode -> courseElementsKV.add(SelectionValues.entry(courseNode.getIdent(), courseNode.getShortName())));
			}

			SelectionValues coursesKV = new SelectionValues();
			if (createContext.isGlobalBadge()) {
				SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(getIdentity(), Roles.administratorRoles());
				params.setResourceTypes(Collections.singletonList("CourseModule"));
				repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true).stream()
						.filter(re -> RepositoryEntryStatusEnum.published.equals(re.getEntryStatus()))
						.forEach(re -> coursesKV.add(SelectionValues.entry(re.getOlatResource().getKey().toString(), re.getDisplayname())));
			}

			SelectionValues globalBadgesKV = new SelectionValues();
			if (createContext.isGlobalBadge()) {
				openBadgesManager.getBadgeClasses(null).stream()
						.filter(badgeClass -> !badgeClass.getKey().equals(createContext.getBadgeClass().getKey()))
						.map((badgeClass) -> SelectionValues.entry(Long.toString(badgeClass.getKey()), badgeClass.getName()))
						.forEach(globalBadgesKV::add);
			}

			SelectionValues conditionsKV = new SelectionValues();
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

			SelectionValues symbolsKV = new SelectionValues();
			symbolsKV.add(SelectionValues.entry(Symbol.greaterThan.name(), Symbol.greaterThan.getSymbolString()));
			symbolsKV.add(SelectionValues.entry(Symbol.greaterThanOrEqual.name(), Symbol.greaterThanOrEqual.getSymbolString()));
			symbolsKV.add(SelectionValues.entry(Symbol.equals.name(), Symbol.equals.getSymbolString()));
			symbolsKV.add(SelectionValues.entry(Symbol.lessThanOrEqual.name(), Symbol.lessThanOrEqual.getSymbolString()));
			symbolsKV.add(SelectionValues.entry(Symbol.lessThan.name(), Symbol.lessThan.getSymbolString()));

			String mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());

			conditionContext = new ConditionRow.ConditionRowContext(createContext.getBadgeClass().getEntry(),
					badgesKV, coursesKV, courseElementsKV,
					globalBadgesKV, conditionsKV, symbolsKV, mediaUrl);
		}

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

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == awardProcedureCards) {
				boolean awardAutomatically = KEY_AUTOMATIC.equals(awardProcedureCards.getSelectedKey());
				flc.contextPut("awardAutomatically", awardAutomatically);
			} else if (source.getUserObject() instanceof ConditionRow conditionRow) {
				if (source == conditionRow.getConditionDropdown()) {
					conditionRow.updateVisibilities();
					setVelocityConditions();
				} else if (source == conditionRow.getDeleteLink()) {
					conditionRows.remove(conditionRow);
					setVelocityConditions();
				}
			} else if (source == newRule) {
				doAddCondition(ureq);
			}
			super.formInnerEvent(ureq, source, event);
		}

		private void setVelocityConditions() {
			flc.contextPut("conditionRows", conditionRows);

			SelectionValues newConditionsKV = new SelectionValues();
			newConditionsKV.addAll(conditionContext.conditionsKV());

			Set<String> unusedBadgeKeys = new HashSet<>(Set.of(conditionContext.badgesKV().keys()));
			Set<String> subIdentsNotUsedInPassedCondition = new HashSet<>(Set.of(conditionContext.courseElementsKV().keys()));
			for (ConditionRow condition : conditionRows) {
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

		private void doAddCondition(UserRequest ureq) {
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
				String id = Long.toString(conditionRows.size());
				ConditionRow conditionRow = new ConditionRow(ureq, id, newBadgeCondition, flc, !conditionRows.isEmpty(),
						getTranslator(), conditionContext);
				conditionRows.add(conditionRow);
				setVelocityConditions();
			}
		}

		private String getUnusedBadgeKey() {
			Set<String> unusedBadgeKeys = new HashSet<>(Set.of(conditionContext.badgesKV().keys()));
			for (ConditionRow conditionRow : conditionRows) {
				if (conditionRow.asBadgeCondition() instanceof OtherBadgeEarnedCondition otherBadgeEarnedCondition) {
					unusedBadgeKeys.remove(otherBadgeEarnedCondition.getBadgeClassUuid());
				}
			}
			return unusedBadgeKeys.isEmpty() ? null : unusedBadgeKeys.iterator().next();
		}

		private SelectionValues.SelectionValue getUnusedCourseElementPassedConditions() {
			Set<String> usedSubIdents = conditionRows.stream().map(ConditionRow::asBadgeCondition)
					.filter(bc -> bc instanceof CourseElementPassedCondition)
					.map(CourseElementPassedCondition.class::cast)
					.map(CourseElementPassedCondition::getSubIdent)
					.collect(Collectors.toSet());
			for (SelectionValues.SelectionValue courseElement : conditionContext.courseElementsKV().keyValues()) {
				if (!usedSubIdents.contains(courseElement.getKey())) {
					return courseElement;
				}
			}
			return null;
		}

		private String getSubIdentsNotUsedInPassedCondition() {
			Set<String> unusedSubIdents = new HashSet<>(Set.of(conditionContext.courseElementsKV().keys()));
			for (ConditionRow condition : conditionRows) {
				if (condition.asBadgeCondition() instanceof CourseElementPassedCondition courseElementPassedCondition) {
					unusedSubIdents.remove(courseElementPassedCondition.getSubIdent());
				}
			}
			return unusedSubIdents.isEmpty() ? null : unusedSubIdents.iterator().next();
		}

		private String getSubIdentsNotUsedInScoreCondition() {
			Set<String> unusedSubIdents = new HashSet<>(Set.of(conditionContext.courseElementsKV().keys()));
			for (ConditionRow condition : conditionRows) {
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
				if (conditionRows.isEmpty()) {
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
			badgeCriteria.setConditions(conditionRows.stream().map(ConditionRow::asBadgeCondition).collect(Collectors.toList()));

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
			descriptionEl.setHelpText(translate("form.criteria.description.help"));
			descriptionEl.setPlaceholderText(translate("form.criteria.description.placeholder"));
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
					formLayout, conditionContext.conditionsKV().keys(), conditionContext.conditionsKV().values());
			newRule.enableNoneSelection(translate("form.criteria.new.rule"));
			newRule.addActionListener(FormEvent.ONCHANGE);

			buildConditionsFromContext(ureq, formLayout);
		}

		private void buildConditionsFromContext(UserRequest ureq, FormItemContainer formLayout) {
			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			List<BadgeCondition> badgeConditions = badgeCriteria.getConditions(
					Set.of(conditionContext.conditionsKV().keys()),
					Set.of(conditionContext.courseElementsKV().keys()));
			conditionRows = new ArrayList<>();
			for (int i = 0; i < badgeConditions.size(); i++) {
				BadgeCondition badgeCondition = badgeConditions.get(i);
				ConditionRow condition = new ConditionRow(ureq, Integer.toString(i), badgeCondition, formLayout, i > 0, getTranslator(), conditionContext);
				conditionRows.add(condition);
			}
			setVelocityConditions();
		}
	}
}
