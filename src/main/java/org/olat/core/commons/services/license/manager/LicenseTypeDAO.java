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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.model.LicenseTypeImpl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LicenseTypeDAO {
	
	public static final String NO_LICENSE_NAME = "no.license";
	public static final String FREETEXT_NAME = "freetext";
	
	@Autowired
	private DB dbInstance;

	LicenseType create(String name) {
		LicenseTypeImpl licenseType = new LicenseTypeImpl();
		Date now = new Date();
		licenseType.setCreationDate(now);
		licenseType.setLastModified(now);
		licenseType.setName(name);
		licenseType.setPredefined(false);
		return licenseType;
	}

	LicenseType save(LicenseType licenseType) {
		if (licenseType.getKey() == null) {
			if (licenseType.getSortOrder() == 0) {
				initSortOrder(licenseType);
			}
			dbInstance.getCurrentEntityManager().persist(licenseType);
		} else {
			licenseType.setLastModified(new Date());
			licenseType = dbInstance.getCurrentEntityManager().merge(licenseType);
		}
		return licenseType;
	}
	
	private void initSortOrder(LicenseType licenseType) {
		String query = "select max(licensetype.sortOrder) + 1 from licensetype licensetype";
		List<Integer> maxSortOrders = dbInstance.getCurrentEntityManager()
				.createQuery(query, Integer.class)
				.getResultList();
		int sortOrder = maxSortOrders != null && !maxSortOrders.isEmpty() && maxSortOrders.get(0) != null? maxSortOrders.get(0): 0;
		licenseType.setSortOrder(sortOrder);
	}

	LicenseType loadNoLicenseType() {
		String query = "select licensetype from licensetype licensetype where licensetype.name=:name";
		List<LicenseType> licenseTypes = dbInstance.getCurrentEntityManager()
				.createQuery(query, LicenseType.class)
				.setParameter("name", NO_LICENSE_NAME)
				.getResultList();
		return licenseTypes == null || licenseTypes.isEmpty() ? null : licenseTypes.get(0);
	}
	
	boolean isNoLicense(LicenseType licenseType) {
		return licenseType != null && NO_LICENSE_NAME.equals(licenseType.getName());
	}
	
	boolean isFreetext(LicenseType licenseType) {
		return licenseType != null && FREETEXT_NAME.equals(licenseType.getName());
	}

	LicenseType loadLicenseTypeByKey(Long key) {
		if (key == null) return null;
		
		String query = "select licensetype from licensetype licensetype where licensetype.key=:licensetypeKey";
		List<LicenseType> licenseTypes = dbInstance.getCurrentEntityManager()
				.createQuery(query, LicenseType.class)
				.setParameter("licensetypeKey", key)
				.getResultList();
		return licenseTypes == null || licenseTypes.isEmpty() ? null : licenseTypes.get(0);
	}

	LicenseType loadLicenseTypeByName(String name) {
		if (!StringHelper.containsNonWhitespace(name)) return null;

		String query = "select licensetype from licensetype licensetype where licensetype.name=:name";
		List<LicenseType> licenseTypes = dbInstance.getCurrentEntityManager()
				.createQuery(query, LicenseType.class)
				.setParameter("name", name)
				.getResultList();
		return licenseTypes == null || licenseTypes.isEmpty() ? null : licenseTypes.get(0);
	}
	

	List<LicenseType> loadLicenseTypes() {
		String query = "select licensetype from licensetype licensetype";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, LicenseType.class)
				.getResultList();
	}

	List<LicenseType> loadActiveLicenseTypes(LicenseHandler handler) {
		String query = new StringBuilder()
				.append("select licensetype from licensetype licensetype")
				.append(" where licensetype.key in (")
				.append("       select activation.licenseType.key from licensetypeactivation activation")
				.append("        where activation.handlerType=:handlerType")
				.append("       )")
				.toString();

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, LicenseType.class)
				.setParameter("handlerType", handler.getType())
				.getResultList();
	}

	boolean exists(String name) {
		String query = new StringBuilder()
				.append("select count(*) from licensetype licensetype")
				.append(" where name=:name")
				.toString();
		
		Long number = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("name", name)
				.getSingleResult();
		
		return number != null && number > 0;
	}

	List<LicenseType> loadPredefinedLicenseTypes() {
		String query = "select licensetype from licensetype licensetype where predefined is true";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, LicenseType.class)
				.getResultList();
	}

}
