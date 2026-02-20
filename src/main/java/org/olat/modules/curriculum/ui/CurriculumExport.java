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
package org.olat.modules.curriculum.ui;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityOrganisationsRow;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumMemberQueries;
import org.olat.modules.curriculum.model.CurriculumMemberWithElement;
import org.olat.modules.curriculum.model.RepositoryEntryInfos;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.member.AbstractMembersController;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumExport {
	
	private static final Logger log = Tracing.createLoggerFor(CurriculumExport.class);
	
	// UserTableDataModel
	public static final String usageIdentifyer = AbstractMembersController.usageIdentifyer;
	
	public static final String PARTICIPANT = "PARTICIPANT";
	public static final String COACH = "COACH";
	public static final String MASTER_COACH = "MASTER_COACH";
	public static final String COURSE_OWNER = "COURSE_OWNER";
	public static final String ELEM_OWNER = "ELEM_OWNER";
	
	private static final String MANDATORY = " *";

	private final String url;
	private final Translator translator;
	private final Identity identity;
	private final Formatter formatter;
	
	private final List<Curriculum> curriculums;
	private final List<CurriculumElement> implementations;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumMemberQueries curriculumMemberQueries;
	@Autowired
	private IdentityPowerSearchQueries identityPowerSearchQueries;
	
	public CurriculumExport(List<Curriculum> curriculums, Identity identity, Roles roles, String url, Translator translator) {
		this(curriculums, null, identity, roles, url, translator);
	}
	
	public CurriculumExport(List<Curriculum> curriculums, List<CurriculumElement> implementations,
			Identity identity, Roles roles, String url, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.url = url;
		this.identity = identity;
		this.curriculums = curriculums;
		this.implementations = implementations;
		formatter = Formatter.getInstance(translator.getLocale());
		this.translator = userManager.getPropertyHandlerTranslator(translator);
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
	}
	
	public OpenXMLWorkbookResource createMediaResource() {
		final String name = translator.translate("export.filename", formatter.formatDateAndTime(new Date()));
		final String fileName = StringHelper.transformDisplayNameToFileSystemName(name) + ".xlsx";
		
		return new OpenXMLWorkbookResource(fileName) {
			@Override
			protected void generate(OutputStream out) {
				createWorkbook(out);
			}
		};
	}
	
	private void createWorkbook(OutputStream out) {
		List<String> sheetNames = List.of(translator.translate("export.products"),
				translator.translate("export.implementations"), translator.translate("export.members"),
				translator.translate("export.users"), translator.translate("export.informations"));
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 5, sheetNames)) {
			// Curriculums / products
			OpenXMLWorksheet curriculumsSheet = workbook.nextWorksheet();
			addCurriculumsHeader(workbook, curriculumsSheet);
			addCurriculumsContent(workbook, curriculumsSheet);
			// Elements
			OpenXMLWorksheet implementationsSheet = workbook.nextWorksheet();
			addImplementationsHeader(workbook, implementationsSheet);
			List<CurriculumElement> elements;
			if(implementations == null) {
				elements = addCurriculumsImplementationsContent(workbook, implementationsSheet);
			} else {
				elements = addImplementationsContent(workbook, implementationsSheet);
			}
			// Members
			OpenXMLWorksheet membershipsSheet = workbook.nextWorksheet();
			addMembershipsHeader(workbook, membershipsSheet);
			Set<Member> users = addMemberships(elements, workbook, membershipsSheet);
			// Users
			OpenXMLWorksheet usersSheet = workbook.nextWorksheet();
			addUsersHeader(workbook, usersSheet);
			addUsers(users, workbook, usersSheet);
			// Informations
			OpenXMLWorksheet infosSheet = workbook.nextWorksheet();
			addInformations(workbook, infosSheet);
			dbInstance.commitAndCloseSession();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void addMembershipsHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		int col = 0;
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(col++, translator.translate("export.implementation.prod.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.implementation.impl.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.role") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.username") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.registration.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.last.modified"), workbook.getStyles().getHeaderStyle());
	}
	
	private Set<Member> addMemberships(List<CurriculumElement> elements, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		Set<Member> users = new HashSet<>();
		Collection<List<CurriculumElement>> chunks = PersistenceHelper.chunks(elements, 16);
		for(List<CurriculumElement> chunk:chunks) {
			SearchMemberParameters searchParams = new SearchMemberParameters(chunk);
			List<CurriculumMemberWithElement> memberships = curriculumMemberQueries.getCurriculumElementsMembersWith(searchParams);
			for(CurriculumMemberWithElement membership:memberships) {
				Member member = new Member(membership.identity(), userPropertyHandlers, translator.getLocale());
				addMembership(member, membership, workbook, exportSheet);
				users.add(member);
			}
		}
		return users;
	}

	private void addMembership(Member member, CurriculumMemberWithElement membership, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		row.addCell(col++, membership.curriculumIdentifier());
		String implementationIdentifier = membership.implementationKey() == null
				? membership.elementIdentifier()
				: membership.implementationIdentifier();
		row.addCell(col++, implementationIdentifier);
		row.addCell(col++, membership.elementIdentifier());
		row.addCell(col++, formatRole(membership.role()));
		row.addCell(col++, member.getNickName());
		row.addCell(col++, membership.creationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, membership.creationDate(), workbook.getStyles().getDateTimeStyle());
	}
	
	private void addUsersHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		int col = 0;
		Row headerRow = exportSheet.newRow();
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			String name = translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey());
			boolean mandatory = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			if(mandatory) {
				headerRow.addCell(col++, name + MANDATORY, workbook.getStyles().getHeaderStyle());
			}
		}
		
		headerRow.addCell(col++, translator.translate("export.organisation.affilition") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.creation.date"), workbook.getStyles().getHeaderStyle());
	}
	
	private void addUsers(Set<Member> usersSet, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		List<Member> users = new ArrayList<>(usersSet);
		identityPowerSearchQueries.appendOrganisations(users);
		for(Member user:users) {
			addUser(user, workbook, exportSheet);
		}
	}

	private void addUser(Member user, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean mandatory = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			if(mandatory) {
				row.addCell(col++, user.getIdentityProp(i));
			}
		}
		
		// Organisation
		List<OrganisationWithParents> organisations = user.getOrganisations();
		if(organisations == null || organisations.isEmpty()) {
			col++;
		} else {
			List<String> identifiers = organisations.stream()
					.map(OrganisationWithParents::getIdentifier)
					.filter(identifier -> StringHelper.containsNonWhitespace(identifier))
					.toList();
			row.addCell(col++, String.join(";", identifiers));
		}
		
		row.addCell(col++, user.getCreationDate(), workbook.getStyles().getDateTimeStyle());
	}
	
	private void addCurriculumsHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		int col = 0;
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(col++, translator.translate("table.header.title") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.organisation.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.lectures") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("curriculum.description"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.creation.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.last.modified"), workbook.getStyles().getHeaderStyle());
	}
	
	private void addCurriculumsContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		for(Curriculum curriculum:curriculums) {
			curriculum = curriculumService.getCurriculum(curriculum);
			addCurriculumContent(curriculum, workbook, exportSheet);
		}
	}
	
	private void addCurriculumContent(Curriculum curriculum, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		row.addCell(col++, curriculum.getDisplayName());
		row.addCell(col++, curriculum.getIdentifier());
		
		String organisation = curriculum.getOrganisation() != null
				? curriculum.getOrganisation().getIdentifier()
				: null;
		row.addCell(col++, organisation);
		
		String lectures = curriculum.isLecturesEnabled() ? translator.translate("on") : translator.translate("off");
		row.addCell(col++, lectures.toUpperCase());
		row.addCell(col++, curriculum.getDescription());
		row.addCell(col++, curriculum.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, curriculum.getLastModified(), workbook.getStyles().getDateTimeStyle());
	}
	
	private void addImplementationsHeader(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		int col = 0;
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(col++, translator.translate("export.implementation.prod.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.implementation.impl.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.implementation.object.type") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.number"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.title") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.external.ref") + MANDATORY, workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.status"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.start.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.start.time"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.end.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.end.time"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.lectures"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.reference.external.ref"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.location"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.element.type"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.calendars"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.lectures"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("table.header.progress"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.taxonomy"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.creation.date"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(col++, translator.translate("export.last.modified"), workbook.getStyles().getHeaderStyle());
	}

	private List<CurriculumElement> addCurriculumsImplementationsContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		List<CurriculumElement> allElements = new ArrayList<>();
		for(Curriculum curriculum:curriculums) {
			List<CurriculumElement> curriculumImplementations = curriculumService.getImplementations(curriculum, CurriculumElementStatus.notDeleted());
			for(CurriculumElement implementation:curriculumImplementations) {
				List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(implementation);
				addContent(implementation, workbook, exportSheet);
				for(CurriculumElement descendant:descendants) {
					addContent(descendant,  workbook, exportSheet);
				}
				dbInstance.commitAndCloseSession();
				allElements.add(implementation);
				allElements.addAll(descendants);
			}
		}
		return allElements;
	}
	
	private List<CurriculumElement> addImplementationsContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		List<CurriculumElement> allElements = new ArrayList<>();
		for(CurriculumElement element:implementations) {
			element = curriculumService.getCurriculumElement(element);
			List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(element);
			addContent(element, workbook, exportSheet);
			for(CurriculumElement descendant:descendants) {
				addContent(descendant,  workbook, exportSheet);
			}
			dbInstance.commitAndCloseSession();
			allElements.add(element);
			allElements.addAll(descendants);
		}
		return allElements;
	}

	private void addContent(CurriculumElement element, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		addCurriculumElementContent(element, workbook, exportSheet);

		List<RepositoryEntryInfos> entries = curriculumService.getRepositoryEntriesWithInfos(element);
		for(RepositoryEntryInfos entry:entries) {
			addRepositoryEntryContent(entry.repositoryEntry(), CurriculumExportType.COURSE, element, workbook, exportSheet);
		}
		
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(element);
		for(RepositoryEntry template:templates) {
			addRepositoryEntryContent(template, CurriculumExportType.TMPL, element, workbook, exportSheet);
		}
		
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setCurriculumElementPath(null);
		searchParams.setCurriculumElement(element);
		searchParams.setLectureConfiguredRepositoryEntry(false);
		
		List<LectureBlockWithTeachers> blocks = lectureService.getLectureBlocksWithOptionalTeachers(searchParams);
		for(LectureBlockWithTeachers block:blocks) {
			addLectureBlockContent(block.lectureBlock(), element, workbook, exportSheet);
		}
	}
	
	private void addCurriculumElementContent(CurriculumElement element, OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		String curriculumExternalRef = StringHelper.containsNonWhitespace(element.getCurriculum().getIdentifier())
				? element.getCurriculum().getIdentifier()
				: element.getCurriculum().getDisplayName();
		row.addCell(col++, curriculumExternalRef);

		String implementationExternalRef;
		if(element.getImplementation() == null) {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getIdentifier())
					? element.getIdentifier()
					: element.getDisplayName();
		} else {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getImplementation().getIdentifier())
					? element.getImplementation().getIdentifier()
					: element.getImplementation().getDisplayName();
		}
		row.addCell(col++, implementationExternalRef);
		
		if(element.getParent() == null) {
			row.addCell(col++, CurriculumExportType.IMPL.name());
		} else {
			row.addCell(col++, CurriculumExportType.ELEM.name());
		}
		row.addCell(col++, element.getNumberImpl());
		row.addCell(col++, element.getDisplayName());
		row.addCell(col++, element.getIdentifier());
		row.addCell(col++, element.getElementStatus().name().toUpperCase());
	
		row.addCell(col++, element.getBeginDate(), workbook.getStyles().getDateStyle());
		row.addCell(col++, null);// No time
		row.addCell(col++, element.getEndDate(), workbook.getStyles().getDateStyle());
		row.addCell(col++, null);// No time
		row.addCell(col++, null);// No lectures
		row.addCell(col++, null);// No ref. id.
		row.addCell(col++, null);// No location
		
		String elementType = element.getType() != null
				? element.getType().getIdentifier()
				: null;
		row.addCell(col++, elementType);
		row.addCell(col++, getValue(element.getCalendars()));
		row.addCell(col++, getValue(element.getLectures()));
		row.addCell(col++, getValue(element.getLearningProgress()));
		
		String taxonomy = getTaxonomyLevels(element);
		row.addCell(col++, taxonomy);
		row.addCell(col++, element.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, element.getLastModified(), workbook.getStyles().getDateTimeStyle());
	}
	
	private String getTaxonomyLevels(CurriculumElement element) {
		List<TaxonomyLevel> levels = curriculumService.getTaxonomy(element);
		if(levels == null || levels.isEmpty()) return null;
		
		List<String> displayNames = levels.stream()
				.map(level -> TaxonomyUIFactory.translateDisplayName(translator, level))
				.filter(Objects::nonNull)
				.toList();
		return String.join("; ", displayNames);
	}
	
	private String getValue(CurriculumCalendars val) {
		if(val == null) return CurriculumExportOnOff.DEFAULT.name();
		return switch(val) {
			case enabled -> CurriculumExportOnOff.ON.name();
			case disabled -> CurriculumExportOnOff.OFF.name();
			default -> CurriculumExportOnOff.DEFAULT.name();
		};
	}
	
	private String getValue(CurriculumLectures val) {
		if(val == null) return CurriculumExportOnOff.DEFAULT.name();
		return switch(val) {
			case enabled -> CurriculumExportOnOff.ON.name();
			case disabled -> CurriculumExportOnOff.OFF.name();
			default -> CurriculumExportOnOff.DEFAULT.name();
		};
	}
	
	private String getValue(CurriculumLearningProgress val) {
		if(val == null) return CurriculumExportOnOff.DEFAULT.name();
		return switch(val) {
			case enabled -> CurriculumExportOnOff.ON.name();
			case disabled -> CurriculumExportOnOff.OFF.name();
			default -> CurriculumExportOnOff.DEFAULT.name();
		};
	}
	
	private void addRepositoryEntryContent(RepositoryEntry entry, CurriculumExportType type, CurriculumElement element,
			OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		String curriculumExternalRef = StringHelper.containsNonWhitespace(element.getCurriculum().getIdentifier())
				? element.getCurriculum().getIdentifier()
				: element.getCurriculum().getDisplayName();
		row.addCell(col++, curriculumExternalRef);

		String implementationExternalRef;
		if(element.getImplementation() == null) {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getIdentifier())
					? element.getIdentifier()
					: element.getDisplayName();
		} else {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getImplementation().getIdentifier())
					? element.getImplementation().getIdentifier()
					: element.getImplementation().getDisplayName();
		}
		row.addCell(col++, implementationExternalRef);
		row.addCell(col++, type.name());

		row.addCell(col++, element.getNumberImpl());// No number impl
		row.addCell(col++, entry.getDisplayname());
		row.addCell(col++, entry.getExternalRef());
		row.addCell(col++, entry.getStatus().toUpperCase());
		
		Date validFrom = entry.getLifecycle() != null
				? entry.getLifecycle().getValidFrom()
				: null;
		Date validTo = entry.getLifecycle() != null
				? entry.getLifecycle().getValidTo()
				: null;
	
		row.addCell(col++, validFrom, workbook.getStyles().getDateStyle());
		row.addCell(col++, null);// No time
		row.addCell(col++, validTo, workbook.getStyles().getDateStyle());
		row.addCell(col++, null);// No time
		row.addCell(col++, null);// No lectures
		row.addCell(col++, null);// No ref. id.
		row.addCell(col++, null);// No location
		
		row.addCell(col++, null); // No element type
		row.addCell(col++, null); // No calendar option
		row.addCell(col++, null); // No lectures option
		row.addCell(col++, null); // No learning progress option
		
		String taxonomy = getTaxonomyLevels(entry);
		row.addCell(col++, taxonomy);
		row.addCell(col++, entry.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, entry.getLastModified(), workbook.getStyles().getDateTimeStyle());
	}
	
	private String getTaxonomyLevels(RepositoryEntry entry) {
		List<TaxonomyLevel> levels = repositoryService.getTaxonomy(entry);
		if(levels == null || levels.isEmpty()) return null;
		
		List<String> displayNames = levels.stream()
				.map(level -> TaxonomyUIFactory.translateDisplayName(translator, level))
				.filter(Objects::nonNull)
				.toList();
		return String.join("; ", displayNames);
	}
	
	private void addLectureBlockContent(LectureBlock lectureBlock, CurriculumElement element,
			OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		String curriculumExternalRef = StringHelper.containsNonWhitespace(element.getCurriculum().getIdentifier())
				? element.getCurriculum().getIdentifier()
				: element.getCurriculum().getDisplayName();
		row.addCell(col++, curriculumExternalRef);

		String implementationExternalRef;
		if(element.getImplementation() == null) {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getIdentifier())
					? element.getIdentifier()
					: element.getDisplayName();
		} else {
			implementationExternalRef = StringHelper.containsNonWhitespace(element.getImplementation().getIdentifier())
					? element.getImplementation().getIdentifier()
					: element.getImplementation().getDisplayName();
		}
		row.addCell(col++, implementationExternalRef);
		row.addCell(col++, CurriculumExportType.EVENT.name());

		row.addCell(col++, element.getNumberImpl());// Numbering
		row.addCell(col++, lectureBlock.getTitle());
		row.addCell(col++, lectureBlock.getExternalRef());
		row.addCell(col++, null);//No status

		row.addCell(col++, lectureBlock.getStartDate(), workbook.getStyles().getDateStyle());
		String startTime = lectureBlock.getStartDate() != null
				? formatter.formatTimeShort(lectureBlock.getStartDate())
				: null;
		row.addCell(col++, startTime);
		row.addCell(col++, lectureBlock.getEndDate(), workbook.getStyles().getDateStyle());
		String endTime = lectureBlock.getEndDate() != null
				? formatter.formatTimeShort(lectureBlock.getEndDate())
				: null;
		row.addCell(col++, endTime);
		
		row.addCell(col++, lectureBlock.getPlannedLecturesNumber(), workbook.getStyles().getIntegerStyle());
		String entryExternalRef = lectureBlock.getEntry() != null
				? lectureBlock.getEntry().getExternalRef()
				: null;
		row.addCell(col++, entryExternalRef);// Ref. to course
		row.addCell(col++, lectureBlock.getLocation());// Location
		
		row.addCell(col++, null); // No element type
		row.addCell(col++, null); // No calendar option
		row.addCell(col++, null); // No lectures option
		row.addCell(col++, null); // No learning progress option
		
		String taxonomy = getTaxonomyLevels(lectureBlock);
		row.addCell(col++, taxonomy);
		row.addCell(col++, lectureBlock.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, lectureBlock.getLastModified(), workbook.getStyles().getDateTimeStyle());
	}
	
	private String getTaxonomyLevels(LectureBlock lectureBlock) {
		List<TaxonomyLevel> levels = lectureService.getTaxonomy(lectureBlock);
		if(levels == null || levels.isEmpty()) return null;
		
		List<String> displayNames = levels.stream()
				.map(level -> TaxonomyUIFactory.translateDisplayName(translator, level))
				.filter(Objects::nonNull)
				.toList();
		return String.join("; ", displayNames);
	}
	
	private void addInformations(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);

		Row headerRow = exportSheet.newRow();
		headerRow.addCell(0, translator.translate("export.information.info"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(1, translator.translate("export.information.value"), workbook.getStyles().getHeaderStyle());
		// URL
		Row row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.url"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, url, null);
		// Version
		row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.version"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, Settings.getVersion(), null);
		// Language
		row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.language"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, translator.getLocale().getDisplayLanguage(), null);
		// Date
		row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.date"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, new Date(), workbook.getStyles().getDateTimeStyle());
		// Date
		row = exportSheet.newRow();
		row.addCell(0, translator.translate("export.information.by"), workbook.getStyles().getHeaderStyle());
		String fullname = userManager.getUserDisplayName(identity);
		row.addCell(1, fullname, null);
	}
	
	public static final String formatRole(String role) {
		return switch(CurriculumRoles.valueOf(role)) {
			case participant -> PARTICIPANT;
			case coach -> COACH;
			case mastercoach -> MASTER_COACH;
			case owner -> COURSE_OWNER;
			case curriculumelementowner -> ELEM_OWNER;
			default -> null;
		};
	}
	
	private static class Member extends UserPropertiesRow implements IdentityOrganisationsRow {
		
		private final String nickName;
		private final Date creationDate;
		private List<OrganisationWithParents> organisations;
		
		public Member(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
			super(identity, userPropertyHandlers, locale);
			nickName = identity.getUser().getNickName();
			creationDate = identity.getCreationDate();
		}
		
		public String getNickName() {
			return nickName;
		}
		
		public Date getCreationDate() {
			return creationDate;
		}
		
		public List<OrganisationWithParents> getOrganisations() {
			return organisations;
		}

		@Override
		public void setOrganisations(List<OrganisationWithParents> organisations) {
			this.organisations = organisations;
		}

		@Override
		public int hashCode() {
			return getIdentityKey().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof Member m) {
				return getIdentityKey().equals(m.getIdentityKey());
			}
			return false;
		}
	}
}
