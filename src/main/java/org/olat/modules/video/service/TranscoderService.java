package org.olat.modules.video.service;

import java.util.ServiceLoader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TranscoderService{

	@Value("${video.transcoding.provider:handbrake}")
	private static String transcodingProvider;

    private static TranscoderService service;
    private ServiceLoader<Transcoder> loader;

    private TranscoderService() {
        loader = ServiceLoader.load(Transcoder.class);
    }

    public static synchronized TranscoderService getInstance() {
        if (service == null) {
            service = new TranscoderService();
        }
        return service;
    }
}
