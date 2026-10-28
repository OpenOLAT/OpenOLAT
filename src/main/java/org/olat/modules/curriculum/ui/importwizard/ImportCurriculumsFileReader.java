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

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.AbstractExcelReader;
import org.olat.modules.curriculum.ui.CurriculumExport;
import org.olat.modules.roommanagement.RoomManagementModule;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsFileReader extends AbstractExcelReader {
	
	private static final Logger log = Tracing.createLoggerFor(ImportCurriculumsFileReader.class);

	public static final String ON = "ON";
	public static final String OFF = "OFF";
	public static final String DEFAULT = "DEFAULT";
	
	public static final String usageIdentifyer = CurriculumExport.usageIdentifyer;
	
	private final boolean withRooms;
	private final List<UserPropertyHandler> mandatoryUserPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RoomManagementModule roomManagementModule;
	
	public ImportCurriculumsFileReader(Roles roles) {
		CoreSpringFactory.autowireObject(this);
		withRooms = roomManagementModule.isEnabled();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		mandatoryUserPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser)
				.stream()
				.filter(handler -> userManager.isMandatoryUserProperty(usageIdentifyer , handler))
				.toList();
	}
	
	private ImportedRow readCurriculumRow(Row r) {
		// Title, Ext. ref., Organisation ref., absences, description, creation date, last modified
		String title = getString(r, 0);
		String externalRef = getString(r, 1);
		String organisationExtRel = getString(r, 2);
		String absences = getString(r, 3);
		String description = getString(r, 4);
		LocalDateTime creationDate = getDateTime(r, 5);
		LocalDateTime lastModified = getDateTime(r, 6);
		
		return new ImportedRow(r.getRowNum(), title, externalRef, organisationExtRel,
				absences, description, creationDate, lastModified);
	}
	
	private ImportedRow readElementRow(Row r) {
		// Prod. ext. ref, Impl. ext. ref., Object type, Title, Ext. ref., Organisation ref., absences, description, creation date, last modified
		int pos = 0;
		String curriculumIdentifier = getString(r, pos++);
		String implementationIdentifier = getString(r, pos++);
		String type = getString(r, pos++);// 2
		String level = getHierarchicalNumber(r, pos++);
		
		String title = getString(r, pos++);// 4
		String externalRef = getString(r, pos++);
		String elementStatus = getString(r, pos++);
		
		ReaderLocalDate startDate = getDate(r, pos++); // 7
		ReaderLocalTime startTime = getTime(r, pos++);
		ReaderLocalDate endDate = getDate(r, pos++);
		ReaderLocalTime endTime = getTime(r, pos++);
		
		String unit = getNumberAsString(r, pos++); // 11
		String referenceExternalRef = getString(r, pos++);
		String location = getString(r, pos++);
		String rooms = withRooms ? getString(r, pos++) : null;
		String curriculumElementType = getString(r, pos++);
		
		String calendar = getString(r, pos++); // 16 with rooms
		String absences = getString(r, pos++);
		String progress = getString(r, pos++);
		
		String subjects = getString(r, pos++); // 19 with rooms
		
		LocalDateTime creationDate = getDateTime(r, pos++); // 20 with rooms
		LocalDateTime lastModified = getDateTime(r, pos++);
		
		if(!StringHelper.containsNonWhitespace(type)
				&& !StringHelper.containsNonWhitespace(curriculumIdentifier)
				&& !StringHelper.containsNonWhitespace(implementationIdentifier)) {
			return null;
		}
		return new ImportedRow(type, r.getRowNum(), title, externalRef,
					curriculumIdentifier, implementationIdentifier, level, elementStatus, curriculumElementType,
					referenceExternalRef, unit, startDate, startTime, endDate, endTime, location, rooms,
					calendar, absences, progress, subjects, creationDate, lastModified);
	}
	
	private ImportedMembershipRow readMembershipRow(Row r) {
		String curriculumIdentifier = getString(r, 0);
		String implementationIdentifier = getString(r, 1);
		String identifier = getString(r, 2);
		String role = getString(r, 3);
		String username = getString(r, 4);
		LocalDateTime registrationDate = getDateTime(r, 5);
		LocalDateTime lastModified = getDateTime(r, 6);
		
		if(!StringHelper.containsNonWhitespace(identifier)
				&& !StringHelper.containsNonWhitespace(username)
				&& !StringHelper.containsNonWhitespace(role)) {
			return null;
		}
		return new ImportedMembershipRow(r.getRowNum(), curriculumIdentifier, implementationIdentifier, identifier,
				role, username, registrationDate, lastModified);
	}
	
	private ImportedUserRow readUserRow(Row r) {
		int numOfHandlers = mandatoryUserPropertyHandlers.size();
		String[] identityProps = new String[numOfHandlers];
		for (int i = 0; i<numOfHandlers; i++) {
			identityProps[i] = getString(r, i);
			
		}
		int col = numOfHandlers;
		String organisationIdentifier = getString(r, col++);
		ReaderLocalDate expirationDate = getDate(r, col++);
		LocalDateTime creationDate = getDateTime(r, col++);
		String password = getString(r, col++);
		
		if(numOfHandlers > 3
				&& !StringHelper.containsNonWhitespace(identityProps[0])
				&& !StringHelper.containsNonWhitespace(identityProps[1])
				&& !StringHelper.containsNonWhitespace(identityProps[2])) {
			return null;
		}

		return new ImportedUserRow(r.getRowNum(), identityProps, organisationIdentifier, expirationDate, password, creationDate);
	}
	
	public Import loadFile(File file) {
		List<ImportedRow> elementsRows = null;
		List<ImportedRow> curriculumsRows = null;
		List<ImportedUserRow> usersRows = null;
		List<ImportedMembershipRow> membershipsRows = null;
		
		try(ReadableWorkbook wb = new ReadableWorkbook(file)) {
			Sheet curriculumsSheet = wb.getFirstSheet();
			try (Stream<Row> rows = curriculumsSheet.openStream()) {
				curriculumsRows = rows
					.filter(r -> r.getRowNum() > 1)
					.map(r -> readCurriculumRow(r)).toList();
			} catch(Exception e) {
				log.error("", e);
			}
			
			Optional<Sheet> elementsSheet = wb.getSheet(1);
			if(elementsSheet.isPresent()) {
				try (Stream<Row> rows = elementsSheet.get().openStream()) {
					elementsRows = rows
						.filter(r -> r.getRowNum() > 1)
						.map(r -> readElementRow(r))
						.filter(r -> r != null)
						.toList();
				} catch(Exception e) {
					log.error("", e);
				}
			}
			
			Optional<Sheet> membershipsSheet = wb.getSheet(2);
			if(membershipsSheet.isPresent()) {
				try (Stream<Row> rows = membershipsSheet.get().openStream()) {
					membershipsRows = rows
						.filter(r -> r.getRowNum() > 1)
						.map(r -> readMembershipRow(r))
						.filter(r -> r != null)
						.toList();
				} catch(Exception e) {
					log.error("", e);
				}
			}
			
			Optional<Sheet> usersSheet = wb.getSheet(3);
			if(usersSheet.isPresent()) {
				try (Stream<Row> rows = usersSheet.get().openStream()) {
					usersRows = rows
						.filter(r -> r.getRowNum() > 1)
						.map(r -> readUserRow(r))
						.filter(r -> r != null)
						.toList();
				} catch(Exception e) {
					log.error("", e);
				}
			}
			
		} catch(Exception e) {
			log.error("", e);
		}
		return new Import(curriculumsRows, elementsRows, membershipsRows, usersRows);
	}
	
	public record Import(List<ImportedRow> curriculumsRows, List<ImportedRow> elementsRows,
			List<ImportedMembershipRow> membershipsRows, List<ImportedUserRow> usersRows) {
		//
	}
}
