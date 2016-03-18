package org.olat.modules.video.service;

import java.util.Map;

import org.olat.resource.OLATResource;

public interface Transcoder {
	public boolean transcodeVideoRessource(OLATResource video, Map<String, String> params);
}
