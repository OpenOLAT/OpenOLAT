/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Date;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 16 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface OrganisationUnitMembership {
	
	public Long getKey();
	
	public Date getCreationDate();
	
	public Identity getIdentity();
	
	public OrganisationUnit getOrganisationUnit();

}
