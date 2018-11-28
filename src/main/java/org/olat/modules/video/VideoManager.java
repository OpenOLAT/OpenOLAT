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
package org.olat.modules.video;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.video.manager.VideoExportMediaResource;
import org.olat.modules.video.model.TranscodingCount;
import org.olat.modules.video.model.VideoMetaImpl;
import org.olat.modules.video.ui.VideoChapterTableRow;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * The <code>VideoManager</code> singleton is responsible for dealing with video
 * resources.
 *
 * Initial date: 01.04.2015<br>
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public interface VideoManager {
	
	public static final String FILETYPE_SRT = "srt";
	public static final String DOT = "." ;
	
	/**
	 * Checks for video file.
	 *
	 * @param videoResource the video resource
	 * @return true, if successful
	 */
	public boolean hasVideoFile(OLATResource videoResource);
	
	/**
	 * get Videofile as File representation
	 * @param videoResource
	 * @return File 
	 */
	public File getVideoFile(OLATResource videoResource);

	/**
	 * get actually configured posterframe as VFSLeaf representation
	 * @param videoResource
	 * @return VFSLeaf
	 */
	public VFSLeaf getPosterframe(OLATResource videoResource);
	
	/**
	 * set posterframe for given videoResource
	 * @param videoResource
	 * @param posterframe
	 */
	public void setPosterframe(OLATResource videoResource, VFSLeaf posterframe);
	
	/**
	 * Sets the posterframe resize uploadfile.
	 *
	 * @param videoResource the video resource
	 * @param newPosterFile the new poster file
	 */
	public void setPosterframeResizeUploadfile(OLATResource videoResource, VFSLeaf newPosterFile);

	/**
	 * get all available Tracks of given videoResource
	 * @param videoResource
	 * @return HashMap<String, VFSLeaf>
	 */
	public Map<String, VFSLeaf> getAllTracks(OLATResource videoResource);
//
//	/**
//	 * add track file for given language to videoResource
//	 * @param videoResource
//	 * @param lang
//	 * @param trackFile
//	 */
//	public void addTrack(OLATResource videoResource, String lang, VFSLeaf trackFile);

	/**
	 * get Track in given lang as VFSLeaf
	 * @param videoResource
	 * @param lang
	 * @return VFSLeaf
	 */
	public VFSLeaf getTrack(OLATResource videoResource, String lang);

	/**
	 * remove a track in given language in videoResource
	 * @param videoResource
	 * @param lang
	 */
	public void removeTrack(OLATResource videoResource, String lang);

	/**
	 * get Frame at given frameNumber in video and save it in the VFSLeaf 'frame'
	 * @param videoResource
	 * @param frameNumber
	 * @param frame
	 * @return true if successfull or false
	 * @throws IOException
	 */
	public boolean getFrame(OLATResource videoResource, int frameNumber, VFSLeaf frame) throws IOException;

	/**
	 * Read the the metdatadata-xml in the videoresource folder
	 * @param videoResource
	 * @return
	 */
	public VideoMetadata readVideoMetadataFile(OLATResource videoResource);

	/**
	 * Trigger the transcoding process to generate versions of the video
	 * 
	 * @param video
	 * @return
	 */
	public void startTranscodingProcess(OLATResource video);
	
	/**
	 * Get all video transcodings for a specific video resource, sorted by
	 * resolution, highes resolution first
	 * 
	 * @param video
	 * @return
	 */
	public List<VideoTranscoding> getVideoTranscodings(OLATResource video);
	
	public VideoTranscoding getVideoTranscoding(Long key);
	
	/**
	 * Gets the all vidoe transcodings.
	 *
	 * @return the all vidoe transcodings
	 */
	public List<VideoTranscoding> getAllVideoTranscodings();
	
	/**
	 * Gets the one video resolution.
	 *
	 * @return the one video resolution
	 */
	public List<VideoTranscoding> getOneVideoResolution(int resolution);
	
	/**
	 * Gets the all video transcodings.
	 *
	 * @return the all video transcodings
	 */
	public List<TranscodingCount> getAllVideoTranscodingsCount();
	
	/**
	 * Gets the all video transcodings count success.
	 *
	 * @param errorcode the errorcode
	 * @return the all video transcodings count success
	 */
	public List<TranscodingCount> getAllVideoTranscodingsCountSuccess(int errorcode);

	/**
	 * Gets the all video transcodings count fails.
	 *
	 * @param errorcode the errorcode
	 * @return the all video transcodings count fails
	 */
	public List<TranscodingCount> getAllVideoTranscodingsCountFails(int errorcode);

	/**
	 * Get a human readable aspect ratio from the given video size. Recognizes
	 * the most common aspect ratios
	 * 
	 * @param width
	 * @param height
	 * @return String containing a displayable aspect ratio
	 */
	public String getAspectRatio(int width, int height);

	/**
	 * Create a display title for the given resolution. The title uses the i18n
	 * keys for standard resolutions. For non standard resolutions, the
	 * "original" translation is used
	 * 
	 * @param resolution The resolution of the video
	 * @param translator The translator to be used
	 * @return The display title for this resolution
	 */
	public String getDisplayTitleForResolution(int resolution, Translator translator);

	/**
	 * Get the master container for this resource where the actual video is stored
	 * @param videoResource
	 * @return VFSContainer
	 */
	public VFSContainer getMasterContainer(OLATResource videoResource);
	
	/**
	 * Get the container where all the transcoded videos are stored
	 * @param videoResource
	 * @return VFSContainer
	 */
	public VFSContainer getTranscodingContainer(OLATResource videoResource);
	
	/**
	 * Get the master video file 
	 * @param videoResource
	 * @return VFSLeaf or NULL if it does not exist
	 */
	public VFSLeaf getMasterVideoFile(OLATResource videoResource);
	
	/**
	 * Get a media resource which represents an export of a video resource
	 * @param repoEntry
	 * @return VideoExportMediaResource which generates a ZIP file on the fly
	 */
	public VideoExportMediaResource getVideoExportMediaResource(RepositoryEntry repoEntry);

	/**
	 * Check if the given file is an archive generated by the export (and thus
	 * can be imported again). The validation callback is set to true if valid
	 * and the title and description are extracted
	 * 
	 * @param file
	 *            The zip file handle
	 * @param eva
	 *            the resource validation callback
	 * @return true: recognized as video resource; false: not recognized
	 */
	public void validateVideoExportArchive(File file,  ResourceEvaluation eval);

	/**
	 * Import the given file to the resource on disk
	 * @param repoEntry 
	 *            The repository entry that represents the video in the repository
	 * @param masterVideo The video file to be added to the repository. Must be an mp4 file.
	 */
	public boolean importFromMasterFile(RepositoryEntry repoEntry, VFSLeaf masterVideo);

	/**
	 * Import the given export archive to the resource on disk
	 * 
	 * @param repoEntry
	 *            The repository entry that represents the video in the repository
	 * @param exportArchive
	 *            The archive to be added to the repository. The archive must be
	 *            created by the video export feature.
	 */
	public boolean importFromExportArchive(RepositoryEntry repoEntry, VFSLeaf exportArchive);

	/**
	 * Update video transcoding
	 * 
	 * @param videoTranscoding
	 * @return VideoTranscoding the updated transcoding object
	 */
	public VideoTranscoding updateVideoTranscoding(VideoTranscoding videoTranscoding);

	/**
	 * Copy video resource to identical new video resource. 
	 * @param sourceResource the existing video resource
	 * @param targetResource the empty new resource
	 */
	public void copyVideo(OLATResource sourceResource, OLATResource targetResource);

	/**
	 * Delete the video transcodings on disk an in database
	 * @param videoResource
	 * @return true: success; false: failed
	 */
	public boolean deleteVideoTranscodings(OLATResource videoResource);

	/**
	 * Delete single transcoding of resource
	 * @param videoTranscoding
	 * @return
	 */
	public void deleteVideoTranscoding(VideoTranscoding videoTranscoding);
	
	/**
	 * @return List of video transcodings which have not yet been done
	 */
	public List<VideoTranscoding> getVideoTranscodingsPendingAndInProgress();
	
	/**
	 * Gets the failed video transcodings.
	 *
	 * @return list of failed VideoTranscoding
	 */
	public List<VideoTranscoding> getFailedVideoTranscodings();
	
	/**
	 * Returns a list with 
	 * @param videoResource
	 * @return List with versions of videos which are 
	 */
	public List<Integer> getMissingTranscodings(OLATResource videoResource);
	
	/**
	 * create a VideoTransconding with the given configuration.
	 * @param video
	 * @param resolution
	 * @param format
	 * @return
	 */
	public VideoTranscoding createTranscoding(OLATResource video, int resolution,String format);

	/**
	 * Checks for chapters.
	 *
	 * @param videoResource the video resource
	 * @return true, if successful
	 */
	public boolean hasChapters(OLATResource videoResource);

	/**
	 * Load chapters.
	 *
	 * @param chapters the chapters
	 * @param olatResource the video resource
	 */
	public List<VideoChapterTableRow> loadChapters(OLATResource olatResource);

	/**
	 * Save chapters.
	 *
	 * @param chapters the chapters
	 * @param olatResource the video resource
	 */
	public void saveChapters(List<VideoChapterTableRow> chapters, OLATResource olatResource);
	
	
	public VideoMarkers loadMarkers(OLATResource olatResource);
	
	public void saveMarkers(VideoMarkers markers, OLATResource olatResource);
	
	
	/**
	 * Gets the video duration.
	 * 
	 * @param OLATResource videoResource 
	 * @return the video duration
	 */
	public long getVideoDuration(OLATResource videoResource);
	
	public long getVideoFrameCount(OLATResource videoResource);
	
	/**
	 * Gets the video resolution from olat resource.
	 *
	 * @param videoResource the video resource
	 * @return the video resolution from olat resource
	 */
	public Size getVideoResolutionFromOLATResource (OLATResource videoResource);
	
	/**
	 * Gets the all video resources metadata.
	 *
	 * @return the all video resources metadata
	 */
	public List<VideoMetaImpl> getAllVideoResourcesMetadata();
	
	/**
	 * Gets the video meta data.
	 *
	 * @param videoResource the video resource
	 * @return the video meta data
	 */
	public VideoMetaImpl getVideoMetadata(OLATResource videoResource);

	/**
	 * Exchange poster of the new resource.
	 *
	 * @param videoResource the OLATResource
	 */
	public void exchangePoster(OLATResource videoResource);

	/**
	 * Update video metadata.
	 *
	 * @param videoResource the OLATResource
	 * @param uploadVideo the upload video
	 */
	public void updateVideoMetadata(OLATResource videoResource, VFSLeaf uploadVideo);
	
	/**
	 * Gets the all video repo entries.
	 *
	 * @param typename of a type
	 * @return repo entries
	 */
	public List<RepositoryEntry> getAllVideoRepoEntries (String typename);

	/**
	 * Delete video metadata.
	 *
	 * @param videoResource the video resource
	 */
	public boolean deleteVideoMetadata(OLATResource videoResource);

	/**
	 * Creates the video metadata.
	 *
	 * @param repoEntry the repo entry
	 * @param size the size
	 * @param fileName the file name
	 * @return the video meta
	 */
	public VideoMeta createVideoMetadata(RepositoryEntry repoEntry, long size, String fileName);

	/**
	 * Start transcoding process if enabled.
	 *
	 * @param video the video
	 */
	public void startTranscodingProcessIfEnabled(OLATResource video);
	
	/**
	 * Retranscode failed video transcoding.
	 *
	 * @param videoTranscoding
	 * @return 
	 */
	public VideoTranscoding retranscodeFailedVideoTranscoding(VideoTranscoding videoTranscoding);

	/**
	 * Checks if is metadata file valid.
	 *
	 * @param videoResource
	 * @return true, if is metadata file valid
	 */
	public boolean isMetadataFileValid(OLATResource videoResource);

	/**
	 * Checks for master container.
	 *
	 * @param videoResource
	 * @return true, if master container can be resolved
	 */
	public boolean hasMasterContainer(OLATResource videoResource);

	/**
	 * Checks if meta data is available for the given resource
	 * 
	 * @param videoResource
	 * @return true, if meta data can be resolved
	 */
	public boolean hasVideoMetadata(OLATResource videoResource);

	/**
 	 * get Frame at given frameNumber in video and save it in the VFSLeaf 'frame' if the image is not mostly black
 	 *
	 * @param videoResource
	 * @param frameNumber
	 * @param duration
	 * @param frame resource
	 * @return true if image proposal is mostly black
	 */
	public boolean getFrameWithFilter(OLATResource videoResource, int frameNumber, long duration, VFSLeaf frame);

}