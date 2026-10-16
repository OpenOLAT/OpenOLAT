/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.archiver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.nodes.gta.ui.GTAUIFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 16 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseMembershipsExport {

	public static final String USER_PROPS_ID = CourseMembershipsExport.class.getCanonicalName();
	
	private final Identity doer;
	private final Locale locale;
	private final Translator translator;
	private final RepositoryEntry courseEntry;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private GroupDAO groupDao;
	
	public CourseMembershipsExport(RepositoryEntry courseEntry, Identity doer, Locale locale) {
		CoreSpringFactory.autowireObject(this);
		this.doer = doer;
		this.locale = locale;
		this.courseEntry = courseEntry;
		translator = userManager.getPropertyHandlerTranslator(Util.createPackageTranslator(CourseMembershipsExport.class, locale,
				Util.createPackageTranslator(GTAUIFactory.class, locale)));
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, true);
	}
	
	public void export(OutputStream out) throws IOException {
		List<String> sheetNames = List.of(translator.translate("memberships.sheet.participants"),
				translator.translate("memberships.sheet.coaches"), translator.translate("memberships.sheet.owners"),
				translator.translate("export.infos"));
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, sheetNames.size(), sheetNames)) {
			List<BusinessGroup> groups = new ArrayList<>(businessGroupService.findBusinessGroups(new SearchBusinessGroupParams(), courseEntry, 0, -1));
			groups.sort(Comparator.comparing(BusinessGroup::getName, String.CASE_INSENSITIVE_ORDER));
			List<CurriculumElement> elements = new ArrayList<>(curriculumService.getCurriculumElements(courseEntry));
			elements.sort(Comparator.comparing(CurriculumElement::getDisplayName, String.CASE_INSENSITIVE_ORDER));
			
			List<String> columns = new ArrayList<>();
			columns.add(translator.translate("memberships.course"));
			groups.forEach(group -> columns.add(group.getName()));
			elements.forEach(element -> columns.add(element.getDisplayName()));
			
			List<Map<Long, Member>> sheets = loadMembers(groups, elements, columns.size());
			for(Map<Long, Member> sheet:sheets) {
				addMembers(workbook.nextWorksheet(), workbook, columns, sheet);
			}
			addInformations(workbook, workbook.nextWorksheet());
		}
	}
	
	private List<Map<Long, Member>> loadMembers(List<BusinessGroup> groups, List<CurriculumElement> elements, int numOfColumns) {
		Map<Long, Integer> columns = new HashMap<>();
		columns.put(repositoryService.getDefaultGroup(courseEntry).getKey(), 0);
		int column = 0;
		for(BusinessGroup group:groups) {
			columns.put(group.getBaseGroup().getKey(), ++column);
		}
		for(CurriculumElement element:elements) {
			columns.put(element.getGroup().getKey(), ++column);
		}
		List<GroupMembership> memberships = groupDao.getMemberships(columns.keySet(), List.of(GroupRoles.participant.name(), GroupRoles.coach.name(), GroupRoles.owner.name()));
		Map<Long, Member> participants = new LinkedHashMap<>();
		Map<Long, Member> coaches = new LinkedHashMap<>();
		Map<Long, Member> owners = new LinkedHashMap<>();
		Set<Long> groupKeys = groups.stream().map(group -> group.getBaseGroup().getKey()).collect(Collectors.toSet());
		Set<Long> elementKeys = elements.stream().map(element -> element.getGroup().getKey()).collect(Collectors.toSet());
		for(GroupMembership membership:memberships) {
			Identity identity = membership.getIdentity();
			if(identity.getStatus() >= Identity.STATUS_DELETED) {
				continue;
			}
			Long groupKey = membership.getGroup().getKey();
			String prefix = groupKeys.contains(groupKey) ? "group." : elementKeys.contains(groupKey) ? "cpl." : "";
			String role = membership.getRole();
			Map<Long, Member> sheet = GroupRoles.owner.name().equals(role) ? owners : GroupRoles.coach.name().equals(role) ? coaches : participants;
			sheet.computeIfAbsent(identity.getKey(), key -> new Member(identity, numOfColumns)).cells()[columns.get(groupKey)] = translator.translate("memberships.role." + prefix + role);
		}
		return List.of(participants, coaches, owners);
	}
	
	private void addMembers(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook, List<String> columns, Map<Long, Member> members) {
		sheet.setHeaderRows(1);
		Row headerRow = sheet.newRow();
		int pos = 0;
		for(UserPropertyHandler propertyHandler:userPropertyHandlers) {
			headerRow.addCell(pos++, translator.translate(propertyHandler.i18nColumnDescriptorLabelKey()), workbook.getStyles().getHeaderStyle());
		}
		for(String column:columns) {
			headerRow.addCell(pos++, column, workbook.getStyles().getHeaderStyle());
		}
		
		Comparator<String> nameOrder = Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);
		members.values().stream()
			.sorted(Comparator.comparing((Member member) -> member.identity().getUser().getLastName(), nameOrder)
					.thenComparing(member -> member.identity().getUser().getFirstName(), nameOrder))
			.forEach(member -> {
				Row row = sheet.newRow();
				int col = 0;
				for(UserPropertyHandler propertyHandler:userPropertyHandlers) {
					String value = propertyHandler.getUserProperty(member.identity().getUser(), locale);
					if(StringHelper.containsNonWhitespace(value)) {
						row.addCell(col, value);
					}
					col++;
				}
				for(String cell:member.cells()) {
					if(cell != null) {
						row.addCell(col, cell);
					}
					col++;
				}
			});
	}
	
	private void addInformations(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet) {
		sheet.setHeaderRows(1);
		Row headerRow = sheet.newRow();
		headerRow.addCell(0, translator.translate("export.information.info"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(1, translator.translate("export.information.value"), workbook.getStyles().getHeaderStyle());
		
		String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString("[RepositoryEntry:" + courseEntry.getKey() + "]");
		String reference = StringHelper.containsNonWhitespace(courseEntry.getExternalRef())
				? courseEntry.getExternalRef() : courseEntry.getKey().toString();
		addInformation(workbook, sheet, "export.information.url", url);
		addInformation(workbook, sheet, "export.information.version", Settings.getVersion());
		addInformation(workbook, sheet, "export.course.title", courseEntry.getDisplayname());
		addInformation(workbook, sheet, "export.course.reference", reference);
		addInformation(workbook, sheet, "export.information.language", locale.getLanguage().toUpperCase(Locale.ROOT));
		Row row = sheet.newRow();
		row.addCell(0, translator.translate("export.information.date"), workbook.getStyles().getHeaderStyle());
		row.addCell(1, new Date(), workbook.getStyles().getDateTimeStyle());
		addInformation(workbook, sheet, "export.information.by", doer == null ? "" : userManager.getUserDisplayName(doer));
	}
	
	private void addInformation(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet, String i18nKey, String value) {
		Row row = sheet.newRow();
		row.addCell(0, translator.translate(i18nKey), workbook.getStyles().getHeaderStyle());
		row.addCell(1, value, null);
	}
	
	private record Member(Identity identity, String[] cells) {
	
		Member(Identity identity, int numOfColumns) {
			this(identity, new String[numOfColumns]);
		}
	}
}
