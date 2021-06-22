/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.control.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.site.model.CourseSiteConfiguration;
import org.olat.course.site.model.LanguageConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * Description:<br>
 * This is the module for sites definition and configuration
 * 
 * <P>
 * Initial Date:  12.07.2005 <br>
 *
 * @author Felix Jost
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("olatsites")
public class SiteDefinitions extends AbstractSpringModule {
	
	private static final Logger log = Tracing.createLoggerFor(SiteDefinitions.class);

	private Map<String,SiteDefinition> siteDefMap;
	private Map<String,SiteConfiguration> siteConfigMap = new ConcurrentHashMap<>();
	
	private String configSite1;
	private String configSite2;
	private String configSite3;
	private String configSite4;
	private String sitesSettings;
	
	@Autowired
	private List<SiteDefinition> configurers;
	
	private static final XStream xStream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				CourseSiteConfiguration.class, LanguageConfiguration.class,
				SiteConfiguration.class
			};
		xStream.addPermission(new ExplicitTypePermission(types));
		
		xStream.alias("coursesite", CourseSiteConfiguration.class);
		xStream.alias("languageConfig", LanguageConfiguration.class);
		xStream.alias("siteconfig", SiteConfiguration.class);
	}
	
	@Autowired
	public SiteDefinitions(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	
	
	public String getConfigCourseSite1() {
		return configSite1;
	}

	public void setConfigCourseSite1(String config) {
		setStringProperty("site.1.config", config, true);
	}

	public String getConfigCourseSite2() {
		return configSite2;
	}

	public void setConfigCourseSite2(String config) {
		setStringProperty("site.2.config", config, true);
	}
	
	public String getConfigCourseSite3() {
		return configSite3;
	}

	public void setConfigCourseSite3(String config) {
		setStringProperty("site.3.config", config, true);
	}
	
	public String getConfigCourseSite4() {
		return configSite4;
	}

	public void setConfigCourseSite4(String config) {
		setStringProperty("site.4.config", config, true);
	}
	
	public SiteConfiguration getConfigurationSite(String id) {
		return siteConfigMap.computeIfAbsent(id, springId -> {
			SiteConfiguration c = new SiteConfiguration();
			c.setId(id);
			return c;
		});
	}
	
	public SiteConfiguration getConfigurationSite(SiteDefinition siteDef) {
		for(Map.Entry<String, SiteDefinition> entry: siteDefMap.entrySet()) {
			if(entry.getValue() == siteDef) {
				return getConfigurationSite(entry.getKey());
			}
		}
		return null;
	}
	
	public CourseSiteConfiguration getConfigurationCourseSite1() {
		if(StringHelper.containsNonWhitespace(configSite1)) {
			return (CourseSiteConfiguration)xStream.fromXML(configSite1);
		}
		return null;
	}

	public void setConfigurationCourseSite1(CourseSiteConfiguration config) {
		if(config == null) {
			setConfigCourseSite1("");
		} else {
			String configStr = xStream.toXML(config);
			setConfigCourseSite1(configStr);
		}
	}

	public CourseSiteConfiguration getConfigurationCourseSite2() {
		if(StringHelper.containsNonWhitespace(configSite2)) {
			return (CourseSiteConfiguration)xStream.fromXML(configSite2);
		}
		return null;
	}

	public void setConfigurationCourseSite2(CourseSiteConfiguration config) {
		if(config == null) {
			setConfigCourseSite2("");
		} else {
			String configStr = xStream.toXML(config);
			setConfigCourseSite2(configStr);
		}
	}
	
	public CourseSiteConfiguration getConfigurationCourseSite3() {
		if(StringHelper.containsNonWhitespace(configSite3)) {
			return (CourseSiteConfiguration)xStream.fromXML(configSite3);
		}
		return null;
	}

	public void setConfigurationCourseSite3(CourseSiteConfiguration config) {
		if(config == null) {
			setConfigCourseSite3("");
		} else {
			String configStr = xStream.toXML(config);
			setConfigCourseSite3(configStr);
		}
	}
	
	public CourseSiteConfiguration getConfigurationCourseSite4() {
		if(StringHelper.containsNonWhitespace(configSite4)) {
			return (CourseSiteConfiguration)xStream.fromXML(configSite4);
		}
		return null;
	}

	public void setConfigurationCourseSite4(CourseSiteConfiguration config) {
		if(config == null) {
			setConfigCourseSite4("");
		} else {
			String configStr = xStream.toXML(config);
			setConfigCourseSite4(configStr);
		}
	}

	public String getSitesSettings() {
		return sitesSettings;
	}

	public void setSitesSettings(String config) {
		setStringProperty("sites.config", config, true);
	}
	
	public List<SiteConfiguration> getSitesConfiguration() {
		if(StringHelper.containsNonWhitespace(sitesSettings)) {
			return new ArrayList<>(siteConfigMap.values());
		}
		return Collections.emptyList();
	}

	public void setSitesConfiguration(List<SiteConfiguration> configs) {
		String configStr = xStream.toXML(configs);
		setSitesSettings(configStr);
	}
	
	@Override
	public void init() {
		if(configurers != null) {
			log.debug("{} sites configurers found.", configurers.size());
		}
		
		String sitesObj = getStringPropertyValue("sites.config", true);
		if(StringHelper.containsNonWhitespace(sitesObj)) {
			sitesSettings = sitesObj;
			
			@SuppressWarnings("unchecked")
			List<SiteConfiguration> configs = (List<SiteConfiguration>)xStream.fromXML(sitesSettings);
			for(SiteConfiguration siteConfig:configs) {
				siteConfigMap.put(siteConfig.getId(), siteConfig);
			}
		}
		
		String site1Obj = getStringPropertyValue("site.1.config", true);
		if(StringHelper.containsNonWhitespace(site1Obj)) {
			configSite1 = site1Obj;
		}
		
		String site2Obj = getStringPropertyValue("site.2.config", true);
		if(StringHelper.containsNonWhitespace(site2Obj)) {
			configSite2 = site2Obj;
		}
		
		String site3Obj = getStringPropertyValue("site.3.config", true);
		if(StringHelper.containsNonWhitespace(site3Obj)) {
			configSite3 = site3Obj;
		}
		
		String site4Obj = getStringPropertyValue("site.4.config", true);
		if(StringHelper.containsNonWhitespace(site4Obj)) {
			configSite4 = site4Obj;
		}
	}

	@Override
	protected void initDefaultProperties() {
		//do nothing
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	private Map<String,SiteDefinition> getAndInitSiteDefinitionList() {
		if (siteDefMap == null) { // first try non-synchronized for better performance
			synchronized(this) {
				if (siteDefMap == null) {
					Map<String,SiteDefinition> siteDefs = CoreSpringFactory.getBeansOfType(SiteDefinition.class);
					siteDefMap = new ConcurrentHashMap<>(siteDefs);

					List<SiteConfiguration> configs = getSitesConfiguration();
					Map<String,SiteConfiguration> siteConfigs = new HashMap<>();
					for(SiteConfiguration siteConfig:configs) {
						siteConfigs.put(siteConfig.getId(), siteConfig);
					}
					
					for(Map.Entry<String, SiteDefinition> entry: siteDefs.entrySet()) {
						String id = entry.getKey();
						SiteConfiguration config;
						if(siteConfigs.containsKey(id)) {
							config = siteConfigs.get(id);
						} else {
							SiteDefinition siteDef = entry.getValue();
							config = new SiteConfiguration();
							config.setId(id);
							config.setEnabled(siteDef.isEnabled());
							config.setOrder(siteDef.getOrder());
							config.setSecurityCallbackBeanId(siteDef.getDefaultSiteSecurityCallbackBeanId());
						}
						siteConfigMap.put(config.getId(), config);
					}
				}
			}
		}
		return siteDefMap;
	}

	public List<SiteDefinition> getSiteDefList() {
		Map<String,SiteDefinition> allDefList = getAndInitSiteDefinitionList();
		List<SiteDefinitionOrder> enabledOrderedSites = new ArrayList<>(allDefList.size());
		for(Map.Entry<String,SiteDefinition> siteDefEntry:allDefList.entrySet()) {
			String id = siteDefEntry.getKey();
			SiteDefinition siteDef = siteDefEntry.getValue();
			if(siteDef.isFeatureEnabled()) {
				if(siteConfigMap.containsKey(id)) {
					SiteConfiguration config = siteConfigMap.get(id);
					if(config.isEnabled()) {
						enabledOrderedSites.add(new SiteDefinitionOrder(siteDef, config));
					}
				} else if(siteDef.isEnabled()) {
					enabledOrderedSites.add(new SiteDefinitionOrder(siteDef));
				}
			}
		}
		Collections.sort(enabledOrderedSites, new SiteDefinitionOrderComparator());

		List<SiteDefinition> sites = new ArrayList<>(allDefList.size());
		for(SiteDefinitionOrder orderedSiteDef: enabledOrderedSites) {
			sites.add(orderedSiteDef.getSiteDef());
		}
		return sites;
	}
	
	public Map<String,SiteDefinition> getAllSiteDefinitionsList() {
		Map<String,SiteDefinition> allDefList = getAndInitSiteDefinitionList();
		return new HashMap<>(allDefList);
	}
	
	private static class SiteDefinitionOrder {
		private final int order;
		private final SiteDefinition siteDef;
		
		public SiteDefinitionOrder(SiteDefinition siteDef) {
			this.siteDef = siteDef;
			this.order = siteDef.getOrder();
		}
		
		public SiteDefinitionOrder(SiteDefinition siteDef, SiteConfiguration config) {
			this.siteDef = siteDef;
			this.order = config.getOrder();
		}
		
		public int getOrder() {
			return order;
		}
		
		public SiteDefinition getSiteDef() {
			return siteDef;
		}

	}
	
	private static class SiteDefinitionOrderComparator implements Comparator<SiteDefinitionOrder> {

		@Override
		public int compare(SiteDefinitionOrder s1, SiteDefinitionOrder s2) {
			int o1 = s1.getOrder();
			int o2 = s2.getOrder();
			return o1 - o2;
		}

	}
}

