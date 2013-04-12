package org.olat.util.browser.arquillian;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.selenium.configuration.SeleniumConfiguration;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.Instantiator;

import com.thoughtworks.selenium.DefaultSelenium;

public class StudentFactory
	implements Configurator<DefaultSelenium[],StudentConfiguration>,
		Instantiator<DefaultSelenium[],StudentConfiguration>,
		Destructor<DefaultSelenium[]>
{

	@Override
	public int getPrecedence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DefaultSelenium[] createInstance(StudentConfiguration configuration) {
		
		DefaultSelenium[] seleniumArray = new DefaultSelenium[configuration.getCount()];
		
		for(int i = 0; i < configuration.getCount(); i++){
			DefaultSelenium selenium =
					seleniumArray[i] = new DefaultSelenium(configuration.getServerHost(),
					configuration.getServerPort(),
					configuration.getBrowser(),
					configuration.getUrl());
			selenium.start();
	        selenium.setSpeed(String.valueOf(configuration.getSpeed()));
	        selenium.setTimeout(String.valueOf(configuration.getTimeout()));
		}
		
		return(seleniumArray);
	}

	@Override
	public void destroyInstance(DefaultSelenium[] instance) {
		for(DefaultSelenium currentInstance: instance){
			currentInstance.close();
	        currentInstance.stop();
		}
	}

	@Override
	public StudentConfiguration createConfiguration(
			ArquillianDescriptor descriptor,
			Class<? extends Annotation> qualifier) {
		return new StudentConfiguration().configure(descriptor, qualifier);
	}
}
