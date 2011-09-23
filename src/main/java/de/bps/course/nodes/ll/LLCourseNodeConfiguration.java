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
package de.bps.course.nodes.ll;

import java.util.List;
import java.util.Locale;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;

import de.bps.course.nodes.LLCourseNode;

/**
 * Description:<br>
 * Configuration for link lists.
 *
 * <P>
 * Initial Date: 05.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

	/**
	 * [spring only]
	 * @param enabled
	 */
	private LLCourseNodeConfiguration() {
		super();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAlias() {
		return "ll";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconCSSClass() {
		return "o_ll_icon";
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseNode getInstance() {
		return new LLCourseNode();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLinkCSSClass() {
		return "o_ll_icon";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_ll");
	}

	/**
	 * {@inheritDoc}
	 */
	public ExtensionResource getExtensionCSS() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getExtensionResources() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return getAlias();
	}

}
