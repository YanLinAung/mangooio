package io.mangoo.managers;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.eventbus.EventBus;
import com.google.inject.Singleton;

import io.mangoo.enums.Required;

/**
*
* @author svenkubiak
*
*/
@Singleton
public class BusManager {
    private EventBus eventBus;
    private AtomicLong listeners = new AtomicLong();
    private AtomicLong events = new AtomicLong();
    
    public BusManager() {
        this.eventBus = new EventBus();
    }
    
    /**
     * Registers an event listener to the event bus
     * 
     * @param eventListener The listener to register
     */
    public void register(Object eventListener) {
        Objects.requireNonNull(eventListener, Required.EVENT_LISTENER.toString());
        
        this.eventBus.register(eventListener);
        this.listeners.getAndIncrement();
    }
    
    /**
     * Unregisters an event listener to the event bus
     * 
     * @param eventListener The listener to unregister
     */
    public void unregister(Object eventListener) {
        Objects.requireNonNull(eventListener, Required.EVENT_LISTENER.toString());
        
        this.eventBus.unregister(eventListener);
        this.listeners.getAndDecrement();
    }
    
    /**
     * Publishes an event to the event bus
     * 
     * @param event The event to publish
     */
    public void publish(Object event) {
        Objects.requireNonNull(event, Required.EVENT.toString());
        
        this.eventBus.post(event);
        this.events.getAndIncrement();
    }
    
    /**
     * @return The number of registered listeners
     */
    public long getNumListeners() {
        return this.listeners.get();
    }
    
    /**
     * @return The number of published events to the event bus
     */
    public long getNumEvents() {
        return this.events.get();
    }
}