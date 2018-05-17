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

import java.io.File;
import java.util.Date;

/**
 * An ICourseLogExporter is capable of exporting the course log file.
 * <p>
 * The idea is that the export method can be called concurrently by 
 * multiple threads and that the implementor of this class takes care
 * of any synchronization issue.
 * <p>
 * There is a default implementation of the ICourseLogExporter which
 * works with the default database - this default implementation is used
 * if no other courseLogExporter is defined via spring.
 * <P>
 * Initial Date:  06.01.2010 <br>
 * @author Stefan
 */
public interface ICourseLogExporter {

	/**
	 * Export the course log with the given resourceableId, starting from the
	 * given begin date until the given end date.
	 * <p>
	 * Further you can specify whether you want resourceAdminActions to be
	 * exported (or the opposite).
	 * @param outFile the file (to be overwritten if already exists) where the output should be stored
	 * @param resourceableId
	 * @param begin
	 * @param end
	 * @param resourceAdminAction
	 * @return
	 */
	public void exportCourseLog(File outFile, Long resourceableId, Date begin, Date end, boolean resourceAdminAction, boolean anonymize, boolean isAdministrativeUser);
	
}
