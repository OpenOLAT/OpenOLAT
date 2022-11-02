/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.taxonomy.ui;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public class ExportTaxonomyLevels implements MediaResource {

    private static final Logger log = Tracing.createLoggerFor(ExportTaxonomyLevels.class);

    private static final String MEDIA_FOLDER = "/media/";

    private final String encoding;
    private final Taxonomy taxonomy;
    private final TaxonomyService taxonomyService;
    private final Translator translator;
    private final I18nModule i18nModule;
    private final I18nManager i18nManager;

    // Map<Map<TaxonomyLevel, LanguageKey>, Map<typeOfAttribute, attributeValue>> (typeOfAttribute: {displayName, description})
    private Map<Map<TaxonomyLevel, String>, Map<String, String>> attributesToLevels = new HashMap<>();

    public ExportTaxonomyLevels(String encoding, Translator translator, Taxonomy taxonomy,
                                TaxonomyService taxonomyService, I18nManager i18nManager, I18nModule i18nModule) {
        this.encoding = encoding;
        this.translator = translator;
        this.taxonomy = taxonomy;
        this.taxonomyService = taxonomyService;
        this.i18nManager = i18nManager;
        this.i18nModule = i18nModule;
    }

    @Override
    public boolean acceptRanges() {
        return false;
    }

    @Override
    public String getContentType() {
        return "application/zip";
    }

    @Override
    public Long getSize() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public Long getLastModified() {
        return null;
    }

    @Override
    public long getCacheControlDuration() {
        return 0;
    }

    private void createHeader(Translator translator, OpenXMLWorksheet worksheet) {
        Row headerRow = worksheet.newRow();
        int col = 0;

        headerRow.addCell(col++, translator.translate("taxonomy.level.path"));
        headerRow.addCell(col++, translator.translate("level.identifier"));
        headerRow.addCell(col++, translator.translate("level.type"));
        headerRow.addCell(col++, translator.translate("level.sort.order"));

        List<String> validLanguages = attributesToLevels.keySet().stream().map(l -> l.values().stream().distinct().findFirst().get()).distinct().collect(Collectors.toList());

        for (int i = 0; i < validLanguages.size(); i++) {
            headerRow.addCell(col++, translator.translate("level.language"));
            headerRow.addCell(col++, translator.translate("level.displayname"));
            headerRow.addCell(col++, translator.translate("level.description"));
        }
    }

    private void createData(List<TaxonomyLevel> taxonomyLevels, OpenXMLWorksheet worksheet) {
        worksheet.setHeaderRows(1);

        for (TaxonomyLevel level : taxonomyLevels) {
            OpenXMLWorksheet.Row dataRow = worksheet.newRow();
            int c = 0;

            String taxonomyLevelPath = level.getMaterializedPathIdentifiers();
            String taxonomyLevelIdentifier = level.getIdentifier();
            String taxonomyType = level.getType() != null ? level.getType().getIdentifier() : "";
            String taxonomySortOrder = level.getSortOrder() != null ? level.getSortOrder().toString() : "";

            dataRow.addCell(c++, taxonomyLevelPath);
            dataRow.addCell(c++, taxonomyLevelIdentifier);
            dataRow.addCell(c++, taxonomyType);
            dataRow.addCell(c++, taxonomySortOrder);

            List<String> validLanguages = attributesToLevels.keySet().stream().map(l -> l.values().stream().distinct().findFirst().get()).distinct().collect(Collectors.toList());
            Collections.sort(validLanguages);

            for (String languageKey : validLanguages) {
                String taxonomyDisplayName = "";
                String taxonomyLevelDescription = "";

                if (attributesToLevels.get(Map.ofEntries(Map.entry(level, languageKey))) != null) {
                    taxonomyDisplayName = attributesToLevels.get(Map.ofEntries(Map.entry(level, languageKey))).get("displayName");
                    taxonomyLevelDescription = attributesToLevels.get(Map.ofEntries(Map.entry(level, languageKey))).get("description");
                }

                dataRow.addCell(c++, languageKey);
                dataRow.addCell(c++, taxonomyDisplayName);
                dataRow.addCell(c++, taxonomyLevelDescription);
            }
        }
    }

    @Override
    public void prepare(HttpServletResponse hres) {
        try {
            hres.setCharacterEncoding(encoding);
        } catch (Exception e) {
            log.error("", e);
        }

        String label = translator.translate("taxonomy.export.label");
        String file = StringHelper.transformDisplayNameToFileSystemName(label) + ".zip";
        String encodedFileName = StringHelper.urlEncodeUTF8(file);
        hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        hres.setHeader("Content-Description", encodedFileName);

        try (ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
            zout.setLevel(9);

            Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
            Collection<String> languageKeys = i18nModule.getEnabledLanguageKeys();

            for (TaxonomyLevel taxonomyLevel : taxonomyService.getTaxonomyLevels(taxonomy)) {
                String displayNameKey = TaxonomyUIFactory.PREFIX_DISPLAY_NAME + taxonomyLevel.getI18nSuffix();
                String descriptionKey = TaxonomyUIFactory.PREFIX_DESCRIPTION + taxonomyLevel.getI18nSuffix();

                String taxonomyPath = taxonomyLevel.getMaterializedPathIdentifiers();
                LocalFileImpl backgroundImage = ((LocalFileImpl) taxonomyService.getBackgroundImage(taxonomyLevel));
                LocalFileImpl teaserImage = ((LocalFileImpl) taxonomyService.getTeaserImage(taxonomyLevel));

                if (backgroundImage != null) {
                    ZipUtil.addFileToZip(label + MEDIA_FOLDER + taxonomyPath + "/background/" + backgroundImage.getBasefile().getName(), backgroundImage.getBasefile(), zout);
                } else {
                    zout.putNextEntry(new ZipEntry(label + MEDIA_FOLDER + taxonomyPath + "/background/"));
                }
                if (teaserImage != null) {
                    ZipUtil.addFileToZip(label + MEDIA_FOLDER + taxonomyPath + "/teaser/" + teaserImage.getBasefile().getName(), teaserImage.getBasefile(), zout);
                } else {
                    zout.putNextEntry(new ZipEntry(label + MEDIA_FOLDER + taxonomyPath + "/teaser/"));
                }

                for (String languageKey : languageKeys) {
                    Map<String, String> attributesToItem = new HashMap<>();
                    Map<TaxonomyLevel, String> languageToLevel = new HashMap<>();
                    String taxonomyLevelLanguage = "";
                    Locale locale = i18nManager.getLocaleOrDefault(languageKey);

                    I18nItem displayNameItem = i18nManager.getI18nItem(
                            TaxonomyUIFactory.BUNDLE_NAME,
                            displayNameKey,
                            allOverlays.get(locale));
                    I18nItem descriptionItem = i18nManager.getI18nItem(
                            TaxonomyUIFactory.BUNDLE_NAME,
                            descriptionKey,
                            allOverlays.get(locale));

                    String translatedDisplayName = i18nManager.getLocalizedString(displayNameItem, null);
                    String translatedDescription = i18nManager.getLocalizedString(descriptionItem, null);

                    if (translatedDisplayName != null) {
                        taxonomyLevelLanguage = languageKey.toUpperCase();
                        attributesToItem.put("displayName", translatedDisplayName);
                    }

                    if (translatedDescription != null) {
                        taxonomyLevelLanguage = languageKey.toUpperCase();
                        attributesToItem.put("description", translatedDescription);
                    }

                    if (translatedDisplayName != null || translatedDescription != null) {
                        languageToLevel.put(taxonomyLevel, taxonomyLevelLanguage);
                        attributesToLevels.put(languageToLevel, attributesToItem);
                    }
                }
            }

            File excelExport = new File(WebappHelper.getTmpDir() + "/" + label + ".xlsx");
            createExcelSheet(excelExport);
            ZipUtil.addFileToZip(label + "/" + excelExport.getName(), excelExport, zout);

            zout.flush();
        } catch (IOException e) {
            String className = e.getClass().getSimpleName();
            if ("ClientAbortException".equals(className)) {
                log.debug("client browser probably abort when downloading zipped files", e);
            } else {
                log.error("client browser probably abort when downloading zipped files", e);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void createExcelSheet(File excelExport) {
        try (OutputStream out = new FileOutputStream(excelExport);
             OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
            OpenXMLWorksheet sheet = workbook.nextWorksheet();
            createHeader(translator, sheet);
            createData(taxonomyService.getTaxonomyLevels(taxonomy), sheet);
        } catch (IOException e) {
            log.error("Unable to export xlsx", e);
        }
    }

    @Override
    public void release() {
        // No need
    }
}
