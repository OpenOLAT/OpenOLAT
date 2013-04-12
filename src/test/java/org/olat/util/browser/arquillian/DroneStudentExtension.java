package org.olat.util.browser.arquillian;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;

public class DroneStudentExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder.service(Instantiator.class, StudentFactory.class);
        builder.service(Destructor.class, StudentFactory.class);
        builder.service(Configurator.class, StudentFactory.class);
	}

}
