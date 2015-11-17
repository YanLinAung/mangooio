package io.mangoo.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.test.MangooInstance;
import io.mangoo.utils.ConfigUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 * 
 * @author svenkubiak
 *
 */
public class ServerEventManagerTest {
    private static String eventData;
    private static final String COOKIE_NAME = "TEST-AUTH";
    private static final String VALID_COOKIE_VALUE = "359770bc1a7b38a6dee6ea0ce9875a3d71313f78470174fd460258e4010a51cb2db9c728c5d588958c52d2ef9fe9f6f63ed3aeb4f1ab828e29ce963703eb9237|2999-11-11T11:11:11.111|0#mangooio";
    private static final String INVALID_COOKIE_VALUE = "359770bc1a7b38a6dee6ea0ce9875a3d71313f78470174fd460258e4010a51cb2db9c728c5d588958c52d2ef9fe9f6f63ed3aeb4f1ab828e29ce963703eb9237|2999-11-11T11:11:11.111|0#mangooiO";
    
    @Test
    public void testAddConnection() {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        ServerSentEventConnection serverSentEventConnection = Mockito.mock(ServerSentEventConnection.class);
        when(serverSentEventConnection.getRequestURI()).thenReturn("/foo");
        when(serverSentEventConnection.getQueryString()).thenReturn(null);
        
        //when
        serverEventManager.addConnection(serverSentEventConnection);
        
        //then
        assertThat(serverEventManager.getConnections("/foo"), not(nullValue()));
        assertThat(serverEventManager.getConnections("/foo").size(), equalTo(1));
    }
    
    @Test
    public void testRemoveConnection() {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        ServerSentEventConnection serverSentEventConnection = Mockito.mock(ServerSentEventConnection.class);
        when(serverSentEventConnection.getRequestURI()).thenReturn("/foo");
        serverEventManager.addConnection(serverSentEventConnection);
        
        //when
        serverEventManager.removeConnections("/foo");
        
        //then
        assertThat(serverEventManager.getConnections("/foo"), not(nullValue()));
        assertThat(serverEventManager.getConnections("/foo").size(), equalTo(0));
    }
    
    @Test
    public void testCloseConnection() throws InterruptedException {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        
        //when
        WebTarget target = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build()
                .target("http://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/sse");
        EventSource eventSource = EventSource.target(target).build();
        eventSource.open();
        Thread.sleep(1000);
        serverEventManager.close("/sse");
        eventSource.close();
        Thread.sleep(1000);
        
        //then
        assertThat(serverEventManager.getConnections("/sse"), not(nullValue()));
        assertThat(serverEventManager.getConnections("/sse").size(), equalTo(0));
    }
    
    @Test
    public void testSendData() throws InterruptedException {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        eventData = null;
        String data = "Server sent data FTW!";
        
        //when
        WebTarget target = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build()
                .target("http://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/sse");
        EventSource eventSource = EventSource.target(target).build();
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                eventData = inboundEvent.readData(String.class);                    
            }
        };
        eventSource.register(listener);
        eventSource.open();
        Thread.sleep(1000);
        serverEventManager.send("/sse", data);
        Thread.sleep(1000);
        
        //then
        assertThat(eventData, not(nullValue()));
        assertThat(eventData, equalTo(data));
        eventSource.close();
    }
    
    @Test
    public void testSendDataWithValidAuthentication() throws InterruptedException {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        eventData = null;
        String data = "Server sent data with authentication FTW!";
        
        //when
        WebTarget target = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build()
                .target("http://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/sseauth");

        CustomWebTarget customWebTarget = new CustomWebTarget(target, new Cookie(COOKIE_NAME, VALID_COOKIE_VALUE));
        EventSource eventSource = EventSource.target(customWebTarget).build();
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                eventData = inboundEvent.readData(String.class);                    
            }
        };
        eventSource.register(listener);
        eventSource.open();
        Thread.sleep(1000);
        serverEventManager.send("/sseauth", data);
        Thread.sleep(1000);
        
        //then
        assertThat(eventData, not(nullValue()));
        assertThat(eventData, equalTo(data));
        eventSource.close();
    }
    
    @Test
    public void testSendDataWithInvalidAuthentication() throws InterruptedException {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        String data = "Server sent data with authentication FTW!";
        
        //when
        WebTarget target = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build()
                .target("http://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/sseauth");

        CustomWebTarget customWebTarget = new CustomWebTarget(target, new Cookie(COOKIE_NAME, INVALID_COOKIE_VALUE));
        EventSource eventSource = EventSource.target(customWebTarget).build();
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                if (StringUtils.isBlank(eventData)) {
                    eventData = inboundEvent.readData(String.class);                    
                }
            }
        };
        eventSource.register(listener);
        eventSource.open();
        Thread.sleep(1000);
        serverEventManager.send("/sseauth", data);
        Thread.sleep(1000);
        
        //then
        assertThat(eventData, nullValue());
        assertThat(eventData, not(equalTo(data)));
        eventSource.close();
    }
}