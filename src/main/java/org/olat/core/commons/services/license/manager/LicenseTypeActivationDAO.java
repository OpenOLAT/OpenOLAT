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

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.model.LicenseTypeActivation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LicenseTypeActivationDAO {
	
	@Autowired
	private DB dbInstance;
	
	LicenseTypeActivation createAndPersist(LicenseHandler handler, LicenseType licenseType) {
		LicenseTypeActivation licenseTypeActivation = new LicenseTypeActivation();
		licenseTypeActivation.setCreationDate(new Date());
		licenseTypeActivation.setHandlerType(handler.getType());
		licenseTypeActivation.setLicenseType(licenseType);
		dbInstance.getCurrentEntityManager().persist(licenseTypeActivation);
		return licenseTypeActivation;
	}

	boolean isActive(LicenseHandler handler, LicenseType licenseType) {
		String query = new StringBuilder()
				.append("select count(*) from licensetypeactivation activation")
				.append(" where handlerType=:handlerType")
				.append("   and licenseType.key=:licenseTypeKey")
				.toString();
		
		Long number = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("handlerType", handler.getType())
				.setParameter("licenseTypeKey", licenseType.getKey())
				.getSingleResult();
		return number != null && number > 0;
	}
	
	void delete(LicenseHandler handler, LicenseType licenseType) {
		String query = new StringBuilder()
				.append("delete from licensetypeactivation activation")
				.append(" where handlerType=:handlerType")
				.append("   and licenseType.key=:licenseTypeKey")
				.toString();
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("handlerType", handler.getType())
				.setParameter("licenseTypeKey", licenseType.getKey())
				.executeUpdate();
	}

}
