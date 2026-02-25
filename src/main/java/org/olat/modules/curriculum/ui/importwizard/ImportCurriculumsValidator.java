/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.importwizard;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.AbstractExcelReader;
import org.olat.core.util.openxml.AbstractExcelReader.ReaderLocalDate;
import org.olat.core.util.openxml.AbstractExcelReader.ReaderLocalTime;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.ValidationDescription;
import org.olat.login.validation.ValidationResult;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.ui.CurriculumExport;
import org.olat.modules.curriculum.ui.CurriculumExportType;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsValidator {
	
	private final Roles roles;
	private final Identity identity;
	private final Translator translator;
	
	public static final String ON = "ON";
	public static final String OFF = "OFF";
	public static final String DEFAULT = "DEFAULT";
	
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OrganisationService organisationService;
	
	public ImportCurriculumsValidator(Identity identity, Roles roles, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.roles = roles;
		this.identity = identity;
		this.translator = translator;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(CurriculumExport.usageIdentifyer, isAdministrativeUser)
				.stream()
				.filter(handler -> userManager.isMandatoryUserProperty(CurriculumExport.usageIdentifyer, handler))
				.toList();
	}

	public List<UserPropertyHandler> getUserPropertyHandlers() {
		return userPropertyHandlers;
	}
	
	public UserPropertyHandler getUserPropertyHandler(int index) {
		if(index >= 0 && userPropertyHandlers != null && index < userPropertyHandlers.size()) {
			return userPropertyHandlers.get(index);
		}
		return null;
	}
	
	private String translate(String i18nKey) {
		return translator.translate(i18nKey);
	}
	
	public void validateCurriculumsUniqueIdentifiers(List<ImportedRow> importedRows) {
		Map<String, ImportedRow> identifiersMap = new HashMap<>();
		for(ImportedRow row:importedRows) {
			String identifier = row.getIdentifier();
			if(identifiersMap.containsKey(identifier)) {
				notUniqueIdentifierError(row);
				notUniqueIdentifierError(identifiersMap.get(identifier));
			} else {
				identifiersMap.put(identifier, row);
			}
		}
	}
	
	public void validateUniqueIdentifiers(List<ImportedRow> importedRows) {
		// Validate uniqueness of implementations identifiers
		Map<String, ImportedRow> implementationsIdentifiersMap = new HashMap<>();
		for(ImportedRow row:importedRows) {
			String identifier = row.getIdentifier();
			if(row.type() == CurriculumExportType.IMPL) {
				if(implementationsIdentifiersMap.containsKey(identifier)) {
					notUniqueIdentifierError(row);
					notUniqueIdentifierError(implementationsIdentifiersMap.get(identifier));
				} else {
					implementationsIdentifiersMap.put(identifier, row);
				}
			}
		}
		
		// Validate uniqueness of identifiers within an implementation
		validateUniqueIdentifiersInImplementation(importedRows, CurriculumExportType.ELEM);
		validateUniqueIdentifiersInImplementation(importedRows, CurriculumExportType.EVENT);
		validateUniqueIdentifiersInImplementation(importedRows, CurriculumExportType.COURSE);
		validateUniqueIdentifiersInImplementation(importedRows, CurriculumExportType.TMPL);
	}

	private void validateUniqueIdentifiersInImplementation(List<ImportedRow> importedRows, CurriculumExportType type) {
		Map<Identifier, ImportedRow> identifiersMap = new HashMap<>();
		for(ImportedRow row:importedRows) {
			if(row.type() == type) {
				Identifier key = new Identifier(row.getImplementationIdentifier(), row.getIdentifier());
				if(identifiersMap.containsKey(key)) {
					notUniqueIdentifierError(row);
					notUniqueIdentifierError(identifiersMap.get(key));
				} else {
					identifiersMap.put(key, row);
				}
			}
		}
	}
	
	public void validateUsersUniqueUsernames(List<ImportedUserRow> importedRows) {
		Map<String, ImportedUserRow> identifiersMap = new HashMap<>();
		for(ImportedUserRow row:importedRows) {
			String username = row.getUsername();
			if(identifiersMap.containsKey(username)) {
				notUniqueIdentifierError(row);
				notUniqueIdentifierError(identifiersMap.get(username));
			} else {
				identifiersMap.put(username, row);
			}
		}
	}
	
	private void notUniqueIdentifierError(AbstractImportRow importedRow) {
		String column = translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
		importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translate("error.value.duplicate"));
	}
	
	public void validate(ImportedRow importedRow) {
		CurriculumExportType type = importedRow.type();
		if(type == CurriculumExportType.CUR) {
			validateCurriculumRow(importedRow);
		} else if(type == CurriculumExportType.IMPL || type == CurriculumExportType.ELEM) {
			validateCurriculumElementRow(importedRow);
		} else if(type == CurriculumExportType.COURSE) {
			validateCourseAndTemplateRow(importedRow, importedRow.getCourse());
		} else if(type == CurriculumExportType.TMPL) {
			validateCourseAndTemplateRow(importedRow, importedRow.getTemplate());
		} else if(type == CurriculumExportType.EVENT) {
			validateLectureBlockRow(importedRow);
		} else {
			unkownType(importedRow);
		}
		
		if(importedRow.getStatus() == null) {
			CurriculumImportedStatistics statistics = importedRow.getValidationStatistics();
			if(statistics.errors() > 0) {
				importedRow.setStatus(ImportCurriculumsStatus.ERROR);
			} else if(statistics.changes() > 0) {
				importedRow.setStatus(ImportCurriculumsStatus.MODIFIED);
			} else {
				importedRow.setStatus(ImportCurriculumsStatus.NO_CHANGES);
			}
		}
	}
	
	public void validate(ImportedUserRow importedRow) {
		// User properties inclusive username
		for(int i=0; i<userPropertyHandlers.size(); i++) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			String value = importedRow.getIdentityProp(i);
			
			User user = importedRow.getIdentity() == null
					? null
					: importedRow.getIdentity().getUser();
			
			String currentValue = user == null
					? null
					: handler.getUserProperty(user, translator.getLocale());

			ValidationError validationError = new ValidationError();
			String column = translate(handler.i18nColumnDescriptorLabelKey());
			
			if(validateMandatory(importedRow, value, handler)
					&& validateLength(importedRow, value, 128, handler)) {
			
				if(UserConstants.NICKNAME.equals(handler.getName())) {
					if(importedRow.getIdentity() == null) {
						ValidationResult result = olatAuthManager.validateAuthenticationUsername(value, "OLAT", importedRow.getIdentity());
						if(!result.isValid() && result.getInvalidDescriptions() != null) {
							List<ValidationDescription> errors = result.getInvalidDescriptions();
							String description = validationDescriptionToString(errors);
							importedRow.addValidationError(handler.getName(), column, null, description);
						}
					}
				} else if(!handler.isValidValue(user, value, validationError, translator.getLocale())) {
					String description = translate(validationError.getErrorKey());
					importedRow.addValidationError(handler.getName(), column, null, description);
				} else if((handler.getName().equals(UserConstants.INSTITUTIONALEMAIL) && StringHelper.containsNonWhitespace(value)) 
						|| handler.getName().equals(UserConstants.EMAIL)) {
					if (!userManager.isEmailAllowed(value, user)) {
						String description = translator.translate("form.name." + handler.getName() + ".error.exists", new String[] { value });
						importedRow.addValidationError(handler.getName(), column, null, description);
					}
				} else if(currentValue != null && !Objects.equals(currentValue, value)) {
					importedRow.addValidationWarning(handler.getName(), column, null,
							translator.translate("warning.change.ignore", value));
				}
			}
		}
		
		// Organisation
		if(validateMandatory(importedRow, importedRow.getOrganisationIdentifier(), ImportCurriculumsCols.organisationIdentifier)) {
			String organisationColumn = translate(ImportCurriculumsCols.organisationIdentifier.i18nHeaderKey());
			if(importedRow.getOrganisation() == null) {
				// Has an organisation identifier
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						null, translator.translate("error.not.exist", importedRow.getOrganisationIdentifier()));
			} else if(!hasOrganisationPermission(importedRow.getOrganisation())) {
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						null, translate("error.permissions"));
			} else if(importedRow.getIdentity() != null) {
				List<Organisation> currentOrganisations = organisationService.getOrganisations(importedRow.getIdentity(), OrganisationRoles.user);
				if(!currentOrganisations.contains(importedRow.getOrganisation())) {
					importedRow.addValidationWarning(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
							null, translate("error.no.update"));
				}
			}
		}
		
		if(importedRow.getStatus() == null) {
			CurriculumImportedStatistics statistics = importedRow.getValidationStatistics();
			if(statistics.errors() > 0) {
				importedRow.setStatus(ImportCurriculumsStatus.ERROR);
			} else if(statistics.changes() > 0) {
				importedRow.setStatus(ImportCurriculumsStatus.MODIFIED);
			} else {
				importedRow.setStatus(ImportCurriculumsStatus.NO_CHANGES);
			}
		}
	}
	
	public void validatePassword(ImportedUserRow importedRow) {
		if(!StringHelper.containsNonWhitespace(importedRow.getPassword())) return;
		
		if(importedRow.getIdentity() == null) {
			TransientIdentity newIdentity = new TransientIdentity();
			newIdentity.setName(importedRow.getUsername());
			ValidationResult validationResult = olatAuthManager.createPasswordSytaxValidator().validate(importedRow.getPassword(), newIdentity);
			if(!validationResult.isValid()) {
				String column = translate(ImportCurriculumsCols.password.i18nHeaderKey());
				String description = validationDescriptionToString(validationResult.getInvalidDescriptions());
				importedRow.addValidationError(ImportCurriculumsCols.password, column,
						null, description);
			}
		} else if(StringHelper.containsNonWhitespace(importedRow.getPassword())) {
			String column = translate(ImportCurriculumsCols.password.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.password, column,
					null, translate("error.password.user.exists"));
		}
	}
	
	public void validate(ImportedMembershipRow importedRow) {
		// Curriculum / implementations
		validateCurriculumAndImplementation(importedRow);
		
		// User name
		if(validateMandatory(importedRow, importedRow.getUsername(), ImportCurriculumsCols.username)) {
			String sheet = translate("export.users");
			String column = translate(ImportCurriculumsCols.username.i18nHeaderKey());
			if(importedRow.getUserRow() == null) {
				importedRow.addValidationError(ImportCurriculumsCols.username, column, null,
						translator.translate("error.not.exist.sheet", sheet));
			} else if(importedRow.getUserRow().hasValidationErrors()) {
				importedRow.addValidationError(ImportCurriculumsCols.username, column,
						null, translator.translate("error.precent.errors", sheet));
			} else if(importedRow.getUserRow().isIgnored()) {
				importedRow.addValidationError(ImportCurriculumsCols.username, column,
						null, translator.translate("error.ignored.in.sheet", sheet));
			}
		}
		
		// Role
		if(validateMandatory(importedRow, importedRow.getRole(), ImportCurriculumsCols.role)
				&& validateRole(importedRow, importedRow.getRole(), ImportCurriculumsCols.role)) {
			//
		}
		
		if(importedRow.getStatus() == null) {
			CurriculumImportedStatistics statistics = importedRow.getValidationStatistics();
			if(statistics.errors() > 0) {
				importedRow.setStatus(ImportCurriculumsStatus.ERROR);
			} else {
				importedRow.setStatus(ImportCurriculumsStatus.NO_CHANGES);
			}
		}
	}
	
	private void validateCurriculumElementRow(ImportedRow importedRow) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
		
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculumElement().getDisplayName(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);
		
		// Level only for element
		if(importedRow.type() == CurriculumExportType.ELEM) {
			validateLevel(importedRow);
		}
		
		// Curriculum
		if(importedRow.getCurriculum() != null && importedRow.getCurriculumElement() != null
				&& !importedRow.getCurriculum().equals(importedRow.getCurriculumElement().getCurriculum())) {
			String column = translate(ImportCurriculumsCols.curriculumIdentifier.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.curriculumIdentifier, column, null,
					translator.translate("error.no.update", importedRow.getCurriculumIdentifier()));
		} else if(importedRow.type() == CurriculumExportType.ELEM
				&& importedRow.getImplementation() != null && importedRow.getCurriculumElement() != null
				&& !importedRow.getImplementation().equals(importedRow.getCurriculumElement().getImplementation())) {
			String column = translate(ImportCurriculumsCols.implementationIdentifier.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, column, null,
					translator.translate("error.no.update", importedRow.getImplementationIdentifier()));
		}
		
		// Curriculum element type
		if(validateMandatory(importedRow, importedRow.getCurriculumElementTypeIdentifier(), ImportCurriculumsCols.elementType)) {
			String column = translate(ImportCurriculumsCols.elementType.i18nHeaderKey());
			if(importedRow.getCurriculumElementType() == null) {
				importedRow.addValidationError(ImportCurriculumsCols.elementType, column, importedRow.getCurriculumElementTypeIdentifier(),
						translator.translate("error.not.exist", importedRow.getCurriculumElementTypeIdentifier()));
			} else if(!importedRow.isNew() && !Objects.equals(importedRow.getCurriculumElement().getType(), importedRow.getCurriculumElementType())) {
				importedRow.addValidationError(ImportCurriculumsCols.elementType, column, null,
						translate("error.no.update"));
			} else {
				validateCurriculumElementStructuralType(importedRow, importedRow.getCurriculumElementType());
			}
		}
		
		// Status
		if(validateMandatory(importedRow, importedRow.getElementStatus(), ImportCurriculumsCols.elementStatus)) {
			if(!validateCurriculumElementStatus(importedRow.getElementStatus())) {
				String column = translate(ImportCurriculumsCols.elementStatus.i18nHeaderKey());
				if(importedRow.isNew()) {
					importedRow.addValidationError(ImportCurriculumsCols.elementStatus, column,
							importedRow.getElementStatus(), translator.translate("warning.value.not.supported", importedRow.getElementStatus()));
				} else {
					importedRow.addValidationWarning(ImportCurriculumsCols.elementStatus, column,
							importedRow.getElementStatus(), translator.translate("warning.value.not.supported.ignore", importedRow.getElementStatus()));
				}
			}
		}
		
		// Dates
		if(validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& importedRow.getStartDate() != null && !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculumElement().getBeginDate(), importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		validateEmpty(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime);
		
		if(validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& importedRow.getEndDate() != null && !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculumElement().getEndDate(), importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		validateEmpty(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime);
		
		// Absences / calendar / progress
		if(validateMandatory(importedRow, importedRow.getAbsences(), ImportCurriculumsCols.absences)
				&& validateOnOff(importedRow, ImportCurriculumsCols.absences, importedRow.getAbsences())
				&& !importedRow.isNew()) {
			changes(importedRow, toExcelFormat(importedRow.getCurriculumElement().getLectures()), importedRow.getAbsences(), ImportCurriculumsCols.absences);
		}
		if(validateMandatory(importedRow, importedRow.getCalendar(), ImportCurriculumsCols.calendar)
				&& validateOnOff(importedRow, ImportCurriculumsCols.calendar, importedRow.getCalendar())
				&& !importedRow.isNew()) {
			changes(importedRow, toExcelFormat(importedRow.getCurriculumElement().getCalendars()), importedRow.getCalendar(), ImportCurriculumsCols.calendar);
		}
		if(validateMandatory(importedRow, importedRow.getProgress(), ImportCurriculumsCols.progress)
				&& validateOnOff(importedRow, ImportCurriculumsCols.progress, importedRow.getProgress())
				&& !importedRow.isNew()) {
			changes(importedRow, toExcelFormat(importedRow.getCurriculumElement().getLearningProgress()), importedRow.getProgress(), ImportCurriculumsCols.progress);
		}
		
		// Taxonomy
		validateTaxonomy(importedRow);
		
		// Last modified
		validateLastModified(importedRow, importedRow.getCurriculumElement());
	}
	
	
	private boolean validateCurriculumElementStructuralType(ImportedRow importedRow, CurriculumElementType type) {
		boolean allOk = true;

		String column = translate(ImportCurriculumsCols.elementType.i18nHeaderKey());
		if(!type.isAllowedAsRootElement() && importedRow.type() == CurriculumExportType.IMPL) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.impletation.not.allowed"));
			allOk &= false;
		} else if(type.getMaxRepositoryEntryRelations() == 0
				&& importedRow.getNumResources(CurriculumExportType.COURSE) + importedRow.getNumResources(CurriculumExportType.TMPL) > 0) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.content.not.allowed"));
			allOk &= false;
		} else if(type.getMaxRepositoryEntryRelations() == 1
				&& importedRow.getNumResources(CurriculumExportType.COURSE) + importedRow.getNumResources(CurriculumExportType.TMPL) > 1) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.content.one.allowed"));
			allOk &= false;
		} else if(type.getMaxRepositoryEntryRelations() == -1 && importedRow.getNumResources(CurriculumExportType.TMPL) > 0) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.content.no.template"));
			allOk &= false;
		} else if(type.isSingleElement() && importedRow.getNumOfSubCurriculumElements() > 0) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.no.sub.elements"));
			allOk &= false;
		} else if(importedRow.getCurriculumElementParentRow() != null
				&& importedRow.getCurriculumElementParentRow().getCurriculumElementType() != null
				&& !matchSubType(importedRow.getCurriculumElementParentRow().getCurriculumElementType(), type)) {
			importedRow.addValidationError(ImportCurriculumsCols.elementType, column,
					null, translate("error.no.sub.element.of.type"));
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean matchSubType(CurriculumElementType parentType, CurriculumElementType type) {
		// Don't replace with anyMatch, useful for debugging
		List<CurriculumElementType> types = parentType.getAllowedSubTypes().stream()
				.map(CurriculumElementTypeToType::getAllowedSubType)
				.toList();
		return types.contains(type);
	}
	
	private void validateCourseAndTemplateRow(ImportedRow importedRow, RepositoryEntry entry) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
		
		// Level
		validateLevel(importedRow);

		// Identifier
		validateIdentifier(importedRow);
		
		// Identifier matches a repository entry
		if(entry == null) {
			String column = translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translator.translate("error.not.exist", importedRow.getIdentifier()));
		} else {
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(identity, roles, entry);
			if(!reSecurity.isAdministrativeUser()) {
				String column = translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
				importedRow.addValidationError(ImportCurriculumsCols.identifier, column, null, translate("error.permissions"));
			}
		}
		
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& entry != null) {
			String column = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, entry.getDisplayname(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		// Object type matches repository entry runtime type
		if(entry != null && ((importedRow.type() == CurriculumExportType.TMPL && entry.getRuntimeType() != RepositoryEntryRuntimeType.template)
				|| (importedRow.type() == CurriculumExportType.COURSE && entry.getRuntimeType() != RepositoryEntryRuntimeType.curricular))) {
			String column = translate(ImportCurriculumsCols.objectType.i18nHeaderKey());
			importedRow.addValidationError(ImportCurriculumsCols.objectType, column, null, translate("error.wrong.runtime.type"));
		}

		// Dates
		if(validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& importedRow.getStartDate() != null && !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			Date from = entry == null || entry.getLifecycle() == null ? null : entry.getLifecycle().getValidFrom();
			importedRow.addChanged(column, from, importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		validateEmpty(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime);
		if(validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& importedRow.getEndDate() != null && !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			Date to = entry == null || entry.getLifecycle() == null ? null : entry.getLifecycle().getValidTo();
			importedRow.addChanged(column, to, importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		validateEmpty(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime);
				
		// Status
		if(validateMandatory(importedRow, importedRow.getElementStatus(), ImportCurriculumsCols.elementStatus)) {
			if(!validateRepositoryEntryStatus(importedRow.getElementStatus())) {
				String column = translate(ImportCurriculumsCols.elementStatus.i18nHeaderKey());
				if(importedRow.isNew()) {
					importedRow.addValidationError(ImportCurriculumsCols.elementStatus, column,
							importedRow.getElementStatus(), translator.translate("warning.value.not.supported", importedRow.getElementStatus()));
				} else {
					importedRow.addValidationWarning(ImportCurriculumsCols.elementStatus, column,
							importedRow.getElementStatus(), translator.translate("warning.value.not.supported.ignore", importedRow.getElementStatus()));
				}
			}
		}
		
		// Taxonomy
		validateTaxonomy(importedRow);
		
		// Last modified
		validateLastModified(importedRow, entry);
	}
	
	private void validateLectureBlockRow(ImportedRow importedRow) {
		// Curriculum and implementation
		validateCurriculumAndImplementation(importedRow);
		
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getTitle(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);

		// Reference to course
		String referenceColumn = translate(ImportCurriculumsCols.referenceIdentifier.i18nHeaderKey());
		if(!StringHelper.containsNonWhitespace(importedRow.getReferenceExternalRef())) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					translate("error.no.value"), translate("error.value.required"));
		} else if(importedRow.getCourse() == null) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					null, translator.translate("error.not.exist", importedRow.getReferenceExternalRef()));
		} else if(importedRow.getLectureBlock() != null && importedRow.getCourse() != null
				&& !Objects.equals(importedRow.getLectureBlock().getEntry(), importedRow.getCourse())) {
			importedRow.addValidationError(ImportCurriculumsCols.referenceIdentifier, referenceColumn,
					null, translate("error.no.update"));
		}
		
		// Dates: dates are mandatory, need a valid format, and end date must be after the start
		if(validateMandatory(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate)
				&& validateDate(importedRow, importedRow.getStartDate(), ImportCurriculumsCols.startDate, true)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.startDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getStartDate(), importedRow.getStartDate().date(), ImportCurriculumsCols.startDate);
		}
		if(validateMandatory(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime)
				&& validateTime(importedRow, importedRow.getStartTime(), ImportCurriculumsCols.startTime, true)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.startTime.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getStartDate(), importedRow.getStartTime().time(), ImportCurriculumsCols.startTime);
		}
		if(validateMandatory(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate)
				&& validateDate(importedRow, importedRow.getEndDate(), ImportCurriculumsCols.endDate, true)
				&& validateAfter(importedRow, ImportCurriculumsCols.endDate)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.endDate.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getEndDate(), importedRow.getEndDate().date(), ImportCurriculumsCols.endDate);
		}
		if(validateMandatory(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime)
				&& validateTime(importedRow, importedRow.getEndTime(), ImportCurriculumsCols.endTime, true)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.endTime.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getLectureBlock().getEndDate(), importedRow.getEndTime().time(), ImportCurriculumsCols.endTime);
		}
		
		// Unit
		if(StringHelper.containsNonWhitespace(importedRow.getLocation())
				&& validatePlannedLectures(importedRow, importedRow.getUnit(), ImportCurriculumsCols.unit)
				&& !importedRow.isNew()) {
			String col = translate(ImportCurriculumsCols.unit.i18nHeaderKey());
			importedRow.addChanged(col, Integer.toString(importedRow.getLectureBlock().getPlannedLecturesNumber()), importedRow.getUnit(), ImportCurriculumsCols.unit);
		}
		
		// Location
		if(StringHelper.containsNonWhitespace(importedRow.getLocation())
				&& validateTruncateLength(importedRow, importedRow.getLocation(), 128, ImportCurriculumsCols.location)
				&& !importedRow.isNew()) {
			String col = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(col, importedRow.getLectureBlock().getLocation(), importedRow.getLocation(), ImportCurriculumsCols.location);
		}
		
		// Taxonomy
		validateTaxonomy(importedRow);
		
		// Last modified
		validateLastModified(importedRow, importedRow.getImplementation());
	}
	
	private void validateCurriculumRow(ImportedRow importedRow) {
		// Display name / title
		if(validateMandatory(importedRow, importedRow.getDisplayName(), ImportCurriculumsCols.displayName)
				&& validateTruncateLength(importedRow, importedRow.getDisplayName(), 255, ImportCurriculumsCols.displayName)
				&& !importedRow.isNew()) {
			String column = translate(ImportCurriculumsCols.displayName.i18nHeaderKey());
			importedRow.addChanged(column, importedRow.getCurriculum().getDisplayName(), importedRow.getDisplayName(), ImportCurriculumsCols.displayName);
		}
		
		validateIdentifier(importedRow);
		
		// Absences
		if(validateMandatory(importedRow, importedRow.getAbsences(), ImportCurriculumsCols.absences)
				&& validateOnOff(importedRow, ImportCurriculumsCols.absences, importedRow.getAbsences())
				&& !importedRow.isNew()) {
			changes(importedRow, importedRow.getCurriculum().isLecturesEnabled(), "ON".equalsIgnoreCase(importedRow.getAbsences()), ImportCurriculumsCols.absences);
		}

		// Description
		if(importedRow.getCurriculum() != null) {
			String descriptionColumn = translate(ImportCurriculumsCols.description.i18nHeaderKey());
			importedRow.addChanged(descriptionColumn, importedRow.getCurriculum().getDescription(), importedRow.getDescription(), ImportCurriculumsCols.description);
		}
		
		// Organisation
		if(validateMandatory(importedRow, importedRow.getOrganisationIdentifier(), ImportCurriculumsCols.organisationIdentifier)) {
			String organisationColumn = translate(ImportCurriculumsCols.organisationIdentifier.i18nHeaderKey());
			if(importedRow.getOrganisation() == null) {
				String placeholder = StringHelper.containsNonWhitespace(importedRow.getOrganisationIdentifier())
						? null
						: translate("error.no.value");
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						placeholder, translate("error.value.required"));
			} else if(!hasOrganisationPermission(importedRow.getOrganisation())) {
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						null, translate("error.permissions"));
			} else if(importedRow.getCurriculum() != null && !Objects.equals(importedRow.getCurriculum().getOrganisation(), importedRow.getOrganisation())) {
				importedRow.addValidationError(ImportCurriculumsCols.organisationIdentifier, organisationColumn,
						null, translate("error.no.update"));
			}
		}
		
		// Last modified
		validateLastModified(importedRow, importedRow.getCurriculum());
	}
	
	/**
	 * Validate the curriculum identifier / external ref. and the implementation identifier
	 * / external ref. which are mandatory for all elements.
	 * @param importedRow The row
	 */
	private void validateCurriculumAndImplementation(ImportedRow importedRow) {
		if(validateMandatory(importedRow, importedRow.getCurriculumIdentifier(), ImportCurriculumsCols.curriculumIdentifier)) {
			if(importedRow.getCurriculum() == null &&  importedRow.getCurriculumRow() == null) {
				String column = translate(ImportCurriculumsCols.curriculumIdentifier.i18nHeaderKey());
				importedRow.addValidationError(ImportCurriculumsCols.curriculumIdentifier, column,
						null, translator.translate("error.not.exist", importedRow.getCurriculumIdentifier()));
			}
		}
		
		if(validateMandatory(importedRow, importedRow.getImplementationIdentifier(), ImportCurriculumsCols.implementationIdentifier)) {
			if(importedRow.type() == CurriculumExportType.IMPL) {
				if(!Objects.equals(importedRow.getImplementationIdentifier(), importedRow.getIdentifier())) {
					importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, translate(ImportCurriculumsCols.implementationIdentifier.i18nHeaderKey()),
							null, translator.translate("error.impletation.identifiers.doesnt.match", importedRow.getIdentifier()));
				}
			} else if(importedRow.getImplementation() == null && importedRow.getImplementationRow() == null) {
				String column = translate(ImportCurriculumsCols.implementationIdentifier.i18nHeaderKey());
				importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, column,
						null, translator.translate("error.not.exist", importedRow.getImplementationIdentifier()));
			}
		}
	}
	
	private void validateCurriculumAndImplementation(ImportedMembershipRow importedRow) {
		if(validateMandatory(importedRow, importedRow.getCurriculumIdentifier(), ImportCurriculumsCols.curriculumIdentifier)) {
			String sheet = translate("export.products");
			String column = translate(ImportCurriculumsCols.curriculumIdentifier.i18nHeaderKey());
			if(importedRow.getCurriculumRow() == null) {
				importedRow.addValidationError(ImportCurriculumsCols.curriculumIdentifier, column,
						null, translator.translate("error.not.exist", importedRow.getCurriculumIdentifier()));
			} else if(importedRow.getCurriculumRow().hasValidationErrors()) {
				importedRow.addValidationError(ImportCurriculumsCols.curriculumIdentifier, column,
						null, translator.translate("error.precent.errors", sheet));
			} else if(importedRow.getCurriculumRow().isIgnored()) {
				importedRow.addValidationError(ImportCurriculumsCols.curriculumIdentifier, column,
						null, translator.translate("error.ignored.in.sheet", sheet));
			} 
		}
		
		if(validateMandatory(importedRow, importedRow.getImplementationIdentifier(), ImportCurriculumsCols.implementationIdentifier)) {
			String sheet = translate("export.implementations");
			String column = translate(ImportCurriculumsCols.implementationIdentifier.i18nHeaderKey());
			if(importedRow.getImplementationRow() == null) {
				importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, column,
						null, translator.translate("error.not.exist", importedRow.getImplementationIdentifier()));
			} else if(importedRow.getImplementationRow().hasValidationErrors()) {
				importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, column,
						null, translator.translate("error.precent.errors", sheet));
			} else if(importedRow.getImplementationRow().isIgnored()) {
				importedRow.addValidationError(ImportCurriculumsCols.implementationIdentifier, column,
						null, translator.translate("error.ignored.in.sheet", sheet));
			} 
		}
		
		if(validateMandatory(importedRow, importedRow.getImplementationIdentifier(), ImportCurriculumsCols.identifier)) {
			String sheet = translate("export.implementations");
			String column = translate(ImportCurriculumsCols.identifier.i18nHeaderKey());
			if(importedRow.getImplementationRow() == null) {
				importedRow.addValidationError(ImportCurriculumsCols.identifier, column,
						null, translator.translate("error.not.exist", importedRow.getImplementationIdentifier()));
			} else if(importedRow.getImplementationRow().hasValidationErrors()) {
				importedRow.addValidationError(ImportCurriculumsCols.identifier, column,
						null, translator.translate("error.precent.errors", sheet));
			} else if(importedRow.getImplementationRow().isIgnored()) {
				importedRow.addValidationError(ImportCurriculumsCols.identifier, column,
						null, translator.translate("error.ignored.in.sheet", sheet));
			} 
		}
	}
	
	private boolean validateIdentifier(ImportedRow importedRow) {
		// Identifier / external ref.
		boolean allOk = validateMandatory(importedRow, importedRow.getIdentifier(), ImportCurriculumsCols.identifier);
		allOk &= validateLength(importedRow, importedRow.getIdentifier(), 255, ImportCurriculumsCols.identifier);
		return allOk;
	}
	
	private void validateLevel(ImportedRow importedRow) {
		// Identifier / external ref.
		validateMandatory(importedRow, importedRow.getLevel(), ImportCurriculumsCols.level);
	}
	
	private boolean hasOrganisationPermission(Organisation organisation) {
		return roles
				.getOrganisationsWithRoles(OrganisationRoles.curriculummanager, OrganisationRoles.administrator)
				.stream()
				.anyMatch(ref -> ref.getKey().equals(organisation.getKey()));
	}
	
	private boolean validateLastModified(ImportedRow importedRow, ModifiedInfo object) {
		boolean allOk = true;
		if(object != null && importedRow.getLastModified() != null) {
			LocalDateTime currentDate = DateUtils.toLocalDateTime(object.getLastModified())
					.withSecond(0).withNano(0);
			LocalDateTime date = importedRow.getLastModified()
					.withSecond(0).withNano(0);
			
			if(date.isBefore(currentDate)) {
				String column = translate(ImportCurriculumsCols.lastModified.i18nHeaderKey());
				importedRow.addValidationWarning(ImportCurriculumsCols.lastModified, column, null,
						translate("warning.last.modified"));
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private boolean validateTaxonomy(ImportedRow importedRow) {
		boolean allOk = true;
		
		String subjects = importedRow.getSubjects();
		List<String> subjectsList = importedRow.getSubjectsList();
		List<String> currentPaths = ImportCurriculumsObjectsLoader.loadTaxonomyLevels(importedRow);
		String column = translate(ImportCurriculumsCols.taxonomyLevels.i18nHeaderKey());
		
		for(String subject:subjectsList) {
			if(importedRow.getTaxonomyLevel(subject) == null) {
				if(importedRow.isNew()) {
					importedRow.addValidationError(ImportCurriculumsCols.taxonomyLevels, column, null,
							translator.translate("error.not.exist", subjects));
					allOk &= false;
				} else {
					importedRow.addValidationWarning(ImportCurriculumsCols.taxonomyLevels, column, null,
							translator.translate("warning.not.exist.ignore", subjects));
					allOk &= false;
				}
				break;// only one error per field
			}
		}
		
		if(allOk && (!subjectsList.isEmpty() || !currentPaths.isEmpty())) {
			importedRow.addChanged(column, currentPaths, subjectsList, ImportCurriculumsCols.taxonomyLevels);
		}
		return allOk;
	}
	
	private boolean validateRole(AbstractImportRow importedRow, String val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(CurriculumExport.parseRole(val) == null) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translator.translate("warning.value.not.supported", val));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(ImportedUserRow importedRow, String val, UserPropertyHandler handler) {
		boolean allOk = true;
		if(!StringHelper.containsNonWhitespace(val)) {
			String column = translate(handler.i18nColumnDescriptorLabelKey());
			importedRow.addValidationError(handler.getName(), column, translate("error.no.value"), translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(AbstractImportRow importedRow, String val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(!StringHelper.containsNonWhitespace(val)) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translate("error.no.value"), translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(AbstractImportRow importedRow, ReaderLocalDate val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null && val.date() == null && !StringHelper.containsNonWhitespace(val.val())) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translate("error.no.value"), translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateMandatory(AbstractImportRow importedRow, ReaderLocalTime val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null && val.time() == null && !StringHelper.containsNonWhitespace(val.val())) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, translate("error.no.value"), translate("error.value.required"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateEmpty(AbstractImportRow importedRow, Object val, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(val != null) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translate("error.value.must.be.empty"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateLength(ImportedRow importedRow, String val, int maxLength, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translate("error.value.length"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateLength(ImportedUserRow importedRow, String val, int maxLength, UserPropertyHandler handler) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			String column = translate(handler.i18nColumnDescriptorLabelKey());
			importedRow.addValidationError(handler.getName(), column, null, translate("error.value.length"));
			allOk &= false;
		}
		return allOk;
	} 
	
	private boolean validateDate(ImportedRow importedRow, ReaderLocalDate val, ImportCurriculumsCols col, boolean warningForCurrent) {
		boolean allOk = true;
		if(val != null && val.date() == null && StringHelper.containsNonWhitespace(val.val())) {
			String column = translate(col.i18nHeaderKey());
			if(!importedRow.isNew() && warningForCurrent) {
				importedRow.addValidationWarning(col, column, null, translator.translate("warning.format.invalid.ignore", val.val()));
			} else {
				importedRow.addValidationError(col, column, null, translator.translate("error.format.invalid", val.val()));
			}
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateTime(ImportedRow importedRow, ReaderLocalTime val, ImportCurriculumsCols col, boolean warningForCurrent) {
		boolean allOk = true;
		if(val != null && val.time() == null && StringHelper.containsNonWhitespace(val.val())) {
			String column = translate(col.i18nHeaderKey());
			if(!importedRow.isNew() && warningForCurrent) {
				importedRow.addValidationWarning(col, column, null, translator.translate("warning.format.invalid.ignore", val.val()));
			} else {
				importedRow.addValidationError(col, column, null, translator.translate("error.format.invalid", val.val()));
			}
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateAfter(ImportedRow importedRow, ImportCurriculumsCols col) {
		boolean allOk = true;
		Date first = AbstractExcelReader.toDate(importedRow.getStartDate(), importedRow.getStartTime());
		Date second = AbstractExcelReader.toDate(importedRow.getEndDate(), importedRow.getEndTime());
		if(first != null && second != null && first.compareTo(second) > 0) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationError(col, column, null, translator.translate("error.invalid.period"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validatePlannedLectures(ImportedRow importedRow, String val, ImportCurriculumsCols col) {
		boolean allOk = true;
		
		if(!StringHelper.isLong(val)) {
			allOk &= false;
		} else {
			int plannedLectures = Integer.parseInt(val);
			if(plannedLectures < 0 || plannedLectures > LectureBlock.MAX_PLANNED_LECTURES) {
				allOk &= false;
			}
		}
		
		if(!allOk) {
			String column = translate(col.i18nHeaderKey());
			if(val == null) {
				val = "";
			}
			if(importedRow.isNew()) {
				importedRow.addValidationError(col, column, null, translator.translate("warning.value.not.supported", val));
			} else {
				importedRow.addValidationWarning(col, column, null, translator.translate("warning.value.not.supported", val));
			}
		}
		
		return allOk;
	}
	
	private boolean validateTruncateLength(ImportedRow importedRow, String val, int maxLength, ImportCurriculumsCols col) {
		boolean allOk = true;
		if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			String column = translate(col.i18nHeaderKey());
			importedRow.addValidationWarning(col, column, null, translator.translate("warning.value.truncate"));
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateOnOff(ImportedRow importedRow, ImportCurriculumsCols optionColumn, String val) {
		boolean allOk = true;
		
		if(!ImportCurriculumsValidator.ON.equalsIgnoreCase(val)
				&& !ImportCurriculumsValidator.OFF.equalsIgnoreCase(val)
				&& !ImportCurriculumsValidator.DEFAULT.equals(val)) {
			
			String message = translator.translate("warning.value.not.supported", val);
			String column = translate(optionColumn.i18nHeaderKey());
			if(importedRow.isNew()) {
				importedRow.addValidationError(optionColumn, column, null, message);	
			} else {
				importedRow.addValidationWarning(optionColumn, column, null, message);
			}
			
			allOk &= false;
		}
		return allOk;
	}
	
	private void unkownType(ImportedRow importedRow) {
		String column = translate(ImportCurriculumsCols.objectType.i18nHeaderKey());
		if(StringHelper.containsNonWhitespace(importedRow.getRawType())) {
			importedRow.addValidationError(ImportCurriculumsCols.objectType, column, importedRow.getRawType(),
					translator.translate("warning.value.not.supported", importedRow.getRawType()));
		} else {
			importedRow.addValidationError(ImportCurriculumsCols.objectType, column,
					translate("error.no.value"), translate("error.value.required"));
		}
	}
	
	private void changes(ImportedRow importedRow, Object currentValue, Object newValue, ImportCurriculumsCols col) {
		String column = translate(col.i18nHeaderKey());
		importedRow.addChanged(column, currentValue, newValue, col);
	}
	
	private String validationDescriptionToString(List<ValidationDescription> errors) {
		if(errors == null || errors.isEmpty()) return "";
		return errors.stream()
					.map(d -> d.getText(translator.getLocale()))
					.collect(Collectors.joining("; "));
	}
	
	private String toExcelFormat(Enum<?> val) {
		if(val == null) return null;
		
		if("enabled".equals(val.name())) {
			return "ON";
		}
		if("disabled".equals(val.name())) {
			return "OFF";
		}
		if("inherited".equals(val.name())) {
			return "DEFAULT";
		}	
		return null;
	}
	
	private static boolean validateCurriculumElementStatus(String val) {
		boolean ok = false;
		if(StringHelper.containsNonWhitespace(val)) {
			for(CurriculumElementStatus status:CurriculumElementStatus.values()) {
				if(status.name().equalsIgnoreCase(val)) {
					ok = true;
				}
			}
		}
		return ok;
	}
	
	private static boolean validateRepositoryEntryStatus(String val) {
		boolean ok = false;
		if(StringHelper.containsNonWhitespace(val)) {
			for(RepositoryEntryStatusEnum status:RepositoryEntryStatusEnum.values()) {
				if(status.name().equalsIgnoreCase(val)) {
					ok = true;
				}
			}
		}
		return ok;
	}
	
	
	
	public record Identifier(String implementationIdentifier, String identifier) {
		@Override
		public int hashCode() {
			return (implementationIdentifier == null ? 0 : implementationIdentifier.hashCode())
					+ (identifier == null ? 0 : identifier.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof Identifier key) {
				return implementationIdentifier != null && implementationIdentifier.equals(key.implementationIdentifier)
						&& identifier != null && identifier.equals(key.identifier);
			}
			return false;
		}
	}
	
	public record LevelKey(String implementationIdentifier, String level) {
		@Override
		public int hashCode() {
			return (implementationIdentifier == null ? 0 : implementationIdentifier.hashCode())
					+ (level == null ? 0 : level.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof LevelKey key) {
				return implementationIdentifier != null && implementationIdentifier.equals(key.implementationIdentifier)
						&& level != null && level.equals(key.level);
			}
			return false;
		}
	}
}
