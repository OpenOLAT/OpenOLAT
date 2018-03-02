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
package org.olat.core.commons.services.license.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.model.LicenseImpl;
import org.olat.core.id.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class LicenseDAO {
	
	@Autowired
	private DB dbInstance;

	License createAndPersist(OLATResourceable ores, LicenseType licenseType) {
		return createAndPersist(ores, licenseType, null);
	}

	License createAndPersist(OLATResourceable ores, LicenseType licenseType, String licensor) {
		LicenseImpl license = new LicenseImpl();
		Date now = new Date();
		license.setCreationDate(now);
		license.setLastModified(now);
		license.setOLATResourceable(ores);
		license.setLicenseType(licenseType);
		license.setLicensor(licensor);
		dbInstance.getCurrentEntityManager().persist(license);
		return license;
	}

	License save(License license) {
		license.setLastModified(new Date());
		license = dbInstance.getCurrentEntityManager().merge(license);
		return license;
	}

	License loadByResource(OLATResourceable ores) {
		if (ores == null) return null;
		
		String query = new StringBuilder(256)
				.append("select license")
				.append("  from license license")
				.append("        inner join fetch license.licenseType as licenseType")
				.append("  where license.resName=:resName and license.resId=:resId")
				.toString();
		List<License> licenses = dbInstance.getCurrentEntityManager()
				.createQuery(query, License.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.getResultList();
		return licenses == null || licenses.isEmpty() ? null : licenses.get(0);
	}

	List<License> loadLicenses(Collection<OLATResourceable> resources) {
		if (resources == null || resources.isEmpty()) return new ArrayList<>();
		
		Set<String> resNames = new HashSet<>();
		Set<Long> resIds = new HashSet<>();
		for (OLATResourceable resource: resources) {
			resNames.add(resource.getResourceableTypeName());
			resIds.add(resource.getResourceableId());
		}
		
		String query =  new StringBuilder(256)
				.append("select license")
				.append("  from license license")
				.append("        inner join fetch license.licenseType as licenseType")
				.append("  where license.resName in (:resNames)")
				.append("    and license.resId in (:resIds)")
				.toString();
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, License.class)
				.setParameter("resNames", resNames)
				.setParameter("resIds", resIds)
				.getResultList();
	}
	
}
