/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.helper.utils;

import me.lucko.helper.Events;
import me.lucko.helper.terminable.Terminable;

import org.bukkit.Bukkit;
import org.bukkit.event.server.ServiceEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;

import java.util.Optional;

/**
 * A wrapper to always provide the latest instance of a service.
 *
 * @param <T> the service class type
 */
public final class ServiceCallback<T> implements Terminable {

    /**
     * Create a new ServiceCallback for the given class.
     *
     * @param serviceClass the service class
     * @param <T> the service class type
     * @return a new service callback
     */
    public static <T> ServiceCallback<T> of(Class<T> serviceClass) {
        return new ServiceCallback<>(serviceClass);
    }

    private T instance = null;
    private Class<T> serviceClass;
    private final Events.MergedHandler listener;

    private ServiceCallback(Class<T> serviceClass) {
        this.serviceClass = serviceClass;
        refresh();

        // listen for service updates
        this.listener = Events.merge(ServiceEvent.class, ServiceRegisterEvent.class, ServiceUnregisterEvent.class)
                .filter(e -> e.getProvider() != null && e.getProvider().getService().equals(serviceClass))
                .handler(e -> refresh());
    }

    /**
     * Refreshes the backing instance of the service
     */
    public void refresh() {
        instance = Bukkit.getServicesManager().load(serviceClass);
    }

    /**
     * Gets the service provider, or null if it is not provided for.
     *
     * @return the service provider
     */
    public T getNullable() {
        return this.instance;
    }

    /**
     * Gets the service provider.
     *
     * @return the service provider
     */
    public Optional<T> get() {
        return Optional.ofNullable(this.instance);
    }

    @Override
    public boolean terminate() {
        return listener.terminate();
    }

    @Override
    public boolean hasTerminated() {
        return listener.hasTerminated();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ServiceCallback)) return false;
        final ServiceCallback other = (ServiceCallback) o;
        return this.serviceClass.equals(other.serviceClass);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.serviceClass.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ServiceCallback(serviceClass=" + this.serviceClass + ")";
    }
}
