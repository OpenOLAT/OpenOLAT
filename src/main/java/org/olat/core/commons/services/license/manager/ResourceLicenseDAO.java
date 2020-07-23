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
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.model.ResourceLicenseImpl;
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
class ResourceLicenseDAO {
	
	@Autowired
	private DB dbInstance;

	ResourceLicense createAndPersist(OLATResourceable resource, LicenseType licenseType) {
		return createAndPersist(resource, licenseType, null);
	}

	ResourceLicense createAndPersist(OLATResourceable resource, LicenseType licenseType, String licensor) {
		return createAndPersist(resource, licenseType, licensor, null);
	}

	ResourceLicense createAndPersist(OLATResourceable resource, License license) {
		return createAndPersist(resource, license.getLicenseType(), license.getLicensor(), license.getFreetext());
	}
	
	private ResourceLicense createAndPersist(OLATResourceable resource, LicenseType licenseType, String licensor,
			String freetext) {
		ResourceLicenseImpl license = new ResourceLicenseImpl();
		Date now = new Date();
		license.setCreationDate(now);
		license.setLastModified(now);
		license.setOLATResourceable(resource);
		license.setLicenseType(licenseType);
		license.setLicensor(licensor);
		license.setFreetext(freetext);
		dbInstance.getCurrentEntityManager().persist(license);
		return license;
	}

	ResourceLicense save(ResourceLicense license) {
		license.setLastModified(new Date());
		license = dbInstance.getCurrentEntityManager().merge(license);
		return license;
	}

	ResourceLicense loadByResource(OLATResourceable resource) {
		if (resource == null) return null;
		
		String query = new StringBuilder(256)
				.append("select license")
				.append("  from license license")
				.append("       inner join fetch license.licenseType as licenseType")
				.append(" where license.resName=:resName and license.resId=:resId")
				.toString();
		List<ResourceLicense> licenses = dbInstance.getCurrentEntityManager()
				.createQuery(query, ResourceLicense.class)
				.setParameter("resName", resource.getResourceableTypeName())
				.setParameter("resId", resource.getResourceableId())
				.getResultList();
		return licenses == null || licenses.isEmpty() ? null : licenses.get(0);
	}

	List<ResourceLicense> loadLicenses(Collection<? extends OLATResourceable> resources) {
		if (resources == null || resources.isEmpty()) return new ArrayList<>();
		
		Set<String> resNames = new HashSet<>();
		Set<Long> resIds = new HashSet<>();
		for (OLATResourceable resource: resources) {
			resNames.add(resource.getResourceableTypeName());
			resIds.add(resource.getResourceableId());
		}
		List<Long> resIdsList = new ArrayList<>(resIds);
		
		String query =  new StringBuilder(256)
				.append("select license")
				.append("  from license license")
				.append("       inner join fetch license.licenseType as licenseType")
				.append(" where license.resName in (:resNames)")
				.append("   and license.resId in (:resIds)")
				.toString();

		List<ResourceLicense> licenses = new ArrayList<>();
		for(int i=0; i < resIdsList.size(); ) {
			int nextInc = Math.min(resIdsList.size() - i, 10000);
			List<Long> batchOfResIds = resIdsList.subList(i, i + nextInc);
			List<ResourceLicense> bacthOfLicenses = dbInstance.getCurrentEntityManager()
				.createQuery(query, ResourceLicense.class)
				.setParameter("resNames", resNames)
				.setParameter("resIds", batchOfResIds)
				.getResultList();
			licenses.addAll(bacthOfLicenses);
			i = i + nextInc;
	    }
		return licenses;
	}

	public void delete(OLATResourceable resource) {
		if (resource == null) return;
		
		String query = new StringBuilder(256)
				.append("delete ")
				.append("  from license license")
				.append(" where license.resName=:resName and license.resId=:resId")
				.toString();
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("resName", resource.getResourceableTypeName())
				.setParameter("resId", resource.getResourceableId())
				.executeUpdate();
	}
	
}
