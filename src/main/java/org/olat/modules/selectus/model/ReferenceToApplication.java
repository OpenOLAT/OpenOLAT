/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;

/**
 * 
 * Initial date: 6 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ReferenceToApplication extends CreateInfo {

	public Long getKey();
	
	public Application getApplication();

	public Reference getReference();
	
}
