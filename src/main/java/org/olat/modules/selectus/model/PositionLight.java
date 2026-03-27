/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Date;
import java.util.Locale;

import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PositionLight extends PositionRef, OLATResourceable, PositionCommonFields {
	
	public Date getCreationDate();

	public String getPlaningsNumber();
	
	public String getMLDepartment(Locale locale);
	
	public String getMLTitle(Locale locale);

	public Date getApplicationDeadline();
	
	public String getProfessorship();
	
	public Organisation getOrganisation();

	public String getStatus();

	public boolean isValid();
	
	public String toStringFull();
}
