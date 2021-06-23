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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.ExcelMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;
import org.olat.modules.taxonomy.model.TaxonomyLevelTypeImpl;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jan 12, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class TaxonomyImportStep1 extends BasicStep {

	private TaxonomyImportContext context;
	
	public TaxonomyImportStep1(UserRequest ureq, TaxonomyImportContext context) {
		super(ureq);
		
		this.context = context;
	
		setI18nTitleAndDescr("import.taxonomy.step.1.title", "import.taxonomy.step.1.desc");
		setNextStep(new TaxonomyImportStep2(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		// Add the context to the StepsRunContext
		// It can be retrieved in every step if needed
		stepsRunContext.put(TaxonomyImportContext.CONTEXT_KEY, context);
		
		return new TaxonomyImportStep1Controller(ureq, windowControl, form, stepsRunContext);
	}
	
	private class TaxonomyImportStep1Controller extends StepFormBasicController {

		private TaxonomyImportContext context;
		
		private FormLink templateDownloadLink;
		private TextAreaElement importDataElement;
		
		private ExcelMediaResource importTemplate;
		private int importColumns;
		
		@Autowired
		private TaxonomyService taxonomyService;
		@Autowired
		private DB dbInstance;
		
		public TaxonomyImportStep1Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT_2_10, null);
			
			this.context = (TaxonomyImportContext) runContext.get(TaxonomyImportContext.CONTEXT_KEY);
			// Import template is always generated so the amount of columns is calculated automatically
			this.importTemplate = createExcelTemplate(ureq);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("import.taxonomy.step.1.desc");
			
			// Add template download link
			templateDownloadLink = uifactory.addFormLink("templateDownloadLink", "import.taxonomy.template.link", "import.taxonomy.template.link.label", formLayout, Link.LINK);
			templateDownloadLink.setIconLeftCSS("o_icon o_filetype_xls o_icon-lg");
			
			// Add input element for taxonomy levels
			importDataElement = uifactory.addTextAreaElement("importElement", "import.taxonomy.levels", -1, 10, -1, false, true, true, null, formLayout);
			importDataElement.setNotEmptyCheck("import.taxonomy.levels.mandatory");
			importDataElement.setMandatory(true);
			importDataElement.setLineNumbersEnbaled(true);
			importDataElement.setStripedBackgroundEnabled(true);
			importDataElement.setFixedFontWidth(true);
		}	
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == templateDownloadLink) {
				ureq.getDispatchResult().setResultingMediaResource(importTemplate);
			}
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			// Fire event to get to the next step
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			allOk &= validateExcelInput(importDataElement);
			
			return allOk;
		}
		
		/**
		 * Creates a template to import contact tracing locations
		 * 
		 * @param ureq
		 * @return A ExcelMediaRessource template file
		 */
		private ExcelMediaResource createExcelTemplate(UserRequest ureq) {
			String charset = UserManager.getInstance().getUserCharset(ureq.getIdentity());
			StringBuilder headerRow = new StringBuilder();
			StringBuilder dataRow1 = new StringBuilder();
			StringBuilder dataRow2 = new StringBuilder();
			
			// Add columns
			importColumns = 0;
			
			// Path
			headerRow.append(translate("taxonomy.level.path")).append("\t");
			dataRow1.append("/MATH").append("\t");
			dataRow2.append("/MATH/GEO").append("\t");
			importColumns++;
			
			// Display name
			headerRow.append(translate("level.displayname")).append("\t");
			dataRow1.append("Mathematik").append("\t");
			dataRow2.append("Geometrie").append("\t");
			importColumns++;
			
			// Type
			headerRow.append(translate("level.type")).append("\t");
			dataRow1.append("F").append("\t");
			dataRow2.append("K").append("\t");
			importColumns++;
			
			// Sort order
			headerRow.append(translate("level.sort.order")).append("\t");
			dataRow1.append("").append("\t");
			dataRow2.append("").append("\t");
			importColumns++;
			
			// Description
			headerRow.append(translate("level.description")).append("\t");
			dataRow1.append("").append("\t");
			dataRow2.append("").append("\t");
			importColumns++;
			
			// Concatenate header and data row
			StringBuilder writeToFile = new StringBuilder();
			writeToFile.append(headerRow).append("\n").append(dataRow1).append("\n").append(dataRow2);
			
			ExcelMediaResource emr = new ExcelMediaResource(writeToFile.toString(), charset);
			emr.setFilename("TaxonomyLevelsImportTemplate");
			return emr;
		}
		
		private boolean validateExcelInput(TextAreaElement inputEl) {
			boolean allOk = validateFormItem(inputEl);
			
			if (allOk == false || !StringHelper.containsNonWhitespace(inputEl.getValue())) {
				return false;
			}
			
			String[] lines = inputEl.getValue().split("\r?\n");
			
			List<TaxonomyLevel> existingLevels = taxonomyService.getTaxonomyLevels(context.getTaxonomy());
			List<TaxonomyLevelType> existingLevelTypes = taxonomyService.getTaxonomyLevelTypes(context.getTaxonomy());
			
			List<TaxonomyLevelRow> reviewList = new ArrayList<>();
			List<TaxonomyLevel> updatedLevels = new ArrayList<>();
			List<TaxonomyLevel> newLevels = new ArrayList<>();
			List<TaxonomyLevelType> newLevelTypes = new ArrayList<>();
			
			List<Integer> errorRows = new ArrayList<>();
			
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				
				if (!StringHelper.containsNonWhitespace(line)) {
					continue;
				}
				
				String delimiter = "\t";
				// Use comma as fallback delimiter, e.g. for better testing
				if (line.indexOf(delimiter) == -1) {
					delimiter = ",";
				}
				
				String[] columns = line.split(delimiter);
				
				if (columns.length < 2) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error", null);
					errorRows.add(i);
					continue;
				}
				
				String path = StringHelper.xssScan(columns[0]).trim().replaceAll("\\s*[/]\\s*", "/");
				String displayName = StringHelper.xssScan(columns[1]).trim();
				String type = columns.length > 2 ? StringHelper.xssScan(columns[2]).trim() : null;
				String orderString = columns.length > 3 ? StringHelper.xssScan(columns[3]).trim() : null;
				String description = columns.length > 4 ? StringHelper.xssScan(columns[4]).trim() : null;		
			
				// If no path is given
				if (!StringHelper.containsNonWhitespace(path) || path.equals("/")) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error", null);
					errorRows.add(i);
					continue;
				}
				

				
				// Split by slash and take last entry as identifier
				// NPE not possible, because of the check above
				String[] identifierArray = path.split("/");
				String identifier = identifierArray[identifierArray.length - 1];
				
				// Check identifier lengt
				if (identifier.length() > 64) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error.identifier.too.long", new String[] {"64"});
					errorRows.add(i);
					continue;
				}
				
				// Check display name lenght
				if (displayName.length() > 255) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error.displayname.too.long", new String[] {"255"});
					errorRows.add(i);
					continue;
				}
				
				
				if (!path.endsWith("/")) {
					path += "/";
				}
				
				Integer order = null;
				if (StringHelper.containsNonWhitespace(orderString)) {
					try {
						order = Integer.valueOf(orderString); 
					} catch (Exception e) {
						allOk = false;
						inputEl.setErrorKey("import.taxonomy.error", null);
						errorRows.add(i);
						continue;
					}
				}
							
				TaxonomyLevel currentLevel = getExistingTaxonomyLevelOrCreate(context.getTaxonomy(), existingLevels, newLevels, path, identifier);
				
				
				if (currentLevel == null) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error", null);
					errorRows.add(i);
					continue;
				}
				
				TaxonomyLevelType currentLevelType = checkLevelType(context.getTaxonomy(), existingLevelTypes, newLevelTypes, type);
				
				if (StringHelper.containsNonWhitespace(type) && currentLevelType == null) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error", null);
					errorRows.add(i);
					continue;
				}
				
				currentLevel.setDisplayName(displayName);
				currentLevel.setDescription(description);
				currentLevel.setSortOrder(order);
				currentLevel.setType(currentLevelType);
				
				if (currentLevel.getKey() == null) {
					newLevels.add(currentLevel);
				} else {
					updatedLevels.add(currentLevel);
				}
				
				if (currentLevelType != null && currentLevelType.getKey() == null && !newLevelTypes.contains(currentLevelType)) {
					newLevelTypes.add(currentLevelType);
				}
				
				reviewList.add(new TaxonomyLevelRow(currentLevel, currentLevel.getKey() != null));
			}
			
			context.setReviewList(reviewList);
			context.setTaxonomyLevelCreateList(newLevels);
			context.setTaxonomyLevelUpdateList(updatedLevels);
			context.setTaxonomyLevelTypeCreateList(newLevelTypes);
			importDataElement.setErrors(errorRows);
			
			return allOk;
		}
		
		private TaxonomyLevelType checkLevelType(Taxonomy taxonomy, List<TaxonomyLevelType> existingLevelTypes, List<TaxonomyLevelType> newLevelTypes, final String type) {
			if (!StringHelper.containsNonWhitespace(type)) {
				return null;
			}
			
			// Check existing types
			List<TaxonomyLevelType> possibleTypes = existingLevelTypes.stream().filter(levelType -> levelType.getIdentifier().equals(type)).collect(Collectors.toList());
			
			// If more than one found, show error
			if (possibleTypes != null && possibleTypes.size() > 1) {
				return null;
			} else if (possibleTypes != null && possibleTypes.size() == 1) {
				return possibleTypes.get(0);
			} 
			
			// Check new types
			List<TaxonomyLevelType> possibleNewTypes = newLevelTypes.stream().filter(newLevelType -> newLevelType.getIdentifier().equals(type)).collect(Collectors.toList());
			
			// If more than one found, show error
			if (possibleNewTypes != null && possibleNewTypes.size() > 1) {
				return null;
			} else if (possibleNewTypes != null && possibleNewTypes.size() == 1) {
				return possibleNewTypes.get(0);
			} 
			
			// Create a new type
			TaxonomyLevelTypeImpl newType = new TaxonomyLevelTypeImpl();
			newType.setTaxonomy(taxonomy);
			newType.setIdentifier(type);
			newType.setDisplayName(type);
			
			return newType;
		}
		
		private TaxonomyLevel getExistingTaxonomyLevelOrCreate(Taxonomy taxonomy, List<TaxonomyLevel> allExistingLevels, List<TaxonomyLevel> newLevels, final String path, final String identifier) {
			String parent = "/";
			
			// Parent path is only existing, if there are at least two slashes and one character inbetween 
			if (path.length() > 3) {
				parent = path.substring(0, path.length() - 1);
				parent = parent.substring(0, parent.lastIndexOf("/") + 1);
			}
			
			// Final to use in lambda expressions
			final String parentPath = parent;
			
			// Search existing taxonomy levels
			List<TaxonomyLevel> existingLevels = allExistingLevels.stream().filter(level -> level.getMaterializedPathIdentifiers().equals(path)).collect(Collectors.toList());
			if (existingLevels != null && existingLevels.size() != 0) {
				// If multiple entries found, don't proceed
				if (existingLevels.size() > 1) {
					return null;
				}
				
				// Return the first level found
				TaxonomyLevel existingLevel = existingLevels.get(0);
				dbInstance.getCurrentEntityManager().detach(existingLevel);
				return existingLevel;
			}
			
			// Search possible parents in existing levels
			List<TaxonomyLevel> possibleParents = allExistingLevels.stream().filter(level -> level.getMaterializedPathIdentifiers().equals(parentPath)).collect(Collectors.toList());
			if (possibleParents != null && possibleParents.size() != 0) {
				// If multiple parents found, don't proceed
				if (possibleParents.size() > 1) {
					return null;
				}
				
				TaxonomyLevelImpl newLevel = new TaxonomyLevelImpl();
				newLevel.setTaxonomy(taxonomy);
				newLevel.setIdentifier(identifier);
				newLevel.setParent(possibleParents.get(0));
				newLevel.setMaterializedPathIdentifiers(getMaterializedPathIdentifiers(possibleParents.get(0), newLevel));
				
				return newLevel;
			}
			
			// Search possible parents in new levels
			List<TaxonomyLevel> possibleNewParents = newLevels.stream().filter(level -> level.getMaterializedPathIdentifiers().equals(parentPath)).collect(Collectors.toList());
			if (possibleNewParents != null && possibleNewParents.size() != 0) {
				// If multiple new parents found, don't proceed
				if (possibleNewParents.size() > 1) {
					return null;
				}
				
				TaxonomyLevelImpl newLevel = new TaxonomyLevelImpl();
				newLevel.setTaxonomy(taxonomy);
				newLevel.setIdentifier(identifier);
				newLevel.setParent(possibleNewParents.get(0));
				newLevel.setMaterializedPathIdentifiers(getMaterializedPathIdentifiers(possibleNewParents.get(0), newLevel));
				
				return newLevel;
			}		
			
			// Return null if path contains error, e.g. referencing a level which does not exist
			if (parentPath != null && !parentPath.equals("/")) {
				return null;
			} else {
				TaxonomyLevelImpl newLevel = new TaxonomyLevelImpl();
				newLevel.setTaxonomy(taxonomy);
				newLevel.setIdentifier(identifier);
				newLevel.setMaterializedPathIdentifiers(getMaterializedPathIdentifiers(null, newLevel));
				
				return newLevel;
			}
		}
		
		private String getMaterializedPathIdentifiers(TaxonomyLevel parent, TaxonomyLevel level) {
			if(parent != null) {
				String parentPathOfIdentifiers = parent.getMaterializedPathIdentifiers();
				if(parentPathOfIdentifiers == null || "/".equals(parentPathOfIdentifiers)) {
					parentPathOfIdentifiers = "/";
				}
				return parentPathOfIdentifiers + level.getIdentifier()  + "/";
			}
			return "/" + level.getIdentifier()  + "/";
		}
	}
}
