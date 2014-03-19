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
package org.olat.course.statistic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.logging.AssertException;
import org.olat.course.ICourse;

public class StatisticAutoCreator extends AutoCreator {

	private final Class<?>[] ARGCLASSES = new Class[] { UserRequest.class, WindowControl.class, ICourse.class, IStatisticManager.class }; 
	private String className;
	private IStatisticManager statisticManager_;
	
	@Override
	public Controller createController(UserRequest lureq, WindowControl lwControl) {
		return super.createController(lureq, lwControl);
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
	
	/** set by spring **/
	public void setStatisticManager(IStatisticManager statisticManager) {
		statisticManager_ = statisticManager;
	}
	
	public Controller createController(UserRequest lureq, WindowControl lwControl, ICourse course){
		Exception re = null;
		try {
			Class<?> cclazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			Constructor<?> con = cclazz.getConstructor(ARGCLASSES);
			Object o = con.newInstance(new Object[]{ lureq, lwControl, course, statisticManager_});
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
	
}