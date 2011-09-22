package org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.quotespliter;

import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin;

public class QuoteSpliterPlugin extends TinyMCECustomPlugin {
	/** The TinyMCE plugin name */
	public static final String PLUGIN_NAME = "quotespliter";
	
	@Override
	public String getPluginButtons() {
		return null;
	}

	@Override
	public String getPluginButtonsLocation() {
		return null;
	}

	@Override
	public int getPluginButtonsRowForProfile(int profile) {
		return 0;
	}

	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}

	@Override
	public boolean isEnabledForProfile(int profile) {
		return true;
	}
}
