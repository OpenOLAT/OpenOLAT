/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.application;

import org.olat.modules.selectus.model.ApplicationRef;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationRefImpl implements ApplicationRef {
	
	private final Long key;
	
	public ApplicationRefImpl(Long key) {
		this.key = key;
	}
	
	@Override
	public Long getKey() {
		return key;
	}

}
