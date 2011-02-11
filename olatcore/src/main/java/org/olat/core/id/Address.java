/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.id;

/**
 * Desciption:
 * Interface for an international address. This is a component object
 * of an OLAT user.
 *
 * @author Florian Gnägi
 */
public interface Address {
	/**
	 * Returns the city.
	 * @return String
	 */
	public String getCity();


	/**
	 * Returns the country.
	 * @return String
	 */
	public String getCountry();


	/**
	 * Returns the extendedAddress.
	 * @return String
	 */
	public String getExtendedAddress();


	/**
	 * Returns the pCode.
	 * @return String
	 */
	public String getZipCode();


	/**
	 * Returns the poBox.
	 * @return String
	 */
	public String getPoBox();

	/**
	 * Returns the region.
	 * @return String
	 */
	public String getRegion();

	/**
	 * Returns the street.
	 * @return String
	 */
	public String getStreet();

	/**
	 * Sets the city.
	 * @param city The city to set
	 */
	public void setCity(String city);

	/**
	 * Sets the country.
	 * @param country The country to set
	 */
	public void setCountry(String country);

	/**
	 * Sets the extendedAddress.
	 * @param extendedAddress The extendedAddress to set
	 */
	public void setExtendedAddress(String extendedAddress);

	/**
	 * Sets the pCode.
	 * @param pCode The pCode to set
	 */
	public void setZipCode(String pCode);

	/**
	 * Sets the poBox.
	 * @param poBox The poBox to set
	 */
	public void setPoBox(String poBox);

	/**
	 * Sets the region.
	 * @param region The region to set
	 */
	public void setRegion(String region);

	/**
	 * Sets the street.
	 * @param street The street to set
	 */
	public void setStreet(String street);
}
