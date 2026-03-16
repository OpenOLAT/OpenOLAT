/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.category;

import org.olat.modules.selectus.model.Category;

/**
 * 
 * Initial date: 16 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCategoryInfos {
	
	private final Long applicationKey;
	private final Category category;
	private final boolean administrative;
	
	public ApplicationCategoryInfos(Long applicationKey, Category category, boolean administrative) {
		this.applicationKey = applicationKey;
		this.category = category;
		this.administrative = administrative;
	}
	
	public String tagName() {
		if(administrative) {
			return "a:".concat(category.getName());
		}
		return category.getName();
	}

	public Long getApplicationKey() {
		return applicationKey;
	}

	public Category getCategory() {
		return category;
	}
	
	public boolean isAdministrative() {
		return administrative;
	}
}
