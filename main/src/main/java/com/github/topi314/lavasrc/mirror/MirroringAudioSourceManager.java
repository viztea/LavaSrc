package com.github.topi314.lavasrc.mirror;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.topi314.lavasrc.ExtendedAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated
 */
@Deprecated(forRemoval = true)
public abstract class MirroringAudioSourceManager extends ExtendedAudioSourceManager implements HttpConfigurable {

	public static final String ISRC_PATTERN = "%ISRC%";
	public static final String QUERY_PATTERN = "%QUERY%";
	private static final Logger log = LoggerFactory.getLogger(MirroringAudioSourceManager.class);
	protected final Supplier<AudioPlayerManager> audioPlayerManager;
	protected final MirroringAudioTrackResolver resolver;

	protected final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();

	protected MirroringAudioSourceManager(AudioPlayerManager audioPlayerManager, MirroringAudioTrackResolver resolver) {
		this(() -> audioPlayerManager, resolver);
	}

	protected MirroringAudioSourceManager(Supplier<AudioPlayerManager> audioPlayerManager, MirroringAudioTrackResolver resolver) {
		this.audioPlayerManager = audioPlayerManager;
		this.resolver = resolver;
	}

	@Override
	public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
		this.httpInterfaceManager.configureRequests(configurator);
	}

	@Override
	public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
		this.httpInterfaceManager.configureBuilder(configurator);
	}

	public HttpInterface getHttpInterface() {
		return this.httpInterfaceManager.getInterface();
	}

	@Override
	public void shutdown() {
		try {
			this.httpInterfaceManager.close();
		} catch (IOException e) {
			log.error("Failed to close HTTP interface manager", e);
		}
	}

	public AudioPlayerManager getAudioPlayerManager() {
		return this.audioPlayerManager.get();
	}

	public MirroringAudioTrackResolver getResolver() {
		return this.resolver;
	}

	public MirroringResources getResources() {
		return new MirroringResources(this.audioPlayerManager, this.resolver);
	}
}
