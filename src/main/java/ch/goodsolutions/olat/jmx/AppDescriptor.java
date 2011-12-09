/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * This software is protected by the OLAT software license.<br>
 * Use is subject to license terms.<br>
 * See LICENSE.TXT in this distribution for details.
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch
 *
 * All rights reserved.
 * <p>
 */
package ch.goodsolutions.olat.jmx;

public class AppDescriptor {

	private String contextPath;
	private String webappBasePath;
	private String state;
	private String instanceID;
	private String version;
	private String build;

	public AppDescriptor(String contextPath, String webappBasePath, String state, String instanceID, String version, String build) {
		this.contextPath = contextPath;
		this.webappBasePath = webappBasePath;
		this.state = state;
		this.instanceID = instanceID;
		this.version = version;
		this.build = build;
	}

	public boolean isGenuinOLAT() {
		return build != null;
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public String getWebappBasePath() {
		return webappBasePath;
	}

	public String getState() {
		return state;
	}
	
	public String getInstanceID() {
		return instanceID;
	}

	public String getVersion() {
		return version;
	}

	public String getBuild() {
		return build;
	}

	public void setState(String state) {
		this.state = state;
	}

}
