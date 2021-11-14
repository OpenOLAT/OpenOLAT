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
package org.olat.modules.qpool.ui.metadata;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExtendedFlexiTableSearchController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.QItemDocument;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory.KeyValues;
import org.olat.modules.qpool.ui.tree.QPoolTaxonomyTreeBuilder;
import org.olat.search.model.AbstractOlatDocument;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExtendedSearchController extends FormBasicController implements ExtendedFlexiTableSearchController {
	
	private FormLink searchButton;
	
	private final SearchAttributes searchAttributes;
	private final List<ConditionalQuery> uiQueries = new ArrayList<>();
	
	private final String prefsKey;
	private ExtendedSearchPrefs prefs;
	private final boolean allTaxonomyLevels;
	private final List<QItemType> excludedItemTypes;
	private boolean enabled = true;
	private final QPoolSecurityCallback qPoolSecurityCallback;
	
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QPoolTaxonomyTreeBuilder qpoolTaxonomyTreeBuilder;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;

	public ExtendedSearchController(UserRequest ureq, WindowControl wControl,
			QPoolSecurityCallback qPoolSecurityCallback, String prefsKey, Form mainForm,
			List<QItemType> excludedItemTypes, boolean allTaxonomyLevels) {
		super(ureq, wControl, LAYOUT_CUSTOM, "extended_search", mainForm);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		this.qPoolSecurityCallback = qPoolSecurityCallback;
		this.allTaxonomyLevels = allTaxonomyLevels;
		this.excludedItemTypes = excludedItemTypes;
		searchAttributes = new SearchAttributes();
		
		this.prefsKey = prefsKey;
		prefs = (ExtendedSearchPrefs) ureq.getUserSession().getGuiPreferences()
				.get(ExtendedFlexiTableSearchController.class, prefsKey);
		
		if(prefs != null && !prefs.getCondQueries().isEmpty()) {
			for(ExtendedSearchPref pref:prefs.getCondQueries()) {
				uiQueries.add(new ConditionalQuery(pref));
			}
		} else {
			uiQueries.add(new ConditionalQuery());
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("uiQueries", uiQueries);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		searchButton = uifactory.addFormLink("search", buttonsCont, Link.BUTTON);
	}
	
	@Override
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(enabled) {
			fireSearchEvent(ureq);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == searchButton) {
			fireSearchEvent(ureq);
		} else if (source instanceof SingleSelection) {
			SingleSelection attrEl = (SingleSelection)source;
			if(attrEl.isOneSelected()) {
				Object uObject = attrEl.getUserObject();
				if(uObject instanceof ConditionalQuery) {
					ConditionalQuery query = (ConditionalQuery)uObject;
					query.selectAttributeType(attrEl.getSelectedKey(), null);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			if(button.getCmd().startsWith("add")) {
				ConditionalQuery query = (ConditionalQuery)button.getUserObject();
				addParameter(query);
			} else if(button.getCmd().startsWith("remove")) {
				ConditionalQuery query = (ConditionalQuery)button.getUserObject();
				removeParameter(query);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void addParameter(ConditionalQuery query) {
		int index = uiQueries.indexOf(query);
		ConditionalQuery newQuery = new ConditionalQuery();
		if(index < 0 || (index + 1) > uiQueries.size()) {
			uiQueries.add(newQuery);
		} else {
			uiQueries.add(index+1, newQuery);
		}
	}
	
	private void removeParameter(ConditionalQuery query) {
		if(uiQueries.size() > 1 && uiQueries.remove(query)) {
			flc.setDirty(true);
		}
	}
	
	private void fireSearchEvent(UserRequest ureq) {
		SearchQuestionItemParams searchParams = new SearchQuestionItemParams(null, null, null);
		
		List<ExtendedSearchPref> params = new ArrayList<>();
		for(ConditionalQuery uiQuery:uiQueries) {
			boolean empty = uiQuery.fillSearchParams(searchParams);
			if(!empty) {
				params.add(new ExtendedSearchPref(uiQuery.getAttribute(), uiQuery.getValue()));
			}
		}
		
		if (prefs == null){
			prefs = new ExtendedSearchPrefs();
		}
		prefs.setCondQueries(params);
		ureq.getUserSession().getGuiPreferences().putAndSave(ExtendedFlexiTableSearchController.class, prefsKey, prefs);
		fireEvent(ureq, new QPoolSearchEvent(searchParams));
	}
	
	public class ConditionalQuery {
		
		private SingleSelection attributeChoice;
		private FormItem parameter;
		private QueryParameterFactory parameterFactory;
		private FormLink addButton;
		private FormLink removeButton;

		public ConditionalQuery() {
			this(null);
		}
		
		public ConditionalQuery(ExtendedSearchPref pref) {
			long id = CodeHelper.getRAMUniqueID();

			String[] attrKeys = searchAttributes.getKeys();
			String[] attrValues = new String[attrKeys.length];
			for(int i=attrValues.length; i-->0; ) {
				attrValues[i] = translate(attrKeys[i]);
			}

			attributeChoice = uifactory.addDropdownSingleselect("attr-" + id, null, flc, attrKeys, attrValues, null);
			if(pref == null) {
				selectAttributeType(attrKeys[0], null);
			} else {
				selectAttributeType(pref.getAttribute(), pref.getValue());
			}
			
			boolean found = false;
			if(pref != null && StringHelper.containsNonWhitespace(pref.getAttribute())) {
				String attr = pref.getAttribute();
				for(String attrKey:attrKeys) {
					if(attr.equals(attrKey)) {
						attributeChoice.select(attrKey, true);
						found = true;
					}
				}
			}
			if(!found) {
				attributeChoice.select(attrKeys[0], true);
			}
			
			if(pref == null) {
				selectAttributeType(attrKeys[0], null);
			} else {
				selectAttributeType(pref.getAttribute(), pref.getValue());
			}
			attributeChoice.addActionListener(FormEvent.ONCHANGE);
			attributeChoice.setUserObject(this);
			flc.add(attributeChoice.getName(), attributeChoice);
			addButton = uifactory.addFormLink("add-" + id, "add", null, flc, Link.BUTTON);
			addButton.setUserObject(this);
			flc.add(addButton.getComponent().getComponentName(), addButton);
			removeButton = uifactory.addFormLink("remove-"+ id, "remove", null, flc, Link.BUTTON);
			removeButton.setUserObject(this);
			flc.add(removeButton.getComponent().getComponentName(), removeButton);
		}
		
		public String getAttribute() {
			return attributeChoice.isOneSelected() ? attributeChoice.getSelectedKey() : null;
		}
		
		public String getValue() {
			return "test";
		}
		
		public SingleSelection getAttributChoice() {
			return attributeChoice;
		}
		
		public FormItem getParameterItem() {
			return parameter;
		}
		
		public FormLink getAddButton() {
			return addButton;
		}

		public FormLink getRemoveButton() {
			return removeButton;
		}

		public void selectAttributeType(String type, String value) {
			parameterFactory = searchAttributes.getQueryParameterFactory(type);
			if(parameterFactory != null) {
				parameter = parameterFactory.createItem(value);
			}
		}
		
		public boolean fillSearchParams(SearchQuestionItemParams searchParams) {
			boolean empty = true;
			if(parameterFactory != null && parameter != null) {
				empty = parameterFactory.fillSearchParams(searchParams, parameter);
			}
			return empty;
		}
	}

	public static interface QueryParameterFactory {
		public String getValue(FormItem item);
		
		public FormItem createItem(String startValue);
		
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item);
	}
	
	private class SearchAttributes {
		private List<SearchAttribute> attributes = new ArrayList<>();
		
		public SearchAttributes() {
			//general
			attributes.add(new SearchAttribute("general.title", new StringQueryParameter(AbstractOlatDocument.TITLE_FIELD_NAME)));
			attributes.add(new SearchAttribute("general.topic", new StringQueryParameter(QItemDocument.TOPIC_FIELD)));
			attributes.add(new SearchAttribute("general.keywords", new StringQueryParameter(QItemDocument.KEYWORDS_FIELD)));
			attributes.add(new SearchAttribute("general.coverage", new StringQueryParameter(QItemDocument.COVERAGE_FIELD)));
			attributes.add(new SearchAttribute("general.additional.informations", new StringQueryParameter(QItemDocument.ADD_INFOS_FIELD)));
			attributes.add(new SearchAttribute("general.language", new StringQueryParameter(QItemDocument.LANGUAGE_FIELD)));
			if (qPoolSecurityCallback.canUseTaxonomy()) {
				attributes.add(new SearchAttribute("classification.taxonomy.level", new TaxonomicFieldQueryParameter()));
				attributes.add(new SearchAttribute("classification.taxonomic.path.incl", new TaxonomicPathQueryParameter()));
			}
			attributes.add(new SearchAttribute("owner", new StringQueryParameter(AbstractOlatDocument.AUTHOR_FIELD_NAME)));
			//educational
			if (qPoolSecurityCallback.canUseEducationalContext()) {
				attributes.add(new SearchAttribute("educational.context", new ContextQueryParameter()));
			}
			//question
			attributes.add(new SearchAttribute("question.type", new TypeQueryParameter()));
			attributes.add(new SearchAttribute("question.assessmentType", new AssessmentQueryParameter()));
			//lifecycle
			attributes.add(new SearchAttribute("lifecycle.status", new StatusQueryParameter()));
			//technical
			attributes.add(new SearchAttribute("technical.editor", new StringQueryParameter(QItemDocument.EDITOR_FIELD)));
			attributes.add(new SearchAttribute("technical.format", new FormatQueryParameter()));
			//rights
			if (licenseModule.isEnabled(licenseHandler)) {
				attributes.add(new SearchAttribute("rights.license", new LicenseQueryParameter()));	
			}
		}
		
		public QueryParameterFactory getQueryParameterFactory(String type) {
			for(SearchAttribute attribute:attributes) {
				if(type.equals(attribute.getI18nKey())) {
					return attribute.getFactory();
				}
			}
			return null;
		}
		
		public String[] getKeys() {
			String[] keys = new String[attributes.size()];
			for(int i=keys.length; i-->0; ) {
				keys[i] = attributes.get(i).getI18nKey();
			}
			return keys;
		}
	}
	
	public class StringQueryParameter implements QueryParameterFactory {
		private final String docAttribute;
		
		public StringQueryParameter(String docAttribute) {
			this.docAttribute = docAttribute;
		}

		@Override
		public String getValue(FormItem item) {
			if(item instanceof TextElement) {
				return ((TextElement)item).getValue();
			}
			return null;
		}

		@Override
		public FormItem createItem(String startValue) {
			return uifactory.addTextElement("type-" + CodeHelper.getRAMUniqueID(), null, 50, startValue, flc);
		}

		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			String val = getValue(item);
			if(StringHelper.containsNonWhitespace(val)) {
				if(AbstractOlatDocument.TITLE_FIELD_NAME.equals(docAttribute)) {
					searchParams.setTitle(val);
				} else if(QItemDocument.TOPIC_FIELD.equals(docAttribute)) {
					searchParams.setTopic(val);
				} else if(QItemDocument.KEYWORDS_FIELD.equals(docAttribute)) {
					searchParams.setKeywords(val);
				} else if(AbstractOlatDocument.AUTHOR_FIELD_NAME.equals(docAttribute)) {
					searchParams.setOwner(val);
				} else if(QItemDocument.COVERAGE_FIELD.equals(docAttribute)) {
					searchParams.setCoverage(val);
				} else if(QItemDocument.ADD_INFOS_FIELD.equals(docAttribute)) {
					searchParams.setInformations(val);
				} else if(QItemDocument.LANGUAGE_FIELD.equals(docAttribute)) {
					searchParams.setLanguage(val);
				}
				return true;
			}
			return false;
		}
	}
	
	public class TaxonomicFieldQueryParameter extends SingleChoiceQueryParameter {
		
		public TaxonomicFieldQueryParameter() {
			super(QItemDocument.TAXONOMIC_FIELD);
		}
		
		@Override
		public FormItem createItem(String startValue) {
			qpoolTaxonomyTreeBuilder.loadTaxonomyLevelsSelection(getIdentity(), false, allTaxonomyLevels);
			return createItem(qpoolTaxonomyTreeBuilder.getSelectableKeys(),
					qpoolTaxonomyTreeBuilder.getSelectableValues(), startValue);
		}

		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			searchParams.setTaxonomyLevel(qpoolTaxonomyTreeBuilder.getTaxonomyLevel(getValue(item)));
			return searchParams.getTaxonomyLevel() != null;
		}
	}
	
	public class TaxonomicPathQueryParameter extends SingleChoiceQueryParameter {
		
		public TaxonomicPathQueryParameter() {
			super(QItemDocument.TAXONOMIC_PATH_FIELD);
		}
		
		@Override
		public FormItem createItem(String startValue) {
			qpoolTaxonomyTreeBuilder.loadTaxonomyLevelsSelection(getIdentity(), false, allTaxonomyLevels);
			return createItem(qpoolTaxonomyTreeBuilder.getTaxonomicKeyPaths(),
					qpoolTaxonomyTreeBuilder.getSelectableValues(), startValue);
		}

		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			String val = getValue(item);
			if(StringHelper.containsNonWhitespace(val)) {
				searchParams.setLikeTaxonomyLevel(qpoolTaxonomyTreeBuilder.getTaxonomyLevel(val));
			}
			return searchParams.getLikeTaxonomyLevel() != null;
		}
	}
	
	public class LicenseQueryParameter extends SingleChoiceQueryParameter {
		
		private final LicenseSelectionConfig config;
		
		public LicenseQueryParameter() {
			super(AbstractOlatDocument.LICENSE_TYPE_FIELD_NAME);
			config = LicenseUIFactory.createLicenseSelectionConfig(licenseHandler);
		}
		
		@Override
		public FormItem createItem(String startValue) {
			return createItem(config.getLicenseTypeKeys(), config.getLicenseTypeValues(getLocale()), startValue);
		}

		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			String val = getValue(item);
			if(StringHelper.containsNonWhitespace(val)) {
				searchParams.setLicenseType(config.getLicenseType(val));
			}
			return searchParams.getLicenseType() != null;
		}
	}
	
	public class TypeQueryParameter extends SingleChoiceQueryParameter {
		
		public TypeQueryParameter() {
			super(QItemDocument.ITEM_TYPE_FIELD);
		}
		
		@Override
		public FormItem createItem(String startValue) {
			KeyValues types = MetaUIFactory.getQItemTypeKeyValues(getTranslator(), excludedItemTypes, qpoolService);
			return createItem(types.getKeys(), types.getValues(), startValue);
		}

		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			String val = getValue(item);
			if(StringHelper.containsNonWhitespace(val)) {
				searchParams.setItemType(MetaUIFactory.getQItemTypeByKey(val, qpoolService));
			}
			return searchParams.getItemType() != null;
		}
	}
	
	public class FormatQueryParameter extends SingleChoiceQueryParameter {
		
		public FormatQueryParameter() {
			super(QItemDocument.FORMAT_FIELD);
		}
		
		@Override
		public FormItem createItem(String startValue) {
			KeyValues formats = MetaUIFactory.getFormats();
			return createItem(formats.getKeys(), formats.getValues(), startValue);
		}
		
		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			searchParams.setFormat(getValue(item));
			return StringHelper.containsNonWhitespace(searchParams.getFormat());
		}
	}
	
	public class ContextQueryParameter extends SingleChoiceQueryParameter {
		
		public ContextQueryParameter() {
			super(QItemDocument.EDU_CONTEXT_FIELD);
		}
		
		@Override
		public FormItem createItem(String startValue) {
			KeyValues contexts = MetaUIFactory.getContextKeyValues(getTranslator(), qpoolService);
			return createItem(contexts.getKeys(), contexts.getValues(), startValue);
		}

		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			String val = getValue(item);
			if(StringHelper.containsNonWhitespace(val)) {
				searchParams.setLevel(MetaUIFactory.getContextByKey(val, qpoolService));
			}
			return searchParams.getLevel() != null;
		}
	}
	
	public class AssessmentQueryParameter extends SingleChoiceQueryParameter {
		
		public AssessmentQueryParameter() {
			super(QItemDocument.ASSESSMENT_TYPE_FIELD);
		}
		
		@Override
		public FormItem createItem(String startValue) {
			KeyValues types = MetaUIFactory.getAssessmentTypes(getTranslator());
			return createItem(types.getKeys(), types.getValues(), startValue);
		}

		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			String val = getValue(item);
			if(StringHelper.containsNonWhitespace(val)) {
				searchParams.setAssessmentType(val);
			}
			return StringHelper.containsNonWhitespace(searchParams.getAssessmentType());
		}
	}
	
	public class StatusQueryParameter extends SingleChoiceQueryParameter {
		public StatusQueryParameter() {
			super(QItemDocument.ITEM_STATUS_FIELD);
		}
		
		@Override
		public FormItem createItem(String startValue) {
			KeyValues types = MetaUIFactory.getStatus(getTranslator());
			return createItem(types.getKeys(), types.getValues(), startValue);
		}

		@Override
		public boolean fillSearchParams(SearchQuestionItemParams searchParams, FormItem item) {
			String val = getValue(item);
			if(StringHelper.containsNonWhitespace(val)) {
				searchParams.setQuestionStatus(QuestionStatus.valueOf(val));
			}
			return searchParams.getQuestionStatus() != null;
		}
	}
	
	public abstract class SingleChoiceQueryParameter implements QueryParameterFactory {
		private final String docAttribute;
		
		public SingleChoiceQueryParameter(String docAttribute) {
			this.docAttribute = docAttribute;
		}

		@Override
		public String getValue(FormItem item) {
			if(item instanceof SingleSelection && ((SingleSelection)item).isOneSelected()) {
				return ((SingleSelection)item).getSelectedKey();
			}
			return null;
		}

		protected FormItem createItem(String[] keys, String[] values, String startValue) {
			SingleSelection choice = uifactory.addDropdownSingleselect(docAttribute + "-" + CodeHelper.getRAMUniqueID(),  flc,
					keys, values, null);
			
			if(startValue != null) {
				for(String key:keys) {
					if(key.equals(startValue)) {
						choice.select(key, true);
						
					}
				}	
			}
			return choice;
		}

		public String getDocAttribute() {
			return docAttribute;
		}
	}
	
	private static class SearchAttribute {
		private final String i18nKey;
		private final QueryParameterFactory factory;
		
		public SearchAttribute(String i18nKey, QueryParameterFactory factory) {
			this.i18nKey = i18nKey;
			this.factory = factory;
		}

		public String getI18nKey() {
			return i18nKey;
		}

		public QueryParameterFactory getFactory() {
			return factory;
		}
	}
}