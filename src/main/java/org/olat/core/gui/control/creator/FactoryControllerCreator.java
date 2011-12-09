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
package org.olat.core.gui.control.creator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * This Controller creator can be used to instantiate an ActionExtension in
 * olat_extensions.xml which uses a Factory to get a controller. Needed
 * arguments: - factoryName name of the factory class - factoryMethod Method to
 * invoke on factory, should return a Controller
 * 
 * <P>
 * Initial Date: 30.03.2009 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class FactoryControllerCreator extends AutoCreator implements ControllerCreator {
	private static final Class[] ARGCLASSES = new Class[] { UserRequest.class, WindowControl.class };
	private String factoryName;
	private String factoryMethod;

	public FactoryControllerCreator() {
		super();
	}

	/**
	 * @see org.olat.core.gui.control.creator.ControllerCreator#createController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		Exception re = null;
		try {
			Object o = null;
			Class<?> cclazz = Thread.currentThread().getContextClassLoader().loadClass(factoryName);

			try {
				Constructor<?> con = cclazz.getConstructor(ARGCLASSES);
				o = con.newInstance(new Object[] { lureq, lwControl });
			} catch (NoSuchMethodException e) {
				// constructor does not exist with arguments ARGCLASSES => try invoking
				// method without instantiated class (o = null).
			}

			Method method = cclazz.getMethod(factoryMethod, ARGCLASSES);
			Object result = method.invoke(o, new Object[] { lureq, lwControl });
			Controller c = (Controller) result;
			return c;
		} catch (ClassNotFoundException e) {
			re = e;
		} catch (SecurityException e) {
			re = e;
		} catch (NoSuchMethodException e) {
			re = e;
		} catch (IllegalArgumentException e) {
			re = e;
		} catch (IllegalAccessException e) {
			re = e;
		} catch (InvocationTargetException e) {
			re = e;
		} catch (InstantiationException e) {
			re = e;
		} finally {
			if (re != null) { throw new AssertException("could not create controller via reflection. factoryName: " + factoryName + " method: "
					+ factoryMethod, re); }
		}
		return null;
	}

	/**
	 * @return Returns the factoryName.
	 */
	public String getFactoryName() {
		return factoryName;
	}

	/**
	 * @param factoryName The factoryName to set.
	 */
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	/**
	 * @return Returns the factoryMethod.
	 */
	public String getFactoryMethod() {
		return factoryMethod;
	}

	/**
	 * @param factoryMethod The factoryMethod to set.
	 */
	public void setFactoryMethod(String factoryMethod) {
		this.factoryMethod = factoryMethod;
	}

	public String getClassName() {
		return getFactoryName();
	}

}
