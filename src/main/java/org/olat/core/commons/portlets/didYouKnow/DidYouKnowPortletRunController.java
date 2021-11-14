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

package org.olat.core.commons.portlets.didYouKnow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;

/**
 * Description:<br>
 * Run controller for the "did you know" portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class DidYouKnowPortletRunController extends DefaultController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(DidYouKnowPortlet.class);
	private PackageTranslator trans;
	private VelocityContainer didYouKnowVC;
	private int currentLookupPosition = 0;
	private List<String> lookupList;
	private Link nextLink;
	
	/**
	 * Internal constructor
	 * @param ureq
	 */
	protected DidYouKnowPortletRunController(UserRequest ureq, WindowControl wControl) {
		super(wControl);
		this.trans = (PackageTranslator)Util.createPackageTranslator(DidYouKnowPortlet.class, ureq.getLocale());
		this.didYouKnowVC = new VelocityContainer("didYouKnowVC", VELOCITY_ROOT + "/didYouKnowPortlet.html", trans, this);
		nextLink = LinkFactory.createLink("next", didYouKnowVC, this);
		
		// search in property file from this package for all questions
		Properties propertiesFile = I18nManager.getInstance().getResolvedProperties(trans.getLocale(), trans.getPackageName());
		
		//fxdiff FXOLAT-103 lookup in base language if nothing found in original language
		if (propertiesFile.size() == 0) {
			String langCode = trans.getLocale().getLanguage();
			String countryCode = trans.getLocale().getCountry();
			String variant = trans.getLocale().getVariant();
			// first try without variant
			if (StringHelper.containsNonWhitespace(variant)) {
				Locale loc = I18nManager.getInstance().getLocaleOrNull(langCode + "_" + countryCode);
				if (loc == null) {
					loc = I18nManager.getInstance().getLocaleOrNull(langCode);
				}
				if (loc != null) {					
					propertiesFile = I18nManager.getInstance().getResolvedProperties(loc, trans.getPackageName());
				}
			}
			// if still nothing found, try without country
			if (propertiesFile.size() == 0) {
				if (StringHelper.containsNonWhitespace(countryCode)) {
					Locale loc = I18nManager.getInstance().getLocaleOrNull(langCode);
					if (loc != null) {					
						propertiesFile = I18nManager.getInstance().getResolvedProperties(loc, trans.getPackageName());
					}			
				}
			}
		}

		lookupList = new ArrayList<>();
		Set<Object> keys =  propertiesFile.keySet();
		for (Iterator<Object> iterator = keys.iterator(); iterator.hasNext();) {
			String key = (String)iterator.next();
			// Questions start with 'Q-'
			if (key.startsWith("Q-")) {
				String id = key.substring(2, key.length());
				lookupList.add(id);
			}
		}
		Collections.shuffle(lookupList);
		// calculate the next tip number		
		addNextQuestionToVelocity();
		
		setInitialComponent(didYouKnowVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == nextLink) {
			addNextQuestionToVelocity();
		}
	}

	/**
	 * Gets the question from the translation files and pushes it to velocity
	 */
	private void addNextQuestionToVelocity() {
		if (lookupList.size() == 0) {
			didYouKnowVC.contextPut("questionId", -1);
		} else {
			// reset and start from 0 when reaching the end
			if (currentLookupPosition >= lookupList.size()) {
				currentLookupPosition = 0;
			}
			// get question and answer for given id
			String questionId = lookupList.get(currentLookupPosition);
			didYouKnowVC.contextPut("questionId", questionId);
			// update position
			currentLookupPosition++;
		}
	}
}