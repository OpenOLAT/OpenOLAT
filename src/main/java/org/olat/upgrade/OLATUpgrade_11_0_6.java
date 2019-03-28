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
package org.olat.upgrade;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_11_0_6 extends OLATUpgrade {
	
	private static final String RECERTIFICATION_DATE = "RECERTIFICATION DATE";
	private static final String VERSION = "OLAT_11.0.6";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;

	public OLATUpgrade_11_0_6() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= upgradeRecertificationDates(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_11_0_6 successfully!");
		} else {
			log.audit("OLATUpgrade_11_0_6 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeRecertificationDates(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(RECERTIFICATION_DATE)) {
			
			List<OLATResource> resources = certificatesManager.getResourceWithCertificates();
			for(OLATResource resource:resources) {
				if(resource == null) continue;
				
				try {
					ICourse course = CourseFactory.loadCourse(resource);
					if(course.getCourseConfig().isRecertificationEnabled()) {
						processCourse(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
					}
					dbInstance.commitAndCloseSession();
				} catch (CorruptedCourseException e) {
					log.error("", e);
				}
			}
			
			uhd.setBooleanDataValue(RECERTIFICATION_DATE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void processCourse(RepositoryEntry entry) {
		List<Certificate> certificates = certificatesManager.getCertificates(entry.getOlatResource());
		for(Certificate certificate:certificates) {
			if(certificate.getNextRecertificationDate() != null) continue;

			Date nextCertification = certificatesManager.getDateNextRecertification(certificate, entry);
			if(nextCertification != null) {
				certificate.setNextRecertificationDate(nextCertification);
				dbInstance.getCurrentEntityManager().merge(certificate);
			}
		}
	}
}
