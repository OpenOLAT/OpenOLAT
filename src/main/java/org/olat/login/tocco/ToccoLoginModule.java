package org.olat.login.tocco;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ToccoLoginModule extends AbstractSpringModule implements ConfigOnOff  {
	
	public static final String TOCCO_PROVIDER = "TOCCO";
	
	@Value("${tocco.enable:true}")
	private boolean enabled;	
	@Value("${tocco.server.url:true}")
	private String toccoServerUrl;
	
	@Autowired
	public ToccoLoginModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		// 
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public String getToccoServerUrl() {
		return toccoServerUrl;
	}

}
