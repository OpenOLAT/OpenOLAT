/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.modules.selectus.ApplicationFieldType;
import org.olat.modules.selectus.ApplicationFieldType.Type;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.ProjectImpl;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.components.IntegerLenientFormatter;
import org.olat.modules.selectus.ui.components.ReflectionStaticElement;
import org.olat.modules.selectus.ui.components.ReflectionStaticElement.ReflectionType;
import org.olat.modules.selectus.ui.components.SelectusUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProjectController extends FormBasicController {
	
	private final int descriptionMaxLength;

	private TextElement titleEl;
	private TextElement descriptionEl;
	private TextElement acronymEl;
	private TextElement disciplinesEl;
	private TextElement keywordsEl;
	private FormItem financialImpact1El;
	private FormItem financialImpact2El;
	private FormItem financialImpact3El;
	private FormItem financialImpact4El;
	private FormItem financialImpact5El;
	private TextElement durationEl;
	private DateChooser startDateEl;
	
	private List<FormItem> additionalAttributesEl = new ArrayList<>();
	private final ApplicationAttributesDelegate attributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.project);
	
	private final boolean admin;
	private Position position;
	private Application application;
	private final TabConfiguration tabConfiguration;
	
	private final boolean editable;
	private final boolean segmented;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public ProjectController(UserRequest ureq, WindowControl wControl, Form rootForm, Application application,
			TabConfiguration tabConfiguration, boolean admin, boolean segmented, boolean editable) {
		super(ureq, wControl, null, Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		this.segmented = segmented;
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.admin = admin;
		this.editable = editable;
		this.application = application;
		position = application.getPosition();descriptionMaxLength = descriptionMaxLength();
		this.tabConfiguration = tabConfiguration == null
				? position.getTabConfiguration(Tab.project) : tabConfiguration;

		initForm(ureq);
	}
	
	private int descriptionMaxLength() {
		int maxLength = recruitingModule.getApplicationProjectDescriptionMaxLength();
		if(application.getProject() != null) {
			String currentDescription = application.getProject().getDescription();
			int currentLength = currentDescription == null ? 0 : currentDescription.length() + 100;
			maxLength = Math.max(maxLength, currentLength);
		}
		return maxLength;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!segmented) {
			setFormTitle("wizard.edit_project.legend", new String[] { StringHelper.escapeHtml(position.getMLTitle(getLocale())) });
		}
		
		String explanation = tabConfiguration.getHelp(getLocale());
		if(StringHelper.containsNonWhitespace(explanation)) {
			setFormTranslatedDescription(RecruitingHelper.escWithBR(explanation));
		} else if(admin) {
			setFormDescription("wizard.edit_project.explanation.admin");
		} else {
			setFormDescription("wizard.edit_project.explanation");
		}
		formLayout.setElementCssClass("o_sel_edit_project");
		
		String heading = tabConfiguration.getHeading(getLocale());
		if(StringHelper.containsNonWhitespace(heading)) {
			StaticTextElement headingEl = uifactory.addStaticTextElement("project-data", "wizard.edit_project.legend", "", formLayout);
			headingEl.setElementCssClass("o_static_heading");
			headingEl.setLabel(heading, null, false);
		}
		
		initProject(formLayout);
		initCustomAttributes(formLayout);
	}
	
	private void initProject(FormItemContainer formLayout) {
		Project project = application.getProject();
		String title = project == null ? "" : project.getTitle();
		titleEl = uifactory.addTextElement("project.title", "edit.application.project.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_edit_project_title");
		titleEl.setDomReplacementWrapperRequired(false);
		titleEl.setEnabled(editable);
		titleEl.setMandatory(!admin && !recruitingModule.isApplicationProjectTitleOptional());
		titleEl.setVisible(recruitingModule.isApplicationProjectTitleEnabled());
		if(!StringHelper.containsNonWhitespace(title) && titleEl.isVisible()) {
			titleEl.setFocus(true);
		}
		
		String acronym = project == null ? "" : project.getAcronym();
		acronymEl = uifactory.addTextElement("project.acronym", "edit.application.project.acronym", 255, acronym, formLayout);
		acronymEl.setElementCssClass("o_sel_edit_project_acronym");
		acronymEl.setDomReplacementWrapperRequired(false);
		acronymEl.setMandatory(!admin && !recruitingModule.isApplicationProjectAcronymOptional());
		acronymEl.setVisible(recruitingModule.isApplicationProjectAcronymEnabled());
		acronymEl.setEnabled(editable);
		
		String keywords = project == null ? "" : project.getKeywords();
		keywordsEl = uifactory.addTextElement("project.keywords", "edit.application.project.keywords", 255, keywords, formLayout);
		keywordsEl.setPlaceholderKey("edit.application.project.keywords.placeholder", null);
		keywordsEl.setElementCssClass("o_sel_edit_project_keywords");
		keywordsEl.setDomReplacementWrapperRequired(false);
		keywordsEl.setMandatory(!admin && !recruitingModule.isApplicationProjectKeywordsOptional());
		keywordsEl.setVisible(recruitingModule.isApplicationProjectKeywordsEnabled());
		keywordsEl.setEnabled(editable);
		
		String disciplines = project == null ? "" : project.getDisciplines();
		disciplinesEl = uifactory.addTextElement("project.disciplines", "edit.application.project.disciplines", 255, disciplines, formLayout);
		disciplinesEl.setElementCssClass("o_sel_edit_project_disciplines");
		disciplinesEl.setDomReplacementWrapperRequired(false);
		disciplinesEl.setMandatory(!admin && !recruitingModule.isApplicationProjectDisciplinesOptional());
		disciplinesEl.setVisible(recruitingModule.isApplicationProjectDisciplinesEnabled());
		disciplinesEl.setEnabled(editable);
		
		Date startDate = project == null ? null : project.getStartDate();
		startDateEl = uifactory.addDateChooser("project.startDate", "edit.application.project.start.date", startDate, formLayout);
		startDateEl.setElementCssClass("o_sel_edit_project_start");
		startDateEl.setDomReplacementWrapperRequired(false);
		startDateEl.setMandatory(!admin && !recruitingModule.isApplicationProjectStartDateOptional());
		startDateEl.setVisible(recruitingModule.isApplicationProjectStartDateEnabled());
		startDateEl.setEnabled(editable);
		
		String duration = project == null ? "" : project.getDuration();
		durationEl = uifactory.addTextElement("project.duration", "edit.application.project.duration", 255, duration, formLayout);
		durationEl.setPlaceholderKey("edit.application.project.duration.placeholder", null);
		durationEl.setElementCssClass("o_sel_edit_project_duration");
		durationEl.setDomReplacementWrapperRequired(false);
		durationEl.setMandatory(!admin && !recruitingModule.isApplicationProjectDurationOptional());
		durationEl.setVisible(recruitingModule.isApplicationProjectDurationEnabled());
		durationEl.setEnabled(editable);
		
		String description = project == null ? "" : project.getDescription();
		descriptionEl = uifactory.addTextAreaElement("project.description", "edit.application.project.description", descriptionMaxLength, 8, 72, true, false, false, description, formLayout);
		descriptionEl.setElementCssClass("o_sel_edit_project_description");
		descriptionEl.setExampleKey("edit.application.project.description.example", new String[] { Integer.toString(descriptionMaxLength) });
		descriptionEl.setMandatory(!admin && !recruitingModule.isApplicationProjectDescriptionOptional());
		descriptionEl.setDomReplacementWrapperRequired(false);
		descriptionEl.setVisible(recruitingModule.isApplicationProjectDescriptionEnabled());
		descriptionEl.setEnabled(editable);
		
		if(RecruitingHelper.isVisible(recruitingModule.getApplicationProjectFinancialImpact1Type(),
				recruitingModule.getApplicationProjectFinancialImpact2Type(),
				recruitingModule.getApplicationProjectFinancialImpact3Type(),
				recruitingModule.getApplicationProjectFinancialImpact4Type(),
				recruitingModule.getApplicationProjectFinancialImpact5Type())) {
			uifactory.addStaticTextElement("edit.application.project.financialimpact", "", formLayout);
		}
		
		String financialImpact1 = project == null ? "" : project.getFinancialImpact1();
		financialImpact1El = initFinancialImpactFormItem(1, financialImpact1, recruitingModule.getApplicationProjectFinancialImpact1Type(), formLayout);

		String financialImpact2 = project == null ? "" : project.getFinancialImpact2();
		financialImpact2El = initFinancialImpactFormItem(2, financialImpact2, recruitingModule.getApplicationProjectFinancialImpact2Type(), formLayout);

		String financialImpact3 = project == null ? "" : project.getFinancialImpact3();
		financialImpact3El = initFinancialImpactFormItem(3, financialImpact3, recruitingModule.getApplicationProjectFinancialImpact3Type(), formLayout);

		String financialImpact4 = project == null ? "" : project.getFinancialImpact4();
		financialImpact4El = initFinancialImpactFormItem(4, financialImpact4, recruitingModule.getApplicationProjectFinancialImpact4Type(), formLayout);

		String financialImpact5 = project == null ? "" : project.getFinancialImpact5();
		financialImpact5El = initFinancialImpactFormItem(5, financialImpact5, recruitingModule.getApplicationProjectFinancialImpact5Type(), formLayout);
		
		initSum(new FormItem[] {financialImpact1El, financialImpact2El, financialImpact3El, financialImpact4El, financialImpact5El});
	}
	
	private void initCustomAttributes(FormItemContainer formLayout) {
		if(attributesDelegate.hasSomeAttributes(application)) {
			SpacerElement spacer = uifactory.addSpacerElement("project-add-attributes-spacer", formLayout, false);
			spacer.setElementCssClass("o_sel_spacer_project_add_attributes");
			attributesDelegate.initAdditionalAttributes(formLayout, additionalAttributesEl, application, admin, editable, getLocale());
		}
	}
			
	private FormItem initFinancialImpactFormItem(int num, String val, ApplicationFieldType fieldType, FormItemContainer formLayout) {
		FormItem financialImpactEl;
		String elementId = "project.financialimpact" + num;
		String i18nLabel = "edit.application.project.financialimpact." + num;
		String i18nPlaceholder = "edit.application.project.financialimpact.placeholder." + num;;
		if(fieldType.getType() == Type.integer) {
			IntegerElement el = uifactory.addIntegerElement(elementId, i18nLabel, 0, formLayout);
			el.setLenientFormatter(new IntegerLenientFormatter());
			el.setElementCssClass("form-inline");
			el.setTextAddOn("edit.application.project.financialimpact.unit." + num);
			el.setPlaceholderKey(i18nPlaceholder, null);
			el.setDisplaySize(12);
			el.setMaxLength(12);
			el.setValue(val);
			financialImpactEl = el;
		} else if(fieldType.getType() == Type.sum) {
			ReflectionStaticElement el = SelectusUIFactory.addReflectionStaticText(elementId, i18nLabel, null, formLayout);
			el.setTextAddOn("edit.application.project.financialimpact.unit." + num);
			financialImpactEl = el;
		} else {
			TextElement el = uifactory.addTextElement(elementId, i18nLabel, 255, val, formLayout);
			el.setPlaceholderKey(i18nPlaceholder, null);
			el.setDomReplacementWrapperRequired(false);
			financialImpactEl = el;
		}
		financialImpactEl.setElementCssClass("o_sel_edit_project_financial_" + num);
		financialImpactEl.setUserObject(fieldType);
		financialImpactEl.setMandatory(!admin && !fieldType.isOptional());
		financialImpactEl.setVisible(fieldType.isEnabled());
		financialImpactEl.setEnabled(editable);
		return financialImpactEl;
	}
	
	private void initSum(FormItem[] items) {
		for(FormItem item:items) {
			if(item instanceof ReflectionStaticElement) {
				ApplicationFieldType fieldType = (ApplicationFieldType)item.getUserObject();
				if(fieldType.getType() == ApplicationFieldType.Type.sum) {
					List<String> dependencies = fieldType.getDependencies();
					List<TextElement> elementsToSum = new ArrayList<>();
					for(String dependency:dependencies) {
						int index = Integer.parseInt(dependency) - 1;
						if(index >= 0 && index < items.length && items[index] instanceof TextElement) {
							elementsToSum.add((TextElement)items[index]);
						}
					}
					((ReflectionStaticElement)item).setReflectionType(ReflectionType.sumInteger);
					((ReflectionStaticElement)item).setTextElements(elementsToSum);
					((ReflectionStaticElement)item).setFormatTextElements(true);
				}
			}
		}
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= RecruitingHelper.validateTextElement(titleEl, 255, titleEl.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(acronymEl, 255, acronymEl.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(keywordsEl, 255, keywordsEl.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(disciplinesEl, 255, disciplinesEl.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(startDateEl, 255, startDateEl.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(durationEl, 255, durationEl.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateFieldElement(financialImpact1El, 255, financialImpact1El.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateFieldElement(financialImpact2El, 255, financialImpact2El.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateFieldElement(financialImpact3El, 255, financialImpact3El.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateFieldElement(financialImpact4El, 255, financialImpact4El.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateFieldElement(financialImpact5El, 255, financialImpact5El.isMandatory(), new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(descriptionEl, descriptionMaxLength, descriptionEl.isMandatory(), new OWASPAntiSamyXSSFilter());

		allOk &= attributesDelegate.validateFormLogic(additionalAttributesEl, admin);
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		attributesDelegate.formInnerEvent(source);
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public void commitChanges(Application app) {
		Project project = app.getProject();
		if(project == null) {
			project = new ProjectImpl();
		}
		
		project.setTitle(titleEl.getValue());
		project.setAcronym(acronymEl.getValue());
		project.setKeywords(keywordsEl.getValue());
		project.setDisciplines(disciplinesEl.getValue());
		project.setStartDate(startDateEl.getDate());
		project.setDuration(durationEl.getValue());
		project.setFinancialImpact1(getValue(financialImpact1El));
		project.setFinancialImpact2(getValue(financialImpact2El));
		project.setFinancialImpact3(getValue(financialImpact3El));
		project.setFinancialImpact4(getValue(financialImpact4El));
		project.setFinancialImpact5(getValue(financialImpact5El));
		project.setDescription(descriptionEl.getValue());
		
		attributesDelegate.commitChanges(additionalAttributesEl, app);
	}
	
	private String getValue(FormItem element) {
		if(element instanceof TextElement) {
			return ((TextElement)element).getValue();
		} else if(element instanceof ReflectionStaticElement) {
			return ((ReflectionStaticElement)element).getReflectedValue();
		}
		return null;
	}
}
