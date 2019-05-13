/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.portfolio.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.portfolio.model.EPFilterSettings;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Manager to handle users settings depending the ePortfolio
 * 
 * <P>
 * Initial Date:  30.11.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
@Service("epSettingsManager")
public class EPSettingsManager {

	private static final Logger log = Tracing.createLoggerFor(EPSettingsManager.class);

	private static final String EPORTFOLIO_ARTEFACTS_ATTRIBUTES = "eportfolio-artAttrib";
	private static final String EPORTFOLIO_FILTER_SETTINGS = "eportfolio-filterSettings";
	private static final String EPORTFOLIO_LASTUSED_STRUCTURE = "eportfolio-lastStruct";
	private static final String EPORTFOLIO_ARTEFACTS_VIEWMODE = "eportfolio-artViewMode";
	private static final String EPORTFOLIO_CATEGORY = "eportfolio";
	
	@Autowired
	private PropertyManager propertyManager;
	
	public EPSettingsManager(){
		//
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Boolean> getArtefactAttributeConfig(Identity ident) {
		Property p = propertyManager.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_ATTRIBUTES);
		TreeMap<String, Boolean> disConfig;
		if (p == null) {
			disConfig = new TreeMap<>();
			// attributes from an artefact ?!
			disConfig.put("artefact.author", true);
			disConfig.put("artefact.description", false);
			disConfig.put("artefact.reflexion", false);
			disConfig.put("artefact.source", true);
			disConfig.put("artefact.sourcelink", false);
			disConfig.put("artefact.title", true);
			disConfig.put("artefact.date", true);
			disConfig.put("artefact.tags", true);
			disConfig.put("artefact.used.in.maps", true);
			disConfig.put("artefact.handlerdetails", false);
		} else {
			XStream xStream = XStreamHelper.createXStreamInstance();
			disConfig = (TreeMap<String, Boolean>) xStream.fromXML(p.getTextValue());
		}
		return disConfig;
	}
	
	public void setArtefactAttributeConfig(Identity ident, Map<String, Boolean> artAttribConfig) {
		Property p = propertyManager.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_ATTRIBUTES);
		if (p == null) {
			p = propertyManager.createUserPropertyInstance(ident, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_ATTRIBUTES, null, null, null, null);
		}
		XStream xStream = XStreamHelper.createXStreamInstance();
		String artAttribXML = xStream.toXML(artAttribConfig);
		p.setTextValue(artAttribXML);
		propertyManager.saveProperty(p);
	}
	
	@SuppressWarnings("unchecked")
	public List<EPFilterSettings> getSavedFilterSettings(Identity ident){
		Property p = propertyManager.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_FILTER_SETTINGS);
		List<EPFilterSettings> result = new ArrayList<>();
		if (p == null) {
			result.add(new EPFilterSettings());
		} else {
			XStream xStream = XStreamHelper.createXStreamInstance();
			xStream.aliasType("EPFilterSettings", EPFilterSettings.class);
			try {
				result = (List<EPFilterSettings>) xStream.fromXML(p.getTextValue());
			} catch (Exception e) {
				//it's not a live critical part
				log.warn("Cannot read filter settings", e);
			}
		}
		return result;		
	}
	
	public void setSavedFilterSettings(Identity ident, List<EPFilterSettings> filterList){
		Property p = propertyManager.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_FILTER_SETTINGS);
		if (p == null) {
			p = propertyManager.createUserPropertyInstance(ident, EPORTFOLIO_CATEGORY, EPORTFOLIO_FILTER_SETTINGS, null, null, null, null);
		}
		// don't persist filters without a name
		for (Iterator<EPFilterSettings> iterator = filterList.iterator(); iterator.hasNext();) {
			EPFilterSettings epFilterSettings = iterator.next();
			if (!StringHelper.containsNonWhitespace(epFilterSettings.getFilterName())){
				iterator.remove();
			}
		}
		XStream xStream = XStreamHelper.createXStreamInstance();
		xStream.aliasType("EPFilterSettings", EPFilterSettings.class);
		String filterListXML = xStream.toXML(filterList);
		p.setTextValue(filterListXML);
		propertyManager.saveProperty(p);		
	}
	
	public void deleteFilterFromUsersList(Identity ident, String filterID){
		List<EPFilterSettings> usersFilters = getSavedFilterSettings(ident);
		for (Iterator<EPFilterSettings> iterator = usersFilters.iterator(); iterator.hasNext();) {
			EPFilterSettings epFilterSettings = iterator.next();
			if (epFilterSettings.getFilterId().equals(filterID)) iterator.remove();
		}
		setSavedFilterSettings(ident, usersFilters);
	}
	
	public String getUsersPreferedArtefactViewMode(Identity ident, String context){
		Property p = propertyManager.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_VIEWMODE + "." + context);
		if (p != null) {
			return p.getStringValue();
		}
		return null;		
	}
	
	public void setUsersPreferedArtefactViewMode(Identity ident, String preferedMode, String context){
		Property p = propertyManager.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_VIEWMODE + "." + context);
		if (p == null) {
			p = propertyManager.createUserPropertyInstance(ident, EPORTFOLIO_CATEGORY, EPORTFOLIO_ARTEFACTS_VIEWMODE + "." + context, null, null, null, null);
		}
		p.setStringValue(preferedMode);
		propertyManager.saveProperty(p);
	}
	
	public void setUsersLastUsedPortfolioStructure(Identity ident, PortfolioStructure struct){
		Property p = propertyManager.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_LASTUSED_STRUCTURE);
		if (p == null) {
			p = propertyManager.createUserPropertyInstance(ident, EPORTFOLIO_CATEGORY, EPORTFOLIO_LASTUSED_STRUCTURE, null, null, null, null);
		}
		p.setLongValue(struct.getKey());
		propertyManager.saveProperty(p);
	}
	
	public Long getUsersLastUsedPortfolioStructureKey (Identity ident) {
		Property p = propertyManager.findProperty(ident, null, null, EPORTFOLIO_CATEGORY, EPORTFOLIO_LASTUSED_STRUCTURE);
		if (p != null) {
			return p.getLongValue();
		}
		return null;
	}
	
}
