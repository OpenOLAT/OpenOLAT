package org.olat.core.gui.components.date;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Description:<br>
 * Wrapper for the DateComponent to use in flexi form layout.
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DateElement extends FormItemImpl {
	
	private final DateComponent dateComponent;
	
	public DateElement(String name, DateComponent dateComponent) {
		super(name);
		this.dateComponent = dateComponent;
	}
	
	@Override
	protected Component getFormItemComponent() {
		return dateComponent;
	}
	@Override
	protected void rootFormAvailable() {
		//
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void validate(List validationResults) {
		//
	}
	
	@Override
	public void reset() {
		//
	}
}
