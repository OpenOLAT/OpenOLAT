/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.model;

import org.olat.modules.selectus.model.Category;

/**
 * 
 * Initial date: 30 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppToCategory {
	
	private final Category category;
	private final boolean administrative;
	
	public AppToCategory(Category category, boolean administrative) {
		this.category = category;
		this.administrative = administrative;
	}
	
	public Long getCategoryKey() {
		return category.getKey();
	}

	public String getCategoryName() {
		return category.getName();
	}
	
	public String getCategoryColor() {
		return category.getColor();
	}

	public boolean isAdministrative() {
		return administrative;
	}

}
