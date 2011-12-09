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

package org.olat.core.commons.services.search.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.olat.core.commons.services.search.ResultDocument;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * Based on the same principe as the AutoCreator, this one creates
 * a ResultControler with the right constructor: UserRequest, WindowControl,
 * Form and ResultDocument.
 * 
 * <P>
 * Initial Date:  8 déc. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ResultControllerCreator {

	private static final Class[] ARGCLASSES = new Class[] { UserRequest.class, WindowControl.class, Form.class, ResultDocument.class }; 

	private String fileType;
	private String className;

	public ResultController createController(UserRequest ureq, WindowControl wControl, Form mainForm, ResultDocument document) {
		Exception re = null;
		try {
			Class cclazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			Constructor con = cclazz.getConstructor(ARGCLASSES);
			Object o = con.newInstance(new Object[]{ ureq, wControl, mainForm, document });
			ResultController c = (ResultController)o;
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

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
}
