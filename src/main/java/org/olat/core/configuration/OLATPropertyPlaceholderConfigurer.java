/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.configuration;

import java.io.File;

import org.olat.core.logging.StartupException;
import org.olat.core.util.FileUtils;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Description:<br>
 * this overwrites the default spring property placeholder configurator with our custom one
 * 
 * <P>
 * Initial Date:  01.02.2010 <br>
 * @author guido
 */
public class OLATPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	private String localProps = "olat.local.properties";
	private final String INTRO = "************************************************************************************************\n" +
													"OLAT comes with default config values in olat.propeties and you can overwrite this properties " +
													"with a file called olat.local.properties which gets search on the classpath.\nThe classpath is searched in the following order: " +
													"WEB-INF/classes $CATALINA_HOME/lib .\n" +
													"\nTo have the local config outside of OLAT we recoment to use $CATALINA_HOME/lib folder for your configuration";
	public OLATPropertyPlaceholderConfigurer() {
		//check at construction time whether the olat.local,properties file exists
		//in not try to create an empty one, if this fails throw an exception
		Resource overwritePropertiesRes = new ClassPathResource(localProps);
		if (!overwritePropertiesRes.exists()) {
			String catalinaHome = System.getProperty("catalina.home");
			if (catalinaHome == null) {
				String msg = 	INTRO +
										"There is no "+localProps+" file in the classpath and I cannot figure out where to save one." +
										" Please save yourself an empty "+localProps+" file to your tomcat/lib/ directory" +
										"************************************************************************************************\n";
				throw new StartupException(msg);
			}
			File props = new File(catalinaHome+"/lib/"+localProps);
			FileUtils.save(props, "", "utf-8");
			throw new StartupException(INTRO+ "\n\nOLAT Created automatically an empty "+localProps+" file for you at \""+props.getAbsolutePath()+"\", just start OLAT again and your done!\n");
		}
	}

}
