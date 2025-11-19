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

import org.olat.core.commons.persistence.DB;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramLog;
import org.olat.modules.certificationprogram.model.CertificationProgramLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationProgramLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CertificationProgramLog createMailLog(Certificate certificate, CertificationProgramMailConfiguration mailConfiguration) {
		CertificationProgramLogImpl mailLog = new CertificationProgramLogImpl();
		mailLog.setCreationDate(new Date());
		mailLog.setCertificate(certificate);
		mailLog.setMailConfiguration(mailConfiguration);
		dbInstance.getCurrentEntityManager().persist(mailLog);
		return mailLog;
	}

}
