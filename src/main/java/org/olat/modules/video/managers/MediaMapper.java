package org.olat.modules.video.managers;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

public class MediaMapper implements Mapper {
	
	private final VFSContainer mediaBase;
	
	public MediaMapper(VFSContainer mediaBase) {
		this.mediaBase = mediaBase;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		VFSItem mediaFile = mediaBase.resolve(relPath);
		if (mediaFile instanceof VFSLeaf){
			return new VFSMediaResource((VFSLeaf)mediaFile);
		} else {
 			return new NotFoundMediaResource(relPath);
		}
	}
}
