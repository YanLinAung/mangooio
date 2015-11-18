package io.mangoo.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.test.Mangoo;
import io.mangoo.utils.ConfigUtils;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

/**
 * 
 * @author svenkubiak
 *
 */
public class WebSocketManagerTest {
    private static String eventData;
    private static final String COOKIE_NAME = "TEST-AUTH";
    private static final String VALID_COOKIE_VALUE = "359770bc1a7b38a6dee6ea0ce9875a3d71313f78470174fd460258e4010a51cb2db9c728c5d588958c52d2ef9fe9f6f63ed3aeb4f1ab828e29ce963703eb9237|2999-11-11T11:11:11.111|0#mangooio";
    private static final String INVALID_COOKIE_VALUE = "359770bc1a7b38a6dee6ea0ce9875a3d71313f78470174fd460258e4010a51cb2db9c728c5d588958c52d2ef9fe9f6f63ed3aeb4f1ab828e29ce963703eb9237|2999-11-11T11:11:11.111|0#mangooiO";
    
    @Test
    public void testAddChannel() {
        //given
        WebSocketManager webSocketManager = Mangoo.TEST.getInstance(WebSocketManager.class);
        WebSocketChannel channel = Mockito.mock(WebSocketChannel.class);
        
        //when
        webSocketManager.addChannel("/websocket", null, channel);
        
        //then
        assertThat(webSocketManager.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketManager.getChannels("/websocket").size(), equalTo(1));
    }
    
    @Test
    public void testRemoveChannel() {
        //given
        WebSocketManager webSocketManager = Mangoo.TEST.getInstance(WebSocketManager.class);
        WebSocketChannel channel = Mockito.mock(WebSocketChannel.class);
        
        //when
        webSocketManager.addChannel("/websocket", null, channel);
        webSocketManager.removeChannels("/websocket");
        
        //then
        assertThat(webSocketManager.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketManager.getChannels("/websocket").size(), equalTo(0));
    }
    
    @Test
    public void testCloseChannel() throws Exception {
        //given
        WebSocketManager webSocketManager = Mangoo.TEST.getInstance(WebSocketManager.class);
        webSocketManager.removeChannels("/websocket");
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        String uri = "ws://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/websocket";
        
        //when
        WebSocketClient client = new WebSocketClient(factory);
        client.open(new URI(uri), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // intentionally left blank
            }

            @Override
            public void onClose(int closeCode, String message) {
                // intentionally left blank
            }

            @Override
            public void onMessage(String data) {
                // intentionally left blank
            }
        }).get(5, TimeUnit.SECONDS);
        
        webSocketManager.close("/websocket");
        
        //then
        assertThat(webSocketManager.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketManager.getChannels("/websocket").size(), equalTo(0));
    }
    
    @Test
    public void testSendData() throws Exception {
        //given
        WebSocketManager webSocketManager = Mangoo.TEST.getInstance(WebSocketManager.class);
        webSocketManager.removeChannels("/websocket");
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        String uri = "ws://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/websocket";
        String data = "Server sent data FTW!";
        eventData = null;
        
        //when
        new WebSocketClient(factory).open(new URI(uri), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // intentionally left blank
            }

            @Override
            public void onClose(int closeCode, String message) {
                // intentionally left blank
            }

            @Override
            public void onMessage(String data) {
                eventData = data;                    
            }
        }).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);
        webSocketManager.getChannels("/websocket").forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }               
            } catch (IOException e) {
                e.printStackTrace();
            }
         });
        Thread.sleep(500);
        
        //then
        assertThat(eventData, not(nullValue()));
        assertThat(eventData, equalTo(data));
    }
    
    @Test
    public void testSendDataWithValidAuthentication() throws Exception {
        //given
        WebSocketManager webSocketManager = Mangoo.TEST.getInstance(WebSocketManager.class);
        webSocketManager.removeChannels("/websocketauth");
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        String uri = "ws://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/websocketauth";
        String data = "Server sent data with authentication FTW!";
        eventData = null;
        
        //when
        WebSocketClient client = new WebSocketClient(factory);
        client.getCookies().put(COOKIE_NAME, VALID_COOKIE_VALUE);
        client.open(new URI(uri), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // intentionally left blank
            }

            @Override
            public void onClose(int closeCode, String message) {
                // intentionally left blank
            }

            @Override
            public void onMessage(String data) {
                if (StringUtils.isBlank(eventData)) {
                    eventData = data;                    
                }
            }
        }).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);
        webSocketManager.getChannels("/websocketauth").forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }             
            } catch (IOException e) {
                e.printStackTrace();
            }
         });
        Thread.sleep(500);
        
        //then
        assertThat(eventData, not(nullValue()));
        assertThat(eventData, equalTo(data));
    }
    
    @Test
    public void testSendDataWithInvalidAuthentication() throws Exception {
        //given
        WebSocketManager webSocketManager = Mangoo.TEST.getInstance(WebSocketManager.class);
        webSocketManager.removeChannels("/websocketauth");
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        String uri = "ws://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/websocketauth";
        String data = "Server sent data with authentication FTW!";
        eventData = null;
        
        //when
        WebSocketClient client = new WebSocketClient(factory);
        client.getCookies().put(COOKIE_NAME, INVALID_COOKIE_VALUE);
        client.open(new URI(uri), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // intentionally left blank
            }

            @Override
            public void onClose(int closeCode, String message) {
                // intentionally left blank
            }

            @Override
            public void onMessage(String data) {
                if (StringUtils.isBlank(eventData)) {
                    eventData = data;                    
                }
            }
        }).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);        
        webSocketManager.getChannels("/websocketauth").forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
         });
        Thread.sleep(500);
        
        //then
        assertThat(eventData, nullValue());
        assertThat(eventData, not(equalTo(data)));
    }
}