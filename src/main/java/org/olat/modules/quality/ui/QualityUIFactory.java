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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.RepositoryEntryRefImpl;
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
	private static final Comparator<IdentityShort> IDENTITY_LAST_FIRST_COMPARATOR = new LastFirstNameComporator();
	
	public static String[] emptyArray() {
		return EMPTY_ARRAY;
	}
	
	public static String formatTopic(QualityDataCollectionView dataCollectionView) {
		return formatTopic(dataCollectionView.getTopicType(), dataCollectionView.getTranslatedTopicType(),
				dataCollectionView.getTopic());
	}

	public static String formatTopic(QualityExecutorParticipation qualityParticipation) {
		return formatTopic(qualityParticipation.getTopicType(), qualityParticipation.getTranslatedTopicType(),
				qualityParticipation.getTopic());
	}

	public static String formatTopic(QualityDataCollectionTopicType type, String translatedType, String topic) {
		StringBuilder formatedTopic = new StringBuilder();
		if (!CUSTOM.equals(type)) {
			formatedTopic.append(translatedType).append(" ");
		}
		formatedTopic.append(topic);
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
				.map(type -> type.getI18nKey())
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
				.map(Curriculum::getKey)
				.map(String::valueOf)
				.toArray(String[]::new);
		String[] values = curriculumsCopy.stream()
				.sorted(DISPLAY_NAME_COMPARATOR)
				.map(Curriculum::getDisplayName)
				.toArray(String[]::new);
		return new KeysValues(keys, values);
	}

	public static String getCurriculumKey(Curriculum curriculum) {
		return String.valueOf(curriculum.getKey());
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
			keys[i] = Long.toString(element.getKey());
			ArrayList<String> names = new ArrayList<>();
			addParentCurriculumElementNames(names, element);
			values[i] = String.join(FLAT_DELIMITER, names);
		}
		return new KeysValues(keys, values);
	}
	
	public static void addParentCurriculumElementNames(List<String> names, CurriculumElement curriculumElement) {
		names.add(curriculumElement.getDisplayName());
		CurriculumElement parent = curriculumElement.getParent();
		if (parent != null) {
			addParentCurriculumElementNames(names, parent);
		}
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
			keys[i] = Long.toString(element.getKey());
			values[i] = getCurriculumElementValue(element);
		}
		return new KeysValues(keys, values);
	}

	private static String getCurriculumElementValue(CurriculumElement element) {
		StringBuilder sb = computeIntendentionForCurriculumElement(element, new StringBuilder());
		if (StringHelper.containsNonWhitespace(element.getIdentifier())) {
			sb.append(element.getIdentifier()).append(": ");
		}
		sb.append(element.getDisplayName());
		return sb.toString();
	}
	
	private static void curriculumElementTreeToList(List<CurriculumElement> elements, INode node) {
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

	public static String getCurriculumElementKey(CurriculumElement curriculumElement) {
		return String.valueOf(curriculumElement.getKey());
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
	
	public static KeysValues getCurriculumElementTypeKeysValues(List<CurriculumElementType> types) {
		String[] keys = new String[types.size()];
		String[] values = new String[types.size()];
		for (int i = types.size(); i-->0; ) {
			CurriculumElementType type = types.get(i);
			keys[i] = Long.toString(type.getKey());
			values[i] = type.getDisplayName();
		}
		return new KeysValues(keys, values);
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
			keys[i] = Long.toString(organisation.getKey());
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
			keys[i] = Long.toString(organisation.getKey());
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
	
	static String getOrganisationKey(Organisation organisation) {
		return String.valueOf(organisation.getKey());
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
	
	public static void initOrganisations(UserSession usess, MultipleSelectionElement organisationsEl,
			List<Organisation> currentOrganisations) {
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
		
		// Sort the organisations
		Collections.sort(allOrganisations, new OrganisationNameComparator(usess.getLocale()));
		
		// Make the keys and values for the form element
		List<String> keyList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();
		for(Organisation elOrganisation:allOrganisations) {
			keyList.add(elOrganisation.getKey().toString());
			valueList.add(elOrganisation.getDisplayName());
		}
		
		// Update the for element and select the active organisations.
		organisationsEl.setKeysAndValues(keyList.toArray(new String[keyList.size()]),
				valueList.toArray(new String[valueList.size()]));
		for(Organisation reOrganisation:currentOrganisations) {
			if(keyList.contains(reOrganisation.getKey().toString())) {
				organisationsEl.select(reOrganisation.getKey().toString(), true);
			}
		}
	}
	
	public static List<OrganisationRef> getSelectedOrganisationRefs(MultipleSelectionElement organisationsEl) {
		return organisationsEl.getSelectedKeys().stream()
				.map(key -> getOrganisationRef(key))
				.collect(Collectors.toList());
	}
	
	public static List<Organisation> getSelectedOrganisations(MultipleSelectionElement organisationsEl,
			List<Organisation> currentOrganisations) {
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);

		// Copy the current organisations
		List<Organisation> organisations = new ArrayList<>(currentOrganisations);
		Collection<String> selectedOrganisationKeys = organisationsEl.getSelectedKeys();
		
		// Remove unselected organisations
		organisations.removeIf(organisation -> !selectedOrganisationKeys.contains(organisation.getKey().toString()));

		// Add newly selected organisations
		Collection<String> organisationKeys = organisations.stream()
				.map(Organisation::getKey)
				.map(String::valueOf)
				.collect(Collectors.toList());
		for (String selectedOrganisationKey: selectedOrganisationKeys) {
			if (!organisationKeys.contains(selectedOrganisationKey)) {
				Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(Long.valueOf(selectedOrganisationKey)));
				if (organisation != null) {
					organisations.add(organisation);
				}
			}
		}
		return organisations;
	}
	
	public static KeysValues getIdentityKeysValues(List<IdentityShort> identities) {
		List<IdentityShort> idents = new ArrayList<>(identities);
		idents.sort(IDENTITY_LAST_FIRST_COMPARATOR);
		String[] keys = new String[idents.size()];
		String[] values = new String[idents.size()];
		for (int i = idents.size(); i-->0; ) {
			IdentityShort identity = idents.get(i);
			keys[i] = Long.toString(identity.getKey());
			values[i] = new StringBuilder().append(identity.getLastName()).append(" ").append(identity.getFirstName()).toString();
		}
		return new KeysValues(keys, values);
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
			keys[i] = Long.toString(entry.getKey());
			values[i] = entry.getDisplayname();
		}
		return new KeysValues(keys, values);
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
	
	public static void addParentTaxonomyLevelNames(List<String> names, TaxonomyLevel level) {
		names.add(level.getDisplayName());
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentTaxonomyLevelNames(names, parent);
		}
	}
	
	public static String getIntendedTaxonomyLevel(TaxonomyLevel level) {
		StringBuilder sb = new StringBuilder();
		computeIntendentionForTaxonomyLevel(sb, level);
		return sb.append(level.getDisplayName()).toString();
	}
	
	private static StringBuilder computeIntendentionForTaxonomyLevel(StringBuilder intendation, TaxonomyLevel level) {
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			intendation = intendation.append(INTENDING).append(INTENDING).append(INTENDING).append(INTENDING);
			computeIntendentionForTaxonomyLevel(intendation, parent);
		}
		return intendation;
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
	
	private static final class LastFirstNameComporator implements Comparator<IdentityShort> {
		
		@Override
		public int compare(IdentityShort i1, IdentityShort i2) {
			// Compare last name...
			String lastName1 = i1.getLastName();
			String lastName2 = i2.getLastName();
			// nulls last
			if (lastName1 == null) return 1;
			if (lastName2 == null) return -1;
			
			int lastNameComp = lastName1.toLowerCase().compareTo(lastName2.toLowerCase());
			if (lastNameComp != 0) return lastNameComp;
			
			// ...and then the fist name
			String firstName1 = i1.getFirstName();
			String firstName2 = i2.getFirstName();
			// nulls last
			if (firstName1 == null) return 1;
			if (firstName2 == null) return -1;
			
			return firstName1.toLowerCase().compareTo(firstName2.toLowerCase());
		}
		
	}

}
