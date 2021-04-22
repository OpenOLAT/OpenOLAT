package org.olat.login.tocco;

import org.olat.login.auth.AuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ToccoAuthenticationProvider extends AuthenticationProvider {
	
	@Autowired
	private ToccoLoginModule toccoLoginModule;
	
	protected ToccoAuthenticationProvider(String name, String clazz, boolean enabled, boolean isDefault,
			String iconCssClass) {
		super(name, clazz, enabled, isDefault, iconCssClass);
	}
	
	@Override
	public boolean isEnabled() {
		return toccoLoginModule.isEnabled();
	}



}
