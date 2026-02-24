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
	
	private final List<UserPropertyHandler> mandatoryUserPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public ImportCurriculumsFileReader(Roles roles) {
		CoreSpringFactory.autowireObject(this);
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
		String curriculumIdentifier = getString(r, 0);
		String implementationIdentifier = getString(r, 1);
		String type = getString(r, 2);
		String level = getHierarchicalNumber(r, 3);
		
		String title = getString(r, 4);
		String externalRef = getString(r, 5);
		String elementStatus = getString(r, 6);
		
		ReaderLocalDate startDate = getDate(r, 7);
		ReaderLocalTime startTime = getTime(r, 8);
		ReaderLocalDate endDate = getDate(r, 9);
		ReaderLocalTime endTime = getTime(r, 10);
		
		String unit = getNumberAsString(r, 11);
		String referenceExternalRef = getString(r, 12);
		String location = getString(r, 13);
		String curriculumElementType = getString(r, 14);
		
		String calendar = getString(r, 15);
		String absences = getString(r, 16);
		String progress = getString(r, 17);
		
		String subjects = getString(r, 18);
		
		LocalDateTime creationDate = getDateTime(r, 19);
		LocalDateTime lastModified = getDateTime(r, 20);
		
		if(!StringHelper.containsNonWhitespace(type)
				&& !StringHelper.containsNonWhitespace(curriculumIdentifier)
				&& !StringHelper.containsNonWhitespace(implementationIdentifier)) {
			return null;
		}
		return new ImportedRow(type, r.getRowNum(), title, externalRef,
					curriculumIdentifier, implementationIdentifier, level, elementStatus, curriculumElementType,
					referenceExternalRef, unit, startDate, startTime, endDate, endTime, location,
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
				&& !StringHelper.containsNonWhitespace(curriculumIdentifier)
				&& !StringHelper.containsNonWhitespace(implementationIdentifier)) {
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
		LocalDateTime creationDate = getDateTime(r, col++);
		String password = getString(r, col++);

		return new ImportedUserRow(r.getRowNum(), identityProps, organisationIdentifier, password, creationDate);
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
			
			Sheet elementsSheet = wb.getSheet(1)
					.orElse(null);
			try (Stream<Row> rows = elementsSheet.openStream()) {
				elementsRows = rows
					.filter(r -> r.getRowNum() > 1)
					.map(r -> readElementRow(r))
					.filter(r -> r != null)
					.toList();
			} catch(Exception e) {
				log.error("", e);
			}
			
			Sheet membershipsSheet = wb.getSheet(2)
					.orElse(null);
			try (Stream<Row> rows = membershipsSheet.openStream()) {
				membershipsRows = rows
					.filter(r -> r.getRowNum() > 1)
					.map(r -> readMembershipRow(r))
					.filter(r -> r != null)
					.toList();
			} catch(Exception e) {
				log.error("", e);
			}
			
			Sheet usersSheet = wb.getSheet(3)
					.orElse(null);
			try (Stream<Row> rows = usersSheet.openStream()) {
				usersRows = rows
					.filter(r -> r.getRowNum() > 1)
					.map(r -> readUserRow(r))
					.filter(r -> r != null)
					.toList();
			} catch(Exception e) {
				log.error("", e);
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
