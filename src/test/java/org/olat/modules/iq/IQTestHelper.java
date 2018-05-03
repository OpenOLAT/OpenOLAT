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
package org.olat.modules.iq;

import java.util.Calendar;
import java.util.Date;

import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultSet;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.OlatTestCase;

/**
 * some functions usefull for unit testing the QTI statistics and QTI database
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQTestHelper extends OlatTestCase {
	
	public static Date modDate(int day, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2014);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static QTIResultSet createSet(float score, long assessmentId, Identity id, RepositoryEntry re, String resSubPath, Date creationDate, Date modDate) {
		QTIResultSet test = new QTIResultSet();
		if (score >= 4.0d) {
			test.setIsPassed(true);
		} else {
			test.setIsPassed(false);
		}
		test.setOlatResource(re.getOlatResource().getResourceableId());
		test.setOlatResourceDetail(resSubPath);
		test.setRepositoryRef(re.getKey());
		test.setScore(score);
		long duration = Math.round((Math.random() * 1000 * 60 * 60) + 1l);
		test.setDuration(duration);
		test.setIdentity(id);
		test.setAssessmentID(assessmentId);
		test.setCreationDate(creationDate);
		test.setLastModified(modDate);
		
		DB dbInstance = DBFactory.getInstance();
		dbInstance.getCurrentEntityManager().persist(test);
		return test;
	}
	
	public static QTIResult createResult(String itemIdent, String answer, QTIResultSet set) {
		QTIResult result = new QTIResult();
		result.setResultSet(set);
		long tempDuration = Math.round((Math.random() * 10000.0) + 1);
		result.setDuration(tempDuration);
		result.setIp("127.0.0.1");
		result.setAnswer("asdf");
		int tempScore = (int)Math.round(Math.ceil(Math.random()));
		result.setScore(tempScore);
		result.setItemIdent(itemIdent);
		result.setAnswer(answer);
		result.setLastModified(new Date());
		result.setTstamp(result.getLastModified());

		DB dbInstance = DBFactory.getInstance();
		dbInstance.getCurrentEntityManager().persist(result);
		return result;
	}
	
	public static RepositoryEntry createRepository() {
		DB dbInstance = DBFactory.getInstance();
		OLATResource r =  CoreSpringFactory.getImpl(OLATResourceManager.class).createOLATResourceInstance("QTIStatisticsTest");
		dbInstance.getCurrentEntityManager().persist(r);
		dbInstance.commit();
		
		Organisation defOrganisation = CoreSpringFactory.getImpl(OrganisationService.class).getDefaultOrganisation();
		RepositoryEntry d = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(null, "Rei Ayanami", "QTIStatisticsTest", "QTIStatisticsTest", "Repo entry", r, 0, defOrganisation);
		d.setOlatResource(r);
		dbInstance.getCurrentEntityManager().persist(d);
		dbInstance.commit();
		return d;
	}

}
