package org.olat.core.gui;

import org.olat.core.gui.control.winmgr.AJAXFlags;

/**
 * 
 * Initial date: 28 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DefaultGlobalSettings implements GlobalSettings {
	@Override
	public int getFontSize() {
		return 100;
	}
	
	@Override
	public AJAXFlags getAjaxFlags() {
		return new EmptyAJAXFlags();
	}
	
	@Override
	public boolean isIdDivsForced() {
		return false;
	}
	
	private static class EmptyAJAXFlags extends AJAXFlags {
		
		public EmptyAJAXFlags() {
			super(null);
		}
		
		@Override
		public boolean isIframePostEnabled() {
			return false;
		}
	}
}
