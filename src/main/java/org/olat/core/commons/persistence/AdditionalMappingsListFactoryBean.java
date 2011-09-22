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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.commons.persistence;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreBeanTypes;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * Description:<br>
 * FactoryBean
 * 
 * <P>
 * Initial Date:  23 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AdditionalMappingsListFactoryBean implements FactoryBean<List>, ApplicationContextAware {
	private ApplicationContext appCtx;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.appCtx = applicationContext;
	}

	@Override
	public List<AdditionalDBMappings> getObject() throws Exception {
		List<AdditionalDBMappings> result = new ArrayList<AdditionalDBMappings>();
        result.addAll(appCtx.getBeansOfType(CoreBeanTypes.additionalDBMappings.getExtensionTypeClass(), false, false).values());
        return result;
	}

	@Override
	public Class<List> getObjectType() {
		return List.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
