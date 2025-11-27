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
package org.olat.modules.certificationprogram.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationProgramMailQueries {
	
	private static final Logger log = Tracing.createLoggerFor(CertificationProgramMailQueries.class);

	@Autowired
	private DB dbInstance;
	
	public List<Certificate> getExpiredCertificates(CertificationProgramMailConfiguration configuration, Date referenceDate) {
		String query = """
				select cer from certificate as cer
				inner join fetch cer.certificationProgram program
				inner join certificationprogrammailconfiguration as config on (config.certificationProgram.key=program.key)
				where cer.last=true and config.key=:configKey
				and cer.nextRecertificationDate<:from
				and not exists (select log from certificationprogramlog as log
				  inner join log.mailConfiguration as mailConfig
				  where log.certificate.key=cer.key and log.mailConfiguration.key=:configKey
				)
				""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Certificate.class)
				.setParameter("configKey", configuration.getKey())
				.setParameter("from", referenceDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<Certificate> getRemovedCertificates(CertificationProgramMailConfiguration configuration, Date referenceDate) {
		String query = """
				select cer from certificate as cer
				inner join fetch cer.certificationProgram program
				inner join certificationprogrammailconfiguration as config on (config.certificationProgram.key=program.key)
				where cer.last=true and config.key=:configKey
				and cer.nextRecertificationDate<:from and (cer.recertificationWindowDate is null or cer.recertificationWindowDate<:from)
				and not exists (select log from certificationprogramlog as log
				  inner join log.mailConfiguration as mailConfig
				  where log.certificate.key=cer.key and log.mailConfiguration.key=:configKey
				)
				""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Certificate.class)
				.setParameter("configKey", configuration.getKey())
				.setParameter("from", referenceDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<Certificate> getUpcomingCertificates(CertificationProgramMailConfiguration configuration, Date referenceDate) {
		if(configuration.getTimeUnit() == null) {
			log.warn("Upcoming certification reminder need a time: {} ({})", configuration.getTitle(), configuration.getKey());
			return List.of();
		}
		
		// Certificate validity goes up to 23:59:59, add an additional day to catch the late time
		Date up = DateUtils.getStartOfDay(configuration.getTimeUnit().toDate(referenceDate, configuration.getTime() + 1));

		String query = """
				select cer from certificate as cer
				inner join fetch cer.certificationProgram program
				inner join certificationprogrammailconfiguration as config on (config.certificationProgram.key=program.key)
				where cer.last=true and config.key=:configKey
				and cer.nextRecertificationDate<:up
				and not exists (select log from certificationprogramlog as log
				  inner join log.mailConfiguration as mailConfig
				  where log.certificate.key=cer.key and log.mailConfiguration.key=:configKey
				)
				and (config.creditBalanceTooLow is false or exists (select wallet.key from creditpointwallet as wallet
					inner join wallet.creditPointSystem system
					where program.creditPointSystem.key=system.key and wallet.balance<program.creditPoints
				))
				""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Certificate.class)
				.setParameter("configKey", configuration.getKey())
				.setParameter("up", up, TemporalType.TIMESTAMP)
				.getResultList();
	}
	

	public List<Certificate> getOverdueCertificates(CertificationProgramMailConfiguration configuration, Date referenceDate) {
		Date to = configuration.getTimeUnit() == null
				? DateUtils.addYears(referenceDate, 36)// A lot
				// Certificate recertification window goes up to 23:59:59, add an additional day to catch the late time
				: DateUtils.getStartOfDay(configuration.getTimeUnit().toDate(referenceDate, configuration.getTime() + 1));
		return getOverdueCertificates(configuration, referenceDate, to);
	}
	
	/**
	 * Found all certificates with the specified configuration where the recertification
	 * 
	 * @param configuration The configuration
	 * @param from Start of the range
	 * @param to End of the range
	 * @return
	 */
	protected List<Certificate> getOverdueCertificates(CertificationProgramMailConfiguration configuration, Date from, Date to) {
		String query = """
				select cer from certificate as cer
				inner join fetch cer.certificationProgram program
				inner join certificationprogrammailconfiguration as config on (config.certificationProgram.key=program.key)
				where cer.last=true and config.key=:configKey
				and cer.recertificationWindowDate>=:from and cer.recertificationWindowDate<:to 
				and not exists (select log from certificationprogramlog as log
				  inner join log.mailConfiguration as mailConfig
				  where log.certificate.key=cer.key and log.mailConfiguration.key=:configKey
				)
				and (config.creditBalanceTooLow is false or exists (select wallet.key from creditpointwallet as wallet
					inner join wallet.creditPointSystem system
					where program.creditPointSystem.key=system.key and wallet.balance<program.creditPoints
				))
				""";

		return dbInstance.getCurrentEntityManager().createQuery(query, Certificate.class)
				.setParameter("configKey", configuration.getKey())
				.setParameter("from", from, TemporalType.TIMESTAMP)
				.setParameter("to", to, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	
}
