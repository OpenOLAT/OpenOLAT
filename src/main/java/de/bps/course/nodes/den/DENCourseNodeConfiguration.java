/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.course.nodes.den;

import java.util.List;
import java.util.Locale;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;

import de.bps.course.nodes.DENCourseNode;

public class DENCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {
	
	DENCourseNodeConfiguration() {
	}
	
	public String getAlias() {
		return "den";
	}

	public String getIconCSSClass() {
		return "o_den_icon";
	}

	public CourseNode getInstance() {
		return new DENCourseNode();
	}

	public String getLinkCSSClass() {
		return null;
	}

	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_den");
	}

	public ExtensionResource getExtensionCSS() {
		return null;
	}

	public List getExtensionResources() {
		return null;
	}

	public String getName() {
		return getAlias();
	}

}
