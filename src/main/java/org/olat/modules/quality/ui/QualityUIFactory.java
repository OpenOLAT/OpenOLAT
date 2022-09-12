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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.QualityDataCollectionTopicType.CUSTOM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.nodes.INode;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.ui.CurriculumTreeModel;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.user.IdentityComporatorFactory;
import org.olat.user.ui.organisation.OrganisationTreeModel;

/**
 * 
 * Initial date: 15.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityUIFactory {

	private static final String[] EMPTY_ARRAY = {};
	private static final String FLAT_DELIMITER = " > ";
	private static final String INTENDING = "\u00a0"; // &nbsp; non-breaking space
	private static final Comparator<? super Curriculum> DISPLAY_NAME_COMPARATOR = 
			(c1, c2) -> c1.getDisplayName().compareTo(c2.getDisplayName());
	
	public static String[] emptyArray() {
		return EMPTY_ARRAY;
	}
	
	public static String formatTopic(QualityDataCollectionView dataCollectionView, Locale locale) {
		return formatTopic(dataCollectionView.getTopicType(), dataCollectionView.getTranslatedTopicType(),
				dataCollectionView.getTopic(), locale);
	}

	public static String formatTopic(QualityExecutorParticipation qualityParticipation, Locale locale) {
		return formatTopic(qualityParticipation.getTopicType(), qualityParticipation.getTranslatedTopicType(),
				qualityParticipation.getTopic(), locale);
	}

	public static String formatTopic(QualityDataCollectionTopicType type, String translatedType, String topic, Locale locale) {
		StringBuilder formatedTopic = new StringBuilder();
		if (!CUSTOM.equals(type)) {
			formatedTopic.append(translatedType).append(" ");
		}
		if (StringHelper.containsNonWhitespace(topic)) {
			formatedTopic.append(topic);
		} else if (QualityDataCollectionTopicType.IDENTIY == type) {
			formatedTopic.append(Util.createPackageTranslator(DataCollectionController.class, locale).translate("data.collection.topic.identity.unknown"));
		}
		return formatedTopic.toString();
	}
	
	static String getTopicTypeKey(QualityDataCollectionTopicType topicType) {
		return topicType.name();
	}
	
	static QualityDataCollectionTopicType getTopicTypeEnum(String key) {
		return QualityDataCollectionTopicType.valueOf(key);
	}

	static String[] getTopicTypeKeys(QualityDataCollectionTopicType actual) {
		return Arrays.stream(QualityDataCollectionTopicType.values())
				.filter(organisationDisabled(actual))
				.filter(curriculumDisabled(actual))
				.map(QualityDataCollectionTopicType::name)
				.toArray(String[]::new);
	}

	static String[] getTopicTypeValues(Translator translator, QualityDataCollectionTopicType actual) {
		return Arrays.stream(QualityDataCollectionTopicType.values())
				.filter(organisationDisabled(actual))
				.filter(curriculumDisabled(actual))
				.map(QualityDataCollectionTopicType::getI18nKey)
				.map(i18n -> translator.translate(i18n))
				.toArray(String[]::new);
	}
	
	private static Predicate<QualityDataCollectionTopicType> organisationDisabled(QualityDataCollectionTopicType actual) {
		return tt ->
				   (actual != null && actual.equals(tt))
				|| !QualityDataCollectionTopicType.ORGANISATION.equals(tt)
				|| CoreSpringFactory.getImpl(OrganisationModule.class).isEnabled();
	}
	
	private static Predicate<QualityDataCollectionTopicType> curriculumDisabled(QualityDataCollectionTopicType actual) {
		return tt -> 
				   (actual != null && actual.equals(tt))
				|| !(QualityDataCollectionTopicType.CURRICULUM.equals(tt)
						|| QualityDataCollectionTopicType.CURRICULUM_ELEMENT.equals(tt))
				|| CoreSpringFactory.getImpl(CurriculumModule.class).isEnabled();
	}
	
	public static KeysValues getCurriculumKeysValues(List<Curriculum> curriculums, Curriculum current) {
		List<Curriculum> curriculumsCopy = new ArrayList<>(curriculums);
		if (current != null && !curriculumsCopy.contains(current)) {
			curriculumsCopy.add(0, current);
		}
		String[] keys = curriculumsCopy.stream()
				.sorted(DISPLAY_NAME_COMPARATOR)
				.map(QualityUIFactory::getCurriculumKey)
				.toArray(String[]::new);
		String[] values = curriculumsCopy.stream()
				.sorted(DISPLAY_NAME_COMPARATOR)
				.map(Curriculum::getDisplayName)
				.toArray(String[]::new);
		return new KeysValues(keys, values);
	}

	public static String getCurriculumKey(CurriculumRef curriculumRef) {
		return String.valueOf(curriculumRef.getKey());
	}

	public static CurriculumRef getCurriculumRef(String curriculumKey) {
		if (StringHelper.containsNonWhitespace(curriculumKey)) {
			try {
				Long key = Long.valueOf(curriculumKey);
				return new CurriculumRefImpl(key);
			} catch (Exception e) {
				//
			}
		}
		return null;
	}
	
	public static KeysValues getCurriculumElementFlatKeysValues(List<CurriculumElement> curriculumElements, CurriculumElement current) {
		List<CurriculumElement> elements = new ArrayList<>(curriculumElements);
		if (current != null && !elements.contains(current)) {
			elements.add(0, current);
		}
		String[] keys = new String[elements.size()];
		String[] values = new String[elements.size()];
		for (int i = elements.size(); i-->0; ) {
			CurriculumElement element = elements.get(i);
			keys[i] = getCurriculumElementKey(element);
			values[i] = getCurriculumElementName(element);
		}
		return new KeysValues(keys, values);
	}

	public static KeysValues getCurriculumElementKeysValues(CurriculumTreeModel curriculumTreeModel, CurriculumElement current) {
		List<CurriculumElement> elements = new ArrayList<>();
		curriculumElementTreeToList(elements, curriculumTreeModel.getRootNode());
		if (current != null && !elements.contains(current)) {
			elements.add(0, current);
		}
		String[] keys = new String[elements.size()];
		String[] values = new String[elements.size()];
		for (int i = elements.size(); i-->0; ) {
			CurriculumElement element = elements.get(i);
			keys[i] = getCurriculumElementKey(element);
			values[i] = getCurriculumElementValue(element);
		}
		return new KeysValues(keys, values);
	}

	public static String getCurriculumElementValue(CurriculumElement element) {
		StringBuilder sb = computeIntendentionForCurriculumElement(element, new StringBuilder());
		if (StringHelper.containsNonWhitespace(element.getIdentifier())) {
			sb.append(element.getIdentifier()).append(": ");
		}
		sb.append(element.getDisplayName());
		return sb.toString();
	}
	
	public static void curriculumElementTreeToList(List<CurriculumElement> elements, INode node) {
		if (node instanceof GenericTreeNode) {
			GenericTreeNode genericTreeNode = (GenericTreeNode) node;
			Object userObject = genericTreeNode.getUserObject();
			if (userObject instanceof CurriculumElement) {
				CurriculumElement element = (CurriculumElement) userObject;
				elements.add(element);
			}
			for (int i = 0; i < genericTreeNode.getChildCount(); i++) {
				curriculumElementTreeToList(elements, genericTreeNode.getChildAt(i));
			}
		}
	}

	private static StringBuilder computeIntendentionForCurriculumElement(CurriculumElement element, StringBuilder intendation) {
		CurriculumElement parent = element.getParent();
		if (parent != null) {
			intendation = intendation.append(INTENDING).append(INTENDING).append(INTENDING).append(INTENDING);
			computeIntendentionForCurriculumElement(parent, intendation);
		}
		return intendation;
	}

	public static String getCurriculumElementKey(CurriculumElementRef curriculumElementRef) {
		return String.valueOf(curriculumElementRef.getKey());
	}

	public static CurriculumElementRef getCurriculumElementRef(String curriculumElementKey) {
		if (StringHelper.containsNonWhitespace(curriculumElementKey)) {
			try {
				Long key = Long.valueOf(curriculumElementKey);
				return new CurriculumElementRefImpl(key);
			} catch (Exception e) {
				//
			}
		}
		return null;
	}
	
	public static String getCurriculumElementName(CurriculumElement element) {
		boolean separator = false;
		StringBuilder displayName = new StringBuilder();
		if (StringHelper.containsNonWhitespace(element.getIdentifier())) {
			displayName.append(element.getIdentifier());
			separator = true;
		}
		if (StringHelper.containsNonWhitespace(element.getDisplayName())) {
			if (separator) {
				displayName.append(" | ");
			}
			displayName.append(element.getDisplayName());
			separator = true;
		}
		if (element.getType() != null && StringHelper.containsNonWhitespace(element.getType().getDisplayName())) {
			if (separator) {
				displayName.append(" | ");
			}
			displayName.append(element.getType().getDisplayName());
		}
		return displayName.toString();
	}
	
	public static KeysValues getCurriculumElementTypeKeysValues(List<CurriculumElementType> types) {
		String[] keys = new String[types.size()];
		String[] values = new String[types.size()];
		for (int i = types.size(); i-->0; ) {
			CurriculumElementType type = types.get(i);
			keys[i] = getCurriculumElementTypeKey(type);
			values[i] = type.getDisplayName();
		}
		return new KeysValues(keys, values);
	}
	
	public static String getCurriculumElementTypeKey(CurriculumElementTypeRef typeRef) {
		return Long.toString(typeRef.getKey());
	}
	
	public static CurriculumElementTypeRef getCurriculumElementTypeRef(String typeKey) {
		if (StringHelper.containsNonWhitespace(typeKey)) {
			try {
				Long key = Long.valueOf(typeKey);
				return new CurriculumElementTypeRefImpl(key);
			} catch (Exception e) {
				//
			}
		}
		return null;
	}
	
	public static KeysValues getOrganisationFlatKeysValues(List<Organisation> organisations, Organisation current) {
		List<Organisation> orgs = new ArrayList<>(organisations);
		if (current != null && !orgs.contains(current)) {
			orgs.add(0, current);
		}
		String[] keys = new String[orgs.size()];
		String[] values = new String[orgs.size()];
		for (int i = orgs.size(); i-->0; ) {
			Organisation organisation = orgs.get(i);
			keys[i] = getOrganisationKey(organisation);
			ArrayList<String> names = new ArrayList<>();
			addParentOrganisationNames(names, organisation);
			values[i] = String.join(FLAT_DELIMITER, names);
		}
		return new KeysValues(keys, values);
	}

	public static void addParentOrganisationNames(List<String> names, Organisation organisation) {
		names.add(organisation.getDisplayName());
		Organisation parent = organisation.getParent();
		if (parent != null) {
			addParentOrganisationNames(names, parent);
		}
	}

	public static KeysValues getOrganisationKeysValues(OrganisationTreeModel organisationModel, Organisation current) {
		List<Organisation> organisations = new ArrayList<>();
		organsiationTreeToList(organisations, organisationModel.getRootNode());
		if (current != null && !organisations.contains(current)) {
			organisations.add(0, current);
		}
		String[] keys = new String[organisations.size()];
		String[] values = new String[organisations.size()];
		for (int i = organisations.size(); i-->0; ) {
			Organisation organisation = organisations.get(i);
			keys[i] = getOrganisationKey(organisation);
			values[i] = computeIntendentionForOrganisation(organisation, new StringBuilder()).append(organisation.getDisplayName()).toString();
		}
		return new KeysValues(keys, values);
	}
	
	private static void organsiationTreeToList(List<Organisation> organisations, INode node) {
		if (node instanceof GenericTreeNode) {
			GenericTreeNode genericTreeNode = (GenericTreeNode) node;
			Object userObject = genericTreeNode.getUserObject();
			if (userObject instanceof Organisation) {
				Organisation organisation = (Organisation) userObject;
				organisations.add(organisation);
			}
			for (int i = 0; i < genericTreeNode.getChildCount(); i++) {
				organsiationTreeToList(organisations, genericTreeNode.getChildAt(i));
			}
		}
	}

	private static StringBuilder computeIntendentionForOrganisation(Organisation organisation, StringBuilder intendation) {
		Organisation parent = organisation.getParent();
		if (parent != null) {
			intendation = intendation.append(INTENDING).append(INTENDING).append(INTENDING).append(INTENDING);
			computeIntendentionForOrganisation(parent, intendation);
		}
		return intendation;
	}
	
	public static String getOrganisationKey(OrganisationRef organisationRef) {
		return String.valueOf(organisationRef.getKey());
	}

	public static OrganisationRef getOrganisationRef(String organisationKey) {
		if (StringHelper.containsNonWhitespace(organisationKey)) {
			try {
				Long key = Long.valueOf(organisationKey);
				return new OrganisationRefImpl(key);
			} catch (Exception e) {
				//
			}
		}
		return null;
	}
	
	public static SelectionValues getOrganisationSV(UserSession usess, List<Organisation> currentOrganisations) {
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		
		// Get all organisations of the user
		List<Organisation> userOrganisations = organisationService.getOrganisations(usess.getIdentity(), usess.getRoles(),
				OrganisationRoles.administrator, OrganisationRoles.qualitymanager);
		List<Organisation> allOrganisations = new ArrayList<>(userOrganisations);

		// Complete with the active organisations
		for(Organisation activeOrganisation:currentOrganisations) {
			if(activeOrganisation != null && !allOrganisations.contains(activeOrganisation)) {
				allOrganisations.add(activeOrganisation);
			}
		}
		
		return OrganisationUIFactory.createSelectionValues(allOrganisations);
	}
	
	public static List<OrganisationRef> getSelectedOrganisationRefs(MultiSelectionFilterElement organisationsEl) {
		return organisationsEl.getSelectedKeys().stream()
				.map(QualityUIFactory::getOrganisationRef)
				.collect(Collectors.toList());
	}
	
	public static List<Organisation> getSelectedOrganisations(MultiSelectionFilterElement organisationsEl,
			List<Organisation> currentOrganisations) {
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);

		// Copy the current organisations
		List<Organisation> organisations = new ArrayList<>(currentOrganisations);
		Collection<String> selectedOrganisationKeys = organisationsEl.getSelectedKeys();
		
		// Remove unselected organisations
		organisations.removeIf(organisation -> !selectedOrganisationKeys.contains(getOrganisationKey(organisation)));

		// Add newly selected organisations
		Collection<String> organisationKeys = organisations.stream()
				.map(QualityUIFactory::getOrganisationKey)
				.collect(Collectors.toList());
		for (String selectedOrganisationKey: selectedOrganisationKeys) {
			if (!organisationKeys.contains(selectedOrganisationKey)) {
				Organisation organisation = organisationService.getOrganisation(getOrganisationRef(selectedOrganisationKey));
				if (organisation != null) {
					organisations.add(organisation);
				}
			}
		}
		return organisations;
	}
	
	public static KeysValues getIdentityKeysValues(List<IdentityShort> identities) {
		List<IdentityShort> idents = new ArrayList<>(identities);
		idents.sort(IdentityComporatorFactory.createLastnameFirstnameShortComporator());
		String[] keys = new String[idents.size()];
		String[] values = new String[idents.size()];
		for (int i = idents.size(); i-->0; ) {
			IdentityShort identity = idents.get(i);
			keys[i] = getIdentityKey(identity);
			values[i] = new StringBuilder().append(identity.getLastName()).append(" ").append(identity.getFirstName()).toString();
		}
		return new KeysValues(keys, values);
	}

	public static String getIdentityKey(IdentityRef identityRef) {
		return Long.toString(identityRef.getKey());
	}

	public static IdentityRef getIdentityRef(String identityKey) {
		if (StringHelper.containsNonWhitespace(identityKey)) {
			try {
				Long key = Long.valueOf(identityKey);
				return new IdentityRefImpl(key);
			} catch (Exception e) {
				//
			}
		}
		return null;
	}

	public static KeysValues getRepositoryEntriesFlatKeysValues(List<RepositoryEntry> entries) {
		List<RepositoryEntry> repoEntries = new ArrayList<>(entries);
		repoEntries.sort(Comparator.comparing(RepositoryEntry::getDisplayname));
		String[] keys = new String[repoEntries.size()];
		String[] values = new String[repoEntries.size()];
		for (int i = repoEntries.size(); i-->0; ) {
			RepositoryEntry entry = repoEntries.get(i);
			keys[i] = getRepositoryEntryKey(entry);
			values[i] = entry.getDisplayname();
		}
		return new KeysValues(keys, values);
	}

	public static String getRepositoryEntryKey(RepositoryEntryRef entryRef) {
		return Long.toString(entryRef.getKey());
	}

	public static RepositoryEntryRef getRepositoryEntryRef(String entryKey) {
		if (StringHelper.containsNonWhitespace(entryKey)) {
			try {
				Long key = Long.valueOf(entryKey);
				return new RepositoryEntryRefImpl(key);
			} catch (Exception e) {
				//
			}
		}
		return null;
	}
	
	public static void addParentTaxonomyLevelNames(Translator translator, List<String> names, TaxonomyLevel level) {
		names.add(TaxonomyUIFactory.translateDisplayName(translator, level));
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentTaxonomyLevelNames(translator, names, parent);
		}
	}
	
	public static String getIntendedTaxonomyLevel(Translator translator, TaxonomyLevel level) {
		StringBuilder sb = new StringBuilder();
		computeIntendentionForTaxonomyLevel(sb, level);
		return sb.append(TaxonomyUIFactory.translateDisplayName(translator, level)).toString();
	}
	
	private static StringBuilder computeIntendentionForTaxonomyLevel(StringBuilder intendation, TaxonomyLevel level) {
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			intendation = intendation.append(INTENDING).append(INTENDING).append(INTENDING).append(INTENDING);
			computeIntendentionForTaxonomyLevel(intendation, parent);
		}
		return intendation;
	}
	
	public static String getTaxonomyLevelKey(TaxonomyLevelRef taxonomyLevelRef) {
		return Long.toString(taxonomyLevelRef.getKey());
	}

	public static TaxonomyLevelRef getTaxonomyLevelRef(String taxonomyLevelKey) {
		if (StringHelper.containsNonWhitespace(taxonomyLevelKey)) {
			try {
				Long key = Long.valueOf(taxonomyLevelKey);
				return new TaxonomyLevelRefImpl(key);
			} catch (Exception e) {
				//
			}
		}
		return null;
	}
	
	public static String toHtmlList(List<String> values) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (String value : values) {
			sb.append("<li>").append(value.trim()).append("</li>");
		}
		sb.append("</ul>");
		String value = sb.toString();
		return value;
	}
	
	public static String toHtmlList(SelectionValues selectionValues) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (SelectionValue selectionValue : selectionValues.keyValues()) {
			sb.append("<li>").append(selectionValue.getValue().trim()).append("</li>");
		}
		sb.append("</ul>");
		String value = sb.toString();
		return value;
	}
	
	public static boolean validateInteger(TextElement el, int min, int max) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				
				try {
					int value = Integer.parseInt(val);
					if(min > value) {
						el.setErrorKey("error.number.greater", new String[] {String.valueOf(min)});
						allOk = false;
					} else if(max < value) {
						el.setErrorKey("error.number.lower", new String[] {String.valueOf(max)});
						allOk = false;
					}
				} catch (NumberFormatException e) {
					el.setErrorKey("error.wrong.number", null);
					allOk = false;
				}
			}
		}
		return allOk;
	}
	
	public static boolean validateLong(TextElement el, long min, long max) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				
				try {
					long value = Long.parseLong(val);
					if(min > value) {
						el.setErrorKey("error.number.greater", new String[] {String.valueOf(min)});
						allOk = false;
					} else if(max < value) {
						el.setErrorKey("error.number.lower", new String[] {String.valueOf(max)});
						allOk = false;
					}
				} catch (NumberFormatException e) {
					el.setErrorKey("error.wrong.number", null);
					allOk = false;
				}
			}
		}
		return allOk;
	}
	
	public static boolean validateDouble(TextElement el, int min, int max) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				
				try {
					double value = Double.parseDouble(val);
					if(min > value) {
						el.setErrorKey("error.wrong.number", null);
						allOk = false;
					} else if(max < value) {
						el.setErrorKey("error.wrong.number", null);
						allOk = false;
					}
				} catch (NumberFormatException e) {
					el.setErrorKey("error.wrong.number", null);
					allOk = false;
				}
			}
		}
		return allOk;
	}
	
	public static boolean validateIsMandatory(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String value = el.getValue();
			if (!StringHelper.containsNonWhitespace(value)) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		return allOk;
	}
	
	public static boolean validateIsMandatory(SingleSelection el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			if (!el.isOneSelected()) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		return allOk;
	}

	public static boolean validateIsMandatory(MultipleSelectionElement el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			if (!el.isAtLeastSelected(1)) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		return allOk;
	}

	public static boolean validateIsMandatory(MultiSelectionFilterElement el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			if (el.getSelectedKeys().isEmpty()) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		return allOk;
	}
	
	public static boolean validateIsMandatory(DateChooser el) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			if (el.getDate() == null) {
				el.setErrorKey("form.mandatory.hover", null);
				allOk = false;
			}
		}
		return allOk;
	}

	public static boolean validateEmailAdresses(List<String> emailAddresses) {
		for (String emailAddress : emailAddresses) {
			boolean valid = MailHelper.isValidEmailAddress(emailAddress);
			if (!valid) {
				return false;
			}
		}
		return true;
	}
	
	public static class KeysValues {
		
		private final String[] keys;
		private final String[] values;
		
		protected KeysValues(String[] keys, String[] values) {
			super();
			this.keys = keys;
			this.values = values;
		}

		public String[] getKeys() {
			return keys;
		}

		public String[] getValues() {
			return values;
		}
		
	}
	
}
