/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.model;

import com.lyncode.builder.ListBuilder;
import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Organisation;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.modules.oaipmh.common.model.About;
import org.olat.modules.oaipmh.common.model.Metadata;
import org.olat.modules.oaipmh.common.oaidc.Element;
import org.olat.modules.oaipmh.common.oaidc.OAIDCMetadata;
import org.olat.modules.oaipmh.common.oaioo.OAIOOMetadata;
import org.olat.modules.oaipmh.common.oaioo.OOElement;
import org.olat.modules.oaipmh.dataprovider.repository.MetadataSetRepository;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataItems implements Item {
    private static final RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
    private static final LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);
    private static final OAIPmhModule oaiPmhModule = CoreSpringFactory.getImpl(OAIPmhModule.class);
    private Map<String, Object> values = new HashMap<>();

    public static MetadataItems item() {
        return new MetadataItems();
    }

    public static List<MetadataItems> repositoryDCItems(MetadataSetRepository setRepository) {
        List<MetadataItems> metadataItems = new ArrayList<>();

        List<RepositoryEntry> repositoryEntries = repositoryService.loadRepositoryForMetadata(RepositoryEntryStatusEnum.published);

        for (RepositoryEntry repositoryEntry : repositoryEntries) {
            String rights = "";
            ListBuilder<String> setSpec;
            ResourceLicense license = licenseService.loadLicense(repositoryEntry.getOlatResource());
            List<Organisation> organisationList = new ArrayList<>();
            organisationList.add(repositoryEntry.getOrganisations().stream().findAny().get().getOrganisation());

            if (oaiPmhModule.isLicenseAllow() &&
                    (license == null ||
                            license.getLicenseType().getName().equals("no.license"))) {
                continue;
            }

            setSpec = getSetSpecByRepositoryEntry(repositoryEntry, license, organisationList);

            if (license != null) {
                if (license.getLicenseType().getName().equals("freetext")) {
                    rights += license.getLicenseType() == null ? "" : license.getFreetext();
                } else {
                    rights += license.getLicenseType() == null ? "" : license.getLicenseType().getName();
                }
                rights += license.getLicensor() == null ? "" : " - " + license.getLicensor();
            }

            metadataItems.add(new MetadataItems()
                    .with("title", repositoryEntry.getDisplayname())
                    .with("creator", repositoryEntry.getInitialAuthor())
                    .with("subject", repositoryEntry.getResourcename())
                    .with("description", repositoryEntry.getDescription())
                    .with("publisher", Arrays.stream(OrganisationUIFactory.createSelectionValues(organisationList).values()).findFirst().get().replace(" ", ""))
                    .with("contributer", repositoryEntry.getAuthors())
                    .with("date", repositoryEntry.getCreationDate())
                    .with("type", repositoryEntry.getTechnicalType())
                    .with("format", NewControllerFactory.translateResourceableTypeName(repositoryEntry.getOlatResource().getResourceableTypeName(), Locale.getDefault()))
                    .with("identifier", "oo:" + Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey())
                    .with("language", repositoryEntry.getMainLanguage())
                    .with("coverage", repositoryEntry.getTeaser())
                    .with("rights", rights)
                    .with("sets", setSpec.build().isEmpty() ? setSpec.add("").build() : setSpec.build())
                    .with("deleted", false));

            setRepository.withSet(repositoryEntry.getDisplayname(), setSpec.build().get(0));
        }

        return metadataItems;
    }

    public static List<MetadataItems> repositoryOOItems(MetadataSetRepository setRepository) {
        List<MetadataItems> metadataItems = new ArrayList<>();

        List<RepositoryEntry> repositoryEntries = repositoryService.loadRepositoryForMetadata(RepositoryEntryStatusEnum.published);

        for (RepositoryEntry repositoryEntry : repositoryEntries) {
            String licenseName = "";
            String licensor = "";
            ListBuilder<String> setSpec;
            ResourceLicense license = licenseService.loadLicense(repositoryEntry.getOlatResource());

            List<Organisation> organisationList = new ArrayList<>();
            organisationList.add(repositoryEntry.getOrganisations().stream().findAny().get().getOrganisation());

            if (oaiPmhModule.isLicenseAllow() &&
                    (license == null ||
                            license.getLicenseType().getName().equals("no.license"))) {
                continue;
            }

            setSpec = getSetSpecByRepositoryEntry(repositoryEntry, license, organisationList);

            if (license != null) {
                if (license.getLicenseType().getName().equals("freetext")) {
                    licenseName = license.getLicenseType() == null ? "" : license.getFreetext();
                } else {
                    licenseName = license.getLicenseType() == null ? "" : license.getLicenseType().getName();
                }
                licensor = license.getLicensor() == null ? "" : license.getLicensor();
            }

            metadataItems.add(new MetadataItems()
                    .with("displayname", repositoryEntry.getDisplayname())
                    .with("initialauthor", repositoryEntry.getInitialAuthor())
                    .with("taxonomy", repositoryEntry.getResourcename())
                    .with("description", repositoryEntry.getDescription())
                    .with("publisher", Arrays.stream(OrganisationUIFactory.createSelectionValues(organisationList).values()).findFirst().get().replace(" ", ""))
                    .with("authors", repositoryEntry.getAuthors())
                    .with("creationdate", repositoryEntry.getCreationDate())
                    .with("r_identifier", repositoryEntry.getEducationalType())
                    .with("format", NewControllerFactory.translateResourceableTypeName(repositoryEntry.getOlatResource().getResourceableTypeName(), Locale.getDefault()))
                    .with("identifier", "oo:" + Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey())
                    .with("mainlanguage", repositoryEntry.getMainLanguage())
                    .with("expenditureofwork", repositoryEntry.getExpenditureOfWork())
                    .with("teaser", repositoryEntry.getTeaser())
                    .with("license_name", licenseName)
                    .with("license_licensor", licensor)
                    .with("sets", setSpec.build().isEmpty() ? setSpec.add("").build() : setSpec.build())
                    .with("deleted", false));

            setRepository.withSet(repositoryEntry.getDisplayname(), setSpec.build().get(0));
        }

        return metadataItems;
    }

    private static ListBuilder<String> getSetSpecByRepositoryEntry(
            RepositoryEntry repositoryEntry,
            ResourceLicense license,
            List<Organisation> organisationList) {
        ListBuilder<String> setSpec = new ListBuilder<>();

        if (oaiPmhModule.isApiTypeTaxonomy()) {
            List<String> taxonomyLevels =
                    repositoryEntry.getTaxonomyLevels().stream().map(t -> t.getTaxonomyLevel().getMaterializedPathIdentifiers()).toList();

            for (String taxonomyLevel : taxonomyLevels) {
                setSpec.add("taxon:" + taxonomyLevel.substring(1, taxonomyLevel.length() - 1));
            }
        }
        if (oaiPmhModule.isApiTypeOrganisation()) {
            String organisation = Arrays.stream(OrganisationUIFactory.createSelectionValues(organisationList).values()).findFirst().get().replace(" ", "");

            setSpec.add("org:" + organisation);
        }
        if (oaiPmhModule.isApiTypeLicense()) {
            setSpec.add("license:" + license.getLicenseType().getName());
        }
        if (oaiPmhModule.isApiTypeLearningResource()) {
            String type = NewControllerFactory.translateResourceableTypeName(repositoryEntry.getOlatResource().getResourceableTypeName(), Locale.getDefault());
            setSpec.add("type:" + type);
        }
        if (oaiPmhModule.isApiTypeRelease()) {
            String releaseAble = repositoryEntry.isPublicVisible() ? "guest" : "private";
            setSpec.add("release:" + releaseAble);
        }

        return setSpec;
    }


    public MetadataItems with(String name, Object value) {
        values.put(name, value);
        return this;
    }

    public MetadataItems withSet(String name) {
        ((List<String>) values.get("sets")).add(name);
        return this;
    }

    public MetadataItems getMetadataBySetSpec(String setSpec) {
        getSets().stream().filter(s -> s.getSpec().equals(setSpec)).collect(Collectors.toList());
        return this;
    }

    @Override
    public List<About> getAbout() {
        return new ArrayList<>();
    }

    @Override
    public Metadata getMetadata(String metadataPrefix) {
        if (metadataPrefix.equals("oai_dc")) {
            return new Metadata(toDCMetadata());
        } else if (metadataPrefix.equals("oai_oo")) {
            return new Metadata(toOOMetadata());
        }
        return null;
    }

    private OAIDCMetadata toDCMetadata() {
        OAIDCMetadata builder = new OAIDCMetadata();
        for (String key : values.keySet()) {
            Element elementBuilder = new Element(key);
            Object value = values.get(key);
            if (value instanceof String)
                elementBuilder.withField(key, value.toString());
            else if (value instanceof Date)
                elementBuilder.withField(key, value.toString());
            else if (value instanceof List) {
                List<String> obj = (List<String>) value;
                int i = 1;
                for (String e : obj)
                    elementBuilder.withField(key + (i++), e);
            }
            builder.withElement(elementBuilder);
        }
        return builder;
    }

    private OAIOOMetadata toOOMetadata() {
        OAIOOMetadata builder = new OAIOOMetadata();
        for (String key : values.keySet()) {
            OOElement elementBuilder = new OOElement(key);
            Object value = values.get(key);
            if (value instanceof String)
                elementBuilder.withValue(value.toString());
            else if (value instanceof Date)
                elementBuilder.withValue(value.toString());
            else if (value instanceof List) {
                List<String> obj = (List<String>) value;
                for (String e : obj)
                    elementBuilder.withValue(e);
            }
            builder.withElement(elementBuilder);
        }
        return builder;
    }

    @Override
    public String getIdentifier() {
        return values.get("identifier").toString();
    }

    @Override
    public Date getDatestamp() {
        if (values.get("date") != null) {
            return (Date) values.get("date");
        } else if (values.get("creationdate") != null) {
            return (Date) values.get("creationdate");
        } else {
            return new Date();
        }
    }

    @Override
    public List<Set> getSets() {
        List<String> list = ((List<String>) values.get("sets"));
        return new ListBuilder<String>().add(list.toArray(new String[list.size()])).build(new ListBuilder.Transformer<String, Set>() {
            @Override
            public Set transform(String elem) {
                return new Set(elem);
            }
        });
    }

    @Override
    public boolean isDeleted() {
        return (Boolean) values.get("deleted");
    }


    public MetadataItems withIdentifier(String identifier) {
        this.with("identifier", identifier);
        return this;
    }
}