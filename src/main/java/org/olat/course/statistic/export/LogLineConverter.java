/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.statistic.export;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Converter class to take a LoggingObject and convert it into a (csv) line
 * <p>
 * Primarily a helper class for the ICourseLogExporter implementations.
 * <P>
 * Initial Date:  06.01.2010 <br>
 * @author Stefan
 */
@Service("logLineConverter")
public class LogLineConverter {
	
	public static final String USAGE_IDENTIFIER = LogLineConverter.class.getCanonicalName();
	
	@Autowired
	private UserManager userManager;
	

	/**
	 * Returns the CSV Header line containing all property names in the exact same way as in the config file -
	 * excluding those properties which could not be retrieved, i.e. for which no PropertyDescriptor could be created
	 * @return the CSV Header line containing all property names in the exact same way as in the config file -
	 * excluding those properties which could not be retrieved, i.e. for which no PropertyDescriptor could be created
	 */
	public void setHeader(OpenXMLWorksheet sheet, boolean anonymize, boolean isAdministrativeUser) {
		Row row = sheet.newRow();
		
		int col = 0;
		row.addCell(col++, "creationDate");
		if(isAdministrativeUser) {
			row.addCell(col++, "userName");
		}
		
		if(!anonymize) {
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(USAGE_IDENTIFIER, isAdministrativeUser);
			for (UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				row.addCell(col++, userPropertyHandler.getName());
			}
		}

		row.addCell(col++, "actionCrudType");
		row.addCell(col++, "actionVerb");
		row.addCell(col++, "actionObject");
		row.addCell(col++, "parentResName");
		row.addCell(col, "targetResName");
	}

	/**
	 * Returns a CSV line for the given LoggingObject - containing all properties in the exact same way as in the 
	 * config file - excluding those which could not be retrieved, i.e. for which no PropertyDescriptor could be 
	 * created
	 * @param loggingObject the LoggingObject for which a CSV line should be created
	 * @return the CSV line representing the given LoggingObject
	 */
	public void setRow(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet, LoggingObject loggingObject, Identity identity, User user,
			boolean anonymize, Long resourceableId, boolean isAdministrativeUser) {
		Row row = sheet.newRow();
		
		int col = 0;
		row.addCell(col++, loggingObject.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		if(anonymize) {
			row.addCell(col++, makeAnonymous(identity.getName(), resourceableId));
		} else if(isAdministrativeUser) {
			String userName = user.getNickName();
			if(!StringHelper.containsNonWhitespace(userName)) {
				userName = identity.getName();
			}
			row.addCell(col++, userName);
		}
		
		if(!anonymize) {
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(USAGE_IDENTIFIER, isAdministrativeUser);
			for (UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				row.addCell(col++, user.getProperty(userPropertyHandler.getName(), null));
			}
		}

		row.addCell(col++, loggingObject.getActionCrudType());
		row.addCell(col++, loggingObject.getActionVerb());
		row.addCell(col++, loggingObject.getActionObject());
		row.addCell(col++, loggingObject.getParentResName());
		row.addCell(col, loggingObject.getTargetResName());
	}
	
	/**
	 * encode a string and course resourceable id with MD5
	 * @param s
	 * @param courseResId
	 * @return
	 */
	private String makeAnonymous(String s, Long courseResId) {
		String encodeValue = s + "-" + Long.toString(courseResId);
		// encode with MD5
		return Encoder.md5hash(encodeValue);
	}
}
