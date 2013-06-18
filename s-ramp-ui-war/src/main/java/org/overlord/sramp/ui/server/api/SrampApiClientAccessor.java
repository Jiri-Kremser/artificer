/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.ui.server.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.auth.AuthenticationProvider;
import org.overlord.sramp.ui.server.SrampUIConfig;

/**
 * The class used whenever an Atom API request for data needs to be made.
 *
 * @author eric.wittmann@redhat.com
 */
@Singleton
public class SrampApiClientAccessor {

    private transient SrampAtomApiClient client;

	/**
	 * C'tor.
	 */
    @Inject
	public SrampApiClientAccessor(SrampUIConfig config) {
		String endpoint = (String) config.getConfig().getProperty(SrampUIConfig.SRAMP_API_ENDPOINT);
        boolean validating = "true".equals(config.getConfig().getProperty(SrampUIConfig.SRAMP_API_VALIDATING));
        AuthenticationProvider authProvider = null;
        String authProviderClass = (String) config.getConfig().getProperty(SrampUIConfig.SRAMP_API_AUTH_PROVIDER);
        try {
            if (authProviderClass != null && authProviderClass.trim().length() > 0) {
                Class<?> c = Class.forName(authProviderClass);
                authProvider = (AuthenticationProvider) c.newInstance();
            }
            client = new SrampAtomApiClient(endpoint, authProvider, validating);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @return the atom api client
	 */
	public SrampAtomApiClient getClient() {
	    return client;
	}

}
