/*
 * Copyright 2025 Konstantin Terskikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.spb.tksoft.service.nats.service;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import io.nats.client.Nats;
import io.nats.client.Options;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import ru.spb.tksoft.service.nats.config.NatsConfiguration;
import ru.spb.tksoft.utils.log.LogEx;

/**
 * Thread-safe NATS service.
 * 
 * @author Konstantin Terskikh, kostus.online.1974@yandex.ru, 2025
 */
@Service
@ConditionalOnProperty(name = "nats.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class NatsService {

    private static final Logger log = LoggerFactory.getLogger(NatsService.class);

    private final NatsConfiguration natsConfig;
    private Connection natsConnection;

    private final ReentrantReadWriteLock connectionLock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<String, Dispatcher> dispatchers = new ConcurrentHashMap<>();

    /**
     * Connect to NATS server.
     * 
     * @throws IOException - if an I/O error occurs.
     * @throws InterruptedException - if the thread is interrupted.
     */
    @PostConstruct
    public void connect() throws IOException, InterruptedException {

        connectionLock.writeLock().lock();
        try {
            if (natsConnection != null) {
                LogEx.trace(log, LogEx.me(), LogEx.STOPPED, "Already connected to NATS server");
                return;
            }

            LogEx.trace(log, LogEx.me(), LogEx.STARTING, "Connecting to NATS servers {} ...",
                    natsConfig.getServers());

            Options options = new Options.Builder()
                    .servers(natsConfig.getServers().split(","))
                    .connectionTimeout(Duration.ofMillis(natsConfig.getConnection().getTimeout()))
                    .reconnectWait(Duration.ofSeconds(1))
                    .maxReconnects(natsConfig.getConnection().getMaxReconnects())
                    .build();

            natsConnection = Nats.connect(options);
            LogEx.trace(log, LogEx.me(), LogEx.STOPPED, "Connected to NATS server: {}",
                    natsConfig.getServers());
        } finally {
            connectionLock.writeLock().unlock();
        }
    }

    /**
     * Disconnect from NATS server.
     * 
     * @throws InterruptedException - if the thread is interrupted.
     */
    @PreDestroy
    public void disconnect() throws InterruptedException {

        connectionLock.writeLock().lock();
        try {
            if (natsConnection != null) {
                // Close all dispatchers.
                dispatchers.forEach((subject, dispatcher) -> {
                    try {
                        dispatcher.unsubscribe(subject);
                    } catch (Exception e) {
                        LogEx.error(log, LogEx.me(), LogEx.EXCEPTION_THROWN,
                                "Failed to unsubscribe dispatcher for subject: {}", subject, e);
                    }
                });
                dispatchers.clear();

                natsConnection.close();
                natsConnection = null;
                LogEx.trace(log, LogEx.me(), LogEx.STOPPED, "Disconnected from NATS server");
            }
        } finally {
            connectionLock.writeLock().unlock();
        }
    }

    /**
     * Publish a message to a subject.
     * 
     * @param subject - the subject to publish to.
     * @param message - the message to publish.
     */
    public void publish(String subject, String message) {

        connectionLock.readLock().lock();
        try {
            if (natsConnection == null) {
                LogEx.error(log, LogEx.me(), LogEx.EXCEPTION_THROWN,
                        "Cannot publish message: NATS connection is not established");
                return;
            }

            natsConnection.publish(subject, message.getBytes());

            LogEx.trace(log, LogEx.me(), LogEx.STOPPED, "Message published", subject);
        } catch (Exception e) {
            LogEx.error(log, LogEx.me(), LogEx.EXCEPTION_THROWN,
                    "Failed to publish", subject, e);
        } finally {
            connectionLock.readLock().unlock();
        }
    }

    /**
     * Subscribe to a subject.
     * 
     * @param subject - the subject to subscribe to.
     * @param handler - the handler to handle the messages.
     */
    public void subscribe(String subject, MessageHandler handler) {

        connectionLock.readLock().lock();
        try {
            if (natsConnection == null) {
                LogEx.error(log, LogEx.me(), LogEx.EXCEPTION_THROWN,
                        "Cannot subscribe: NATS connection is not established");
                return;
            }

            // Use existing dispatcher or create a new one.
            Dispatcher dispatcher = dispatchers.computeIfAbsent(subject,
                    s -> natsConnection.createDispatcher(handler));

            dispatcher.subscribe(subject);
            LogEx.trace(log, LogEx.me(), LogEx.STOPPED, "Subscribed to subject: {}", subject);

        } catch (Exception e) {
            LogEx.error(log, LogEx.me(), LogEx.EXCEPTION_THROWN,
                    "Failed to subscribe to subject: {}", subject, e);
        } finally {
            connectionLock.readLock().unlock();
        }
    }
}
