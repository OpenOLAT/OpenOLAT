/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
