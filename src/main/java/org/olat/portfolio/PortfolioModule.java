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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.portfolio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.DeletableGroupData;
import org.olat.group.DeletableReference;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.ElementType;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.properties.NarrowedPropertyManager;
import org.olat.properties.Property;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * The PortfolioModule contains the configurations for the e-Portfolio feature
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class PortfolioModule extends AbstractOLATModule implements ConfigOnOff, UserDataDeletable, DeletableGroupData {
	
	private List<EPArtefactHandler<?>> artefactHandlers = new ArrayList<EPArtefactHandler<?>>();

	private boolean enabled;
	private VFSContainer portfolioRoot;
	private List<String> availableMapStyles = new ArrayList<String>();
	private boolean offerPublicMapList;
	
	public PortfolioModule(){
		BusinessGroupManagerImpl.getInstance().registerDeletableGroupDataListener(this);		
	}
	
	@Override
	public void init() {
		//portfolio enabled/disabled
		String enabledObj = getStringPropertyValue("portfolio.enabled", true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		for(EPArtefactHandler<?> handler:artefactHandlers) {
			String enabledHandler = getStringPropertyValue("handler." + handler.getClass().getName(), true);
			if(StringHelper.containsNonWhitespace(enabledHandler)) {
				((EPAbstractHandler<?>)handler).setEnabled("true".equals(enabledHandler));
			}
		}
		
		String styles = getStringPropertyValue("portfolio.map.styles", true);
		if(StringHelper.containsNonWhitespace(styles)) {
			this.availableMapStyles = new ArrayList<String>();
			for(String style:styles.split(",")) {
				availableMapStyles.add(style);
			}
		}
		
		String offerPublicSetting = getStringPropertyValue("portfolio.offer.public.map.list", true);
		if(StringHelper.containsNonWhitespace(offerPublicSetting)) {
			setOfferPublicMapList("true".equals(offerPublicSetting));
		}
		
		logInfo("ePortfolio is enabled: " + Boolean.toString(enabled));
	}

	@Override
	protected void initDefaultProperties() {
		enabled = getBooleanConfigParameter("portfolio.enabled", true);
		
		for(EPArtefactHandler<?> handler:artefactHandlers) {
			boolean enabledHandler = getBooleanConfigParameter("handler." + handler.getClass().getName(), true);
			((EPAbstractHandler<?>)handler).setEnabled(enabledHandler);
		}
		
		this.availableMapStyles = new ArrayList<String>();
		String styles = this.getStringConfigParameter("portfolio.map.styles", "default", false);
		if(StringHelper.containsNonWhitespace(styles)) {	
			for(String style:styles.split(",")) {
				availableMapStyles.add(style);
			}
		}
		
		setOfferPublicMapList(getBooleanConfigParameter("portfolio.offer.public.map.list", true));
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			setStringProperty("portfolio.enabled", Boolean.toString(enabled), true);
		}
	}
	
	/**
	 * Return all the configured artefact handlers, enabled or not
	 * @return
	 */
	public List<EPArtefactHandler<?>> getAllAvailableArtefactHandlers() {
		List<EPArtefactHandler<?>> handlers = new ArrayList<EPArtefactHandler<?>>(artefactHandlers.size());
		handlers.addAll(artefactHandlers);
		return handlers;
	}
	
	public void setEnableArtefactHandler(EPArtefactHandler<?> handler, boolean enabled) {
		setStringProperty("handler." + handler.getClass().getName(), Boolean.toString(enabled), true);
	}

	/**
	 * Return the enabled artefact handler
	 * @return
	 */
	public List<EPArtefactHandler<?>> getArtefactHandlers() {
		List<EPArtefactHandler<?>> handlers = new ArrayList<EPArtefactHandler<?>>(artefactHandlers.size());
		for(EPArtefactHandler<?> handler:artefactHandlers) {
			if(handler.isEnabled()) {
				handlers.add(handler);
			}
		}
		return handlers;
	}

	/**
	 * [used by Spring]
	 * @param artefacthandlers
	 */
	public void setArtefactHandlers(List<EPArtefactHandler<?>> artefacthandlers) {
		this.artefactHandlers.addAll(artefacthandlers);
	}
	
	public EPArtefactHandler<?> getArtefactHandler(String type) {
		for(EPArtefactHandler<?> handler:artefactHandlers) {
			if(type.equals(handler.getType())) {
				return handler;
			}
		}
		logWarn("Either tried to get a disabled handler or could not return a handler for artefact-type: " + type, null);
		return null;
	}
	
	public EPArtefactHandler<?> getArtefactHandler(AbstractArtefact artefact) {
		return getArtefactHandler(artefact.getResourceableTypeName());
	}
	
	public void addArtefactHandler(EPArtefactHandler<?> artefacthandler) {
		artefactHandlers.add(artefacthandler);

		String settingName = "handler." + artefacthandler.getClass().getName();
		String propEnabled = getStringPropertyValue(settingName, true);
		if(StringHelper.containsNonWhitespace(propEnabled)) {
			//system properties settings
			((EPAbstractHandler<?>)artefacthandler).setEnabled("true".equals(propEnabled));
		} else {
			//default settings
			boolean defEnabled = getBooleanConfigParameter(settingName, true);
			((EPAbstractHandler<?>)artefacthandler).setEnabled(defEnabled);
		}
	}

	public boolean removeArtefactHandler(EPArtefactHandler<?> artefacthandler) {
		return artefactHandlers.remove(artefacthandler);
	}
	
	public VFSContainer getPortfolioRoot() {
		if(portfolioRoot == null) {
			portfolioRoot = new OlatRootFolderImpl(File.separator + "portfolio", null);
		}
		return portfolioRoot;
	}

	/**
	 * @param availableMapStyles The availableMapStyles to set.
	 */
	public void setAvailableMapStylesStr(String availableMapStylesStr) {
		this.availableMapStyles = new ArrayList<String>();
		if(StringHelper.containsNonWhitespace(availableMapStylesStr)) {	
			String[] styles = availableMapStylesStr.split(",");
			for(String style:styles) {
				availableMapStyles.add(style);
			}
		}
	}

	/**
	 * @return Returns the availableMapStyles.
	 */
	public List<String> getAvailableMapStyles() {
		return availableMapStyles;
	}
	
	public void setAvailableMapStylesS(List<String> availableMapStyles) {
		this.availableMapStyles = availableMapStyles;
	}

	/**
	 * @param offerPublicMapList The offerPublicMapList to set.
	 */
	public void setOfferPublicMapList(boolean offerPublicMapList) {
		this.offerPublicMapList = offerPublicMapList;
	}

	/**
	 * Return setting for public map list.
	 * Systems with more than 500 public maps, should disable this feature as it gets too slow!
	 * @return Returns the offerPublicMapList.
	 */
	public boolean isOfferPublicMapList() {
		return offerPublicMapList;
	}
	
	/**
	 * [spring]
	 * 
	 * @param userDeletionManager
	 */
	@Autowired(required = true)
	public void setUserDeletionManager(final UserDeletionManager userDeletionManager) {
		userDeletionManager.registerDeletableUserData(this);
	}

	// used for user deletion
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		ePFMgr.deleteUsersArtefacts(identity);
		
		List<PortfolioStructure> userPersonalMaps = ePFMgr.getStructureElementsForUser(identity, ElementType.DEFAULT_MAP, ElementType.STRUCTURED_MAP);
		for (PortfolioStructure portfolioStructure : userPersonalMaps) {
			
			ePFMgr.deletePortfolioStructure(portfolioStructure);
		}
		
	}

	// used for group deletion
	@Override
	public DeletableReference checkIfReferenced(BusinessGroup group, Locale locale) {
		return DeletableReference.createNoDeletableReference(); // dont show special reference info, just delete a linked map
	}

	// used for group deletion
	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(group);
		final Property mapKeyProperty = npm.findProperty(null, null, CollaborationTools.PROP_CAT_BG_COLLABTOOLS, CollaborationTools.KEY_PORTFOLIO);
		if (mapKeyProperty != null) {
			final Long mapKey = mapKeyProperty.getLongValue();
			final PortfolioStructure map = ePFMgr.loadPortfolioStructureByKey(mapKey);
			ePFMgr.deletePortfolioStructure(map);
			return true;
		}
		return false;
	}
	
}
