package org.olat.modules.video.service;

import org.olat.core.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TranscoderService{

	@Value("${video.transcoding.provider:handbrake}")
	private static String transcodingProvider;

    private static TranscoderService service;

    private TranscoderService() {
    }

    public static synchronized TranscoderService getInstance() {
        if (service == null) {
            service =  CoreSpringFactory.getImpl(TranscoderService.class);
        }
        return service;
    }
    

}
