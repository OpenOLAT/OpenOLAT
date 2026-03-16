/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 11 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ApplicationAttributeLight extends Persistable {
	
	public Long getApplicationKey();
	
	public Long getPositionKey();
	
	public Long getDefinitionKey();
	
	public String getValue();

}
