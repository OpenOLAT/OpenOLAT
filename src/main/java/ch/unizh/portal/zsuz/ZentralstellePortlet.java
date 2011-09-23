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
* <p>
*/
package ch.unizh.portal.zsuz;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZentrallStellePortlet
 * 
 * <P>
 * Initial Date:  06.06.2008 <br>
 * @author patrickb
 */
public class ZentralstellePortlet extends AbstractPortlet {

	private ZentralstellePortletRunController runCtrl;
	final static  ZentralstelleIrchel drucki = new ZentralstelleIrchel();
	final static ZentralstelleZentrum druckz = new ZentralstelleZentrum();

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#createInstance(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest, java.util.Map)
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	public Portlet createInstance(WindowControl control, UserRequest ureq, Map portletConfig) { 
		Portlet p = new ZentralstellePortlet();
		p.setName(this.getName());
 	 	p.setConfiguration(portletConfig);
 	 	p.setTranslator(new PackageTranslator(Util.getPackageName(ZentralstellePortlet.class), ureq.getLocale()));
 	 	return p;
	}

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#disposeRunComponent()
	 */
	public void disposeRunComponent() {
		if(this.runCtrl != null) {
			runCtrl.dispose();
			runCtrl = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#getCssClass()
	 */
	public String getCssClass() {
		//the zentralstelle icon
		return "o_portlet_zsuz";
	}

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		return getTranslator().translate("zsuz.infotext0");
	}

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(this.runCtrl != null) runCtrl.dispose();
		runCtrl = new ZentralstellePortletRunController(ureq, wControl);
		return runCtrl.getInitialComponent();
	}

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		return getTranslator().translate("zsuz.title");
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	public void dispose() {
	// TODO Auto-generated method stub

	}

	
	static class ZentralstelleIrchel implements Identity{

		public Long getKey() {
			// TODO Auto-generated method stub
			return null;
		}
		@SuppressWarnings("unused")
		public boolean equalsByPersistableKey(Persistable arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		public Date getLastModified() {
			// TODO Auto-generated method stub
			return null;
		}

		public Date getCreationDate() {
			// TODO Auto-generated method stub
			return null;
		}
		@SuppressWarnings("unused")
		public void setStatus(Integer arg0) {
			// TODO Auto-generated method stub

		}
		@SuppressWarnings("unused")
		public void setLastLogin(Date arg0) {
			// TODO Auto-generated method stub

		}

		public User getUser() {
			return new User(){
				Map<String, String> data = new HashMap<String, String>();
				{
					data.put(UserConstants.FIRSTNAME, "Zsuz Irchel");
					data.put(UserConstants.LASTNAME, "Druckerei Irchel");
					data.put(UserConstants.EMAIL, "drucki@zsuz.uzh.ch");
					data.put(UserConstants.INSTITUTIONALNAME, "Zentralstelle UZH");
					data.put(UserConstants.INSTITUTIONALEMAIL, "drucki@zsuz.uzh.ch");
				}
				
				public Long getKey() {
					// TODO Auto-generated method stub
					return null;
				}
				@SuppressWarnings("unused")
				public boolean equalsByPersistableKey(Persistable persistable) {
					// TODO Auto-generated method stub
					return false;
				}
			
				public Date getLastModified() {
					// TODO Auto-generated method stub
					return null;
				}
			
				public Date getCreationDate() {
					// TODO Auto-generated method stub
					return null;
				}
				@SuppressWarnings("unused")
				public void setProperty(String propertyName, String propertyValue) {
					// TODO Auto-generated method stub
					
				}
				@SuppressWarnings("unused")
				public void setPreferences(Preferences prefs) {
					// TODO Auto-generated method stub
					
				}
				@SuppressWarnings("unused")
				public String getProperty(String propertyName, Locale locale) {					
					return data.get(propertyName);
				}

				public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {
					throw new AssertException("SETTER not yet implemented, not used in case of ZentralstellePortlet");
				}	

				public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
					throw new AssertException("GETTER not yet implemented, not used in case of ZentralstellePortlet");
				}
				
				public Preferences getPreferences() {
					// TODO Auto-generated method stub
					return null;
				}
			
			};
		}

		public Integer getStatus() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return "zentralstelle_druckerei_irchel";
		}

		public Date getLastLogin() {
			// TODO Auto-generated method stub
			return null;
		}
		public void setName(String loginName) {
			// TODO Auto-generated method stub
			
		}

	}
	
	
	static class ZentralstelleZentrum implements Identity{

		public Long getKey() {
			// TODO Auto-generated method stub
			return null;
		}
		@SuppressWarnings("unused")
		public boolean equalsByPersistableKey(Persistable arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		public Date getLastModified() {
			// TODO Auto-generated method stub
			return null;
		}

		public Date getCreationDate() {
			// TODO Auto-generated method stub
			return null;
		}
		@SuppressWarnings("unused")
		public void setStatus(Integer arg0) {
			// TODO Auto-generated method stub

		}
		@SuppressWarnings("unused")
		public void setLastLogin(Date arg0) {
			// TODO Auto-generated method stub

		}

		public User getUser() {
			return new User(){
				Map<String, String> data = new HashMap<String, String>();
				{
					data.put(UserConstants.FIRSTNAME, "Zsuz Zentrum");
					data.put(UserConstants.LASTNAME, "Druckerei Zentrum");
					data.put(UserConstants.EMAIL, "druckz@zsuz.uzh.ch");
					data.put(UserConstants.INSTITUTIONALNAME, "Zentralstelle UZH");
					data.put(UserConstants.INSTITUTIONALEMAIL, "druckz@zsuz.uzh.ch");
				}
				
				public Long getKey() {
					// TODO Auto-generated method stub
					return null;
				}
				@SuppressWarnings("unused")
				public boolean equalsByPersistableKey(Persistable persistable) {
					// TODO Auto-generated method stub
					return false;
				}
			
				public Date getLastModified() {
					// TODO Auto-generated method stub
					return null;
				}
			
				public Date getCreationDate() {
					// TODO Auto-generated method stub
					return null;
				}
				@SuppressWarnings("unused")
				public void setProperty(String propertyName, String propertyValue) {
					// TODO Auto-generated method stub
					
				}
				@SuppressWarnings("unused")
				public void setPreferences(Preferences prefs) {
					// TODO Auto-generated method stub
					
				}
				@SuppressWarnings("unused")
				public String getProperty(String propertyName, Locale locale) {					
					return data.get(propertyName);
				}

				public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {
					throw new AssertException("SETTER not yet implemented, not used in case of ZentralstellePortlet");
				}	

				public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
					throw new AssertException("GETTER not yet implemented, not used in case of ZentralstellePortlet");
				}

				public Preferences getPreferences() {
					// TODO Auto-generated method stub
					return null;
				}
			
			};
		}

		public Integer getStatus() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return "zentralstelle_druckerei_zentrum";
		}

		public Date getLastLogin() {
			// TODO Auto-generated method stub
			return null;
		}
		public void setName(String loginName) {
			// TODO Auto-generated method stub
			
		}

	}
	
}
