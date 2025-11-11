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

package ru.spb.tksoft.service.nats.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * NATS configuration.
 * 
 * @author Konstantin Terskikh, kostus.online.1974@yandex.ru, 2025
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "nats")
public class NatsConfiguration {

    public static final String DEFAULT_SERVERS = "nats://localhost:4222";

    /** NATS enabled flag. */
    private boolean enabled = true;

    /** NATS servers with default value. */
    private String servers = DEFAULT_SERVERS;

    /** NATS connection configuration. */
    private Connection connection = new Connection();

    /** NATS connection configuration implementation. */
    @Data
    public static class Connection {

        private int timeout = 5000;
        private boolean reconnect = true;
        private int maxReconnects = -1;
    }
}
