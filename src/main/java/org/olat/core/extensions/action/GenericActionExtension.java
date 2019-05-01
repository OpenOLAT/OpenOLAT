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
package org.olat.core.extensions.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.extensions.AbstractExtension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;

/**
 * Description:<br>
 * This type of ActionExtension can be used to create menu content nodes together with an implementation
 * of GenericMainController.
 * 
 * <P>
 * Initial Date: 03.07.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GenericActionExtension extends AbstractExtension implements ActionExtension {

	private Map<String, ExtensionElement> elements = new HashMap<>();
	private ControllerCreator actionControllerCreator;
	private String i18nActionKey;
	private String i18nDescriptionKey;
	/*
	 * We use this navigationKey to find the correct actionExtension in a
	 * genericMainController. (to select the correct tree-entry...)
	 */
	private String navigationKey;
	private List<String> alternativeNavigationKeys;
	private List<String> extensionPoints;
	private String translationPackageName;
	private String translationPackageNameDerived;
	private String contentControllerClassName;
	
	private String cssClass;
	private String iconCssClass;
	
	protected final OLog log = Tracing.createLoggerFor(GenericActionExtension.class);
	
	public GenericActionExtension() {
		//only for instantiation by spring
	}
	
	public void initExtensionPoints() {
		for (String extPoint : extensionPoints) {
			elements.put(extPoint, this);
		}
	}
	
	@Override
	public GenericTreeNode createMenuNode(UserRequest ureq){
		GenericTreeNode node = new GenericTreeNode();
		node.setAltText(getDescription(ureq.getLocale()));
		node.setTitle(getActionText(ureq.getLocale()));
		node.setIconCssClass(getIconCssClass());
		node.setCssClass(getCssClass());
		
		node.setUserObject(this);
		return node;
	}
	
	@Override
	public String getUniqueExtensionID(){
		StringBuilder sb = new StringBuilder(128);
		if (extensionPoints != null){
			for(String ext: extensionPoints)
				sb.append(ext).append(":");
		}
		if(getActionController() instanceof AutoCreator){
			sb.append(((AutoCreator) getActionController()).getClassName()).append(":");
		}
		sb.append(getActionText(I18nManager.getInstance().getLocaleOrDefault(null)))
		  .append(":").append(getOrder())
		  .append(":").append(getNavigationKey());
		return sb.toString();	
	}
	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		return actionControllerCreator.createController(ureq, wControl);
	}

	@Override
	public ExtensionElement getExtensionFor(String extensionPoint) {
		if (isEnabled()) return elements.get(extensionPoint);
		else return null;
	}

	@Override
	public String getActionText(Locale loc) {
		Translator translator = createPackageTranslator(loc);
		if (i18nActionKey == null) i18nActionKey = getClassNameOfCorrespondingController() + ".menu.title";
		return translator.translate(i18nActionKey);
	}

	@Override
	public String getDescription(Locale loc) {
		Translator translator = createPackageTranslator(loc);
		if (i18nDescriptionKey == null) i18nDescriptionKey = getClassNameOfCorrespondingController() + ".menu.title.alt";
		return translator.translate(i18nDescriptionKey);
	}

	public String getNavigationKey() {
		return navigationKey;
	}

	public List<String> getAlternativeNavigationKeys() {
		return alternativeNavigationKeys;
	}

	public String getClassNameOfCorrespondingController(){
		if(contentControllerClassName == null) return "";
		return contentControllerClassName.substring(contentControllerClassName.lastIndexOf(".")+1);
	}
	
	private Translator createPackageTranslator(Locale loc){
		if (translationPackageName==null){
			translationPackageName = translationPackageNameDerived;
		}
		return new PackageTranslator(translationPackageName, loc);
	}
	
	/**
	 * [used by spring]
	 */
	public void setTranslationPackage(String transPackage) {
		translationPackageName = transPackage;
	}

	/**
	 * [used by spring] REVIEW:RH:2009-12-19:PB:Fixed problematic cast. 
	 */
	public void setActionController(AutoCreator actionControllerCreator) {
		this.actionControllerCreator = actionControllerCreator;
		contentControllerClassName = actionControllerCreator.getClassName();
		translationPackageNameDerived = contentControllerClassName.substring(0, contentControllerClassName.lastIndexOf("."));
	}

	public void setActionControllerWithClassname(ControllerCreator actionControllerCreator, String contentControllerClassName){
		this.actionControllerCreator = actionControllerCreator;
		this.contentControllerClassName = contentControllerClassName; 
		translationPackageNameDerived = contentControllerClassName.substring(0, contentControllerClassName.lastIndexOf("."));
	}

	protected ControllerCreator getActionController(){
		return actionControllerCreator;
	}
	
	/**
	 * [used by spring]
	 */
	public void setI18nActionKey(String i18nActionKey) {
		this.i18nActionKey = i18nActionKey;
	}

	/**
	 * [used by spring]
	 */
	public void setI18nDescriptionKey(String i18nDescriptionKey) {
		this.i18nDescriptionKey = i18nDescriptionKey;
	}

	public void setNavigationKey(String navKey) {
		this.navigationKey = navKey;
	}
	
	public void setAlternativeNavigationKeys(String keys) {
		if(StringHelper.containsNonWhitespace(keys)) {
			alternativeNavigationKeys = new ArrayList<>();
			for(String key:keys.split(",")) {
				alternativeNavigationKeys.add(key);
			}
		}
	}

	public List<String> getExtensionPoints() {
		return extensionPoints;
	}

	public void setExtensionPoints(List<String> extensionPoints) {
		this.extensionPoints = extensionPoints;
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String classname) {
		cssClass = classname;
	}

	public String getIconCssClass() {
		return iconCssClass;
	}
	
	public void setIconCssClass(String icon) {
		iconCssClass = icon;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder(512);
		sb.append(" controller: ").append(contentControllerClassName);
		sb.append(" actionKey: ").append(i18nActionKey);
		sb.append(" order: ").append(getOrder());
		sb.append(" navigationKey: ").append(navigationKey);
		return sb.toString();
	}
}
