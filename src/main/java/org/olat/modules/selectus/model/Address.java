/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
