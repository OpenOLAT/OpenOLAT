/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
