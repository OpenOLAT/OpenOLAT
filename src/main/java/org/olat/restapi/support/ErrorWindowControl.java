package org.olat.restapi.support;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.id.context.BusinessControl;

/**
 * 
 * Description:<br>
 * Collect messages: info, warning and error<br>
 * WARNING! use this class with extrem cautious and only if you know what you do
 * as some methods return null!
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ErrorWindowControl implements WindowControl {
	private String info;
	private String error;
	private String warning;
	
	public ErrorWindowControl() {/* */}
	
	@Override
	public void pushToMainArea(Component comp) {/* */}

	@Override
	public void pushAsModalDialog(Component comp) {/* */}

	@Override
	public void pop() {/* */}

	public String getInfo() {
		return info;
	}

	@Override
	public void setInfo(String string) {
		this.info = string;
	}
	
	public String getError() {
		return error;
	}
	@Override
	public void setError(String string) {
		this.error = string;
	}
	
	public String getWarning() {
		return warning;
	}

	@Override
	public void setWarning(String string) {
		this.warning = string;
	}

	@Override
	public WindowControlInfo getWindowControlInfo() {
		return null;
	}

	@Override
	public void makeFlat() {/* */}

	@Override
	public BusinessControl getBusinessControl() {
		return null;
	}

	@Override
	public WindowBackOffice getWindowBackOffice() {
		return null;
	}
}