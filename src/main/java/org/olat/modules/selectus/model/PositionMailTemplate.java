/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Locale;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PositionMailTemplate extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public String getId();
	
	public String getName();
	
	public void setName(String name);
	
	public String getSubject();

	public String getSubjectDe();
	
	public String getSubjectFr();

	public String getSubject(Locale locale);
	
	public void setSubject(String text, Locale locale);
	
	public String getMLSubject(Locale locale);
	
	public String getBody();
	
	public String getBodyDe();
	
	public String getBodyFr();
	
	public String getBody(Locale locale);
	
	public void setBody(String text, Locale locale);
	
	public String getMLBody(Locale locale);
	
	public String getLetter();
	
	public void setLetter(String configuration);

}
