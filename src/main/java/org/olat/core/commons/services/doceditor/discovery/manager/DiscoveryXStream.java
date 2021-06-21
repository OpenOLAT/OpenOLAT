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
package org.olat.core.commons.services.doceditor.discovery.manager;

import org.olat.core.commons.services.doceditor.discovery.model.ActionImpl;
import org.olat.core.commons.services.doceditor.discovery.model.AppImpl;
import org.olat.core.commons.services.doceditor.discovery.model.DiscoveryImpl;
import org.olat.core.commons.services.doceditor.discovery.model.NetZoneImpl;
import org.olat.core.commons.services.doceditor.discovery.model.ProofKeyImpl;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 1 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class DiscoveryXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				DiscoveryImpl.class, NetZoneImpl.class, AppImpl.class, ActionImpl.class, ProofKeyImpl.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));

		xstream.alias("wopi-discovery", DiscoveryImpl.class);
		xstream.aliasField("proof-key", DiscoveryImpl.class, "proofKey");
		xstream.addImplicitCollection(DiscoveryImpl.class, "netZones");
		
		xstream.alias("net-zone", NetZoneImpl.class);
		xstream.aliasAttribute(NetZoneImpl.class, "name", "name");
		xstream.addImplicitCollection(NetZoneImpl.class, "apps");
		
		xstream.alias("app", AppImpl.class);
		xstream.aliasAttribute(AppImpl.class, "name", "name");
		xstream.aliasAttribute(AppImpl.class, "favIconUrl", "favIconUrl");
		xstream.aliasAttribute(AppImpl.class, "checkLicense", "checkLicense");
		xstream.addImplicitCollection(AppImpl.class, "actions");
		
		xstream.alias("action", ActionImpl.class);
		xstream.aliasAttribute(ActionImpl.class, "name", "name");
		xstream.aliasAttribute(ActionImpl.class, "ext", "ext");
		xstream.aliasAttribute(ActionImpl.class, "urlSrc", "urlsrc");
		xstream.aliasAttribute(ActionImpl.class, "requires", "requires");
		xstream.aliasAttribute(ActionImpl.class, "targetExt", "targetext");
		
		xstream.alias("proof-key", ProofKeyImpl.class);
		xstream.aliasAttribute(ProofKeyImpl.class, "value", "value");
		xstream.aliasAttribute(ProofKeyImpl.class, "modulus", "modulus");
		xstream.aliasAttribute(ProofKeyImpl.class, "exponent", "exponent");
		xstream.aliasAttribute(ProofKeyImpl.class, "oldValue", "oldvalue");
		xstream.aliasAttribute(ProofKeyImpl.class, "oldModulus", "oldmodulus");
		xstream.aliasAttribute(ProofKeyImpl.class, "oldExponent", "oldexponent");
	}
	
	@SuppressWarnings("unchecked")
	static <U> U fromXml(String xml, @SuppressWarnings("unused") Class<U> cl) {
		Object obj = xstream.fromXML(xml);
		return (U)obj;
	}

}
