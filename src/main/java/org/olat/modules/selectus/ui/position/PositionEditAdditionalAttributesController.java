/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.PositionImpl;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionComparator;
import org.olat.modules.selectus.model.attributes.SelectConfiguration;
import org.olat.modules.selectus.model.attributes.SelectConfiguration.Display;
import org.olat.modules.selectus.model.attributes.StaticTextConfiguration;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.app_wizard.AcademicalBackgroundController;
import org.olat.modules.selectus.ui.app_wizard.CustomAttributesController;
import org.olat.modules.selectus.ui.app_wizard.EditPersonController;
import org.olat.modules.selectus.ui.app_wizard.ProjectController;
import org.olat.modules.selectus.ui.app_wizard.WizardConstants;
import org.olat.modules.selectus.ui.components.PositionAttributeDefinitionTypeCellRenderer;
import org.olat.modules.selectus.ui.components.YesNoCellRenderer;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionEditAdditionalAttributesDataModel.AttributeCols;
import org.olat.modules.selectus.ui.position.PositionEditStandardAttributesDataModel.StandardAttributeCols;
import org.olat.modules.selectus.ui.position.component.VisibleAttributeCellRenderer;
import org.olat.modules.selectus.ui.position.model.EditVisibilityStepSettings;
import org.olat.modules.selectus.ui.position.model.PositionAdditionalAttributeRow;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 6 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditAdditionalAttributesController extends FormBasicController implements PositionEditableController {
	
	protected static final int CHECKBOX_OFFSET = 500;
	
	private static final String[] mandatoryKeys = new String[] { "mandatory" };
	private static final String[] mandatoryValues = new String[] { "" };
	
	private static final XStream positionXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(positionXStream);
		positionXStream.ignoreUnknownElements();
		positionXStream.alias("position", PositionImpl.class);
		positionXStream.omitField(PositionImpl.class, "key");
		positionXStream.omitField(PositionImpl.class, "reviewDefinition");
		positionXStream.omitField(PositionImpl.class, "attributesDefinitions");
		positionXStream.omitField(PositionImpl.class, "committeeGroup");
		positionXStream.omitField(PositionImpl.class, "committeeHeadGroup");
		positionXStream.omitField(PositionImpl.class, "secretaryGroup");
		positionXStream.omitField(PositionImpl.class, "exOfficioGroup");
		positionXStream.omitField(OrganisationImpl.class, "group");
		positionXStream.omitField(OrganisationImpl.class, "children");
		
		positionXStream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	private DropdownItem dropdown;
	private FormLink addSelectButton;
	private FormLink addNumberButton;
	private FormLink addPercentageButton;
	private FormLink addDateButton;
	private FormLink addHeadingButton;
	private FormLink addSingleLineTextButton;
	private FormLink addSeparatorButton;
	private FormLink addStaticTextButton;
	
	private List<FormLink> previewButtons = new ArrayList<>(2);
	private List<TextElement> helpEls = new ArrayList<>(2);
	private FormLayoutContainer helpContainer;
	private FlexiTableElement standardTableEl;
	private FlexiTableElement additionalTableEl;
	private PositionEditStandardAttributesDataModel standardTableModel;
	private PositionEditAdditionalAttributesDataModel additionalTableModel;
	private final FlexiTableColumnModel standardColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
	private final FlexiTableColumnModel additionalColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

	private Controller previewCtrl;
	private CloseableModalController cmc;
	private PositionEditAdditionalAttributeController addAttributeCtrl;
	private PositionEditAdditionalAttributeController editAttributeCtrl;
	private ConfirmDeleteAdditionalAttributesController confirmDeleteCtrl;
	private PositionEditAdditionalAttributeLabelController editAttributeLabelCtrl;
	private PositionEditAdditionalAttributePlaceholderController editAttributePlaceholderCtrl;
	private CloseableCalloutWindowController editCallout;
	
	private Position position;
	private final boolean readOnly;
	private int maxNumberOfAttributes;
	private List<Locale> positionLanguages;
	private TabConfiguration tabConfiguration;
	private final PositionApplicationAttributeTabEnum tab;
	private EditVisibilityStepSettings visibilityStepSettings;
	private List<ApplicationsFeedbackConfiguration> configurations;
	private final TabsConfigurationDelegate tabsConfigurationDelegate;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditAdditionalAttributesController(UserRequest ureq, WindowControl wControl,
			Position position, PositionApplicationAttributeTabEnum tab, boolean readOnly) {
		super(ureq, wControl, "custom_attributes", Util.createPackageTranslator(PositionController.class, ureq.getLocale(),
				Util.createPackageTranslator(WizardConstants.class, ureq.getLocale())));
		this.position = position;
		this.readOnly = readOnly;
		this.tab = tab;
		positionLanguages = recruitingModule.getPositionLocales(position);
		tabConfiguration = position.getTabConfiguration(tab.tab());
		tabsConfigurationDelegate = new TabsConfigurationDelegate(tab.tab());
		tabsConfigurationDelegate.defaultHelpText(position, tabConfiguration);
		maxNumberOfAttributes = recruitingModule.getPositionMaxNumberOfAdditionalAttributes();
		configurations = feedbackService.getApplicationsFeedbackConfigurations(position);
		visibilityStepSettings = new EditVisibilityStepSettings(position, configurations);
		initForm(ureq);
		loadModel();
	}
	
	public PositionEditAdditionalAttributesController(UserRequest ureq, WindowControl wControl, PositionApplicationAttributeTabEnum tab) {
		super(ureq, wControl, "custom_attributes", Util.createPackageTranslator(PositionController.class, ureq.getLocale(),
				Util.createPackageTranslator(WizardConstants.class, ureq.getLocale())));
		this.position = null;
		this.readOnly = false;
		this.tab = tab;
		positionLanguages = new ArrayList<>();
		positionLanguages.add(recruitingModule.getReportingLocale());
		tabConfiguration = new TabConfiguration(tab.tab());
		tabsConfigurationDelegate = new TabsConfigurationDelegate(tab.tab());
		maxNumberOfAttributes = recruitingModule.getPositionMaxNumberOfAdditionalGlobalAttributes();
		initForm(ureq);
		loadModel();
	}
	
	public void setFormTitleTranslated(String info) {
		flc.contextPut("off_title", info);
	}
	
	public void setFormInfoTranslated(String info) {
		flc.contextPut("off_info", info);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		
		positionLanguages = recruitingModule.getPositionLocales(position);
		tabsConfigurationDelegate.updateHelps(positionLanguages, tabConfiguration, helpContainer,
				helpEls, null, getWindowControl(), false);
		
		configurations = feedbackService.getApplicationsFeedbackConfigurations(position);
		visibilityStepSettings = new EditVisibilityStepSettings(position, configurations);
		standardTableModel.updateVisibilityStepSettings(visibilityStepSettings);
		additionalTableModel.updateVisibilityStepSettings(visibilityStepSettings);
		
		initStandardAttributesColumns();
		initAdditionalAttributesColumns();
		loadModel();
		initPreviewButtons(flc);
		
		if(standardTableEl != null) {
			standardTableEl.reset(false, false, true);
			for(int i=0; i<standardColumnsModel.getColumnCount(); i++) {
				standardTableEl.setColumnModelVisible(standardColumnsModel.getColumnModel(i), true);
			}			
		}
		
		if(additionalTableEl != null) {
			additionalTableEl.reset(false, false, true);
			for(int i=0; i<additionalColumnsModel.getColumnCount(); i++) {
				additionalTableEl.setColumnModelVisible(additionalColumnsModel.getColumnModel(i), true);
			}			
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("custom.attributes.title");
		if(position != null) {
			helpContainer = tabsConfigurationDelegate
					.initHelpTexts(positionLanguages, tabConfiguration, formLayout, mainForm, helpEls, null, getWindowControl(), false, readOnly);
		}
		initFormAdditionalAttributes(formLayout, ureq);
		initFormStandardAttributes(formLayout);
	}
	
	private void initFormStandardAttributes(FormItemContainer formLayout) {
		initStandardAttributesColumns();
		
		List<String> excludeAttributes = position == null ? recruitingModule.getNewPositionExcludedAttributesList() : position.getExcludedAttributesList();

		standardTableModel = new PositionEditStandardAttributesDataModel(standardColumnsModel, visibilityStepSettings, tab.tab());
		standardTableModel.setObjects(StandardAttributesDelegate.getRows(tab, excludeAttributes, getTranslator()));
		standardTableEl = uifactory.addTableElement(getWindowControl(), "standard.attributes.table", standardTableModel,
				24, true, getTranslator(), formLayout);
		standardTableEl.setNumOfRowsEnabled(false);
		standardTableEl.setCustomizeColumns(false);
		standardTableEl.setVisible(standardTableModel.getRowCount() > 0);
		standardTableEl.setElementCssClass("o_std_attr_table");
	}
	
	private void initStandardAttributesColumns() {
		standardColumnsModel.clear();
		
		standardColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StandardAttributeCols.type));
		standardColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StandardAttributeCols.heading));
		standardColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StandardAttributeCols.labels));
		if(tab != PositionApplicationAttributeTabEnum.global) {
			standardColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StandardAttributeCols.mandatory,
					new YesNoCellRenderer(getTranslator())));
		}
		
		if(visibilityStepSettings != null && recruitingModule.isReferenceEnabled()) {
			if(position.isRefereeRecommendationEnabled()) {
				standardColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StandardAttributeCols.visibilityReferees,
						new VisibleAttributeCellRenderer()));
			}
			if(position.isExpertRecommendationEnabled()) {
				standardColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StandardAttributeCols.visibilityExperts,
						new VisibleAttributeCellRenderer()));
			}
			if(position.isComparativeAssessmentExpertEnabled()) {
				standardColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StandardAttributeCols.visibilityComparativeAssessment,
						new VisibleAttributeCellRenderer()));
			}
			
			if(configurations != null) {
				for(int i=0; i<configurations.size(); i++) {
					ApplicationsFeedbackConfiguration configuration = configurations.get(i);
					if(configuration.isEnabled()) {
						String key = "config_" + i;
						DefaultFlexiColumnModel feedbackCol = new DefaultFlexiColumnModel(key, CHECKBOX_OFFSET + i, false, null,
								new VisibleAttributeCellRenderer());
						feedbackCol.setHeaderLabel(configuration.getConfigurationName());
						standardColumnsModel.addFlexiColumnModel(feedbackCol);
					}
				}
			}
		}
	}
	
	private void initFormAdditionalAttributes(FormItemContainer formLayout, UserRequest ureq) {
		
		if(position != null && position.getAttributesDefinitions(tab).size() > maxNumberOfAttributes) {
			// to prevent losing silently data
			maxNumberOfAttributes = position.getAttributesDefinitions(tab).size();
		}
		formLayout.contextPut("additionalAttributesEnabled", Boolean.valueOf(maxNumberOfAttributes > 0));
		
		initAdditionalAttributesColumns();
		additionalTableModel = new PositionEditAdditionalAttributesDataModel(additionalColumnsModel, visibilityStepSettings, tab.tab());
		additionalTableEl = uifactory.addTableElement(getWindowControl(), "attributes", additionalTableModel, 24, false, getTranslator(), formLayout);
		additionalTableEl.setNumOfRowsEnabled(false);
		additionalTableEl.setCustomizeColumns(false);
		additionalTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
					.withMessageI18nKey(position == null ? "no.custom.global.attributes" : "no.custom.attributes")
					.build());
		additionalTableEl.setElementCssClass("o_edit_ml_table");
		
		FormSubmit saveButton = uifactory.addFormSubmitButton("save", formLayout);
		saveButton.setVisible(maxNumberOfAttributes > 0 && !readOnly);
		
		initAddAttributesList(formLayout, maxNumberOfAttributes);
		if(position != null) {
			initPreviewButtons(formLayout);
			FormCancel cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
			cancelButton.setVisible(!readOnly);
		}
	}
	
	private void initAddAttributesList(FormItemContainer formLayout, int maxAttributes) {
		addHeadingButton = uifactory.addFormLink("add.heading.attribute", formLayout, Link.LINK);
		addSingleLineTextButton = uifactory.addFormLink("add.single.line.text.attribute", formLayout, Link.LINK);
		addSelectButton = uifactory.addFormLink("add.select.attribute", formLayout, Link.LINK);
		addNumberButton = uifactory.addFormLink("add.number.attribute", formLayout, Link.LINK);
		addPercentageButton = uifactory.addFormLink("add.percentage.attribute", formLayout, Link.LINK);
		addDateButton = uifactory.addFormLink("add.date.attribute", formLayout, Link.LINK);
		addSeparatorButton = uifactory.addFormLink("add.separator.attribute", formLayout, Link.LINK);
		addStaticTextButton = uifactory.addFormLink("add.static.text.attribute", formLayout, Link.LINK);
		SpacerElement spacer = uifactory.addSpacerElement("add.spacer", formLayout, false);

		dropdown = new DropdownItem("add.custom.attribute.list", "add.custom.attribute.list", getTranslator());
		dropdown.setDomReplacementWrapperRequired(false);
		formLayout.add("add.custom.attribute.list", dropdown);
		dropdown.setVisible(maxAttributes > 0 && !readOnly);
		//TODO selectus
		//dropdown.setTextReasonForDisabling(translate("add.custom.attribute.max.number",
		//		new String[] { Integer.toString(maxAttributes) }));
		
		dropdown.addElement(addSingleLineTextButton);
		dropdown.addElement(addSelectButton);
		dropdown.addElement(addNumberButton);
		dropdown.addElement(addPercentageButton);
		dropdown.addElement(addDateButton);
		dropdown.addElement(spacer);
		dropdown.addElement(addHeadingButton);
		dropdown.addElement(addSeparatorButton);
		dropdown.addElement(addStaticTextButton);
		dropdown.setButton(true);
		dropdown.setEmbbeded(true);
	}
	
	private void initPreviewButtons(FormItemContainer formLayout) {
		if(position == null) return;
		
		if(!previewButtons.isEmpty()) {
			for(FormLink previewButton:previewButtons) {
				formLayout.remove(previewButton);
			}
			previewButtons.clear();
		}
		
		List<Locale> locales  = recruitingModule.getPositionLocales(position);
		for(Locale locale:locales) {
			String link;
			if(locales.size() == 1) {
				link = translate("edit.template.preview");
			} else {
				link = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
			}
			FormLink previewButton = uifactory.addFormLink("preview_".concat(locale.getLanguage()), "preview", link, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			previewButton.setUserObject(locale);
			previewButtons.add(previewButton);
		}
		formLayout.contextPut("previewButtons", previewButtons);
	}
	
	private void initAdditionalAttributesColumns() {
		additionalColumnsModel.clear();
		
		boolean multiLanguages = false;
		if(positionLanguages.size() > 1 && (position == null || position.getAvailableLanguagesArray().length != 1)) {
			multiLanguages = true;
		}
		
		int inputAlignement = multiLanguages ? FlexiColumnModel.ALIGNMENT_RIGHT : FlexiColumnModel.ALIGNMENT_LEFT;

		additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.up));
		additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.down));
		additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.type,
				new PositionAttributeDefinitionTypeCellRenderer(getTranslator())));
		DefaultFlexiColumnModel labelCol = new DefaultFlexiColumnModel(AttributeCols.label);
		labelCol.setAlignment(inputAlignement);
		additionalColumnsModel.addFlexiColumnModel(labelCol);
		if(multiLanguages) {
			DefaultFlexiColumnModel editLabelCol = new DefaultFlexiColumnModel(AttributeCols.editLabel);
			editLabelCol.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
			additionalColumnsModel.addFlexiColumnModel(editLabelCol);
		}
		
		DefaultFlexiColumnModel placeholderCol = new DefaultFlexiColumnModel(AttributeCols.placeholder);
		placeholderCol.setAlignment(inputAlignement);
		additionalColumnsModel.addFlexiColumnModel(placeholderCol);
		if(multiLanguages) {
			DefaultFlexiColumnModel editLabelCol = new DefaultFlexiColumnModel(AttributeCols.editPlaceholder);
			editLabelCol.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
			additionalColumnsModel.addFlexiColumnModel(editLabelCol);
		}
		
		if(tab != PositionApplicationAttributeTabEnum.global) {
			additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.mandatory));
		}
		
		if(visibilityStepSettings != null && recruitingModule.isReferenceEnabled()) {
			if(position.isRefereeRecommendationEnabled()) {
				additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.visibilityReferees,
						new VisibleAttributeCellRenderer()));
			}
			if(position.isExpertRecommendationEnabled()) {
				additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.visibilityExperts,
						new VisibleAttributeCellRenderer()));
			}
			if(position.isComparativeAssessmentExpertEnabled()) {
				additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.visibilityComparativeAssessment,
						new VisibleAttributeCellRenderer()));
			}

			if(configurations != null) {
				for(int i=0; i<configurations.size(); i++) {
					ApplicationsFeedbackConfiguration configuration = configurations.get(i);
					if(configuration.isEnabled()) {
						String key = "config_" + i;
						DefaultFlexiColumnModel feedbackCol = new DefaultFlexiColumnModel(key, CHECKBOX_OFFSET + i, false, null,
								new VisibleAttributeCellRenderer());
						feedbackCol.setHeaderLabel(configuration.getConfigurationName());
						additionalColumnsModel.addFlexiColumnModel(feedbackCol);
					}
				}
			}
		}
		
		additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.edit));
		additionalColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AttributeCols.delete));
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doFinalizeDeleteCustomAttribute(confirmDeleteCtrl.getDefinition());
			}
			cmc.deactivate();
			cleanUp();
		} else if(editAttributeLabelCtrl == source || editAttributePlaceholderCtrl == source) {
			additionalTableEl.reset(false, false, true);
			editCallout.deactivate();
			cleanUp();
			markDirty();
		} else if(addAttributeCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doFinalizeAddCustomAttribute(addAttributeCtrl.getAttributeDefinition());
			}
			cmc.deactivate();
			cleanUp();
			markDirty();
		} else if(editAttributeCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doFinalizeEditAttribute(editAttributeCtrl.getAttributeDefinition());
			}
			cmc.deactivate();
			cleanUp();
			markDirty();
		} else if(editCallout == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editAttributePlaceholderCtrl);
		removeAsListenerAndDispose(editAttributeLabelCtrl);
		removeAsListenerAndDispose(editAttributeCtrl);
		removeAsListenerAndDispose(addAttributeCtrl);
		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(editCallout);
		removeAsListenerAndDispose(cmc);
		editAttributePlaceholderCtrl = null;
		editAttributeLabelCtrl = null;
		editAttributeCtrl = null;
		addAttributeCtrl = null;
		previewCtrl = null;
		editCallout = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addSingleLineTextButton == source) {
			doAddAttribute(ureq, PositionAttributeDefinitionTypeEnum.question, null);
		} else if(addHeadingButton == source) {
			doAddAttribute(ureq, PositionAttributeDefinitionTypeEnum.heading, null);
		} else if(addSelectButton == source) {
			doAddAttribute(ureq, PositionAttributeDefinitionTypeEnum.select, SelectConfiguration.defaultMultiple());
		} else if(addNumberButton == source) {
			doAddAttribute(ureq, PositionAttributeDefinitionTypeEnum.number, null);
		} else if(addPercentageButton == source) {
			doAddAttribute(ureq, PositionAttributeDefinitionTypeEnum.percentage, null);
		} else if(addDateButton == source) {
			doAddAttribute(ureq, PositionAttributeDefinitionTypeEnum.date, null);
		} else if(addSeparatorButton == source) {
			doAddAttribute(ureq, PositionAttributeDefinitionTypeEnum.separator, null);
		} else if(addStaticTextButton == source) {
			doAddAttribute(ureq, PositionAttributeDefinitionTypeEnum.text, null);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("mllabel".equals(link.getCmd())) {
				doEditLabel(ureq, link, (PositionAdditionalAttributeRow)link.getUserObject());
			} else if("mlplaceholder".equals(link.getCmd())) {
				doEditPlaceholder(ureq, link, (PositionAdditionalAttributeRow)link.getUserObject());
			} else if("up".equals(link.getCmd())) {
				doUp((PositionAdditionalAttributeRow)link.getUserObject());
			} else if("down".equals(link.getCmd())) {
				doDown((PositionAdditionalAttributeRow)link.getUserObject());
			} else if("edit".equals(link.getCmd())) {
				doEditCustomAttribute(ureq, (PositionAdditionalAttributeRow)link.getUserObject());
			} else if("delete".equals(link.getCmd())) {
				doConfirmDelete(ureq, (PositionAdditionalAttributeRow)link.getUserObject());
			} else if("preview".equals(link.getCmd())) {
				doPreview(ureq, (Locale)link.getUserObject());
			}
		} else {
			Object uobject = source.getUserObject();
			if(uobject instanceof PositionAdditionalAttributeRow) {
				PositionAdditionalAttributeRow row = (PositionAdditionalAttributeRow)uobject;
				if(source.getComponent().getComponentName().startsWith("mandatory_")) {
					row.getAttributeDefinition().setMandatory(row.getMandatoryEl().isAtLeastSelected(1));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(PositionAdditionalAttributeRow row:additionalTableModel.getObjects()) {
			TextElement labelEl = row.getLabelEl();
			if(labelEl != null) {
				allOk &= RecruitingHelper.validateTextElement(labelEl, 255, true, new OWASPAntiSamyXSSFilter());
			}
			TextElement placeholderEl = row.getPlaceholderEl();
			if(placeholderEl != null) {
				if(row.getType() == PositionAttributeDefinitionTypeEnum.text) {
					allOk &= RecruitingHelper.validateTextElement(placeholderEl, 32000, false, new OWASPAntiSamyXSSFilter());
				} else {
					allOk &= RecruitingHelper.validateTextElement(placeholderEl, 60, false, new OWASPAntiSamyXSSFilter());	
				}
			}
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent;
		if(position == null) {
			doneEvent = Event.DONE_EVENT;
		} else if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
			doneEvent = Event.DONE_EVENT;
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		commitChanges();
		if(position == null) {
			saveGlobal();
		} else {
			savePosition();
		}
		
		loadModel();
		fireEvent(ureq, doneEvent);
	}
	
	private void saveGlobal() {
		// remove deleted attributes
		List<PositionAttributeDefinition> toDelete = new ArrayList<>();
		List<PositionAttributeDefinition> globalDefinitions = this.recruitingService.getGlobalAttributeDefinition();				
		for(PositionAttributeDefinition dbDefinition:globalDefinitions) {
			if(dbDefinition.getTabEnum() != tab) continue;
			
			boolean deleted = true;
			for(PositionAdditionalAttributeRow row:additionalTableModel.getObjects()) {
				if(row.getAttributeDefinition().equals(dbDefinition)) {
					deleted = false;
					break;
				}
			}
			if(deleted) {
				toDelete.add(dbDefinition);
			}
		}
		
		for(PositionAttributeDefinition def:toDelete) {
			if(def.getTabEnum() == tab) {
				recruitingService.deleteAttributeDefinition(null, def);
			}
		}
		
		// reorder ???
		
		// save data
		int count = 0;
		List<PositionAttributeDefinition> dbsDefinitions = new ArrayList<>(globalDefinitions);
		for(PositionAdditionalAttributeRow row:additionalTableModel.getObjects()) {
			PositionAttributeDefinition uiDefinition = row.getAttributeDefinition();
			PositionAttributeDefinition dbDefinition = getAttributeDefinition(dbsDefinitions, uiDefinition);
			if(dbDefinition == null) {
				dbDefinition = uiDefinition;
			} else {
				for(Locale positionLanguage:positionLanguages) {
					dbDefinition.setLabel(uiDefinition.getLabel(positionLanguage), positionLanguage);
					dbDefinition.setPlaceholder(uiDefinition.getPlaceholder(positionLanguage), positionLanguage);
				}
				dbDefinition.setMandatory(uiDefinition.isMandatory());
				dbDefinition.setAttributeConfiguration(uiDefinition.getAttributeConfiguration());
			}
			dbDefinition.setOrderPosition(Integer.valueOf(count++));
			recruitingService.updateAttributeDefinition(dbDefinition);
		}
	}
	
	/**
	 * Save the position, reload before calling the method
	 */
	private void savePosition() {
		
		tabsConfigurationDelegate.save(position, tabConfiguration, helpEls, null);
		
		// remove deleted attributes
		List<PositionAttributeDefinition> toDelete = new ArrayList<>();
		for(PositionAttributeDefinition dbDefinition:position.getAttributesDefinitions()) {
			if(dbDefinition.getTabEnum() != tab) continue;
			
			boolean deleted = true;
			for(PositionAdditionalAttributeRow row:additionalTableModel.getObjects()) {
				if(row.getAttributeDefinition().equals(dbDefinition)) {
					deleted = false;
					break;
				}
			}
			if(deleted) {
				toDelete.add(dbDefinition);
			}
		}
		for(PositionAttributeDefinition def:toDelete) {
			if(def.getTabEnum() == tab) {
				position = recruitingService.deleteAttributeDefinition(position, def);
			}
		}
		
		// reorder
		List<PositionAttributeDefinition> dbsDefinitions = new ArrayList<>(position.getAttributesDefinitions());
		for(Iterator<PositionAttributeDefinition> defIt=position.getAttributesDefinitions().iterator(); defIt.hasNext(); ) {
			PositionAttributeDefinition def = defIt.next();
			if(def == null || def.getTabEnum() == tab) {
				defIt.remove();
			}
		}
		
		// save data
		for(PositionAdditionalAttributeRow row:additionalTableModel.getObjects()) {
			PositionAttributeDefinition uiDefinition = row.getAttributeDefinition();
			PositionAttributeDefinition dbDefinition = getAttributeDefinition(dbsDefinitions, row.getAttributeDefinition());
			if(dbDefinition == null) {
				position.getAttributesDefinitions().add(uiDefinition);
			} else {
				for(Locale positionLanguage:positionLanguages) {
					dbDefinition.setLabel(uiDefinition.getLabel(positionLanguage), positionLanguage);
					dbDefinition.setPlaceholder(uiDefinition.getPlaceholder(positionLanguage), positionLanguage);
				}
				dbDefinition.setMandatory(uiDefinition.isMandatory());
				dbDefinition.setAttributeConfiguration(uiDefinition.getAttributeConfiguration());
				position.getAttributesDefinitions().add(dbDefinition);
			}
		}
		
		// remove null
		for(Iterator<PositionAttributeDefinition> defIt=position.getAttributesDefinitions().iterator(); defIt.hasNext(); ) {
			if(defIt.next() == null) {
				defIt.remove();
			}
		}

		position = recruitingService.savePosition(position);
		dbInstance.commit();
	}
	
	private PositionAttributeDefinition getAttributeDefinition(List<PositionAttributeDefinition> dbsDefinitions, PositionAttributeDefinition definition) {
		for(PositionAttributeDefinition def:dbsDefinitions) {
			if(definition.equals(def)) {
				return def;
			}
		}
		return null;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	private void loadModel() {
		List<PositionAttributeDefinition> definitions;
		if(position == null ) {
			definitions = recruitingService.getGlobalAttributeDefinition();
			if(definitions.size() > 1) {
				Collections.sort(definitions, new PositionAttributeDefinitionComparator());
			}
		} else {
			definitions = position.getAttributesDefinitions();
		}

		List<PositionAttributeDefinition> tabDefinitions = definitions.stream()
			.filter(def -> def != null)
			.filter(def -> tab.equals(def.getTabEnum()))
			.collect(Collectors.toList());
		
		List<PositionAdditionalAttributeRow> rows = new ArrayList<>(tabDefinitions.size());
		for(PositionAttributeDefinition tabDefinition:tabDefinitions) {
			PositionAdditionalAttributeRow row = forgeRow(tabDefinition);
			rows.add(row);
		}
		
		additionalTableModel.setObjects(rows);
		updateUpDown();
		additionalTableEl.reset(false, false, true);
	}
	
	private PositionAdditionalAttributeRow forgeRow(PositionAttributeDefinition attributeDefinition) {
		PositionAttributeDefinitionTypeEnum type = attributeDefinition.getTypeEnum();
		String attrKey = getAttrKey(attributeDefinition);
		
		Locale fieldLocale = getFieldLocale();
		MultipleSelectionElement mandatoryEl = null;
		
		if(type == PositionAttributeDefinitionTypeEnum.question || type == PositionAttributeDefinitionTypeEnum.select
				|| type == PositionAttributeDefinitionTypeEnum.number || type == PositionAttributeDefinitionTypeEnum.percentage
				|| type == PositionAttributeDefinitionTypeEnum.date) {
			mandatoryEl = uifactory.addCheckboxesHorizontal("mandatory_".concat(attrKey), null, flc, mandatoryKeys, mandatoryValues);
			mandatoryEl.addActionListener(FormEvent.ONCHANGE);
			mandatoryEl.setAjaxOnly(true);
			mandatoryEl.setEnabled(!readOnly);
			if(attributeDefinition.isMandatory()) {
				mandatoryEl.select(mandatoryKeys[0], true);
			}
		}
		
		TextElement labelEl = null;
		if(type == PositionAttributeDefinitionTypeEnum.question || type == PositionAttributeDefinitionTypeEnum.heading
				|| type == PositionAttributeDefinitionTypeEnum.select || type == PositionAttributeDefinitionTypeEnum.number
				|| type == PositionAttributeDefinitionTypeEnum.percentage || type == PositionAttributeDefinitionTypeEnum.date) {
			String label = attributeDefinition.getLabel(fieldLocale, true);
			labelEl = uifactory.addTextElement("label_".concat(attrKey), null, 255, label, flc);
			labelEl.setEnabled(!readOnly);
		}
		
		TextElement placeholderEl = null;
		if(type == PositionAttributeDefinitionTypeEnum.question || type == PositionAttributeDefinitionTypeEnum.number
				|| type == PositionAttributeDefinitionTypeEnum.percentage || isDropdown(attributeDefinition)) {
			String placeholder = attributeDefinition.getPlaceholder(fieldLocale, true);
			placeholderEl = uifactory.addTextElement("placeholder_".concat(attrKey), null, 255, placeholder, flc);
			placeholderEl.setEnabled(!readOnly);
		} else if(type == PositionAttributeDefinitionTypeEnum.text) {
			StaticTextConfiguration configuration = attributeDefinition.getConfiguration(StaticTextConfiguration.class);
			String text = configuration == null ? null : configuration.getText(fieldLocale);
			placeholderEl = uifactory.addTextElement("placeholder_".concat(attrKey), null, 32000, text, flc);
			placeholderEl.setEnabled(!readOnly);
		}
		
		PositionAdditionalAttributeRow row = new PositionAdditionalAttributeRow(attributeDefinition, mandatoryEl, labelEl, placeholderEl);
		if(labelEl != null) {
			labelEl.setUserObject(row);
			
			FormLink editLabelButton = uifactory.addFormLink("mllabel_".concat(attrKey), "mllabel", "", null, null, Link.BUTTON | Link.NONTRANSLATED);
			editLabelButton.setDomReplacementWrapperRequired(false);
			editLabelButton.setIconLeftCSS("o_icon o_icon-lg o_icon_language");
			editLabelButton.setUserObject(row);
			editLabelButton.setVisible(!readOnly);
			row.setEditLabelButton(editLabelButton);
		}
		if(placeholderEl != null) {
			placeholderEl.setUserObject(row);
			FormLink editPlaceholderButton = getPlaceholderEditButton(row, attrKey);
			editPlaceholderButton.setVisible(!readOnly);
			row.setEditPlaceholderButton(editPlaceholderButton);
		}
		if(mandatoryEl != null) {
			mandatoryEl.setUserObject(row);
		}

		FormLink editButton = uifactory.addFormLink("edit_".concat(attrKey), "edit", "", null, null, Link.LINK | Link.NONTRANSLATED);
		editButton.setDomReplacementWrapperRequired(false);
		editButton.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
		editButton.setUserObject(row);
		editButton.setVisible(!readOnly);
		editButton.setEnabled(!readOnly);
		row.setEditButton(editButton);
		
		FormLink upButton = uifactory.addFormLink("up_".concat(attrKey), "up", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		upButton.setDomReplacementWrapperRequired(false);
		upButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		upButton.setUserObject(row);
		upButton.setVisible(!readOnly);
		row.setUpButton(upButton);
		
		FormLink downButton = uifactory.addFormLink("down_".concat(attrKey), "down", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		downButton.setDomReplacementWrapperRequired(false);
		downButton.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		downButton.setUserObject(row);
		downButton.setVisible(!readOnly);
		row.setDownButton(downButton);
		
		FormLink deleteButton = uifactory.addFormLink("delete_".concat(attrKey), "delete", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		deleteButton.setDomReplacementWrapperRequired(false);
		deleteButton.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
		deleteButton.setUserObject(row);
		deleteButton.setVisible(!readOnly);
		row.setDeleteButton(deleteButton);

		return row;
	}
	
	private FormLink getPlaceholderEditButton(PositionAdditionalAttributeRow row, String attrKey) {
		FormLink editPlaceholderButton = uifactory.addFormLink("mlplaceholder_".concat(attrKey), "mlplaceholder", "", null, null, Link.BUTTON | Link.NONTRANSLATED);
		editPlaceholderButton.setDomReplacementWrapperRequired(false);
		editPlaceholderButton.setIconLeftCSS("o_icon o_icon-lg o_icon_language");
		editPlaceholderButton.setUserObject(row);
		return editPlaceholderButton;
	}
	
	private String getAttrKey(PositionAttributeDefinition attributeDefinition) {
		return attributeDefinition.getKey() == null ? Long.toString(CodeHelper.getForeverUniqueID()) : attributeDefinition.getKey().toString();
	}
	
	private boolean isDropdown(PositionAttributeDefinition attributeDefinition) {
		PositionAttributeDefinitionTypeEnum type = attributeDefinition.getTypeEnum();
		 if(type == PositionAttributeDefinitionTypeEnum.select) {
			SelectConfiguration config = attributeDefinition.getConfiguration(SelectConfiguration.class);
			if(config != null && config.getDisplay() == Display.dropdown) {
				return true;
			}
		}
		return false;
	}
	
	public void updateUpDown() {
		List<PositionAdditionalAttributeRow> rows = additionalTableModel.getObjects();
		for(int i=0; i<rows.size(); i++) {
			PositionAdditionalAttributeRow row = rows.get(i);
			row.getUpButton().setVisible(i != 0 && !readOnly);
			row.getDownButton().setVisible(i + 1 < rows.size() && !readOnly);
		}
		boolean canAdd = rows.size() < maxNumberOfAttributes;
		dropdown.setEnabled(canAdd);
	}
	
	private void doUp(PositionAdditionalAttributeRow row) {
		List<PositionAdditionalAttributeRow> definitions = additionalTableModel.getObjects();
		int index = definitions.indexOf(row);
		if(index >= 1) {
			PositionAdditionalAttributeRow definition = definitions.remove(index);
			definitions.add(index - 1, definition);
		}
		additionalTableModel.setObjects(definitions);
		additionalTableEl.reset(false, false, true);
		commitChanges();
		updateUpDown();
		markDirty();
	}
	
	private void doDown(PositionAdditionalAttributeRow row) {
		List<PositionAdditionalAttributeRow> definitions = additionalTableModel.getObjects();
		int index = definitions.indexOf(row);
		if(index >= 0 && index + 1 < definitions.size()) {
			PositionAdditionalAttributeRow definition = definitions.remove(index);
			definitions.add(index + 1, definition);
		}
		additionalTableModel.setObjects(definitions);
		additionalTableEl.reset(false, false, true);
		commitChanges();
		updateUpDown();
		markDirty();
	}
	
	private void commitChanges() {
		Locale fieldLocale = getFieldLocale();
		
		for(PositionAdditionalAttributeRow row:additionalTableModel.getObjects()) {
			if(row.getLabelEl() != null) {
				row.getAttributeDefinition().setLabel(row.getLabelEl().getValue(), fieldLocale);
			}
			if(row.getPlaceholderEl() != null) {
				if(row.getAttributeDefinition().getTypeEnum() == PositionAttributeDefinitionTypeEnum.text) {
					StaticTextConfiguration configuration = row.getAttributeDefinition().getConfiguration(StaticTextConfiguration.class);
					configuration.setText(row.getPlaceholderEl().getValue(), fieldLocale);
					row.getAttributeDefinition().setConfiguration(configuration);
				} else {
					row.getAttributeDefinition().setPlaceholder(row.getPlaceholderEl().getValue(), fieldLocale);
				}
			}
			if(row.getMandatoryEl() != null) {
				row.getAttributeDefinition().setMandatory(row.getMandatoryEl().isAtLeastSelected(1));
			}
		}
	}
	
	private Locale getFieldLocale() {
		if(position == null) {
			return recruitingModule.getReportingLocale();
		}
		
		String[] positionLanguages = position.getAvailableLanguagesArray();
		
		Locale fieldLocale = null;
		if(positionLanguages != null && positionLanguages.length > 0) {
			if(positionLanguages.length == 1) {
				fieldLocale = recruitingModule.getPositionLocale(positionLanguages[0]);
			} else {
				// prefer English if possible
				for(String positionLanguage:positionLanguages) {
					if(positionLanguage.equals(getLocale().getLanguage())) {
						fieldLocale = getLocale();
					}
				}
				
				if(fieldLocale == null) {
					fieldLocale = recruitingModule.getPositionLocale(positionLanguages[0]);
				}
			}
		}
		
		if(fieldLocale == null) {
			fieldLocale = getLocale();
		}
		return fieldLocale;
	}

	private void doEditLabel(UserRequest ureq, FormLink link, PositionAdditionalAttributeRow row) {
		commitChanges();
		editAttributeLabelCtrl = new PositionEditAdditionalAttributeLabelController(ureq, getWindowControl(), row);
		listenTo(editAttributeLabelCtrl);

		String title = translate("edit.custom.attribute", new String[] { row.getAttributeDefinition().getLabel(getLocale(), true) });
		editCallout = new CloseableCalloutWindowController(ureq, getWindowControl(), editAttributeLabelCtrl.getInitialComponent(),
				link.getFormDispatchId(), title, true, "");
		listenTo(editCallout);
		editCallout.activate();
	}
	
	private void doEditPlaceholder(UserRequest ureq, FormLink link,  PositionAdditionalAttributeRow row) {
		commitChanges();
		
		// text special treatment
		if(row.getType() == PositionAttributeDefinitionTypeEnum.text) {
			doEditCustomAttribute(ureq, row);
			return;
		}
		
		editAttributePlaceholderCtrl = new PositionEditAdditionalAttributePlaceholderController(ureq, getWindowControl(), row);
		listenTo(editAttributePlaceholderCtrl);

		String title = translate("edit.custom.attribute", new String[] { row.getAttributeDefinition().getPlaceholder(getLocale(), true) });
		editCallout = new CloseableCalloutWindowController(ureq, getWindowControl(), editAttributePlaceholderCtrl.getInitialComponent(),
				link.getFormDispatchId(), title, true, "");
		listenTo(editCallout);
		editCallout.activate();
	}
	
	private void doAddAttribute(UserRequest ureq, PositionAttributeDefinitionTypeEnum type, Object defaultConfiguration) {
		if(guardModalController(addAttributeCtrl)) return;
		
		PositionAttributeDefinition newDefinition = recruitingService
				.createAttributeDefinition(position, tab, type, null, null, false, null, null);
		addAttributeCtrl = getEditAdditionalAttributeController(ureq, newDefinition, defaultConfiguration);
		listenTo(addAttributeCtrl);
		
		String title = translate("add.custom.attribute");
		cmc = new CloseableModalController(getWindowControl(), "c", addAttributeCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doFinalizeAddCustomAttribute(PositionAttributeDefinition newDefinition) {
		PositionAdditionalAttributeRow newRow = forgeRow(newDefinition);
		List<PositionAdditionalAttributeRow> rows = additionalTableModel.getObjects();
		rows.add(newRow);
		additionalTableModel.setObjects(rows);
		additionalTableEl.reset(false, true, true);
		updateUpDown();
	}
	
	private void doEditCustomAttribute(UserRequest ureq, PositionAdditionalAttributeRow row) {
		commitChanges();
		PositionAttributeDefinition definition = row.getAttributeDefinition();
		editAttributeCtrl = getEditAdditionalAttributeController(ureq, definition, null);
		listenTo(editAttributeCtrl);
		
		String titleParam;
		if(row.getType() == PositionAttributeDefinitionTypeEnum.text
				|| row.getType() == PositionAttributeDefinitionTypeEnum.separator
				|| row.getType() == PositionAttributeDefinitionTypeEnum.heading) {
			titleParam = translate(row.getType().i18nKey());
		} else {
			titleParam = row.getAttributeDefinition().getLabel(getLocale(), true);
		}
		String title = translate("edit.custom.attribute", new String[] { titleParam });
		cmc = new CloseableModalController(getWindowControl(), "c", editAttributeCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private PositionEditAdditionalAttributeController getEditAdditionalAttributeController(UserRequest ureq,
			PositionAttributeDefinition definition, Object defaultConfiguration) {
		
		PositionEditAdditionalAttributeController controller;
		switch(definition.getTypeEnum()) {
			case question:
				controller = new PositionEditAdditionalSingleLineTextAttributeController(ureq, getWindowControl(), position, definition, tab);
				break;
			case heading:
				controller = new PositionEditAdditionalHeadingAttributeController(ureq, getWindowControl(), position, definition);
				break;
			case select:
				controller = new PositionEditAdditionalSelectAttributeController(ureq, getWindowControl(),
						position, definition, tab, (SelectConfiguration)defaultConfiguration);
				break;
			case number:
			case percentage:
				controller = new PositionEditAdditionalNumberAttributeController(ureq, getWindowControl(), position, definition, tab);
				break;
			case date:
				controller = new PositionEditAdditionalDateAttributeController(ureq, getWindowControl(), position, definition, tab);
				break;	
			case separator:
				controller = new PositionEditAdditionalSeparatorAttributeController(ureq, getWindowControl(), definition);
				break;
			case text:
				controller = new PositionEditAdditionalTextAttributeController(ureq, getWindowControl(), position, definition);
				break;
			default:
				controller = null;
				break;
				
		}
		return controller;
	}
	
	private void doFinalizeEditAttribute(PositionAttributeDefinition definition) {
		for(PositionAdditionalAttributeRow row:additionalTableModel.getObjects()) {
			if(row.getAttributeDefinition().equals(definition)) {
				doFinalizeEditAttribute(row, definition);
			}
		}
		additionalTableEl.reset(false, false, true);
		updateUpDown();
	}
	
	private void doFinalizeEditAttribute(PositionAdditionalAttributeRow row, PositionAttributeDefinition definition) {
		Locale fieldLocale = getFieldLocale();

		if(row.getLabelEl() != null) {
			row.getLabelEl().setValue(definition.getLabel(fieldLocale));
		}
		
		if(row.getAttributeDefinition().getTypeEnum() == PositionAttributeDefinitionTypeEnum.select) {
			boolean dropdown = isDropdown(definition);
			if(row.getPlaceholderEl() != null) {
				row.getPlaceholderEl().setValue(definition.getPlaceholder(fieldLocale));
				row.getPlaceholderEl().setVisible(dropdown);
				row.getEditPlaceholderButton().setVisible(dropdown && !readOnly);
			} else if(dropdown) {
				String attrKey = getAttrKey(definition);
				String placeholder = definition.getPlaceholder(fieldLocale, true);
				TextElement placeholderEl = uifactory.addTextElement("placeholder_".concat(attrKey), null, 255, placeholder, flc);
				row.setPlaceholderEl(placeholderEl);
				FormLink editPlaceholderButton = getPlaceholderEditButton(row, attrKey);
				row.setEditPlaceholderButton(editPlaceholderButton);
			}
		} else if(row.getPlaceholderEl() != null) {
			String val;
			if(row.getAttributeDefinition().getTypeEnum() == PositionAttributeDefinitionTypeEnum.text) {
				StaticTextConfiguration configuration = definition.getConfiguration(StaticTextConfiguration.class);
				val = configuration == null ? null : configuration.getText(fieldLocale);
			} else {
				val = definition.getPlaceholder(fieldLocale);
			}
			row.getPlaceholderEl().setValue(val);
		}
		
		if(row.getMandatoryEl() != null) {
			if(definition.isMandatory()) {
				row.getMandatoryEl().select(mandatoryKeys[0], true);
			} else {
				row.getMandatoryEl().uncheckAll();
			}
		}
	}

	private void doConfirmDelete(UserRequest ureq, PositionAdditionalAttributeRow row) {
		PositionAttributeDefinition attributeDefinition = row.getAttributeDefinition();
		confirmDeleteCtrl = new ConfirmDeleteAdditionalAttributesController(ureq, getWindowControl(), position, attributeDefinition);
		listenTo(confirmDeleteCtrl);

		String title = translate("confirm.delete.attr.title", new String[] { attributeDefinition.getLabel(getLocale(), true) });
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doFinalizeDeleteCustomAttribute(PositionAttributeDefinition attributeDefinition) {
		List<PositionAdditionalAttributeRow> rows = this.additionalTableModel.getObjects();
		for(PositionAdditionalAttributeRow row:rows) {
			if(row.getAttributeDefinition().equals(attributeDefinition)) {
				rows.remove(row);
				break;
			}
		}
		additionalTableModel.setObjects(rows);
		additionalTableEl.reset(false, true, true);
		updateUpDown();
		markDirty();
	}
	
	private void doPreview(UserRequest ureq, Locale locale) {
		String positionXml = positionXStream.toXML(position);
		List<String> excludedAttributesList = position.getExcludedAttributesList();
		Position clonedPosition = (Position)positionXStream.fromXML(positionXml);
		commitChanges();
		for(PositionAdditionalAttributeRow row:additionalTableModel.getObjects()) {
			clonedPosition.getAttributesDefinitions().add(row.getAttributeDefinition());
		}
		Application app = ReferenceHelper.generateDummyApplication(clonedPosition);
		if(!locale.getLanguage().equals(getLocale().getLanguage())) {
			ureq = new SyntheticUserRequest(getIdentity(), locale);
		}
		
		TabConfiguration tempConfiguration = new TabConfiguration();
		tempConfiguration.setTab(tab.tab());
		if(tabConfiguration != null) {
			tempConfiguration.setTitle(tabConfiguration.getTitle());
			tempConfiguration.setTitleDe(tabConfiguration.getTitleDe());
		}
		
		for(TextElement helpEl:helpEls) {
			Locale loc = (Locale)helpEl.getUserObject();
			tempConfiguration.setHelp(helpEl.getValue(), loc);
		}
		
		if(tab == PositionApplicationAttributeTabEnum.personalData) {
			previewCtrl = new EditPersonController(ureq, getWindowControl(), null,
					app, tempConfiguration, excludedAttributesList, false, false, true);
		} else if(tab == PositionApplicationAttributeTabEnum.academicalBackground) {
			previewCtrl = new AcademicalBackgroundController(ureq, getWindowControl(), null,
					app, tempConfiguration, excludedAttributesList, false, false, true);
		} else if(tab == PositionApplicationAttributeTabEnum.project) {
			previewCtrl = new ProjectController(ureq, getWindowControl(), null,
					app, tempConfiguration, false, false, true);
		} else if(tab == PositionApplicationAttributeTabEnum.custom1 || tab == PositionApplicationAttributeTabEnum.custom2
				|| tab == PositionApplicationAttributeTabEnum.custom3 || tab == PositionApplicationAttributeTabEnum.custom4) {
			previewCtrl = new CustomAttributesController(ureq, getWindowControl(), null,
					app, tempConfiguration, false, false, true);
		}
		
		if(previewCtrl != null) {
			listenTo(previewCtrl);
			
			String title;
			if(previewButtons.size() == 1) {
				title = translate("edit.template.preview");
			} else {
				title = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
			}
			cmc = new CloseableModalController(getWindowControl(), "c", previewCtrl.getInitialComponent(), title);
			listenTo(cmc);
			cmc.activate();
		}
	}
}
