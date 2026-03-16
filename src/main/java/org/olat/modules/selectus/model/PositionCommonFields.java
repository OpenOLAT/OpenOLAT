/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Locale;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 15.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PositionCommonFields {

	public String getAvailableLanguages();

	public default String[] getAvailableLanguagesArray() {
		if(StringHelper.containsNonWhitespace(getAvailableLanguages())) {
			return getAvailableLanguages().split(",");
		}
		return new String[0];
	}
	
	public String getPositionTitle();
	
	public String getPositionTitleDe();
	
	public String getPositionTitleFr();
	
	public String getPositionTitle(Locale locale);
	
	public String getDepartment();
	
	public String getDepartmentDe();
	
	public String getDepartmentFr();
	
	public String getDepartment(Locale locale);

}
