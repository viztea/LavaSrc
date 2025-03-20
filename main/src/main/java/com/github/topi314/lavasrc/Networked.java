package com.github.topi314.lavasrc;

import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Networked extends HttpConfigurable {
    HttpInterfaceManager getHttpInterfaceManager();

    default HttpInterface getHttpInterface() {
        return this.getHttpInterfaceManager().getInterface();
    }

    @Override
    default void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        this.getHttpInterfaceManager().configureRequests(configurator);
    }

    @Override
    default void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        this.getHttpInterfaceManager().configureBuilder(configurator);
    }
}
