/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.PersonName;

/**
 * 
 * Initial date: 17.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface SalutationGenerator {

	public String getTitleLastName(Identity identity, Locale locale);
	
	public String getTitleLastName(PersonName person, Locale locale);
	
	public String getTitleLastName(Application app, Locale locale);
	
	public String getTitleLastName(ApplicationShort app, Locale locale);
	
	public String getTitleLastName(ApplicationShort app, List<? extends ApplicationShort> applicationList, Locale locale);
	

	public String getTitleFirstLastName(Identity identity, Locale locale);
	
	public String getTitleFirstLastName(PersonName person, Locale locale);
	
	public String getTitleFirstLastName(Application app, Locale locale);
	
	public String getTitleFirstLastName(ApplicationShort app, Locale locale);
	
	public String getTitleFirstLastName(ApplicationShort app, List<? extends ApplicationShort> applicationList, Locale locale);


	public String getTitleFullname(Identity identity, Locale locale);
	
	public String getTitleFullname(PersonName person, Locale locale);
	
	public String getTitleFullname(Application app, Locale locale);
	
	public String getTitleFullname(Application app, List<Application> applicationList, Locale locale);
	
	public String getTitleFullname(ApplicationShort app, Locale locale);
	
	
	public String getSalutation(Identity identity, Locale locale);
	
	public String getSalutation(PersonName person, Locale locale);
	
	public String getSalutation(Application app, Locale locale);

	public String getSalutation(ApplicationShort app, Locale locale);


	public String getFullname(Identity identity, Locale locale);
	
	public String getFullname(PersonName person, Locale locale);

	public String getFullname(Application app, Locale locale);
	
	public String getFullname(ApplicationShort app, Locale locale);
	
	public String getFullname(Application app, List<Application> applicationList, Locale locale);

}
