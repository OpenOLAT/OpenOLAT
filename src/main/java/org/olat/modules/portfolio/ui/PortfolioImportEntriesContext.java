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
package org.olat.modules.portfolio.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;


/**
 * Initial date: 26.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class PortfolioImportEntriesContext {
	
	public static final String CONTEXT_KEY = PortfolioImportEntriesContext.class.getSimpleName();
	
	private BinderSecurityCallback binderSecurityCallback;
	
	private Binder currentBinder;
	private String newBinderTitle;
	private String newBinderDescription;
	private File newBinderImage;
	private String newBinderImageName;
	private List<TextBoxItem> newBinderCategories = new ArrayList<>();
	
	private Section currentSection;
	private String newSectionTitle;
	private String newSectionDescription;
	
	private Set<Integer> selectedEntries = new HashSet<>();
	private List<PortfolioElementRow> selectedPortfolioEntries = new ArrayList<>();
	
	
	public Section getCurrentSection() {
		return currentSection;
	}
	
	public void setCurrentSection(Section currentSection) {
		this.currentSection = currentSection;
	}
	
	public Binder getCurrentBinder() {
		return currentBinder;
	}
	
	public void setCurrentBinder(Binder currentBinder) {
		this.currentBinder = currentBinder;
	}
	
	public BinderSecurityCallback getBinderSecurityCallback() {
		return binderSecurityCallback;
	}
	
	public void setBinderSecurityCallback(BinderSecurityCallback binderSecurityCallback) {
		this.binderSecurityCallback = binderSecurityCallback;
	}
	
	public Set<Integer> getSelectedEntries() {
		return selectedEntries;
	}
	
	public void setSelectedEntries(Set<Integer> selectedEntries) {
		this.selectedEntries = selectedEntries;
	}
	
	public List<PortfolioElementRow> getSelectedPortfolioEntries() {
		return selectedPortfolioEntries;
	}
	
	public void setSelectedPortfolioEntries(List<PortfolioElementRow> selectedPortfolioEntries) {
		this.selectedPortfolioEntries = selectedPortfolioEntries;
	}
	
	public String getNewSectionTitle() {
		return newSectionTitle;
	}
	
	public void setNewSectionTitle(String newSectionTitle) {
		this.newSectionTitle = newSectionTitle;
	}
	
	public String getNewSectionDescription() {
		return newSectionDescription;
	}
	
	public void setNewSectionDescription(String newSectionDescription) {
		this.newSectionDescription = newSectionDescription;
	}

	public String getNewBinderTitle() {
		return newBinderTitle;
	}

	public void setNewBinderTitle(String newBinderTitle) {
		this.newBinderTitle = newBinderTitle;
	}

	public String getNewBinderDescription() {
		return newBinderDescription;
	}

	public void setNewBinderDescription(String newBinderDescription) {
		this.newBinderDescription = newBinderDescription;
	}

	public File getNewBinderImage() {
		return newBinderImage;
	}

	public void setNewBinderImage(File newBinderImage) {
		this.newBinderImage = newBinderImage;
	}
	
	public String getNewBinderImageName() {
		return newBinderImageName;
	}
	
	public void setNewBinderImageName(String newBinderImageName) {
		this.newBinderImageName = newBinderImageName;
	}
	
	public List<TextBoxItem> getNewBinderCategories() {
		return newBinderCategories;
	}
	
	public void setNewBinderCategories(List<TextBoxItem> newBinderCategories) {
		this.newBinderCategories = newBinderCategories;
	}
}
