/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.render.velocity;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.util.introspection.SecureUberspector;

/**
 * 
 * Initial date: 26 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class VelocityFactory {
	
	protected static final String RESTRICTED_PACKAGES = "java.lang.reflect,org.openjdk.nashorn,bsh";
	
	private VelocityFactory() {
		//
	}
	
	/**
	 * 
	 * @return A velocity engine
	 */
	public static final VelocityEngine createEngine(boolean cache) {
		// init velocity engine
		Properties p = new Properties();
		VelocityEngine velocityEngine = null;
		try {
			velocityEngine = new VelocityEngine();
			if(cache) {
				p.setProperty(RuntimeConstants.RESOURCE_MANAGER_CACHE_CLASS, "org.olat.core.gui.render.velocity.InfinispanResourceCache");
			}
	        p.setProperty(RuntimeConstants.INTROSPECTOR_RESTRICT_PACKAGES, RESTRICTED_PACKAGES);
			p.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
	        p.setProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, "false");
			p.setProperty(RuntimeConstants.INPUT_ENCODING, VelocityModule.getInputEncoding());
			velocityEngine.init(p);
			velocityEngine.removeDirective("include");
			velocityEngine.removeDirective("evaluate");
		} catch (Exception e) {
			throw new RuntimeException("config error " + p);
		}
		return velocityEngine;
	}
	
	public static final VelocityEngine createNoIntrospectEngine() {
		// init velocity engine
		Properties p = new Properties();
		VelocityEngine velocityEngine = null;
		try {
			velocityEngine = new VelocityEngine();
	        p.setProperty(RuntimeConstants.INTROSPECTOR_RESTRICT_PACKAGES, RESTRICTED_PACKAGES);
			p.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ZeroAccessUberspector.class.getName());
	        p.setProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, "false");
			p.setProperty(RuntimeConstants.INPUT_ENCODING, VelocityModule.getInputEncoding());
			velocityEngine.init(p);
			velocityEngine.removeDirective("include");
			velocityEngine.removeDirective("evaluate");
		} catch (Exception e) {
			throw new RuntimeException("config error " + p);
		}
		return velocityEngine;
	}

}
