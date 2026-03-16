/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.dispatcher;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.util.i18n.I18nModule;

/**
 * Reference are limited to the default language (english).
 * 
 * 
 * Initial date: 11.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceDispatcher extends AbstractRecruitingDispatcher {
	
	public static final String REFERENCE_SOURCE = "reference";

	public ReferenceDispatcher() {
		super(REFERENCE_SOURCE);
	}
	
	@Override
	protected Locale getLang(UserRequest ureq) {
		return I18nModule.getDefaultLocale();
	}
}
