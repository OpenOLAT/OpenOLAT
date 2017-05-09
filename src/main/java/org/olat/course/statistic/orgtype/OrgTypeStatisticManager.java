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

package org.olat.course.statistic.orgtype;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.statistic.IStatisticManager;
import org.olat.course.statistic.StatisticDisplayController;
import org.olat.course.statistic.StatisticResult;
import org.olat.course.statistic.TotalAwareColumnDescriptor;
import org.olat.shibboleth.ShibbolethModule;

/**
 * Implementation of the IStatisticManager for 'organisation type' statistic
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
public class OrgTypeStatisticManager implements IStatisticManager {

	@Override
	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey) {
		String q = "select businessPath,orgType,value from org.olat.course.statistic.orgtype.OrgTypeStat sv where sv.resId=:resId";
		List<Object[]> raw = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(q, Object[].class)
				.setParameter("resId", courseRepositoryEntryKey)
				.getResultList();
		return new StatisticResult(course, raw);
	}
	
	@Override
	public ColumnDescriptor createColumnDescriptor(UserRequest ureq, int column, String headerId) {
		if (column==0) {
			return new DefaultColumnDescriptor("stat.table.header.node", 0, null, ureq.getLocale());
		}
		
		if (headerId!=null) {
			Translator translator = Util.createPackageTranslator(ShibbolethModule.class, ureq.getLocale());
			if (translator!=null) {
				String newHeaderId = translator.translate("swissEduPersonHomeOrganizationType."+headerId);
				if (newHeaderId!=null && !newHeaderId.startsWith(Translator.NO_TRANSLATION_ERROR_PREFIX)) {
					headerId = newHeaderId;
				}
			}
		}
		
		TotalAwareColumnDescriptor cd = new TotalAwareColumnDescriptor(headerId, column, 
				StatisticDisplayController.CLICK_TOTAL_ACTION+column, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT);	
		cd.setTranslateHeaderKey(false);
		return cd;
	}

	@Override
	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey, Date fromDate, Date toDate) {
		return generateStatisticResult(ureq, course, courseRepositoryEntryKey);
	}

}
