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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
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

	private static final Logger log = Tracing.createLoggerFor(TaxonomyImportStep1.class);
	private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");
	private static final String ERROR_MEDIA_ZIP_UPLOAD = "error.upload.media.zip";
	private static final String LEVEL_DISPLAYNAME = "level.displayname";
	private static final String LEVEL_DESCRIPTION = "level.description";
	private static final String LEVEL_LANGUAGE = "level.language";
	private static final String BACKGROUND = "background";
	private static final String TEASER = "teaser";

	private TaxonomyImportContext context;

	private static Map<TaxonomyLevel, Map<String, File>> taxonomyLevelToImage = new HashMap<>();
	private static List<TaxonomyLevelRow> reviewList = new ArrayList<>();
	private static List<TaxonomyLevel> updatedLevels = new ArrayList<>();
	
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
	
	private static class TaxonomyImportStep1Controller extends StepFormBasicController {

		private TaxonomyImportContext context;
		
		private FormLink templateDownloadLink;
		private FormLink zipTemplateDownloadLink;
		private TextAreaElement importDataElement;
		private FileElement uploadFileEl;
		
		private ExcelMediaResource importTemplate;
		
		@Autowired
		private TaxonomyService taxonomyService;
		@Autowired
		private DB dbInstance;
		@Autowired
		private I18nModule i18nModule;
		@Autowired
		private I18nManager i18nManager;
		
		public TaxonomyImportStep1Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
			
			this.context = (TaxonomyImportContext) runContext.get(TaxonomyImportContext.CONTEXT_KEY);
			// Import template is always generated so the amount of columns is calculated automatically
			this.importTemplate = createExcelTemplate(ureq);
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FormLayoutContainer importLayout = FormLayoutContainer.createDefaultFormLayout("importLayout", getTranslator());
			FormLayoutContainer mediaLayout = FormLayoutContainer.createDefaultFormLayout("mediaLayout", getTranslator());
			importLayout.setRootForm(formLayout.getRootForm());
			mediaLayout.setRootForm(formLayout.getRootForm());

			importLayout.setFormTitle(translate("import.taxonomy.structure.title"));
			importLayout.setFormDescription(translate("import.taxonomy.step.1.desc"));

			// Add template download link
			templateDownloadLink = uifactory.addFormLink("templateDownloadLink", "import.taxonomy.template.link", "import.taxonomy.template.link.label", importLayout, Link.LINK);
			templateDownloadLink.setIconLeftCSS("o_icon o_filetype_xls o_icon-lg");
			
			// Add input element for taxonomy levels
			importDataElement = uifactory.addTextAreaElement("importElement", "import.taxonomy.levels", -1, 10, -1, false, true, true, null, importLayout);
			importDataElement.setMandatory(true);
			importDataElement.setLineNumbersEnbaled(true);
			importDataElement.setStripedBackgroundEnabled(true);
			importDataElement.setFixedFontWidth(true);

			mediaLayout.setFormTitle(translate("import.taxonomy.media.title"));
			mediaLayout.setFormDescription(translate("import.taxonomy.step1.media.desc"));

			zipTemplateDownloadLink = uifactory.addFormLink("zipTemplateDownloadLink", "import.taxonomy.template.link.zip", "import.taxonomy.template.link.label", mediaLayout, Link.LINK);
			zipTemplateDownloadLink.setIconLeftCSS("o_icon o_filetype_zip o_icon-lg");

			uploadFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", "import.taxonomy.step1.media.label", mediaLayout);
			uploadFileEl.limitToMimeType(Collections.singleton("application/zip"), "error.mimetype.zip", new String[]{ "ZIP" });
			uploadFileEl.setMandatory(false);
			uploadFileEl.addActionListener(FormEvent.ONCHANGE);

			flc.add(importLayout);
			flc.add(mediaLayout);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == templateDownloadLink) {
				ureq.getDispatchResult().setResultingMediaResource(importTemplate);
			}
			if (source == zipTemplateDownloadLink) {
				ureq.getDispatchResult().setResultingMediaResource(new TaxonomyExportZipTemplateStep1("UTF-8", getTranslator()));
			}
			if (source == uploadFileEl) {
				if (uploadFileEl.getUploadFile() != null
						&& ZipUtil.isReadable(uploadFileEl.getUploadFile())
						&& WebappHelper.getMimeType(uploadFileEl.getUploadFileName()).equals("application/zip")) {
					uploadFileEl.clearError();
				} else {
					uploadFileEl.reset();
					uploadFileEl.setErrorKey(ERROR_MEDIA_ZIP_UPLOAD, new String[0]);
				}
			}
		}

		private boolean importTaxonomyMediaFiles() {
			boolean allOk = true;
			try (ZipInputStream zis = new ZipInputStream(uploadFileEl.getUploadInputStream())) {
				ZipEntry zipEntry = zis.getNextEntry();
				List<TaxonomyLevel> existingLevels = taxonomyService.getTaxonomyLevels(context.getTaxonomy());
				String displayName;
				String description;

				while (zipEntry != null) {
					String uploadFileName = zipEntry.getName().replaceAll("/media.*", "");
					if (zipEntry.getName().matches(uploadFileName + "/media/.+")) {
						TaxonomyLevel matchedLevel = null;

						String finalZipEntry = zipEntry.getName()
								.replaceAll(".+media", "")
								.replaceAll("background/.*", "")
								.replaceAll("teaser/.*", "");

						if (StringHelper.containsNonWhitespace(importDataElement.getValue())) {
							if (context.getTaxonomyLevelUpdateList()
									.stream()
									.anyMatch(updateEl -> updateEl.getMaterializedPathIdentifiers().equals(finalZipEntry))) {
								matchedLevel = context.getTaxonomyLevelUpdateList()
										.stream()
										.filter(updateEl -> updateEl.getMaterializedPathIdentifiers().equals(finalZipEntry))
										.findFirst()
										.get();

							} else if (context.getTaxonomyLevelCreateList()
									.stream()
									.anyMatch(createEl -> createEl.getMaterializedPathIdentifiers().equals(finalZipEntry))) {
								matchedLevel = context.getTaxonomyLevelCreateList()
										.stream()
										.filter(updateEl -> updateEl.getMaterializedPathIdentifiers().equals(finalZipEntry))
										.findFirst()
										.get();
							}
						} else if (existingLevels
								.stream()
								.anyMatch(existingLevel -> existingLevel.getMaterializedPathIdentifiers().equals(finalZipEntry))) {

							matchedLevel = existingLevels.stream().filter(ex -> ex.getMaterializedPathIdentifiers().equals(finalZipEntry)).findFirst().get();

							TaxonomyLevel finalMatchedLevel1 = matchedLevel;
							if (updatedLevels.stream().noneMatch(ul -> ul.getIdentifier().equals(finalMatchedLevel1.getIdentifier()))) {
								updatedLevels.add(matchedLevel);
							}
						}

						if (matchedLevel != null) {
							TaxonomyLevel finalMatchedLevel = matchedLevel;
							displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), matchedLevel);
							description = TaxonomyUIFactory.translateDescription(getTranslator(), matchedLevel);

							if (reviewList.stream().noneMatch(rl -> rl.getIdentifier().equals(finalMatchedLevel.getIdentifier()))) {
								reviewList.add(new TaxonomyLevelRow(matchedLevel, getLocale().getLanguage().toUpperCase(), displayName, description, true, false));
							}

							// Check and set background and teaser image
							if (!zipEntry.isDirectory()) {
								boolean hasBackgroundImage = false;
								boolean hasTeaserImage = false;

								if (zipEntry.getName().contains(BACKGROUND)) {
									hasBackgroundImage = true;
								} else if (zipEntry.getName().contains(TEASER)) {
									hasTeaserImage = true;
								}

								if (hasBackgroundImage && context.getReviewList().isEmpty()) {
									reviewList.stream().filter(rl -> rl.getIdentifier().equals(finalMatchedLevel.getIdentifier())).findFirst().get().setHasBackgroundImage(true);
								} else if (hasBackgroundImage && !context.getReviewList().isEmpty()) {
									context.getReviewList().stream().filter(rl -> rl.getIdentifier().equals(finalMatchedLevel.getIdentifier())).findFirst().get().setHasBackgroundImage(true);
								}
								if (hasTeaserImage && context.getReviewList().isEmpty()) {
									reviewList.stream().filter(rl -> rl.getIdentifier().equals(finalMatchedLevel.getIdentifier())).findFirst().get().setHasTeaserImage(true);
								} else if (hasTeaserImage && !context.getReviewList().isEmpty()) {
									context.getReviewList().stream().filter(rl -> rl.getIdentifier().equals(finalMatchedLevel.getIdentifier())).findFirst().get().setHasTeaserImage(true);
								}
							}

							allOk = createTaxonomyLevelToImageMap(zipEntry, matchedLevel);
							if (!allOk) {
								return false;
							}
						} else {
							uploadFileEl.setErrorKey("import.taxonomy.error.no.match", new String[0]);
							return false;
						}
					}

					zis.closeEntry();
					zipEntry = zis.getNextEntry();
				}

				if (!taxonomyLevelToImage.isEmpty()) {
					context.setTaxonomyLevelToImageMap(taxonomyLevelToImage);
					if (!reviewList.isEmpty() && context.getReviewList().isEmpty()) {
						context.setReviewList(reviewList);
					}
					if (!updatedLevels.isEmpty()) {
						context.setTaxonomyLevelUpdateList(updatedLevels);
					}
				}

			} catch (IOException e) {
				allOk = false;
				log.error("Error processing Zip-File as Stream: {}", e.getMessage());
				uploadFileEl.reset();
				uploadFileEl.setErrorKey(ERROR_MEDIA_ZIP_UPLOAD, new String[0]);
			}
			return allOk;
		}

		private boolean createTaxonomyLevelToImageMap(ZipEntry zipEntry, TaxonomyLevel matchedLevel) {
			boolean allOk = true;
			if (!zipEntry.isDirectory()) {
				Map<String, File> destinationToImage = new HashMap<>();
				String destination;
				String fileName;

				try (ZipFile zipFile = new ZipFile(uploadFileEl.getUploadFile())) {

					InputStream in = zipFile.getInputStream(zipEntry);
					File image;

					if (zipEntry.getName().contains(BACKGROUND)) {
						fileName = zipEntry.getName().replaceAll(".+background/", "");
						destination = BACKGROUND;
					} else if (zipEntry.getName().contains(TEASER)) {
						fileName = zipEntry.getName().replaceAll(".+teaser/", "");
						destination = TEASER;
					} else {
						uploadFileEl.reset();
						uploadFileEl.setErrorKey("import.taxonomy.error.wrong.structure", new String[0]);
						return false;
					}

					if (IMAGE_MIME_TYPES.contains(WebappHelper.getMimeType(fileName))) {
						uploadFileEl.clearError();
						image = new File(WebappHelper.getTmpDir() + "/" + fileName);
						if (image.length() / 1024 < 5025 && destination.equals(BACKGROUND)
						|| image.length() / 1024 < 2049 && destination.equals(TEASER)) {
							FileUtils.copyInputStreamToFile(in, image);
						} else {
							uploadFileEl.reset();
							uploadFileEl.setErrorKey("import.taxonomy.error.image", new String[] { fileName });
							return false;
						}
					} else {
						uploadFileEl.reset();
						uploadFileEl.setErrorKey("error.mimetype", new String[0]);
						return false;
					}



					if (taxonomyLevelToImage.get(matchedLevel) != null) {
						taxonomyLevelToImage.get(matchedLevel).put(destination, image);
					} else {
						destinationToImage.put(destination, image);
						taxonomyLevelToImage.put(matchedLevel, destinationToImage);
					}

				} catch (IOException e) {
					log.error("Error processing Zip-File: {}", e.getMessage());
					uploadFileEl.reset();
					uploadFileEl.setErrorKey(ERROR_MEDIA_ZIP_UPLOAD, new String[0]);
				}
			}
			return allOk;
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			// Fire event to get to the next step
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			reviewList.clear();
			updatedLevels.clear();
			taxonomyLevelToImage.clear();

			if ((!validateFormItem(importDataElement) && !validateFormItem(uploadFileEl))
					|| (uploadFileEl.getUploadFile() == null && !StringHelper.containsNonWhitespace(importDataElement.getValue()))) {
				importDataElement.setErrorKey("import.taxonomy.levels.mandatory.either", new String[0]);
				uploadFileEl.setErrorKey("import.taxonomy.levels.mandatory.either", new String[0]);
				allOk = false;
			} else {
				importDataElement.clearError();
				uploadFileEl.clearError();
				if (StringHelper.containsNonWhitespace(importDataElement.getValue())) {
					allOk &= validateExcelInput(importDataElement);
				}
				if (uploadFileEl.getUploadFile() != null){
					allOk &= importTaxonomyMediaFiles();
				}

			}
			
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
			StringBuilder dataRow3 = new StringBuilder();

			// Add columns

			// Path
			headerRow.append(translate("taxonomy.level.path")).append("\t");
			dataRow1.append("/MATH").append("\t");
			dataRow2.append("/MATH/GEO").append("\t");
			dataRow3.append("/BIO").append("\t");

			// Identifier
			headerRow.append(translate("level.identifier")).append("\t");
			dataRow1.append("MATH").append("\t");
			dataRow2.append("GEO").append("\t");
			dataRow3.append("BIO").append("\t");

			// Type
			headerRow.append(translate("level.type")).append("\t");
			dataRow1.append("F").append("\t");
			dataRow2.append("K").append("\t");
			dataRow3.append("F").append("\t");

			// Sort order
			headerRow.append(translate("level.sort.order")).append("\t");
			dataRow1.append("1").append("\t");
			dataRow2.append("").append("\t");
			dataRow3.append("2").append("\t");

			// Language DE
			headerRow.append(translate(LEVEL_LANGUAGE)).append("\t");
			dataRow1.append("DE").append("\t");
			dataRow2.append("DE").append("\t");
			dataRow3.append("DE").append("\t");

			// Display name DE
			headerRow.append(translate(LEVEL_DISPLAYNAME)).append("\t");
			dataRow1.append("Mathematik").append("\t");
			dataRow2.append("Geometrie").append("\t");
			dataRow3.append("Biologie").append("\t");

			// Description DE
			headerRow.append(translate(LEVEL_DESCRIPTION)).append("\t");
			dataRow1.append("Mathematik ist eine wichtige Disziplin.").append("\t");
			dataRow2.append("Geometrie ist ein Teilgebiet der Mathematik.").append("\t");
			dataRow3.append("Biologie ist die Wissenschaft der Lebewesen.").append("\t");

			// Language EN
			headerRow.append(translate(LEVEL_LANGUAGE)).append("\t");
			dataRow1.append("EN").append("\t");
			dataRow2.append("EN").append("\t");
			dataRow3.append("EN").append("\t");

			// Display name EN
			headerRow.append(translate(LEVEL_DISPLAYNAME)).append("\t");
			dataRow1.append("Mathematics").append("\t");
			dataRow2.append("Geometrics").append("\t");
			dataRow3.append("Biology").append("\t");

			// Description EN
			headerRow.append(translate(LEVEL_DESCRIPTION)).append("\t");
			dataRow1.append("Mathematics is an important discipline.").append("\t");
			dataRow2.append("Geometry is a branch of mathematics.").append("\t");
			dataRow3.append("Biology is the science of living matter, the living beings.").append("\t");

			// Language add new
			headerRow.append(translate(LEVEL_LANGUAGE));
			dataRow1.append("add new language based on language codes from your instance.");

			// Concatenate header and data row
			ExcelMediaResource emr = new ExcelMediaResource(headerRow + "\n" + dataRow1 + "\n" + dataRow2 + "\n" + dataRow3, charset);
			emr.setFilename("TaxonomyLevelsImportTemplate");
			return emr;
		}

		private boolean isValidLanguage(String language) {
			List<Locale> locales = i18nModule.getEnabledLanguageKeys().stream()
					.map(key -> i18nManager.getLocaleOrNull(key))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			return locales.stream().anyMatch(l -> l.toString().toUpperCase().equals(language));
		}

		private boolean validateExcelInput(TextAreaElement inputEl) {
			boolean allOk = true;
			boolean hasMultipleLangs = false;
			
			String[] lines = inputEl.getValue().split("\r?\n");
			
			List<TaxonomyLevel> existingLevels = taxonomyService.getTaxonomyLevels(context.getTaxonomy());
			List<TaxonomyLevelType> existingLevelTypes = taxonomyService.getTaxonomyLevelTypes(context.getTaxonomy());
			
			List<TaxonomyLevelRow> reviewList = new ArrayList<>();
			List<TaxonomyLevel> updatedLevels = new ArrayList<>();
			List<TaxonomyLevel> newLevels = new ArrayList<>();
			List<TaxonomyLevelType> newLevelTypes = new ArrayList<>();
			Map<String, List<String>> nameDescriptionByLanguage = new HashMap<>();
			
			List<Integer> errorRows = new ArrayList<>();
			
			for (int i = 0; i < lines.length; i++) {
				nameDescriptionByLanguage = new HashMap<>();
				String line = lines[i];
				
				if (!StringHelper.containsNonWhitespace(line)) {
					continue;
				}
				
				String delimiter = "\t";
				// Use comma as fallback delimiter, e.g. for better testing
				if (!line.contains(delimiter)) {
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
				String identifier = StringHelper.xssScan(columns[1]).trim();
				String type = columns.length > 2 ? StringHelper.xssScan(columns[2]).trim() : null;
				String orderString = columns.length > 3 ? StringHelper.xssScan(columns[3]).trim() : null;
				String language = "";
				String displayName = "";
				String description = "";

				if (columns.length > 3) {
					for (int j = 4; j < columns.length - 2; j += 3) {
						language = StringHelper.xssScan(columns[j]).trim();
						displayName = columns.length > j + 1 ? StringHelper.xssScan(columns[j+1]).trim() : "";
						description = columns.length > j + 2 ? StringHelper.xssScan(columns[j+2]).trim() : "";

						if (!isValidLanguage(language)) {
							allOk = false;
							inputEl.setErrorKey("import.taxonomy.error.language", new String[] { language });
							errorRows.add(i);
						} else if (StringHelper.containsNonWhitespace(language)
								&& StringHelper.containsNonWhitespace(displayName)) {
							nameDescriptionByLanguage.put(language, Arrays.asList(displayName, description));
						}
					}
				}
			
				// If no path is given
				if (!StringHelper.containsNonWhitespace(path) || path.equals("/")) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error.no.path", null);
					errorRows.add(i);
					continue;
				}
				
				// Check identifier length
				if (identifier.length() > 64) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error.identifier.too.long", new String[] {"64"});
					errorRows.add(i);
					continue;
				}
				
				// Check display name length
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
						inputEl.setErrorKey("import.taxonomy.error.order", null);
						errorRows.add(i);
						continue;
					}
				}
							
				TaxonomyLevel currentLevel = getExistingTaxonomyLevelOrCreate(context.getTaxonomy(), existingLevels, newLevels, path, identifier);

				if (currentLevel == null) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error.level", null);
					errorRows.add(i);
					continue;
				}
				
				TaxonomyLevelType currentLevelType = checkLevelType(context.getTaxonomy(), existingLevelTypes, newLevelTypes, type);
				
				if (StringHelper.containsNonWhitespace(type) && currentLevelType == null) {
					allOk = false;
					inputEl.setErrorKey("import.taxonomy.error.type", null);
					errorRows.add(i);
					continue;
				}

				if (!TaxonomyLevelManagedFlag.isManaged(currentLevel, TaxonomyLevelManagedFlag.sortOrder)) {
					currentLevel.setSortOrder(order);
				}
				if (!TaxonomyLevelManagedFlag.isManaged(currentLevel, TaxonomyLevelManagedFlag.type)) {
					currentLevel.setType(currentLevelType);
				}

				if (currentLevel.getKey() == null) {
					newLevels.add(currentLevel);
				} else {
					updatedLevels.add(currentLevel);
				}
				
				if (currentLevelType != null && currentLevelType.getKey() == null && !newLevelTypes.contains(currentLevelType)) {
					newLevelTypes.add(currentLevelType);
				}

				for (String locale : nameDescriptionByLanguage.keySet()) {
					if (!locale.equals(nameDescriptionByLanguage.keySet().stream().findFirst().get())) {
						hasMultipleLangs = true;
					}
					displayName = nameDescriptionByLanguage.get(locale).get(0);
					description = nameDescriptionByLanguage.get(locale).get(1);

					reviewList.add(new TaxonomyLevelRow(currentLevel, locale, displayName, description, currentLevel.getKey() != null, hasMultipleLangs));
					hasMultipleLangs = false;
				}
			}
			
			context.setReviewList(reviewList);
			context.setTaxonomyLevelCreateList(newLevels);
			context.setTaxonomyLevelUpdateList(updatedLevels);
			context.setTaxonomyLevelTypeCreateList(newLevelTypes);
			context.setNameDescriptionByLanguage(nameDescriptionByLanguage);
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
			String parentPathOfIdentifiers = "/";
			if(parent != null) {
				if(parent.getMaterializedPathIdentifiers() != null || !"/".equals(parent.getMaterializedPathIdentifiers())) {
					parentPathOfIdentifiers = parent.getMaterializedPathIdentifiers();
				}
			}
			return parentPathOfIdentifiers + level.getIdentifier()  + "/";
		}
	}
}
