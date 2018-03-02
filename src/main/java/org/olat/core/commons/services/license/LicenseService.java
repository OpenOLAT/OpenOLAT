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
package org.olat.core.commons.services.license;

import java.util.Collection;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 19.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
/**
 * 
 * Initial date: 22.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface LicenseService {

	/**
	 * Create a new license with the default license type. This method should be
	 * used if a license is created for a new object e.g. a new learning resource.
	 *
	 * @param ores
	 * @param handler
	 * @param licensor
	 * @return
	 */
	public License createDefaultLicense(OLATResourceable ores, LicenseHandler handler, Identity licensor);
	
	/**
	 * Load the license of a resource. If no license was found, a new license with
	 * the type of a "no license" is created and saved. The created license acts as
	 * a fallback for existing objects without a license. For new objects the method
	 * {@link #createDefaultLicense(OLATResource, LicenseHandler, Identity)} should
	 * be used to create a license.
	 *
	 * @param ores
	 * @return
	 */
	public License loadOrCreateLicense(OLATResourceable ores);

	/**
	 * Load the licenses for the resources.
	 *
	 * @param resources
	 * @return
	 */
	public List<License> loadLicenses(Collection<OLATResourceable> resources);

	/**
	 * Save the license.
	 *
	 * @param license
	 * @return
	 */
	public License update(License license);

	/**
	 * Check whether a license type with that name exists.
	 *
	 * @param name
	 * @return
	 */
	public boolean licenseTypeExists(String name);
	
	/**
	 * Creates a new license type (but does not persist it).
	 * 
	 * @param the name for the of the new license type
	 * @return the created license type
	 */
	public LicenseType createLicenseType(String name);
	
	/**
	 * Save the license type.
	 * 
	 * @param licenseType
	 * @return the saved license
	 */
	public LicenseType saveLicenseType(LicenseType licenseType);

	/**
	 * Load a license type by its key.
	 * 
	 * @param licenseTypeKey the key of the license type as a String.
	 * @return the license type or null if no license type was found for the key
	 */
	public LicenseType loadLicenseTypeByKey(String licenseTypeKey);

	/**
	 * Load all license types. This method is primarily intended for the
	 * administration of license types. Regular clients of the license service
	 * should use {@link LicenseService#loadActiveLicenseTypes(LicenseHandler)}.
	 * 
	 * @return all license types
	 */
	public List<LicenseType> loadLicenseTypes();
	
	/**
	 * Load all active license types for a particular license handler.
	 * 
	 * @param handler
	 * @return the license types
	 */
	public List<LicenseType> loadActiveLicenseTypes(LicenseHandler handler);
	
	/**
	 * Check whether the license type is the no license type.
	 *
	 * @param licenseType
	 * @return
	 */
	public boolean isNoLicense(LicenseType licenseType);
	
	/**
	 * Check whether the license type is the free text type.
	 *
	 * @param licenseType
	 * @return
	 */
	public boolean isFreetext(LicenseType licenseType);
	
	/**
	 * Check whether a license type is active for a given license handler.
	 * 
	 * @param handler
	 * @param licenseType
	 * @return
	 */
	public boolean isActive(LicenseHandler handler, LicenseType licenseType);

	/**
	 * Activate a license type for a license handler.
	 *
	 * @param handler
	 * @param licenseType
	 */
	public void activate(LicenseHandler handler, LicenseType licenseType);

	/**
	 * Deactivate a license type for a license handler.
	 *
	 * @param handler
	 * @param licenseType
	 */
	public void deactivate(LicenseHandler handler, LicenseType licenseType);

}
