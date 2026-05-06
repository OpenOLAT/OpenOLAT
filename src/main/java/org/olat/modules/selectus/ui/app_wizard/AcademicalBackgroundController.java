/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import static org.olat.modules.selectus.ui.RecruitingHelper.formatAcademicalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.AcademicalBackground;
import org.olat.modules.selectus.model.AcademicalBackgroundImpl;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.HighestDegreeType;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.AcademicalDateFormat;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AcademicalBackgroundController extends FormBasicController {

	private TextElement hFactorEl;
	private TextElement citationsEl;
	private TextElement impactFactorEl;
	private TextElement numOfLastAuthorshipsEl;
	private TextElement numOfFirstAuthorshipsEl;
	private TextElement numOfOriginalPublicationsEl;
	
	private SingleSelection highestDegreeTypeEl;
	private TextElement highestDegreeDateEl;
	private TextElement highestDegreeDescriptionEl;
	private TextElement highestDegreeInstitutionEl;
	private TextElement workedInAcademiaSinceEl;
	private TextElement workedOutAcademiaSinceEl;
	private TextElement workedOutAcademiaCareSinceEl;
	private TextElement careerDescriptionEl;
	
	private TextElement dissertationTitleEl;
	private TextElement dissertationDateEl;
	private TextElement dissertationInstitutionEl;
	private TextElement dissertationKeyword1El;
	private TextElement dissertationKeyword2El;
	private TextElement dissertationKeyword3El;
	
	private TextElement habilitationTitleEl;
	private TextElement habilitationDateEl;
	private TextElement habilitationInstitutionEl;
	private TextElement orcidEl;
	
	private List<FormItem> additionalAttributesEl = new ArrayList<>();
	private final ApplicationAttributesDelegate attributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.academicalBackground);
	
	private final boolean admin;
	private Position position;
	private Application application;
	private final TabConfiguration tabConfiguration;
	private final List<String> excludedAttributesList;
	
	private final boolean editable;
	private final boolean segmented;
	private final int maxCareerDescriptionLength;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public AcademicalBackgroundController(UserRequest ureq, WindowControl wControl, Form rootForm, Application application,
			TabConfiguration tabConfiguration, List<String> excludedAttributesList, boolean admin, boolean segmented, boolean editable) {
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
		this.position = application.getPosition();
		this.tabConfiguration = tabConfiguration == null
				? position.getTabConfiguration(Tab.academicalBackground) : tabConfiguration;
		this.excludedAttributesList = excludedAttributesList;
	
		// reduce the career description max length from 32'000 to 500
		maxCareerDescriptionLength = (application != null && application.getAcademicalBackground() != null
				&& application.getAcademicalBackground().getCareerDescription() != null
				&& application.getAcademicalBackground().getCareerDescription().length() > 500) ? application.getAcademicalBackground().getCareerDescription().length() : 500;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!segmented) {
			setFormTitle("wizard.edit_background.legend", new String[] { StringHelper.escapeHtml(position.getMLTitle(getLocale())) });
		}
		String explanation = tabConfiguration.getHelp(getLocale());
		if(StringHelper.containsNonWhitespace(explanation)) {
			setFormTranslatedDescription(RecruitingHelper.escWithBR(explanation));
		} else if(admin) {
			setFormDescription("wizard.edit_background.explanation.admin");
		} else {
			setFormDescription("wizard.edit_background.explanation");
		}
		formLayout.setElementCssClass("o_sel_academical_background");
		
		String heading = tabConfiguration.getHeading(getLocale());
		if(StringHelper.containsNonWhitespace(heading)) {
			StaticTextElement headingEl = uifactory.addStaticTextElement("academic-data", "academical_background", "", formLayout);
			headingEl.setElementCssClass("o_static_heading");
			headingEl.setLabel(heading, null, false);
		}
		
		initAcademicalBackground(formLayout);
		initCustomAttributes(formLayout);
	}
	
	protected void initAcademicalBackground(FormItemContainer formLayout) {	
		AcademicalBackground background = application.getAcademicalBackground();

		//highest degree
		boolean highestDegreeEnabled = recruitingModule.isApplicationAcademicalBackgroundHighestDegreeEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE);
		
		StaticTextElement highestDegreeLabel = uifactory.addStaticTextElement("highestdegree.label", "edit.application.highestdegree", "", formLayout);
		highestDegreeLabel.setVisible(highestDegreeEnabled);
		highestDegreeLabel.setElementCssClass("o_static_heading");

		String degreePage = velocity_root + "/degree_typedate.html";
		FormLayoutContainer degreeTypeDateLayout = FormLayoutContainer.createCustomFormLayout("edit.application.highestdegree", getTranslator(), degreePage);
		degreeTypeDateLayout.setLabel("edit.application.highestdegreetypeyear", null);
		degreeTypeDateLayout.setVisible(highestDegreeEnabled
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR));
		degreeTypeDateLayout.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundHighestDegreeOptional());
		degreeTypeDateLayout.setRootForm(mainForm);
		formLayout.add(degreeTypeDateLayout);
		
		SelectionValues typesPK = new SelectionValues();
		typesPK.add(SelectionValues.entry("", translate("please.choose")));
		HighestDegreeType[] types = recruitingModule.getHighestDegreeTypes();
		for(HighestDegreeType type:types) {
			typesPK.add(SelectionValues.entry(type.name(), translate(type.i18nKey())));
			
		}
		highestDegreeTypeEl = uifactory.addDropdownSingleselect("type", "edit.application.type", degreeTypeDateLayout, typesPK.keys(), typesPK.values(), null);
		highestDegreeTypeEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundHighestDegreeOptional());
		highestDegreeTypeEl.setDomReplacementWrapperRequired(false);
		highestDegreeTypeEl.addActionListener(FormEvent.ONCHANGE);
		highestDegreeTypeEl.setLabel(null, null);
		highestDegreeTypeEl.setEnabled(editable);
		highestDegreeTypeEl.setVisible(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE));
		String degreeType = background == null ? null : background.getHighestDegreeType();
		if(StringHelper.containsNonWhitespace(degreeType) && typesPK.containsKey(degreeType)) {
			highestDegreeTypeEl.select(degreeType, true);
		} else {
			highestDegreeTypeEl.select(typesPK.keys()[0], true);
		}

		Date highestDegreeDate = background == null ? null : background.getHighestDegreeDate();
		String year = "";
		if(highestDegreeDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(highestDegreeDate);
			year = Integer.toString(cal.get(Calendar.YEAR));
		}
		highestDegreeDateEl = uifactory.addTextElement("degdate", "edit.application.highestdegreeyear", 6, year, degreeTypeDateLayout);
		highestDegreeDateEl.setDomReplacementWrapperRequired(false);
		highestDegreeDateEl.setLabel(null, null);
		highestDegreeDateEl.setMaxLength(6);
		highestDegreeDateEl.setDisplaySize(4);
		highestDegreeDateEl.setEnabled(editable);
		highestDegreeDateEl.setVisible(!excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR));
		
		String highestDegreeDescription = background == null ? null : background.getHighestDegreeDescription();
		highestDegreeDescriptionEl = uifactory.addTextElement("hdegreedescription", "edit.application.highestdegreedescription", 255, highestDegreeDescription, formLayout);
		highestDegreeDescriptionEl.setMandatory(!admin);
		highestDegreeDescriptionEl.setEnabled(editable);
		highestDegreeDescriptionEl.setVisible(highestDegreeEnabled && HighestDegreeType.other.name().equals(highestDegreeTypeEl.getSelectedKey()));

		String highestDegreeInstitution = background == null ? null : background.getHighestDegreeInstitution();
		highestDegreeInstitutionEl = uifactory.addTextElement("institution", "edit.application.highestdegreeinstitution", 255, highestDegreeInstitution, formLayout);
		highestDegreeInstitutionEl.setElementCssClass("o_sel_edit_highest_degree_institution");
		highestDegreeInstitutionEl.setVisible(highestDegreeEnabled && !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION));
		highestDegreeInstitutionEl.setMandatory(!admin);
		highestDegreeInstitutionEl.setEnabled(editable);
		
		//highest degree worked since
		boolean workedSinceEnabled = recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled();
		
		String workedSinceInfos = translate("edit.application.highestdegreeWorkedSince.infos");
		StaticTextElement workedSinceLabel = uifactory.addStaticTextElement("workedSince.label", "edit.application.highestdegreeWorkedSince", workedSinceInfos, formLayout);
		workedSinceLabel.setElementCssClass("o_static_heading");
		workedSinceLabel.setVisible(workedSinceEnabled);

		String workedInAcademiaSince = background == null ? null : background.getWorkedInAcademiaSince();
		workedInAcademiaSinceEl = uifactory.addTextElement("workedInSince", "edit.application.workedInAcademiaSince.label", 1024, workedInAcademiaSince, formLayout);
		workedInAcademiaSinceEl.setPlaceholderKey("edit.application.workedInAcademiaSince.placeholder", null);
		workedInAcademiaSinceEl.setElementCssClass("o_sel_edit_worked_in_academia_since");
		workedInAcademiaSinceEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceEnabled());
		workedInAcademiaSinceEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceOptional());
		workedInAcademiaSinceEl.setEnabled(editable);
		
		String workedOutAcademiaSince = background == null ? null : background.getWorkedOutAcademiaSince();
		workedOutAcademiaSinceEl = uifactory.addTextElement("workedOutSince", "edit.application.workedOutAcademiaSince.label", 1024, workedOutAcademiaSince, formLayout);
		workedOutAcademiaSinceEl.setPlaceholderKey("edit.application.workedOutAcademiaSince.placeholder", null);
		workedOutAcademiaSinceEl.setElementCssClass("o_sel_edit_worked_out_academia_since");
		workedOutAcademiaSinceEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceEnabled());
		workedOutAcademiaSinceEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceOptional());
		workedOutAcademiaSinceEl.setEnabled(editable);
		
		String workedOutAcademiaCareSince = background == null ? null : background.getWorkedOutAcademiaCareSince();
		workedOutAcademiaCareSinceEl = uifactory.addTextElement("workedOutSinceCare", "edit.application.workedOutAcademiaCareSince.label", 1024, workedOutAcademiaCareSince, formLayout);
		workedOutAcademiaCareSinceEl.setPlaceholderKey("edit.application.workedOutAcademiaCareSince.placeholder", null);
		workedOutAcademiaCareSinceEl.setElementCssClass("o_sel_edit_worked_out_academia_care_since");
		workedOutAcademiaCareSinceEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceEnabled());
		workedOutAcademiaCareSinceEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceOptional());
		workedOutAcademiaCareSinceEl.setEnabled(editable);
		
		String careerDescription = background == null ? null : background.getCareerDescription();
		careerDescriptionEl = uifactory.addTextAreaElement("careerDescription", "edit.application.careerDescription.label",
				32000, 4, 60, false, false, false, careerDescription, formLayout);
		careerDescriptionEl.setPlaceholderKey("edit.application.careerDescription.placeholder", null);
		careerDescriptionEl.setExampleKey("edit.application.careerDescription.example", null);
		careerDescriptionEl.setElementCssClass("o_sel_edit_career_description");
		careerDescriptionEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionEnabled());
		careerDescriptionEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionOptional());
		careerDescriptionEl.setEnabled(editable);
		
		//dissertation
		boolean dissertationEnabled = recruitingModule.isApplicationAcademicalBackgroundDissertationEnabled();
		
		String explainDissertation = translate("edit.application.dissertation.explain"); 
		StaticTextElement dissertationLabel = uifactory.addStaticTextElement("dissertation.label", "edit.application.dissertation", explainDissertation, formLayout);
		dissertationLabel.setVisible(dissertationEnabled
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION));
		dissertationLabel.setElementCssClass("o_static_heading");
		
		String dissertationTitle = background == null ? null : background.getDissertationTitle();
		dissertationTitleEl = uifactory.addTextElement("dis_title", "edit.application.dissertationtitle", 255, dissertationTitle, formLayout);
		dissertationTitleEl.setElementCssClass("o_sel_edit_dissertation_title");
		dissertationTitleEl.setVisible(dissertationEnabled && recruitingModule.isApplicationAcademicalBackgroundDissertationTitleEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION));
		dissertationTitleEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationTitleOptional());
		dissertationTitleEl.setEnabled(editable);
		
		Date disserationDate = background == null ? null : background.getDissertationDate();
		String dissertationYear = RecruitingHelper.formatAcademicalDate(disserationDate);
		dissertationDateEl = uifactory.addTextElement("dis_date", "edit.application.dissertationyear", 7, dissertationYear, formLayout);
		dissertationDateEl.setElementCssClass("o_sel_edit_dissertation_date");
		dissertationDateEl.setVisible(dissertationEnabled && recruitingModule.isApplicationAcademicalBackgroundDissertationDateEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION));
		dissertationDateEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationDateOptional());
		dissertationDateEl.setMaxLength(7);
		dissertationDateEl.setDisplaySize(7);
		dissertationDateEl.setEnabled(editable);
		String format = getAcademicalDatePlaceholder(recruitingModule.getApplicationAcademicalBackgroundDissertationDateFormat());
		dissertationDateEl.setPlaceholderText(translate("edit.application.dissertationyear.placeholder", new String[]{ format }));
		
		String dissertationInstitution = background == null ? null : background.getDissertationInstitution();
		dissertationInstitutionEl = uifactory.addTextElement("dis_institution", "edit.application.dissertationinstitution", 255, dissertationInstitution, formLayout);
		dissertationInstitutionEl.setElementCssClass("o_sel_edit_dissertation_institution");
		dissertationInstitutionEl.setVisible(dissertationEnabled && recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionEnabled()
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_DISSERTATION));
		dissertationInstitutionEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionOptional());
		dissertationInstitutionEl.setEnabled(editable);
		
		String dissertationKeyword1 = background == null ? null : background.getDissertationKeyword1();
		dissertationKeyword1El = uifactory.addTextElement("dis_keyword_1", "edit.application.dissertationkeyword1", 255, dissertationKeyword1, formLayout);
		dissertationKeyword1El.setElementCssClass("o_sel_edit_dissertation_keyword_1");
		dissertationKeyword1El.setVisible(dissertationEnabled && recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Enabled());
		dissertationKeyword1El.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Optional());
		dissertationKeyword1El.setEnabled(editable);
		
		String dissertationKeyword2 = background == null ? null : background.getDissertationKeyword2();
		dissertationKeyword2El = uifactory.addTextElement("dis_keyword_2", "edit.application.dissertationkeyword2", 255, dissertationKeyword2, formLayout);
		dissertationKeyword2El.setElementCssClass("o_sel_edit_dissertation_keyword_2");
		dissertationKeyword2El.setVisible(dissertationEnabled && recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Enabled());
		dissertationKeyword2El.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Optional());
		dissertationKeyword2El.setEnabled(editable);
		
		String dissertationKeyword3 = background == null ? null : background.getDissertationKeyword1();
		dissertationKeyword3El = uifactory.addTextElement("dis_keyword_3", "edit.application.dissertationkeyword3", 255, dissertationKeyword3, formLayout);
		dissertationKeyword3El.setElementCssClass("o_sel_edit_dissertation_keyword_3");
		dissertationKeyword3El.setVisible(dissertationEnabled && recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Enabled());
		dissertationKeyword3El.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Optional());
		dissertationKeyword3El.setEnabled(editable);
		
		//habilitation
		boolean habilitationEnabled = recruitingModule.isApplicationAcademicalBackgroundHabilitationEnabled();
		
		StaticTextElement habilitationEl = uifactory.addStaticTextElement("habilitation.label", "edit.application.habilitation", "", formLayout);
		habilitationEl.setVisible(habilitationEnabled);
		habilitationEl.setElementCssClass("o_static_heading");

		String habilitationTitle = background == null ?  null : background.getHabilitationTitle();
		habilitationTitleEl = uifactory.addTextElement("hab_title", "edit.application.habilitationtitle", 255, habilitationTitle, formLayout);
		habilitationTitleEl.setElementCssClass("o_sel_edit_habilitation_title");
		habilitationTitleEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundHabilitationTitleEnabled());
		habilitationTitleEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundHabilitationTitleOptional());
		habilitationTitleEl.setEnabled(editable);
		
		String habilitationYear = "";
		if(background != null && background.getHabilitationDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(background.getHabilitationDate());
			habilitationYear = Integer.toString(cal.get(Calendar.YEAR));
		}
		habilitationDateEl = uifactory.addTextElement("hab_date", "edit.application.habilitationyear", 6, habilitationYear, formLayout);
		habilitationDateEl.setElementCssClass("o_sel_edit_habilitation_date");
		habilitationDateEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundHabilitationDateEnabled());
		habilitationDateEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundHabilitationDateOptional());
		habilitationDateEl.setMaxLength(6);
		habilitationDateEl.setDisplaySize(4);
		habilitationDateEl.setEnabled(editable);
		
		String habilitationInstitution = background == null ? null : background.getHabilitationInstitution();
		habilitationInstitutionEl = uifactory.addTextElement("hab_institution", "edit.application.habilitationinstitution", 255, habilitationInstitution, formLayout);
		habilitationInstitutionEl.setElementCssClass("o_sel_edit_habilitation_institution");
		habilitationInstitutionEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundHabilitationInstitutionEnabled());
		habilitationInstitutionEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundHabilitationInstitutionOptional());
		habilitationInstitutionEl.setEnabled(editable);
		
		if(recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled()) {
			uifactory.addSpacerElement("orcid-spacer", formLayout, false);
		}
		
		String orcid = background == null ? null : background.getOrcid();
		orcidEl = uifactory.addTextElement("hab_orcid", "edit.application.orcid", 255, orcid, formLayout);
		orcidEl.setDomReplacementWrapperRequired(false);
		orcidEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundOrcidEnabled());
		orcidEl.setMandatory(!admin && !recruitingModule.isApplicationAcademicalBackgroundOrcidOptional());
		orcidEl.setEnabled(editable);
		
		if(recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled()
				|| recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled()) {

			uifactory.addSpacerElement("first-spacer", formLayout, false);
			
			StaticTextElement infosEl = uifactory.addStaticTextElement("publications-infos", "publications.infos", "", formLayout);
			infosEl.setElementCssClass("o_static_heading");
		}
		
		String publications = background == null || background.getNumberOfOriginalPublications() == null ?
				null : background.getNumberOfOriginalPublications().toString();
		numOfOriginalPublicationsEl = uifactory.addTextElement("nop", "edit.application.numberOfOriginalPublications", 255, publications, formLayout);
		numOfOriginalPublicationsEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsEnabled());
		numOfOriginalPublicationsEl.setMandatory(!recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsOptional());
		numOfOriginalPublicationsEl.setElementCssClass("o_sel_edit_original_publications");
		numOfOriginalPublicationsEl.setDisplaySize(4);
		numOfOriginalPublicationsEl.setEnabled(editable);

		String firstAuthorships = background == null || background.getNumberOfFirstAuthorships() == null ?
				null : background.getNumberOfFirstAuthorships().toString();
		numOfFirstAuthorshipsEl = uifactory.addTextElement("nofa", "edit.application.numberOfFirstAuthorships", 255, firstAuthorships, formLayout);
		numOfFirstAuthorshipsEl.setExampleKey("edit.application.numberOfFirstAuthorships.explain", null);
		numOfFirstAuthorshipsEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsEnabled());
		numOfFirstAuthorshipsEl.setMandatory(!recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsOptional());
		numOfFirstAuthorshipsEl.setElementCssClass("o_sel_edit_first_authorships");
		numOfFirstAuthorshipsEl.setDisplaySize(4);
		numOfFirstAuthorshipsEl.setEnabled(editable);
		
		String lastAuthorships = background == null || background.getNumberOfLastAuthorships() == null ?
				null : background.getNumberOfLastAuthorships().toString();
		numOfLastAuthorshipsEl = uifactory.addTextElement("nola", "edit.application.numberOfLastAuthorships", 255, lastAuthorships, formLayout);
		numOfLastAuthorshipsEl.setExampleKey("edit.application.numberOfLastAuthorships.explain", null);
		numOfLastAuthorshipsEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsEnabled());
		numOfLastAuthorshipsEl.setMandatory(!recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsOptional());
		numOfLastAuthorshipsEl.setElementCssClass("o_sel_edit_last_authorships");
		numOfLastAuthorshipsEl.setDisplaySize(4);
		numOfLastAuthorshipsEl.setEnabled(editable);
		
		String citations = background == null || background.getCitations() == null ? null : background.getCitations().toString();
		citationsEl = uifactory.addTextElement("citations", "edit.application.citations", 255, citations, formLayout);
		citationsEl.setExampleKey("edit.application.citations.explanation", null);
		citationsEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundCitationsEnabled());
		citationsEl.setMandatory(!recruitingModule.isApplicationAcademicalBackgroundCitationsOptional());
		citationsEl.setElementCssClass("o_sel_edit_citations");
		citationsEl.setDisplaySize(4);
		citationsEl.setEnabled(editable);
		
		String impactFactor = background == null ? null : RecruitingHelper.formatFactor(background.getImpactFactor());
		impactFactorEl = uifactory.addTextElement("ifactor", "edit.application.impactFactor", 255, impactFactor, formLayout);
		impactFactorEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundImpactFactorEnabled());
		impactFactorEl.setMandatory(!recruitingModule.isApplicationAcademicalBackgroundImpactFactorOptional());
		impactFactorEl.setElementCssClass("o_sel_edit_impact_factor");
		impactFactorEl.setDisplaySize(4);
		impactFactorEl.setEnabled(editable);
		
		String hFactor = background == null ? null : RecruitingHelper.formatFactor(background.getHFactor());
		hFactorEl = uifactory.addTextElement("hfactor", "edit.application.hFactor", 255, hFactor, formLayout);
		hFactorEl.setExampleKey("edit.application.hFactor.explanation", null);
		hFactorEl.setVisible(recruitingModule.isApplicationAcademicalBackgroundHFactorEnabled());
		hFactorEl.setMandatory(!recruitingModule.isApplicationAcademicalBackgroundHFactorOptional());
		hFactorEl.setElementCssClass("o_sel_edit_h_factor");
		hFactorEl.setDisplaySize(4);
		hFactorEl.setEnabled(editable);
	}
	
	private void initCustomAttributes(FormItemContainer formLayout) {
		if(attributesDelegate.hasSomeAttributes(application)) {
			SpacerElement spacer = uifactory.addSpacerElement("add-attributes-spacer", formLayout, false);
			spacer.setElementCssClass("o_sel_spacer_academic_add_attributes");
			attributesDelegate.initAdditionalAttributes(formLayout, additionalAttributesEl, application, admin, editable, getLocale());
		}
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}

	@Override
	public FormItem getInitialFormItem() {
		return flc;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateIntegerElement(numOfOriginalPublicationsEl,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundNumberOfOriginalPublicationsOptional());
		allOk &= validateIntegerElement(numOfFirstAuthorshipsEl,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundNumberOfFirstAuthorshipsOptional());
		allOk &= validateIntegerElement(numOfLastAuthorshipsEl,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundNumberOfLastAuthorshipsOptional());
		allOk &= validateIntegerElement(citationsEl,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundCitationsOptional());
		
		allOk &= validateDoubleElement(impactFactorEl,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundImpactFactorOptional());
		allOk &= validateDoubleElement(hFactorEl,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundHFactorOptional());

		//highest degree
		allOk &= validateTextElement(highestDegreeDescriptionEl, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundHighestDegreeOptional()
				&& HighestDegreeType.other.name().equals(highestDegreeTypeEl.getSelectedKey()),
				new OWASPAntiSamyXSSFilter());

		allOk &= validateTextElement(highestDegreeInstitutionEl, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundHighestDegreeOptional()
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_INSTITUTION),
				new OWASPAntiSamyXSSFilter());
		highestDegreeTypeEl.clearError();
		if(!admin && !recruitingModule.isApplicationAcademicalBackgroundHighestDegreeOptional()
				&& (!highestDegreeTypeEl.isOneSelected() || !StringHelper.containsNonWhitespace(highestDegreeTypeEl.getSelectedKey()))
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
				&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_TYPE)) {
			highestDegreeTypeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		allOk &= validateYearElement(highestDegreeDateEl, AcademicalDateFormat.yearsOnly(),
				!admin && !recruitingModule.isApplicationAcademicalBackgroundHighestDegreeOptional()
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE)
					&& !excludedAttributesList.contains(RecruitingModule.APP_ACADEMIC_HIGHEST_DEGREE_YEAR));
		allOk &= validateTextElement(workedInAcademiaSinceEl, 1024,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundWorkedInAcademiaSinceOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(workedOutAcademiaSinceEl, 1024,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaSinceOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(workedOutAcademiaCareSinceEl, 1024,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundWorkedOutAcademiaCareSinceOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(careerDescriptionEl, maxCareerDescriptionLength,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundCareerDescriptionOptional(),
				new OWASPAntiSamyXSSFilter());

		//dissertation
		allOk &= validateTextElement(dissertationTitleEl, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationTitleOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateYearElement(dissertationDateEl, recruitingModule.getApplicationAcademicalBackgroundDissertationDateFormat(),
				!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationDateOptional());
		allOk &= validateTextElement(dissertationInstitutionEl, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationInstitutionOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(dissertationKeyword1El, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword1Optional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(dissertationKeyword2El, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword2Optional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateTextElement(dissertationKeyword3El, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundDissertationKeyword3Optional(),
				new OWASPAntiSamyXSSFilter());
		
		//habilitation
		allOk &= validateTextElement(habilitationTitleEl, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundHabilitationOptional(),
				new OWASPAntiSamyXSSFilter());
		allOk &= validateYearElement(habilitationDateEl, AcademicalDateFormat.yearsOnly(),
				!admin && !recruitingModule.isApplicationAcademicalBackgroundHabilitationOptional());
		allOk &= validateTextElement(habilitationInstitutionEl, 255,
				!admin && !recruitingModule.isApplicationAcademicalBackgroundHabilitationOptional(),
				new OWASPAntiSamyXSSFilter());
		
		allOk &= attributesDelegate.validateFormLogic(additionalAttributesEl, admin);
		
		return allOk;
	}
	

	private boolean validateYearElement(TextElement textEl, AcademicalDateFormat[] formats, boolean mandatory) {
		if(!textEl.isVisible()) return true;
		
		boolean ok = true;
		textEl.clearError();
		if(mandatory && formatAcademicalDate(textEl.getValue(), formats) == null) {
			textEl.setErrorKey("error.missing.year");
			textEl.setExampleKey("year.format", null);
			ok = false;
		} else if(StringHelper.containsNonWhitespace(textEl.getValue())) {
			try {
				Calendar cal = Calendar.getInstance();
				int currentYear = cal.get(Calendar.YEAR) + 5; 
				Date date = formatAcademicalDate(textEl.getValue(), formats);
				if(date == null) {
					textEl.setErrorKey("academical.date.error", new String[] { getAcademicalDatePlaceholder(formats) });
					ok &= false;
				} else {
					if(textEl.getValue().indexOf('.') < 0) {
						Integer.parseInt(textEl.getValue());
						// not ok -> NumberFormatException
					}
					
					cal.setTime(date);
					int year = cal.get(Calendar.YEAR);
					if(year < 1900 || year > currentYear) {
						ok &= false;
						textEl.setErrorKey("academical.date.error", new String[] { getAcademicalDatePlaceholder(formats) });
					}
				}
			} catch (NumberFormatException e) {
				ok =false;
				textEl.setErrorKey("academical.date.error", new String[] { getAcademicalDatePlaceholder(formats) });
			}
		}
		return ok;
	}
	
	private String getAcademicalDatePlaceholder(AcademicalDateFormat[] formats) {
		if(formats != null && formats.length > 0) {
			if(AcademicalDateFormat.hasFormat(AcademicalDateFormat.monthYear, formats)) {
				return "11.2017";
			} else {
				return "";
			}
		}
		return "";
	}

	private boolean validateIntegerElement(TextElement textEl, boolean mandatory) {
		boolean ok = true;
		textEl.clearError();
		
		String value = textEl.getValue();
		if(mandatory && !StringHelper.containsNonWhitespace(value)) {
			textEl.setErrorKey("form.legende.mandatory");
			ok = false;
		} else if(StringHelper.containsNonWhitespace(value)) {
			try {
				Integer.parseInt(value);
			} catch(NumberFormatException e) {
				textEl.setErrorKey("integer.element.int.error");
				ok = false;
			}
		}

		return ok;
	}
	
	private boolean validateDoubleElement(TextElement textEl, boolean mandatory) {
		boolean ok = true;
		textEl.clearError();
		
		String value = textEl.getValue();
		if(mandatory && !StringHelper.containsNonWhitespace(value)) {
			textEl.setErrorKey("form.legende.mandatory");
			ok = false;
		} else if(StringHelper.containsNonWhitespace(value)) {
			try {
				value = value.replace(",", ".");
				Double.parseDouble(value);
			} catch(NumberFormatException e) {
				textEl.setErrorKey("double.element.error");
				ok = false;
			}
		}

		return ok;
	}
	
	private boolean validateTextElement(TextElement textEl, int length, boolean mandatory, OWASPAntiSamyXSSFilter filter) {
		boolean ok = true;
		textEl.clearError();
		
		String value = textEl.getValue(filter);
		if(mandatory && !StringHelper.containsNonWhitespace(value)) {
			textEl.setErrorKey("form.legende.mandatory");
			ok = false;
		} else if (value.length() > length) {
			textEl.setErrorKey("input.toolong", new String[]{ Integer.toString(length) });
			ok = false;
		} else if(filter.errors(value)) {
			textEl.setErrorKey("form.general.error");
			ok = false;
		}

		return ok;
	}
	
	public void commitChanges(Application app) {
		AcademicalBackground background = app.getAcademicalBackground();
		if(background == null) {
			background = new AcademicalBackgroundImpl();
		}
		
		background.setNumberOfOriginalPublications(toInteger(numOfOriginalPublicationsEl));
		background.setNumberOfFirstAuthorships(toInteger(numOfFirstAuthorshipsEl));
		background.setNumberOfLastAuthorships(toInteger(numOfLastAuthorshipsEl));
		background.setCitations(toInteger(citationsEl));
		background.setImpactFactor(toDouble(impactFactorEl));
		background.setHFactor(toDouble(hFactorEl));

		background.setHighestDegreeInstitution(highestDegreeInstitutionEl.getValue());
		background.setWorkedInAcademiaSince(workedInAcademiaSinceEl.getValue());
		background.setWorkedOutAcademiaSince(workedOutAcademiaSinceEl.getValue());
		background.setWorkedOutAcademiaCareSince(workedOutAcademiaCareSinceEl.getValue());
		background.setCareerDescription(careerDescriptionEl.getValue());
		
		if(highestDegreeTypeEl.isOneSelected()) {
			background.setHighestDegreeType(highestDegreeTypeEl.getSelectedKey());
			if(HighestDegreeType.other.name().equals(highestDegreeTypeEl.getSelectedKey())) {
				background.setHighestDegreeDescription(highestDegreeDescriptionEl.getValue());
			}
		}
		background.setHighestDegreeDate(formatAcademicalDate(highestDegreeDateEl.getValue(),
				 AcademicalDateFormat.yearsOnly()));
		
		background.setDissertationTitle(dissertationTitleEl.getValue());
		background.setDissertationDate(formatAcademicalDate(dissertationDateEl.getValue(),
				recruitingModule.getApplicationAcademicalBackgroundDissertationDateFormat()));
		background.setDissertationInstitution(dissertationInstitutionEl.getValue());
		background.setDissertationKeyword1(dissertationKeyword1El.getValue());
		background.setDissertationKeyword2(dissertationKeyword2El.getValue());
		background.setDissertationKeyword3(dissertationKeyword3El.getValue());
		
		background.setHabilitationTitle(habilitationTitleEl.getValue());
		background.setHabilitationDate(formatAcademicalDate(habilitationDateEl.getValue(),
				AcademicalDateFormat.yearsOnly()));
		background.setHabilitationInstitution(habilitationInstitutionEl.getValue());
		
		background.setOrcid(orcidEl.getValue());
		
		app.setAcademicalBackground(background);
		
		attributesDelegate.commitChanges(additionalAttributesEl, app);
		
		application = app;
	}
	
	private Integer toInteger(TextElement textEl) {
		Integer value = null;
		String val = textEl.getValue();
		if(StringHelper.containsNonWhitespace(val) && StringHelper.isLong(val)) {
			try {
				value = Integer.valueOf(val);
			} catch(NumberFormatException e) {
				logWarn("Cannot parse int:" + val, e);
			}
		}
		return value;
	}
		
	private Double toDouble(TextElement textEl) {
		Double value = null;
		String val = textEl.getValue();
		if(StringHelper.containsNonWhitespace(val)) {
			try {
				val = val.replace(",", ".");
				value = Double.parseDouble(val);
			} catch(NumberFormatException e) {
				logWarn("Cannot parse double:" + val, e);
			}
		}
		return value;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(highestDegreeTypeEl == source) {
			highestDegreeDescriptionEl.clearError();
			highestDegreeDescriptionEl.setVisible(HighestDegreeType.other.name().equals(highestDegreeTypeEl.getSelectedKey()));
		}
		attributesDelegate.formInnerEvent(source);
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitChanges(application);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}