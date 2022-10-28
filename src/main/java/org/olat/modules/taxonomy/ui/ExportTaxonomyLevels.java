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

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.ExcelMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.user.UserManager;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private final Identity identity;
    private final Translator translator;
    private final I18nModule i18nModule;
    private final I18nManager i18nManager;


    public ExportTaxonomyLevels(String encoding, Translator translator, Identity identity,
                                Taxonomy taxonomy, TaxonomyService taxonomyService,
                                I18nManager i18nManager, I18nModule i18nModule) {
        this.encoding = encoding;
        this.translator = translator;
        this.identity = identity;
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

            File excelTaxonomyExport = new File(WebappHelper.getTmpDir() + "/" + label + ".xls");
            FileUtils.copyInputStreamToFile(exportExcelTaxonomyLevels().getInputStream(), excelTaxonomyExport);
            ZipUtil.addFileToZip(label + "/" + label + ".xls", excelTaxonomyExport, zout);

            for (int i = 0; i < taxonomyService.getTaxonomyLevels(taxonomy).size(); i++) {
                String taxonomyPath = taxonomyService.getTaxonomyLevels(taxonomy).get(i).getMaterializedPathIdentifiers();
                LocalFileImpl backgroundImage = ((LocalFileImpl) taxonomyService.getBackgroundImage(taxonomyService.getTaxonomyLevels(taxonomy).get(i)));
                LocalFileImpl teaserImage = ((LocalFileImpl) taxonomyService.getTeaserImage(taxonomyService.getTaxonomyLevels(taxonomy).get(i)));

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
            }
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

    private ExcelMediaResource exportExcelTaxonomyLevels() {
        List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomy);
        Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
        Collection<String> languageKeys = i18nModule.getEnabledLanguageKeys();
        String charset = UserManager.getInstance().getUserCharset(identity);
        Set<String> validLanguages = new HashSet<>();

        StringBuilder headerRow = new StringBuilder();
        String[] dataRows = new String[taxonomyLevels.size()];
        int i = 0;

        headerRow.append(translator.translate("taxonomy.level.path")).append("\t");
        headerRow.append(translator.translate("level.identifier")).append("\t");
        headerRow.append(translator.translate("level.type")).append("\t");
        headerRow.append(translator.translate("level.sort.order")).append("\t");

        for (TaxonomyLevel level : taxonomyLevels) {
            String displayNameKey = TaxonomyUIFactory.PREFIX_DISPLAY_NAME + level.getI18nSuffix();
            String descriptionKey = TaxonomyUIFactory.PREFIX_DESCRIPTION + level.getI18nSuffix();

            String taxonomyLevelPath = level.getMaterializedPathIdentifiers();
            String taxonomyLevelIdentifier = level.getIdentifier();
            String taxonomyType = level.getType() != null ? level.getType().getIdentifier() : "";
            String taxonomySortOrder = level.getSortOrder() != null ? level.getSortOrder().toString() : "";

            dataRows[i] = "";

            dataRows[i] = dataRows[i]
                    .concat(taxonomyLevelPath).concat("\t")
                    .concat(taxonomyLevelIdentifier).concat("\t")
                    .concat(taxonomyType).concat("\t")
                    .concat(taxonomySortOrder).concat("\t");

            for (String languageKey : languageKeys) {
                String taxonomyLevelLanguage = "";
                String taxonomyDisplayName;
                String taxonomyLevelDescription;
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
                    taxonomyDisplayName = translatedDisplayName;
                    taxonomyLevelLanguage = languageKey.toUpperCase();
                    validLanguages.add(taxonomyLevelLanguage);
                } else {
                    taxonomyDisplayName = "";
                }

                if (translatedDescription != null) {
                    taxonomyLevelDescription = translatedDescription;
                    taxonomyLevelLanguage = languageKey.toUpperCase();
                    validLanguages.add(taxonomyLevelLanguage);
                } else {
                    taxonomyLevelDescription = "";
                }

                if (!StringHelper.containsNonWhitespace(taxonomyLevelLanguage)) {
                    continue;
                }

                dataRows[i] = dataRows[i]
                        .concat(taxonomyLevelLanguage).concat("\t")
                        .concat(taxonomyDisplayName).concat("\t")
                        .concat(taxonomyLevelDescription).concat("\t");
            }
            i++;
        }

        for (int j = 0; j < validLanguages.size(); j++) {
            headerRow.append(translator.translate("level.language")).append("\t");
            headerRow.append(translator.translate("level.displayname")).append("\t");
            headerRow.append(translator.translate("level.description")).append("\t");
        }

        ExcelMediaResource emr = new ExcelMediaResource(headerRow + "\n" + String.join("\n", dataRows), charset);
        emr.setFilename("TestExport");

        return emr;
    }

    @Override
    public void release() {
        // No need
    }
}
