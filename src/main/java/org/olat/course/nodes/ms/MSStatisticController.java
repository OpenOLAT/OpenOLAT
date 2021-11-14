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
package org.olat.course.nodes.ms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CssCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSStatisticController extends FormBasicController {
	
	static final int RUBRIC_OFFSET = 600;

	private MultipleSelectionElement slidersEnabledEl;

	private FlexiTableElement tableEl;
	private MSStatisticDataModel dataModel;

	private final CourseEnvironment courseEnv;
	private final AssessmentToolOptions asOptions;
	private final MSCourseNode courseNode;
	private final Form form;
	private final List<RubricWrapper> rubrics;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final boolean sum;
	private final float scale;
	private boolean showSliders = false;
	
	@Autowired
	private MSService msService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public MSStatisticController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			AssessmentToolOptions asOptions, MSCourseNode courseNode) {
		super(ureq, wControl, "statistic");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.courseEnv = courseEnv;
		this.asOptions = asOptions;
		this.courseNode = courseNode;
		this.form = loadForm(courseNode);
		this.rubrics = loadRubrics();
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		String scoreConfig = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
		this.sum = MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreConfig);
		String scaleConfig = config.getStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE,
				MSCourseNode.CONFIG_DEFAULT_EVAL_FORM_SCALE);
		this.scale = Float.parseFloat(scaleConfig);
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
	}

	private Form loadForm(MSCourseNode courseNode) {
		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(courseNode.getModuleConfiguration());
		if (formEntry != null) {
			return evaluationFormManager.loadForm(formEntry);
		}
		return null;
	}

	private List<RubricWrapper> loadRubrics() {
		if (form != null) {
			return wrapRubics(form);
		}
		return Collections.emptyList();
	}

	private List<RubricWrapper> wrapRubics(Form form) {
		int counter = 1;
		List<RubricWrapper> rubrics = new ArrayList<>();
		List<AbstractElement> elements = evaluationFormManager.getUncontainerizedElements(form);
		for (AbstractElement element : elements) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric)element;
				String labelCode = translate("tool.stats.table.title.rubric", new String[] { Integer.toString(counter) });
				String label = getLabel(rubric, counter);
				RubricWrapper wrapper = new RubricWrapper(rubric, counter, labelCode, label);
				wrapSliders(wrapper);
				rubrics.add(wrapper);
				counter++;
			}
		}
		return rubrics;
	}
	
	private String getLabel(Rubric rubric, int index) {
		boolean showName = rubric.getNameDisplays().contains(NameDisplay.report)
				&& StringHelper.containsNonWhitespace(rubric.getName());
		String[] args = showName
				? new String[] { "\"" + rubric.getName() + "\"" }
				: new String[] { String.valueOf(index) };
		return translate("result.details.score", args);
	}
	
	private void wrapSliders(RubricWrapper rubricWrapper) {
		int counter = 1;
		for (Slider slider : rubricWrapper.getRubric().getSliders()) {
			String labelCode = translate("tool.stats.table.title.slider", new String[] { 
					Integer.toString(rubricWrapper.getLabelIndex()), Integer.toString(counter) });
			String label = EvaluationFormFormatter.formatSliderLabel(slider);
			SliderWrapper sliderWrapper = new SliderWrapper(slider, labelCode, label);
			rubricWrapper.addSlider(sliderWrapper);
			counter++;
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		slidersEnabledEl = uifactory.addCheckboxesHorizontal("sliders.enabled", formLayout, new String[] { "enabled" },
				new String[] { translate("tool.stats.sliders.enabled") });
		slidersEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		updateTable();
	}

	private void updateTable() {
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		int rubricIndex = RUBRIC_OFFSET;
		for (RubricWrapper rubric : rubrics) {
			if (showSliders) {
				for (SliderWrapper slider : rubric.getSliders()) {
					DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("tool.stats.table.title.blank", rubricIndex++);
					columnModel.setHeaderLabel(slider.getLabelCode());
					columnModel.setAlwaysVisible(true);
					columnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
					columnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
					columnModel.setSortKey(String.valueOf(rubricIndex));
					columnModel.setSortable(true);
					columnsModel.addFlexiColumnModel(columnModel);
				}
			}
			String cssClass = showSliders? "o_ms_bold": null;
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("tool.stats.table.title.blank",
					rubricIndex++, new CssCellRenderer(cssClass));
			columnModel.setHeaderLabel(rubric.getLabelCode());
			columnModel.setAlwaysVisible(true);
			columnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			columnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
			columnModel.setSortKey(String.valueOf(rubricIndex));
			columnModel.setSortable(true);
			columnsModel.addFlexiColumnModel(columnModel);
		}
		//Total
		DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("tool.stats.table.title.total",
				rubricIndex++, new CssCellRenderer("o_ms_bold"));
		columnModel.setAlwaysVisible(true);
		columnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnModel.setSortKey(String.valueOf(rubricIndex));
		columnModel.setSortable(true);
		columnsModel.addFlexiColumnModel(columnModel);
		
		dataModel = new MSStatisticDataModel(columnsModel, translate("tool.stats.table.footer"), getLocale());
		if (tableEl != null) flc.remove(tableEl);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, getTranslator(), flc);
		tableEl.setEmptyTableMessageKey("tool.stats.empty");
		tableEl.setCustomizeColumns(false);
		tableEl.setSortSettings(options);
		tableEl.setExportEnabled(true);
		tableEl.setFooter(true);
		
		String legendPage = velocity_root + "/stats_legend.html";
		if (!rubrics.isEmpty()) {
			FormLayoutContainer legendLablesLayout = FormLayoutContainer.createCustomFormLayout("legend", getTranslator(), legendPage);
			flc.add("legend", legendLablesLayout);
			legendLablesLayout.setElementCssClass("o_ms_legend");
			legendLablesLayout.contextPut("rubrics", rubrics);
			legendLablesLayout.contextPut("showSliders", Boolean.valueOf(showSliders));
		}
		
		updateModel();
	}

	private void updateModel() {
		List<Identity> identities = loadIdentities();
		
		Map<String, Map<Rubric, RubricStatistic>> identToStatistics = msService
				.getRubricStatistics(courseEnv.getCourseGroupManager().getCourseEntry(), courseNode.getIdent(), form);
		List<MSStatisticRow> rows = new ArrayList<>();
		for (Identity identity: identities) {
			Map<Rubric, RubricStatistic> rubricStatistics = identToStatistics.get(identity.getKey().toString());
			List<Double> rubricValues = new ArrayList<>();
			for (RubricWrapper rubric : rubrics) {
				RubricStatistic rubricStatistic = rubricStatistics != null? rubricStatistics.get(rubric.getRubric()): null;
				if (showSliders) {
					for (SliderWrapper slider : rubric.getSliders()) {
						SliderStatistic sliderStatistic = rubricStatistic != null
								? rubricStatistic.getSliderStatistic(slider.getSlider())
								: null;
						Double sliderValue = getSliderValue(sliderStatistic, slider.getSlider().getWeight());
						rubricValues.add(sliderValue);
					}
				}
				Double rubricTotal = getRubricValue(rubricStatistic);
				rubricValues.add(rubricTotal);
			}
			Double total= getTotal(rubricStatistics);
			rubricValues.add(total);
			MSStatisticRow row = new MSStatisticRow(identity, userPropertyHandlers, getLocale(), rubricValues);
			rows.add(row);
		}
		List<Double> footerValues = getAverages(rows);
		dataModel.setObjects(rows, footerValues);
		tableEl.reset(true, true, true);
	}

	private List<Identity> loadIdentities() {
		List<Identity> identities;
		if(asOptions.getGroup() == null && asOptions.getIdentities() == null) {
			identities = ScoreAccountingHelper.loadUsers(courseEnv);
		} else if (asOptions.getIdentities() != null) {
			identities = asOptions.getIdentities();
		} else {
			identities = businessGroupService.getMembers(asOptions.getGroup());
		}
		return identities;
	}

	private Double getSliderValue(SliderStatistic sliderStatistic, Integer weight) {
		Double value = null;
		if (sliderStatistic != null) {
			if (sum) {
				value = sliderStatistic.getSum() != null? sliderStatistic.getSum() * weight.intValue(): null;
			} else {
				value = sliderStatistic.getAvg();
			}
		}
		return value != null? value * scale: null;
	}

	private Double getRubricValue(RubricStatistic rubricStatistic) {
		Double value = null;
		SliderStatistic sliderStatistic = rubricStatistic != null? rubricStatistic.getTotalStatistic(): null;
		if (sliderStatistic != null) {
			if (sum) {
				value = sliderStatistic.getSum();
			} else {
				value = sliderStatistic.getAvg();
			}
		}
		return value != null? value * scale: null;
	}

	private Double getTotal(Map<Rubric, RubricStatistic> rubricStatistics) {
		Float value = null;
		if (rubricStatistics != null) {
			Function<Rubric, RubricStatistic> rubricFunction = rubric -> {
				return rubricStatistics.get(rubric);
			};
			if (sum) {
				value = msService.calculateScoreBySum(form, rubricFunction);
			} else {
				value = msService.calculateScoreByAvg(form, rubricFunction);
			}
		}
		return value != null? Double.valueOf(value * scale): null;
	}

	private List<Double> getAverages(List<MSStatisticRow> rows) {
		if (rows == null || rows.isEmpty()) return null;

		int columns = rows.get(0).getRubricValues().size();
		List<Double> averages = new ArrayList<>(columns);
		for (int column = 0; column < columns; column++) {
			int count = 0;
			double sumValues = 0;
			for (MSStatisticRow row : rows) {
				Double value = row.getRubricValue(column);
				if (value != null) {
					count++;
					sumValues += value.doubleValue();
				}
			}
			Double average = count > 0 ? Double.valueOf(sumValues / count): null;
			averages.add(average);
		}
		return averages;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == slidersEnabledEl) {
			showSliders = slidersEnabledEl.isAtLeastSelected(1);
			updateTable();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public final static class RubricWrapper {
		
		private final Rubric rubric;
		private final int labelIndex;
		private final String labelCode;
		private final String label;
		private final List<SliderWrapper> sliders = new ArrayList<>();
		
		private RubricWrapper(Rubric rubric, int labelIndex, String labelCode, String label) {
			this.rubric = rubric;
			this.labelIndex = labelIndex;
			this.labelCode = labelCode;
			this.label = label;
		}

		private Rubric getRubric() {
			return rubric;
		}

		public int getLabelIndex() {
			return labelIndex;
		}

		public String getLabelCode() {
			return labelCode;
		}

		public String getLabel() {
			return label;
		}

		public List<SliderWrapper> getSliders() {
			return sliders;
		}
		
		private void addSlider(SliderWrapper slider) {
			this.sliders.add(slider);
		}
		
	}
	
	public final static class SliderWrapper {
		
		private final Slider slider;
		private final String labelCode;
		private final String label;
		
		private SliderWrapper(Slider slider, String labelCode, String label) {
			this.slider = slider;
			this.labelCode = labelCode;
			this.label = label;
		}

		public Slider getSlider() {
			return slider;
		}

		public String getIdentifier() {
			return slider.getId();
		}

		public String getLabelCode() {
			return labelCode;
		}

		public String getLabel() {
			return label;
		}
	}

}
