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
package org.olat.modules.oaipmh.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lyncode.builder.ListBuilder;
import org.apache.commons.lang.StringUtils;
import org.olat.NewControllerFactory;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.oaipmh.OAIPmhMetadataProvider;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.modules.oaipmh.dataprovider.model.MetadataItems;
import org.olat.modules.oaipmh.dataprovider.repository.MetadataSetRepository;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.RepositoryService;
import org.olat.repository.ResourceInfoDispatcher;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class OOOaiPmhMetadataProvider implements OAIPmhMetadataProvider {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private OAIPmhModule oaiPmhModule;
	@Autowired
	private UserManager userManager;

	@Override
	public String getMetadataPrefix() {
		return "oai_oo";
	}

	@Override
	public List<MetadataItems> getMetadata(MetadataSetRepository setRepository) {
		List<MetadataItems> metadataItems = new ArrayList<>();

		List<RepositoryEntry> repositoryEntries = repositoryService.loadRepositoryForMetadata(RepositoryEntryStatusEnum.published);
		Map<Long, ResourceLicense> resourceKeyToLicense = licenseService.loadLicenses(
						repositoryEntries.stream()
								.map(RepositoryEntry::getOlatResource)
								.toList())
				.stream()
				.collect(Collectors.toMap(ResourceLicense::getResId, Function.identity()));

		for (RepositoryEntry repositoryEntry : repositoryEntries) {
			String licenseName = "";
			String licensor = "";
			ListBuilder<String> setSpec;
			List<Organisation> organisationsList = repositoryEntry.getOrganisations().stream().map(RepositoryEntryToOrganisation::getOrganisation).toList();
			Map<String, String> orgaIdToDescMap = new HashMap<>();
			for (Organisation orga : organisationsList) {
				orgaIdToDescMap.put(orga.getIdentifier(), orga.getDisplayName());
			}
			List<String> taxonomyLevels =
					repositoryEntry.getTaxonomyLevels().stream().map(t -> t.getTaxonomyLevel().getMaterializedPathIdentifiers()).toList();
			ResourceLicense license = resourceKeyToLicense.get(repositoryEntry.getOlatResource().getResourceableId());
			MetadataItems metadataItemsObject = new MetadataItems();
			MediaResource reImage = getRepositoryEntryImage(repositoryEntry) != null ? getRepositoryEntryImage(repositoryEntry) : null;
			String imagePath = reImage != null ?
					ResourceInfoDispatcher.getUrl(repositoryEntry.getKey().toString()) +
							"." +
							reImage.getContentType().substring(reImage.getContentType().indexOf('/') + 1)
					: "";

			if (oaiPmhModule.isIndexingRestricted(license)) {
				continue;
			}

			setSpec = oaiPmhModule.getSetSpecByRepositoryEntry(repositoryEntry, license, orgaIdToDescMap);

			if (license != null) {
				if (license.getLicenseType().getName().equals("freetext")) {
					licenseName = license.getLicenseType() == null ? "" : license.getFreetext();
				} else {
					licenseName = license.getLicenseType() == null ? "" : license.getLicenseType().getName();
				}
				licensor = license.getLicensor() == null ? "" : license.getLicensor();
			}

			metadataItemsObject
					.with("identifier", oaiPmhModule.getIdentifierFormat().equals("url") ?
							ResourceInfoDispatcher.getUrl(repositoryEntry.getKey().toString()) :
							"oai:" + Settings.getServerDomainName() + ":" + repositoryEntry.getKey())
					.with("url", Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey())
					.with("info_url", ResourceInfoDispatcher.getUrl(repositoryEntry.getKey().toString()))
					.with("displayname", repositoryEntry.getDisplayname())
					.with("resourcename", repositoryEntry.getResourcename())
					.with("initialauthor", userManager.getUserDisplayName(repositoryEntry.getInitialAuthor()))
					.with("softkey", repositoryEntry.getSoftkey())
					.with("location", repositoryEntry.getLocation())
					.with("requirements", repositoryEntry.getRequirements())
					.with("credits", repositoryEntry.getCredits())
					.with("taxonomy", taxonomyLevels.toString())
					.with("allowtoleave", repositoryEntry.getAllowToLeaveOption().name())
					.with("description", repositoryEntry.getDescription())
					// get(0) is enough for publisher displaying in this case
					.with("publisher", StringHelper.escapeHtml(orgaIdToDescMap.get(organisationsList.get(0).getIdentifier())))
					.with("authors", repositoryEntry.getAuthors())
					.with("creationdate", repositoryEntry.getCreationDate())
					.with("r_identifier", repositoryEntry.getEducationalType() != null ? repositoryEntry.getEducationalType().getIdentifier() : "")
					.with("technical_type", repositoryEntry.getTechnicalType())
					.with("resname", NewControllerFactory.translateResourceableTypeName(repositoryEntry.getOlatResource().getResourceableTypeName(), Locale.ENGLISH))
					.with("mainlanguage", repositoryEntry.getMainLanguage())
					.with("expenditureofwork", repositoryEntry.getExpenditureOfWork())
					.with("teaser", repositoryEntry.getTeaser())
					.with("teaserImage", imagePath)
					.with("canDownload", String.valueOf(repositoryEntry.getCanDownload()))
					.with("canCopy", String.valueOf(repositoryEntry.getCanCopy()))
					.with("canReference", String.valueOf(repositoryEntry.getCanReference()))
					.with("status_published_date", repositoryEntry.getStatusPublishedDate())
					.with("license_name", licenseName)
					.with("license_licensor", licensor)
					.with("sets", setSpec.build().isEmpty() ? setSpec.add("").build() : setSpec.build())
					.with("deleted", false);

			metadataItems.add(metadataItemsObject);

			for (String spec : setSpec.build()) {
				if (spec.startsWith("taxon")) {
					repositoryEntry.getTaxonomyLevels()
							.stream()
							.filter(t -> t.getTaxonomyLevel().getMaterializedPathIdentifiers().contains(StringUtils.substringAfter(spec, ":")))
							.findAny()
							.ifPresent(taxon -> setRepository.withSet(taxon.getTaxonomyLevel().getTaxonomy().getDisplayName(), spec));
				} else if (spec.startsWith("org")) {
					orgaIdToDescMap.entrySet()
							.stream()
							.filter(o -> o.getKey().contains(StringUtils.substringAfter(spec, ":")))
							.findAny()
							.ifPresent(orga -> setRepository.withSet(orga.getValue(), spec));
				} else if (spec.startsWith("license")) {
					if (license != null) {
						setRepository.withSet(license.getLicenseType().getText().isEmpty() ? license.getLicenseType().getName() : license.getLicenseType().getText(), spec);
					}
				} else if (spec.startsWith("type")) {
					String translatedName = NewControllerFactory.translateResourceableTypeName(repositoryEntry.getOlatResource().getResourceableTypeName(), Locale.getDefault());
					setRepository.withSet(translatedName, spec);
				}
			}
		}

		return metadataItems;
	}

	private MediaResource getRepositoryEntryImage(RepositoryEntry entry) {
		VFSLeaf image = repositoryService.getIntroductionImage(entry);
		return image != null ? new VFSMediaResource(image) : null;
	}

}
