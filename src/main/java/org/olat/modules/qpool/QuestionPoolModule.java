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
package org.olat.modules.qpool;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.qpool.site.QuestionPoolSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qpoolModule")
public class QuestionPoolModule extends AbstractSpringModule implements ConfigOnOff {
	
	@Autowired
	private List<QPoolSPI> questionPoolProviders;

	private VFSContainer rootContainer;
	
	@Autowired
	public QuestionPoolModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("QPool",
				new SiteContextEntryControllerCreator(QuestionPoolSite.class));

	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public VFSContainer getRootContainer() {
		if(rootContainer == null) {
			rootContainer = new OlatRootFolderImpl(File.separator + "qpool", null);
		}
		return rootContainer;
	}

	public List<QPoolSPI> getQuestionPoolProviders() {
		List<QPoolSPI> providers = new ArrayList<QPoolSPI>(questionPoolProviders);
		Collections.sort(providers, new QuestionPoolSPIComparator());
		return providers;
	}
	
	public QPoolSPI getQuestionPoolProvider(String format) {
		for(QPoolSPI provider:questionPoolProviders) {
			if(format.equals(provider.getFormat())) {
				return provider;
			}
		}
		return null;
	}
	
	public void addQuestionPoolProvider(QPoolSPI provider) {
		int currentIndex = -1;
		for(int i=questionPoolProviders.size(); i-->0; ) {
			QPoolSPI currentProvider = questionPoolProviders.get(i);
			if(provider.getFormat() != null &&
					provider.getFormat().equals(currentProvider.getFormat())) {
				currentIndex = i;
			}
		}
		
		if(currentIndex >= 0) {
			questionPoolProviders.set(currentIndex, provider);
		} else {
			questionPoolProviders.add(provider);
		}
	}
}
