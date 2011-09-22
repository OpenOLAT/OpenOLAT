package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;

public interface FormLink extends FormItem{

	/**
	 * @param customEnabledLinkCSS The customEnabledLinkCSS to set.
	 */
	public void setCustomEnabledLinkCSS(String customEnabledLinkCSS);

	/**
	 * Set the css that is used for the disabled link status
	 * @param customDisabledLinkCSS
	 */
	public void setCustomDisabledLinkCSS(String customDisabledLinkCSS);

	/**
	 * Set the i18n key for the link text
	 * @param i18n
	 */
	public void setI18nKey(String i18n);

}