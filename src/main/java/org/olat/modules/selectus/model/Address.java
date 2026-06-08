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
package org.olat.modules.selectus.model;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface Address {
	
	public Type getType();
	
	public void setType(Type type);
	
	public String getAddressLine1();

	public void setAddressLine1(String addressLine1);

	public String getAddressLine2();

	public void setAddressLine2(String addressLine2);
	
	public String getAddressLine3();

	public void setAddressLine3(String addressLine3);

	public String getZipCode();

	public void setZipCode(String zipCode);

	public String getCity();

	public void setCity(String city);

	public String getCountry();

	public void setCountry(String country);
	
	public enum Type {
		BUSINESS("business"),
		PRIVATE("private");

		private final String type;
		
		private Type(String type) {
			this.type = type;
		}
		
		public String getType() {
			return type;
		}
		
		public static Type toType(String type) {
			if(type != null && type.length() > 0) {
				for(Type t:Type.values()) {
					if(t.getType().equals(type)) {
						return t;
					}
				}
			}
			return null;
		}
	}
}
