package com.github.topi314.lavasrc.flowerytts;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class FloweryTTSSourceManager implements AudioSourceManager, HttpConfigurable {

	public static final String TTS_PREFIX = "ftts://";
	private static final Logger log = LoggerFactory.getLogger(FloweryTTSSourceManager.class);
	private static final int CHAR_MAX = 2048;
	private static final int SILENCE_MIN = 0;
	private static final int SILENCE_MAX = 10000;
	private static final float SPEED_MIN = 0.5f;
	private static final float SPEED_MAX = 10;
	private final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
	private String voice = null;
	private boolean translate = false;
	private int silence = 0;
	private float speed = 1;
	private String audioFormat = "mp3";

	public FloweryTTSSourceManager() {
	}

	public FloweryTTSSourceManager(String voice) {
		if (voice == null || voice.isEmpty()) {
			throw new IllegalArgumentException("Default voice must be set");
		}
		this.voice = voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public void setTranslate(boolean translate) {
		this.translate = translate;
	}

	public void setSilence(int silence) {
		this.silence = Math.max(SILENCE_MIN, Math.min(SILENCE_MAX, silence));
	}

	public void setSpeed(float speed) {
		this.speed = Math.max(SPEED_MIN, Math.min(SPEED_MAX, speed));
	}

	public void setAudioFormat(String audioFormat) {
		this.audioFormat = audioFormat;
	}

	public Map<String, String> getDefaultConfig() {
		return Map.of(
			"voice", this.voice,
			"translate", Boolean.toString(this.translate),
			"silence", Integer.toString(this.silence),
			"speed", Float.toString(this.speed),
			"audio_format", this.audioFormat
		);
	}

	@Override
	public String getSourceName() {
		return "flowery-tts";
	}

	@Override
	public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
		if (!reference.identifier.startsWith(TTS_PREFIX)) {
			return null;
		}

		try {
			var text = new URI(reference.identifier).getAuthority();
			if (text == null) {
				return null;
			}
			if (text.length() > CHAR_MAX) {
				throw new IllegalArgumentException("Character limit per request exceeded");
			}

			return new FloweryTTSAudioTrack(
				new AudioTrackInfo(
					text,
					"flowery-tts",
					Units.CONTENT_LENGTH_UNKNOWN,
					reference.identifier,
					false,
					null), this);
		} catch (URISyntaxException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isTrackEncodable(AudioTrack track) {
		return true;
	}

	@Override
	public void encodeTrack(AudioTrack track, DataOutput output) {
		// nothing to encode
	}

	@Override
	public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
		return new FloweryTTSAudioTrack(trackInfo, this);
	}

	@Override
	public void shutdown() {
		try {
			this.httpInterfaceManager.close();
		} catch (IOException e) {
			log.error("Failed to close HTTP interface manager", e);
		}
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

}