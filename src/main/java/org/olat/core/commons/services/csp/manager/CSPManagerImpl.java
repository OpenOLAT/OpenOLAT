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
package org.olat.core.commons.services.csp.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.csp.CSPLog;
import org.olat.core.commons.services.csp.CSPManager;
import org.olat.core.commons.services.csp.model.CSPLogImpl;
import org.olat.core.commons.services.csp.model.CSPReport;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CSPManagerImpl implements CSPManager {
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public CSPLog log(CSPReport report, Identity identity) {
		CSPLogImpl log = new CSPLogImpl();
		log.setCreationDate(new Date());
		log.setBlockedUri(cut(report.getBlockedUri(), 1024));
		if(StringHelper.isLong(report.getColumnNumber())) {
			log.setColumnNumber(Long.parseLong(report.getColumnNumber()));
		}
		log.setDisposition(report.getDisposition());
		log.setDocumentUri(cut(report.getDocumentUri(), 1024));
		log.setEffectiveDirective(report.getEffectiveDirective());
		if(StringHelper.isLong(report.getLineNumber())) {
			log.setLineNumber(Long.parseLong(report.getLineNumber()));
		}
		log.setOriginalPolicy(report.getOriginalPolicy());
		log.setReferrer(cut(report.getReferrer(), 1024));
		log.setScriptSample(report.getScriptSample());
		log.setSourceFile(cut(report.getSourceFile(), 1024));
		log.setStatusCode(cut(report.getStatusCode(), 1024));
		log.setViolatedDirective(cut(report.getViolatedDirective(), 1024));
		if(identity != null) {
			log.setIdentityKey(identity.getKey());
		}
		
		dbInstance.getCurrentEntityManager().persist(log);
		return log;
	}
	
	private String cut(String value, int length) {
		if(StringHelper.containsNonWhitespace(value) && value.length() > length) {
			value = value.substring(0, length - 10);
		}
		return value;
	}

	@Override
	public int countLog() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(log.key) from csplog as log");
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).intValue();
	}

	@Override
	public List<CSPLog> getLog(int firstResult, int maxResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("select log from csplog as log")
		  .append(" order by log.creationDate desc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CSPLog.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResult)
				.getResultList();
	}
	
	@Override
	public void cleanup() {
		String query = "delete csplog where creationDate<:date";
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -90);
		dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("date", cal.getTime())
			.executeUpdate();
	}
}
