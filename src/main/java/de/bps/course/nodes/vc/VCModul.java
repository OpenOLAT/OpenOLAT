// <OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2010 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package de.bps.course.nodes.vc;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LogModule;

/**
 * Description:<br>
 * Virtual classroom configuration, setup by spring
 * 
 * <P>
 * Initial Date: 28.08.2010 <br>
 * 
 * @author Jens Lindner (jlindne4@hs-mittweida.de)
 * @author skoeber
 */
public class VCModul {

	static final OLog log = Tracing.createLoggerFor(LogModule.class);

	private String baseUrl, adminLogin, adminPassword, connectID, version;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getAdminLogin() {
		return adminLogin;
	}

	public void setAdminLogin(String adminLogin) {
		this.adminLogin = adminLogin;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public String getConnectID() {
		return connectID;
	}

	public void setConnectID(String connectID) {
		this.connectID = connectID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
// </OLATCE-103>