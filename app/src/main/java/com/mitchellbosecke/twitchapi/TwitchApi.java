package com.mitchellbosecke.twitchapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by mitch_000 on 2016-09-24.
 */
public class TwitchApi {

    private static final int VERSION = 3;
    private static final String BASE_URL = "https://api.twitch.tv/kraken";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String clientId;

    public TwitchApi(String clientId) {
        this.clientId = clientId;
    }

    public ChannelFollowsPage followers(String channel) {
        return followers(channel, null);
    }

    public ChannelFollowsPage followers(String channel, String cursor) {

        String path = "/channels/" + cleanChannelName(channel) + "/follows?limit=100";
        if (cursor != null) {
            path = path + "&cursor=" + cursor;
        }

        ChannelFollowsPage page = performGet(ChannelFollowsPage.class, path);
        page.setChannel(channel);
        return page;
    }

    public Stream stream(String channel) {
        String path = "/streams/" + cleanChannelName(channel);
        return performGet(StreamContainer.class, path).getStream();
    }

    public Channel channel(String channel){
        String path = "/channels/" + cleanChannelName(channel);
        return performGet(Channel.class, path);
    }

    private String cleanChannelName(String channel) {
        return channel.replaceAll("#", "");
    }

    private <T> T performGet(Class<T> clazz, String path) {

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(BASE_URL + path);
        httpGet.addHeader("ACCEPT", "application/vnd.twitchtv.v" + Integer.toString(VERSION) + "+json");
        httpGet.addHeader("Client-ID", clientId);

        CloseableHttpResponse response = null;
        T result = null;
        try {

            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);

            result = mapper.readValue(responseString, clazz);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

}
