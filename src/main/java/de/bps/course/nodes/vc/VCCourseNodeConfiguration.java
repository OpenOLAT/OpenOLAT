//<OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2010 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package de.bps.course.nodes.vc;

import java.util.List;
import java.util.Locale;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;

import de.bps.course.nodes.VCCourseNode;

/**
 * Description:<br>
 * Configuration for date lists - Virtual Classroom dates.
 *
 * <P>
 * Initial Date: 04.07.2010 <br>
 *
 * @author Jens Lindner(jlindne4@hs-mittweida.de)
 * @author skoeber
 */
public class VCCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

	public String getAlias() {
		return "vc";
	}

	public String getIconCSSClass() {
		return "o_vc_icon";
	}

	public CourseNode getInstance() {
		return new VCCourseNode();
	}

	public String getLinkCSSClass() {
		return "o_vc_icon";
	}

	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(this.getClass(), locale, fallback);
		return translator.translate("title_vc");
	}
	
	@Override
	public boolean isEnabled() {
		return super.isEnabled();
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

	public void setup() {
		// no special setup necessary
	}

	public void tearDown() {
		// no special tear down necessary
	}

}
//</OLATCE-103>