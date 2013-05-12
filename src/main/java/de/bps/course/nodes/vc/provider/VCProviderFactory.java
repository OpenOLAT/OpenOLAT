//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Description:<br>
 * Factory to create an instance of a registered virtual classroom provider. Note that
 * the providers are designed to be used as own instances for every single user.
 * 
 * <P>
 * Initial Date:  09.12.2010 <br>
 * @author skoeber
 */
public class VCProviderFactory {
	
	public static String VC_PROVIDER = "vcProvider";
	
	private final static Map<String, VCProvider> _registeredProviders = new HashMap<String, VCProvider>();
	
	public static VCProvider createProvider(String providerId) {
		return _registeredProviders.get(providerId).newInstance();
	}
	
	public static VCProvider createDefaultProvider() {
		if(_registeredProviders == null || _registeredProviders.isEmpty()) {
			return null;
		}
		List<VCProvider> providers = getProviders();
		if(!providers.isEmpty()) {
			return createProvider(providers.get(0).getProviderId());
		}
		return null;
	}
	
	public static boolean existsProvider(String providerId) {
		return _registeredProviders.containsKey(providerId);
	}
	
	public static void registerProvider(VCProvider provider) {
		_registeredProviders.put(provider.getProviderId(), provider);
	}
	
	public void setRegisteredProviders(List<VCProvider> providers) {
		for(VCProvider provider : providers) {
			registerProvider(provider);
		}
	}
	
	public static List<VCProvider> getProviders() {
		List<VCProvider> providers = new ArrayList<VCProvider>();
		for(VCProvider provider:_registeredProviders.values()) {
			if(provider.isEnabled()) {
				providers.add(provider);
			}
		}
		return providers;
	}
	
	/** used by spring */
	public List<VCProvider> getRegisteredProviders() {
		List<VCProvider> providers = new ArrayList<VCProvider>();
		providers.addAll(_registeredProviders.values());
		return providers;
	}

}
//</OLATCE-103>