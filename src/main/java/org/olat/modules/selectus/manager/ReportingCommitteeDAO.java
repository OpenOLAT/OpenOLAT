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
package org.olat.modules.selectus.manager;

import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.committee.ReportCommittee;
import org.olat.modules.selectus.model.committee.ReportCommitteeImpl;

/**
 * 
 * Initial date: 3 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("reportingCommitteeDAO")
public class ReportingCommitteeDAO {

	@Autowired
	private DB dbInstance;

	public ReportCommittee createReportCommittee(Position position) {
		ReportCommitteeImpl report = new ReportCommitteeImpl();
		report.setCreationDate(new Date());
		report.setLastModified(report.getCreationDate());
		report.setPosition(position);
		return report;
	}
	
	public void persistReportCommittee(ReportCommittee report) {
		dbInstance.getCurrentEntityManager().persist(report);
	}
	
	public void deleteReportsCommittee(Position position) {
		String query = "delete from committeereport report where report.position.key=:positionKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("positionKey", position.getKey())
			.executeUpdate();
	}
}
