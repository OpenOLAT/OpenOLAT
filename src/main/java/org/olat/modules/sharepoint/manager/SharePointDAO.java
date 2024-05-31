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
package org.olat.modules.sharepoint.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.sharepoint.model.MicrosoftDrive;
import org.olat.modules.sharepoint.model.MicrosoftDriveItem;
import org.olat.modules.sharepoint.model.MicrosoftSite;
import org.springframework.stereotype.Service;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.core.models.IProgressCallback;
import com.microsoft.graph.core.models.UploadResult;
import com.microsoft.graph.core.tasks.LargeFileUploadTask;
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.drives.item.searchwithq.SearchWithQGetResponse;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveCollectionResponse;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemCollectionResponse;
import com.microsoft.graph.models.DriveItemUploadableProperties;
import com.microsoft.graph.models.Site;
import com.microsoft.graph.models.SiteCollectionResponse;
import com.microsoft.graph.models.ThumbnailSet;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.nimbusds.jose.shaded.gson.JsonObject;

/**
 * 
 * Initial date: 7 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SharePointDAO {

	private static final Logger log = Tracing.createLoggerFor(SharePointDAO.class);
	
	private static final int MAX_ATTEMPTS = 5;

	private static final String[] ATTRS_DRIVE = new String[] { "id", "name", "lastModifiedDateTime", "weburl" };
	private static final String[] ATTRS_DRIVE_ITEM = new String[] { "id", "name", "lastModifiedDateTime", "weburl", "size", "file", "folder", "sensitivityLabel" };
	private static final String[] EXPAND_DRIVE_ITEM = new String[] { "thumbnails" };

	public List<MicrosoftSite> getSites(TokenCredential tokenProvider, String search) {
		try {
			SiteCollectionResponse allSites = client(tokenProvider)
					.sites()
					.get(requestConfiguration -> {
						requestConfiguration.queryParameters.search = search;
						requestConfiguration.queryParameters.filter = null;
					});
			
			List<Site> sitesList = allSites.getValue();
			if(sitesList == null) {
				return List.of();
			}
			
			log.debug("Sites: {}", sitesList.size());
			return sitesList.stream()
					.map(MicrosoftSite::valueOf)
					.toList();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	public List<MicrosoftDrive> getDrives(String siteId, TokenCredential tokenProvider) {
		try {
			DriveCollectionResponse drivePage = client(tokenProvider)
					.sites()
					.bySiteId(siteId)
					.drives()
					.get();
			
			List<Drive> driveList = drivePage.getValue();
			if(driveList == null) {
				return List.of();
			}
			return driveList.stream()
					.map(MicrosoftDrive::valueOf)
					.toList();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public MicrosoftDrive getMeOneDrive(TokenCredential tokenProvider) {
		try {
			Drive drive = client(tokenProvider)
				.me()
				.drive()
				.get(requestConfiguration ->
					requestConfiguration.queryParameters.select = ATTRS_DRIVE
				);
			return MicrosoftDrive.valueOf(drive);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public List<MicrosoftDrive> getMeDrives(TokenCredential tokenProvider) {
		try {
			DriveCollectionResponse drivePage = client(tokenProvider)
					.me()
					.drives()
					.get();
			
			List<Drive> driveList = drivePage.getValue();
			if(driveList == null) {
				return List.of();
			}
			
			return driveList.stream()
					.map(MicrosoftDrive::valueOf)
					.toList();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public DriveItem getRootDriveItem(Drive drive, TokenCredential tokenProvider) {
		try {
			DriveItem rootItem = client(tokenProvider)
					.drives()
					.byDriveId(drive.getId())
					.root()
					.get(requestConfiguration -> 
						requestConfiguration.queryParameters.select = ATTRS_DRIVE_ITEM);
			log.debug("Root item: {}", rootItem.getId());
			return rootItem;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	public List<MicrosoftDriveItem> searchDriveItems(Drive drive, String searchString, TokenCredential tokenProvider) {
		try {
			GraphServiceClient graphClient = client(tokenProvider);
			SearchWithQGetResponse response = graphClient
				.drives()
				.byDriveId(drive.getId())
				.searchWithQ(searchString)
				.get(requestConfiguration -> {
					requestConfiguration.queryParameters.select = ATTRS_DRIVE_ITEM;
					requestConfiguration.queryParameters.expand = EXPAND_DRIVE_ITEM;
				});

			List<DriveItem> driveItemList = response.getValue();
			return  toMicrosoftDriveItemList(driveItemList);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public List<MicrosoftDriveItem> getDriveItems(Drive drive, DriveItem parentItem, TokenCredential tokenProvider) {
		try {
			GraphServiceClient graphClient = client(tokenProvider);
			DriveItemCollectionResponse response = graphClient
				.drives()
				.byDriveId(drive.getId())
				.items()
				.byDriveItemId(parentItem.getId())
				.children()
				.get(requestConfiguration -> {
					requestConfiguration.queryParameters.select = ATTRS_DRIVE_ITEM;
					requestConfiguration.queryParameters.expand = EXPAND_DRIVE_ITEM;
				});

			List<DriveItem> driveItemList = response.getValue();
			return toMicrosoftDriveItemList(driveItemList);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private List<MicrosoftDriveItem> toMicrosoftDriveItemList(List<DriveItem> driveItemList) {
		if(driveItemList == null) {
			return List.of();
		}
		
		List<MicrosoftDriveItem> items = new ArrayList<>(driveItemList.size());
		for(DriveItem driveItem:driveItemList) {
			Map<String,Object> datas = driveItem.getAdditionalData();
			if(datas.containsKey("sensitivityLabel")) {
				Object element = datas.get("sensitivityLabel");
				if(element instanceof JsonObject obj && obj.has("protectionEnabled")) {
					boolean enabled = obj.get("protectionEnabled").getAsBoolean();
					log.info("driveItem has protection enabled: {}", enabled);
				}
			}
			
			ThumbnailSet thumbnails = null;
			List<ThumbnailSet> sets = driveItem.getThumbnails();
			if(sets != null && !sets.isEmpty()) {
				thumbnails = sets.get(0);
			}
			
			boolean directory = driveItem.getFolder() != null;
			items.add(new MicrosoftDriveItem(driveItem, thumbnails, directory));
		}
		return items;
	}
	
	public InputStream getItemContent(Drive drive, DriveItem driveItem, TokenCredential tokenProvider) {
		try {
			return client(tokenProvider)
					.drives()
					.byDriveId(drive.getId())
					.items()
					.byDriveItemId(driveItem.getId())
					.content()
					.get();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public DriveItem uploadLargeFile(Drive drive, DriveItem parentDriveItem, File file, String filename, TokenCredential tokenProvider) {
		try(InputStream fileStream = new FileInputStream(file)) {
			long streamSize = file.length();

			// Set body of the upload session request
			CreateUploadSessionPostRequestBody uploadSessionRequest = new CreateUploadSessionPostRequestBody();
			DriveItemUploadableProperties properties = new DriveItemUploadableProperties();
			properties.getAdditionalData().put("@microsoft.graph.conflictBehavior", "replace");
			uploadSessionRequest.setItem(properties);
	
			GraphServiceClient client = client(tokenProvider);
			// Create an upload session
			final UploadSession uploadSession = client
					.drives()
					.byDriveId(drive.getId())
					.items()
					.byDriveItemId(parentDriveItem.getId())
			        .createUploadSession()
			        .post(uploadSessionRequest);
	
			if (uploadSession != null) {
				// Create a callback used by the upload provider
				final IProgressCallback callback = (long current, long max) -> {
					log.debug("Uploaded {} bytes of {} total bytes", current, max);
				};
				
				// Create the upload task
				int maxSliceSize = 320 * 10;
				LargeFileUploadTask<DriveItem> largeFileUploadTask = new LargeFileUploadTask<>(
						client.getRequestAdapter(),
				        uploadSession,
				        fileStream,
				        streamSize,
				        maxSliceSize,
				        DriveItem::createFromDiscriminatorValue);

				UploadResult<DriveItem> uploadResult = largeFileUploadTask.upload(MAX_ATTEMPTS, callback);
			    if (uploadResult.isUploadSuccessful()) {
			        log.debug("Upload complete. Item ID: {}", uploadResult.itemResponse.getId());
			        return uploadResult.itemResponse; 
			    } 
			    log.warn("Upload failed: {}", filename);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	public static boolean accept(MicrosoftDriveItem driveItem, List<String> exclusionsList) {
		//TODO graph, label needs to be defined
		return accept(driveItem.name(), exclusionsList);
	}
	
	public static boolean accept(MicrosoftDrive drive, List<String> exclusionsList) {
		return accept(drive.name(), exclusionsList);
	}
	
	public static boolean accept(MicrosoftSite site, List<String> exclusionsList) {
		return accept(site.name(), exclusionsList);
	}
	
	private static boolean accept(String name, List<String> exclusionsList) {
		if(StringHelper.containsNonWhitespace(name)
				&& exclusionsList != null && !exclusionsList.isEmpty()) {
			for(String exclusionString:exclusionsList) {
				if(name.equalsIgnoreCase(exclusionString)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public GraphServiceClient client(TokenCredential authProvider) {
		return new GraphServiceClient(authProvider, "Directory.Read.All", "User.Read.All", "Sites.Read.All");
	}
}
