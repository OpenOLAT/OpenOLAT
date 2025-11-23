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
package org.olat.modules.certificationprogram;

import java.util.Date;

import org.olat.core.id.Identity;


/**
 * Orchestrate the generation (or not) of a certificate between the
 * certification program service, the credit point service and the certificate
 * manager.
 * 
 * Initial date: 23 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface CertificationCoordinator {
	
	/**
	 * 
	 * @param identity The user which will receive the certificate
	 * @param certificationProgram The certification request
	 * @param requestMode Request mode (automatic or manual through a coach)
	 * @param referenceDate The date
	 * @param doer Which user are acting
	 * @return true if delivered
	 */
	boolean processCertificationRequest(Identity identity, CertificationProgram certificationProgram,
			RequestMode requestMode, Date referenceDate, Identity doer);
	
	void generateCertificate(Identity identity, CertificationProgram certificationProgram, RequestMode requestMode, Identity actor);
	
	void revokeRecertification(CertificationProgram program, Identity identity, Identity doer);
	
	/**
	 * Send the notifications for certificates which the next recertification date are in the past.
	 * 
	 * @param referenceDate The date
	 */
	void sendExpiredNotifications(Date referenceDate);
	
	/**
	 * Send the notifications for certificates which the next recertification date are in the past
	 * and there certification window is null or in the past too. The participant is effectively
	 * removed from the certification progra.
	 * 
	 * @param referenceDate The date
	 */
	void sendRemovedNotifications(Date referenceDate);

	void sendUpcomingReminders(Date referenceDate);
	
	void sendOverdueReminders(Date referenceDate);
	
	public enum RequestMode {
		/**
		 * The cron job checks the recertification dates and try to renew them
		 */
		AUTOMATIC,
		/**
		 * A manager/coach renew manually a certificate
		 */
		COACH,
		/**
		 * The participant triggers a request by completing a course
		 */
		COURSE
	}
}
