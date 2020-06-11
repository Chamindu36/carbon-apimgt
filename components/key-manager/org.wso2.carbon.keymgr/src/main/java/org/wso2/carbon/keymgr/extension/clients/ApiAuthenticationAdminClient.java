/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.keymgr.extension.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.wso2.carbon.keymgr.extension.utils.KeyManagerApiUtils;
import org.wso2.carbon.keymgr.extension.utils.KeyManagerConstants;

import java.util.Arrays;
import java.util.Set;

/**
 * A service client implementation for the APIAuthenticationService (an admin service offered
 * by the API gateway).
 */
public class ApiAuthenticationAdminClient {

    private static final Log log = LogFactory.getLog(ApiAuthenticationAdminClient.class);

    /**
     * Removes the active tokens that are cached on the API Gateway
     *
     * @param activeTokens - The active access tokens to be removed from the gateway cache.
     */
    public void invalidateCachedTokens(Set<String> activeTokens) {

        JSONArray tokenArray = new JSONArray();
        tokenArray.addAll(activeTokens);
        Object[] event = new Object[]{KeyManagerConstants.GATEWAY_KEY_CACHE_NAME, tokenArray.toJSONString()};
        KeyManagerApiUtils.publishEventToStream(KeyManagerConstants.CACHE_INVALIDATION_STREAM_ID,
                KeyManagerConstants.CACHE_INVALIDATION_EVENT_PUBLISHER, event);
    }

    /**
     * Removes a given username that is cached on the API Gateway
     *
     * @param username - The username to be removed from the gateway cache.
     */
    public void invalidateCachedUsername(String username) {

        invalidateCachedUsernames(new String[]{username});
    }

    /**
     * Removes given usernames that is cached on the API Gateway
     *
     * @param username_list - The list of usernames to be removed from the gateway cache.
     */
    public void invalidateCachedUsernames(String[] username_list) {

        JSONArray userArray = new JSONArray();
        userArray.addAll(Arrays.asList(username_list));
        Object[] event = new Object[]{KeyManagerConstants.GATEWAY_USERNAME_CACHE_NAME, userArray.toJSONString()};
        KeyManagerApiUtils.publishEventToStream(KeyManagerConstants.CACHE_INVALIDATION_STREAM_ID,
                KeyManagerConstants.CACHE_INVALIDATION_EVENT_PUBLISHER, event);
    }

}
