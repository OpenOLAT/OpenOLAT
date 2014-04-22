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
* <p>
*/ 
package org.olat.core.gui.control.creator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * This class automatically creates a controller when needed. the classname of the controller to be
 * created is set via the className setter.
 * The controller to be created must have a constructor with the signature (UserRequest, WindowControl)
 * 
 * This class uses reflection and is a convenience class for creating controllers which need no additional
 * arguments besides the two standard arguments UserRequest and WindowControl.
 * 
 * example usage in spring:
 * <pre>
 * &lt;property name="onSuccessControllerCreator">
 *   &lt;bean class="org.olat.core.gui.control.creator.AutoCreator">
 *     &lt;property name="className" value="ch.goodsolutions.demo.DemoMainController"/>
 *   &lt;/bean>
 * &lt;/property>
 * </pre>
 * 
 * <P>
 * Initial Date: 16.01.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class AutoCreator implements ControllerCreator {
	private static final Class<?>[] ARGCLASSES = new Class[] { UserRequest.class, WindowControl.class }; 
	private String className;

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.creator.ControllerCreator#createController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller createController(UserRequest ureq, WindowControl wControl) {
		if(className == null) return null;
		
		Exception re = null;
		try {
			Class<?> cclazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			Constructor<?> con = cclazz.getConstructor(ARGCLASSES);
			Object o = con.newInstance(new Object[]{ ureq, wControl});
			Controller c = (Controller)o;
			return c;
		} catch (ClassNotFoundException e) {
			re = e;
		} catch (SecurityException e) {
			re = e;
		} catch (NoSuchMethodException e) {
			re = e;
		} catch (IllegalArgumentException e) {
			re = e;
		} catch (InstantiationException e) {
			re = e;
		} catch (IllegalAccessException e) {
			re = e;
		} catch (InvocationTargetException e) {
			re = e;
		}
		finally {
			if (re != null) {
				throw new AssertException("could not create controller via reflection. classname:"+className, re);
			}
		}
		return null;
	}

	/**
	 * [used by spring]
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return Returns the className of the Controller which is created
	 */
	public String getClassName() {
		return className;
	}

}
