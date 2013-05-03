/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.util.browser.arquillian;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
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
