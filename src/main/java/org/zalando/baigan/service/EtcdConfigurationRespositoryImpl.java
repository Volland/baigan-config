/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.baigan.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.baigan.etcd.service.EtcdClient;
import org.zalando.baigan.model.Configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

/**
 * @author mchand
 */

@Component
public class EtcdConfigurationRespositoryImpl
        extends AbstractConfigurationRespository {

    private Logger LOG = LoggerFactory
            .getLogger(EtcdConfigurationRespositoryImpl.class);

    private EtcdClient etcdClient;

    private final String ETCD_URL_ENV_NAME = "ETCD_URL";

    private final String CONFIG_PATH_PREFIX = "/v2/keys/";



    @VisibleForTesting
    public EtcdConfigurationRespositoryImpl(final EtcdClient etcdClient) {
        checkArgument(etcdClient != null);
        this.etcdClient = etcdClient;

    }

    public EtcdConfigurationRespositoryImpl() {
        etcdClient = new EtcdClient(getUrl());
    }

    private String getUrl() {
        String systemEtcdUrl = System.getenv(ETCD_URL_ENV_NAME);
        if (Strings.isNullOrEmpty(systemEtcdUrl)) {
            LOG.error("$" + ETCD_URL_ENV_NAME
                    + " is undefined. This is required in order to by the baigan configuration service.");
        }
        return systemEtcdUrl;
    }

    public void put(final String key, final String value) {
        throw new UnsupportedOperationException(
                "The put operation is not yet supported.");
    }

    @Nonnull
    public Optional<Configuration<?>> getConfig(@Nonnull final String key) {
        try {
            checkArgument(!Strings.isNullOrEmpty(key),
                    "Attempt to get configuration for an empty key !");
            final Optional<String> optionalConfig = etcdClient
                    .get(CONFIG_PATH_PREFIX + key);

            if (optionalConfig.isPresent()) {
                return Optional.of(objectMapper.readValue(optionalConfig.get(),
                        Configuration.class));
            }

        } catch (IOException e) {
            LOG.warn("Error while loading configuration for key: " + key, e);
        }
        return Optional.absent();
    }

}
