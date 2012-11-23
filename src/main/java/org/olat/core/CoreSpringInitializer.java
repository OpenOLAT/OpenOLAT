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
package org.olat.core;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * Bootstrap the spring initialization to import some
 * specific configurations via import + placeholders
 * 
 * 
 * @author srosse, stepgane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoreSpringInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {
  
	public void initialize(ConfigurableWebApplicationContext ctx) {
  	//detect activemq
  	String jmsProvider;
  	try {
			Class.forName("org.apache.activemq.command.ActiveMQTopic");
			jmsProvider = "activemq";
		} catch (ClassNotFoundException e) {
			jmsProvider = "jndi";
		}
  	PropertySource<String> ps = new OpenOLATProperties(jmsProvider);
    ctx.getEnvironment().getPropertySources().addFirst(ps);
	}
	
	private static class OpenOLATProperties extends PropertySource<String> {
		private final String jmsProvider;
		
		public OpenOLATProperties(String jmsProvider) {
			super("openOLATProperties");
			this.jmsProvider = jmsProvider;
		}

		@Override
		public Object getProperty(String name) {
			if("jms.provider".equals(name)) {
				return jmsProvider;
			}
			return null;
		}
		
	}
}

