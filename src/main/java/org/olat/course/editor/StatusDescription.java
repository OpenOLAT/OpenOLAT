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
*/

package org.olat.course.editor;

import java.util.Locale;
import java.util.logging.Level;

import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.course.condition.ConditionEditController;

/**
 * Initial Date: Jun 20, 2005 <br>
 * 
 * @author patrick
 */
public class StatusDescription implements ValidationStatus {
	public final static StatusDescription NOERROR = new StatusDescription();

	private Level theLevel;
	private String shortDesc;
	private String longDesc;
	private String transPckg;
	private String[] params;
	private String isForUnit;

	private String resolveIssueViewIdentifier = null;

	private StatusDescription() {
		theLevel = Level.OFF;
		shortDesc = null;
		longDesc = null;
	}

	public StatusDescription(Level severity, String shortDescKey, String longDescKey, String[] descParams, String translatorPackage) {
		theLevel = severity;
		shortDesc = shortDescKey;
		longDesc = longDescKey;
		transPckg = translatorPackage;
		params = descParams;
	}

	/**
	 * @return getLevel() == ERROR
	 */
	public boolean isError() {
		return theLevel.equals(ERROR);
	}

	/**
	 * @return getLevel() == WARNING
	 */
	public boolean isWarning() {
		return theLevel.equals(WARNING);
	}

	/**
	 * @return getLevel() == INFO
	 */
	public boolean isInfo() {
		return theLevel.equals(INFO);
	}

	/**
	 * the status level corresponds to the definitions in the java util logging
	 * spec.
	 * 
	 * @see java.util.logging.Level
	 * @return
	 */
	public Level getLevel() {
		return theLevel;
	}

	/**
	 * localized short description of the status providing a summary (line).
	 * 
	 * @param locale
	 * @return
	 */
	public String getShortDescription(Locale locale) {
		Translator f = Util.createPackageTranslator(ConditionEditController.class, locale);
		Translator t = new PackageTranslator(transPckg, locale, f);
		return t.translate(shortDesc, params);
	}

	public String getShortDescriptionKey() {
		return shortDesc;
	}

	/**
	 * localized long description of the status containing details, references
	 * etc.
	 * 
	 * @param locale
	 * @return
	 */
	public String getLongDescription(Locale locale) {
		Translator f = Util.createPackageTranslator(ConditionEditController.class, locale);
		Translator t = new PackageTranslator(transPckg, locale, f);
		return t.translate(longDesc, params);
	}

	public String getLongDescriptionKey() {
		return longDesc;
	}

	/**
	 * set the unit identifier for which the status description is. I.e. a course
	 * node id
	 * 
	 * @param name
	 */
	public void setDescriptionForUnit(String name) {
		this.isForUnit = name;
	}

	/**
	 * @return the unit identifier for which the status description is.
	 */
	public String getDescriptionForUnit() {
		return isForUnit;
	}

	/**
	 * It is not always needed to create a complete helper wizard but sufficient
	 * to just activate an exisiting component. I.e. a tab in in tabbed pane as it
	 * is the case in the course editor.
	 * 
	 * @return view identifier for calling activate of an activateable
	 */
	public String getActivateableViewIdentifier() {
		return resolveIssueViewIdentifier;
	}

	public void setActivateableViewIdentifier(String viewIdent) {
		resolveIssueViewIdentifier = viewIdent;
	}

	public String[] getDescriptionParams() {
		return params;
	}

	/**
	 * status description may change their meaning. I.e. the same error/warning in
	 * the course editor means something different during publish.
	 * 
	 * @param longDescKey
	 * @param shortDescKey
	 * @param paramsNew
	 * @return
	 */
	public StatusDescription transformTo(String longDescKey, String shortDescKey, String[] paramsNew) {
		String[] theParams = paramsNew != null ? paramsNew : params;
		StatusDescription retVal = new StatusDescription(theLevel, shortDescKey, longDescKey, theParams, transPckg);
		retVal.isForUnit = this.isForUnit;
		retVal.resolveIssueViewIdentifier = this.resolveIssueViewIdentifier;
		return retVal;
	}

}
