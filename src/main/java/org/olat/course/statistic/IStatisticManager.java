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
package org.olat.course.statistic;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.course.ICourse;

/**
 * An IStatisticManager is used by the StatisticDisplayController in order to
 * generate the statistics table plus the column headers.
 * <p>
 * Usually an IStatisticManager comes in a bundle with a Hibernate xml file, 
 * a Hibernate object class plus an implementation for IStatisticUpdater -
 * the latter taking care of generating the statistics table in the first place.
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
public interface IStatisticManager {

	/**
	 * Generates the statistic table for the given Course (and matching repo entry key)
	 * <p>
	 * @param course the course for which to generate the StatisticResult
	 * @param courseRepositoryEntryKey the key of the RepositoryEntry matching the course
	 * passed to this method
	 * @return the StatisticResult - which carries the table subsequently shown 
	 * by the StatisticDisplayController
	 */
	StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey);
	
	/**
	 * Generates the statistic table for the given Course (and matching repo entry key)
	 * @param ureq
	 * @param course
	 * @param courseRepositoryEntryKey
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey, Date fromDate, Date toDate);

	/**
	 * Create a ColumnDescriptor for the given column (0 represents the course node, 1 and onward
	 * meaning columns number)
	 * @param ureq the userrequest - used to get the Locale from
	 * @param column the column - 0 represents the course node, 1 and onward is the column number
	 * @param headerId more information about the column if contained in the StatisticResult -
	 * some implementors of IStatisticManager might choose to use this information, some might not.
	 * @return a ColumnDescriptor which is then used to create the table
	 */
	ColumnDescriptor createColumnDescriptor(UserRequest ureq, int column, String headerId);

}
