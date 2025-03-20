package com.github.topi314.lavasrc.mirror;

import java.util.function.Supplier;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class MirroringResources {

	public static final String ISRC_PATTERN = "%ISRC%";

	public static final String QUERY_PATTERN = "%QUERY%";

	private final Supplier<AudioPlayerManager> audioPlayerManager;

	private final MirroringAudioTrackResolver resolver;

	public MirroringResources(Supplier<AudioPlayerManager> audioPlayerManager, String[] providers) {
		this(audioPlayerManager, new DefaultMirroringAudioTrackResolver(providers));
	}

	public MirroringResources(Supplier<AudioPlayerManager> audioPlayerManager, MirroringAudioTrackResolver resolver) {
		this.audioPlayerManager = audioPlayerManager;
		this.resolver = resolver;
	}

	public AudioPlayerManager getAudioPlayerManager() {
		return this.audioPlayerManager.get();
	}

	public MirroringAudioTrackResolver getResolver() {
		return this.resolver;
	}
}
