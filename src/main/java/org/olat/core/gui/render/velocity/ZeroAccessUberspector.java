/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.render.velocity;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.SecureIntrospectorControl;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.slf4j.Logger;

/**
 * 
 * Initial date: 26 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ZeroAccessUberspector extends UberspectImpl {
	
    @Override
    public void init() {
        introspector = new NoAccessIntrospectorImpl(log);
    }
  
    /**
     * Get an iterator from the given object.  Since the superclass method
     * this secure version checks for execute permission.
     *
     * @param obj object to iterate over
     * @param i line, column, template info
     * @return Iterator for object
     */
    @Override
	public Iterator getIterator(Object obj, Info i) {
    	log.debug("Access to iterator from {} refused due to security restrictions.", obj.getClass().getName());
        return null;
    }
    
    public static class NoAccessIntrospectorImpl extends Introspector implements SecureIntrospectorControl {
    	
        public NoAccessIntrospectorImpl(final Logger log)
        {
            super(log);
        }
        
        @Override
    	public Method getMethod(Class<?> clazz, String methodName, Object[] params)
    	throws IllegalArgumentException {
            return null;
        }

    	@Override
    	public boolean checkObjectExecutePermission(Class<?> clazz, String method) {
    		return false;
    	}
    }
}
