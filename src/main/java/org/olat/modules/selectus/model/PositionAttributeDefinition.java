/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Locale;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * 
 * 
 * Initial date: 3 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PositionAttributeDefinition extends Persistable, CreateInfo, ModifiedInfo {
	
	public PositionApplicationAttributeTabEnum getTabEnum();
	
	public PositionAttributeDefinitionTypeEnum getTypeEnum();
	
	public boolean isMandatory();
	
	public void setMandatory(boolean mandatory);
	
	public <T> T getConfiguration(Class<T> configurationClass);
	
	public void setConfiguration(Object configuration);
	
	public String getAttributeConfiguration();
	
	public void setAttributeConfiguration(String configuration);
	
	public String getLabel(Locale locale);
	
	/**
	 * Check if the specified label is used by the attribute.
	 * 
	 * @param label A string
	 * @return true if the attribute use the specified string as label in some language
	 */
	public boolean useLabel(String label);
	
	/**
	 * 
	 * @param locale
	 * @param lenient
	 * @return
	 */
	public String getLabel(Locale locale, boolean lenient);
	
	public void setLabel(String text, Locale locale);
	
	public String getPlaceholder(Locale locale);
	
	public String getPlaceholder(Locale locale, boolean lenient);
	
	public void setPlaceholder(String text, Locale locale);
	
	public Integer getOrderPosition();

	public void setOrderPosition(Integer orderPosition);

}
