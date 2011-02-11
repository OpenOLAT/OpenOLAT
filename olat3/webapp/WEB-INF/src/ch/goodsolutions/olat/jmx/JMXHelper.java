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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package ch.goodsolutions.olat.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;


public class JMXHelper {

	public static final String JMX_DOMAIN = "ch.goodsolutions.olat.jmx";
	
	private Logger log = Tracing.getLogger(JMXHelper.class);
	private String serviceURI;
	private String user, pass;
	private MBeanServerConnection connection;
	private static JMXHelper INSTANCE;
	
	public static JMXHelper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JMXHelper(Settings.getServerconfig("server_fqdn"), JMXModule.getPort(), JMXModule.getUser(), JMXModule.getPass());
		}
		return INSTANCE;
	}
	
	private JMXHelper(String host, int port, String user, String pass) {
		this.serviceURI = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
		this.user = user;
		this.pass = pass;
	}
	
	public List findWebappInstances() {
		List instances = new ArrayList();
		Set webappBeanSet = queryNames("Catalina:j2eeType=WebModule,*");

		for (Iterator iter = webappBeanSet.iterator(); iter.hasNext();) {
			ObjectName objName = (ObjectName) iter.next();
			String context = getAttribute(objName, "path");
			String basePath = getAttribute(objName, "docBase");
			String state = getAttribute(objName, "state");
			String instanceID = null;
			String version = null;
			String build = null;
			ObjectName identifierBean = findIdentifierBean(context);
			if (identifierBean != null) {
				instanceID = getAttribute(identifierBean, "InstanceID");
				version = getAttribute(identifierBean, "Version");
				build = getAttribute(identifierBean, "Build");
			}
			instances.add(new AppDescriptor(context, basePath, state, instanceID, version, build));
		}
		return instances;
	}

	private ObjectName findIdentifierBean(String contextPath) {
		Set names = queryNames(JMXHelper.buildRegisteredObjectName(OLATIdentifier.class, contextPath));
		if (names.size() == 1) return (ObjectName)names.iterator().next();
		return null;
	}

	public static String buildRegisteredObjectName(Class clazz, String contextPath) {
		String className = clazz.getName();
		if (className.indexOf('.') > 0)
			className = className.substring(className.lastIndexOf('.') + 1);
		String foo = JMX_DOMAIN + ":class=" + clazz.getName() + ",path=" + contextPath;
		return foo;
	}
	
	public String getAttribute(String objectName, String attribute) {
		try {
			return getAttribute(new ObjectName(objectName), attribute);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getAttribute(ObjectName objectName, String attribute) {
		MBeanServerConnection conn = getConnection();
		if (conn == null) return null;
		try {
			return conn.getAttribute(objectName, attribute).toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	public Set queryNames(String query) {
		Set results = new HashSet();
		MBeanServerConnection conn = getConnection();
		if (conn == null) return results;
		try {
			results = conn.queryNames(new ObjectName(query), null);
		} catch (Exception e) {
			// ignore
		}
		return results;
	}
	
	public MBeanServerConnection getConnection() {
		if (connection != null) return connection;
		try {
			JMXServiceURL url = new JMXServiceURL(serviceURI);
			Map map = new HashMap();
			if (user != null) {
				String[] credentials = new String[] {user, pass};
				map.put("jmx.remote.credentials", credentials);
			}
			JMXConnector conn = JMXConnectorFactory.connect(url, map);
			connection = conn.getMBeanServerConnection();
		} catch (Exception e) {
			log.error("Unable to get JMX connection: ", e);
			return null;
		}
		return connection;
	}
	
}
