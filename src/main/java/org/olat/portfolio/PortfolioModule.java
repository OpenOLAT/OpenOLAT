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
package org.olat.portfolio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * The PortfolioModule contains the configurations for the e-Portfolio feature
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
@Service("portfolioModule")
public class PortfolioModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final Logger log = Tracing.createLoggerFor(PortfolioModule.class);
	

	@Value("${portfolio.enabled:false}")
	private boolean enabled;
	private VFSContainer portfolioRoot;
	@Value("${portfolio.map.styles}")
	private String availableMapStylesProperty;
	private List<String> availableMapStyles = new ArrayList<>();
	@Value("${portfolio.offer.public.map.list:true}")
	private boolean offerPublicMapList;
	@Value("${wizard.step.reflexion:true}")
	private boolean isReflexionStepEnabled;
	@Value("${wizard.step.copyright:true}")
	private boolean isCopyrightStepEnabled;
	
	@Autowired
	private List<EPArtefactHandler<?>> artefactHandlers;
	
	@Autowired
	public PortfolioModule(CoordinatorManager coordinatorManager){
		super(coordinatorManager);
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
			availableMapStyles = new ArrayList<>();
			for(String style:styles.split(",")) {
				availableMapStyles.add(style);
			}
		}
		
		String offerPublicSetting = getStringPropertyValue("portfolio.offer.public.map.list", true);
		if(StringHelper.containsNonWhitespace(offerPublicSetting)) {
			setOfferPublicMapList("true".equals(offerPublicSetting));
		}
		
		isReflexionStepEnabled = getBooleanPropertyValue("wizard.step.reflexion");
		isCopyrightStepEnabled = getBooleanPropertyValue("wizard.step.copyright");
		
		log.info("ePortfolio is enabled: " + Boolean.toString(enabled));
	}
	
	/**
	 * removes the portfolio temp directory if it exists
	 * FXOLAT-386
	 * 
	 */
	private void cleanPortfolioTmpDir(){
		log.info("beginning to delete ePortfolio temp directory...");
		Path portfolioTmp = Paths.get(FolderConfig.getCanonicalRoot(), "tmp", "portfolio");
		if(portfolioTmp.toFile().exists()) {
			try {
				FileUtils.deleteDirsAndFiles(portfolioTmp);
			} catch (IOException e) {
				log.error("Cannot properly delete portfolio temporary portfolio", e);
			}
		}
	}

	@Override
	protected void initDefaultProperties() {
		availableMapStyles = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(availableMapStylesProperty)) {	
			for(String style:availableMapStylesProperty.split(",")) {
				availableMapStyles.add(style);
			}
		}
		cleanPortfolioTmpDir();
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			setStringProperty("portfolio.enabled", Boolean.toString(enabled), true);
			this.enabled = enabled;
		}
	}
	
	/**
	 * Return all the configured artefact handlers, enabled or not
	 * @return
	 */
	public List<EPArtefactHandler<?>> getAllAvailableArtefactHandlers() {
		List<EPArtefactHandler<?>> handlers = new ArrayList<>(artefactHandlers.size());
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
		List<EPArtefactHandler<?>> handlers = new ArrayList<>(artefactHandlers.size());
		for(EPArtefactHandler<?> handler:artefactHandlers) {
			if(handler.isEnabled()) {
				handlers.add(handler);
			}
		}
		return handlers;
	}
	
	public EPArtefactHandler<?> getArtefactHandler(String type) {
		for(EPArtefactHandler<?> handler:artefactHandlers) {
			if(type.equals(handler.getType())) {
				return handler;
			}
		}
		log.warn("Either tried to get a disabled handler or could not return a handler for artefact-type: " + type);
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
			((EPAbstractHandler<?>)artefacthandler).setEnabled(true);
		}
	}

	public boolean removeArtefactHandler(EPArtefactHandler<?> artefacthandler) {
		boolean removed = false;
		for(Iterator<EPArtefactHandler<?>> it=artefactHandlers.iterator(); it.hasNext(); ) {
			if(it.next().getType().equals(artefacthandler.getType())) {
				it.remove();
				removed = true;
			}
		}
		return removed;
	}
	
	public VFSContainer getPortfolioRoot() {
		if(portfolioRoot == null) {
			portfolioRoot = VFSManager.olatRootContainer(File.separator + "portfolio", null);
		}
		return portfolioRoot;
	}

	/**
	 * @param availableMapStyles The availableMapStyles to set.
	 */
	public void setAvailableMapStylesStr(String availableMapStylesStr) {
		this.availableMapStyles = new ArrayList<>();
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
	 * should the artefact collect wizard contain a step to collect a reflexion
	 * @return
	 */
	public boolean isReflexionStepEnabled(){
		return isReflexionStepEnabled;
	}
	
	/**
	 * should the artefact collect wizard contain a step to ask user for copyright on content
	 * @return
	 */
	public boolean isCopyrightStepEnabled(){
		return isCopyrightStepEnabled;
	}
	
	public void setReflexionStepEnabled(boolean isReflexionStepEnabled) {
		this.isReflexionStepEnabled = isReflexionStepEnabled;
		setBooleanProperty("wizard.step.reflexion", isReflexionStepEnabled, true);
	}

	public void setCopyrightStepEnabled(boolean isCopyrightStepEnabled) {
		this.isCopyrightStepEnabled = isCopyrightStepEnabled;
		setBooleanProperty("wizard.step.copyright", isCopyrightStepEnabled, true);
	}
	
}
