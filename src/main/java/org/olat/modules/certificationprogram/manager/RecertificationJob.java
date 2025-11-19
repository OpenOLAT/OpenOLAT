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

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationCoordinator.RequestMode;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 23 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class RecertificationJob extends JobWithDB {

	private static final Logger log = Tracing.createLoggerFor(RecertificationJob.class);
	
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		CertificationProgramService certificationProgramService = CoreSpringFactory.getImpl(CertificationProgramService.class);
		CertificationCoordinator certificationCoordinator = CoreSpringFactory.getImpl(CertificationCoordinator.class);
		
		log.info(Tracing.M_AUDIT, "Process certifications");

		List<CertificationProgram> programs = certificationProgramService.getCertificationPrograms().stream()
				.filter(program -> program.getStatus() == CertificationProgramStatusEnum.active)
				.toList();
		
		Date now = new Date();
		for(CertificationProgram program:programs) {
			List<Identity> eligiblesIdentities = certificationProgramService.getEligiblesIdentitiesToRecertification(program, now);
			for(Identity identity:eligiblesIdentities) {
				certificationCoordinator.processCertificationRequest(identity, program, RequestMode.AUTOMATIC, now, null);
			}
		}
	}
}
