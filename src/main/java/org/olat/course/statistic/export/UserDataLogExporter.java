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
package org.olat.course.statistic.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.EntityManager;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserDataLogExporter implements UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(UserDataLogExporter.class);

	@Autowired
	private DB dbInstance;
	
	@Override
	public String getExporterID() {
		return "logs";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File profileArchive = new File(archiveDirectory, "Log.xlsx");
		try(OutputStream out = new FileOutputStream(profileArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			
			Row header = sheet.newRow();
			header.addCell(0, "Date");
			header.addCell(1, "Action");
			header.addCell(2, "Verb");
			header.addCell(3, "Object");
			header.addCell(4, "Duration");
			header.addCell(5, "Administrative");
			header.addCell(6, "Path");
			header.addCell(7, "Target name");
			header.addCell(8, "Target type");
			header.addCell(9, "Parent name");
			header.addCell(10, "Parent type");

			EntityManager em = dbInstance.getCurrentEntityManager();
			em.clear();

			String query = "select v from loggingobject as v where v.userId=:identityKey";
			List<LoggingObject> queryResult = em.createQuery(query, LoggingObject.class)
					.setParameter("identityKey", identity.getKey())
					.getResultList();
			int count = 0;
			for (LoggingObject loggingObject : queryResult) {

				Row row = sheet.newRow();
				row.addCell(0, loggingObject.getCreationDate(), workbook.getStyles().getDateTimeStyle());
				row.addCell(1, loggingObject.getActionCrudType());
				row.addCell(2, loggingObject.getActionVerb());
				row.addCell(3, loggingObject.getActionObject());
				row.addCell(4, String.valueOf(loggingObject.getSimpleDuration()));
				if(loggingObject.getResourceAdminAction() != null) {
					row.addCell(5, loggingObject.getResourceAdminAction().toString());
				}
				row.addCell(6, loggingObject.getBusinessPath());
				writeData(loggingObject.getTargetResName(), loggingObject.getTargetResType(), identity, row, 7);
				writeData(loggingObject.getParentResName(), loggingObject.getParentResType(), identity, row, 9);

				if(count++ % 1000 == 0) {
					out.flush();
					em.clear();
				}
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile(profileArchive.getName());
	}
	
	private void writeData(String resName, String resType, Identity identity, Row row, int col) {
		row.addCell(col, resType);
		if("targetIdentity".equals(resType)) {
			resName = resName.equals(identity.getName()) ? resName : "";
		}
		row.addCell(col + 1, resName);
	}
}
