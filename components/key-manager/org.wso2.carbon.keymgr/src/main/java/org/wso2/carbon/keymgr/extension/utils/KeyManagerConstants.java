/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.keymgr.extension.utils;

public class KeyManagerConstants {

    public static final String JWT = "JWT";
    public static final String OAUTH_CONFIGS = "OAuthConfigurations.";


    //API caching related constants
    public static final String API_MANAGER_CACHE_MANAGER = "API_MANAGER_CACHE";

    public static final String API_PUBLISHER_ADMIN_PERMISSION_CACHE = "apimAdminPermissionCache";
    public static final String API_USER_ROLE_CACHE = "appPublisherUserRoleCache";
    public static final String RESOURCE_CACHE_NAME = "resourceCache";
    public static final String CACHE_INVALIDATION_STREAM_ID = "org.wso2.apimgt.cache.invalidation.stream:1.0.0";
    public static final String CACHE_INVALIDATION_EVENT_PUBLISHER = "cacheInvalidationEventPublisher";
    public static final String GATEWAY_KEY_CACHE_NAME = "gatewayKeyCache";
    public static final String GATEWAY_USERNAME_CACHE_NAME = "gatewayUsernameCache";
    public static final String CACHE_CONFIGS = "CacheConfigurations.";
    public static final String PUBLISHER_ROLE_CACHE_ENABLED = CACHE_CONFIGS + "EnablePublisherRoleCache";

    public static class JwtTokenConstants {
        public static final String TOKEN_TYPE = "typ";
    }


    //Other Constants
    public static final String TOKEN_REVOCATION_EVENT_PUBLISHER = "tokenRevocationPublisher";
    public static final String BLOCKING_EVENT_TYPE = "wso2event";

    public static final String EMAIL_DOMAIN_SEPARATOR = "@";
    public static final String DOT = ".";

    //Configuration Constants
    public static final String EVENT_MANAGER = "EventManager";
    public static final String DATA_PUBLISHER_CONFIGURAION_TYPE = "Type";
    public static final String DATA_PUBLISHER_CONFIGURAION_REVEIVER_URL_GROUP = "ReceiverUrlGroup";
    public static final String DATA_PUBLISHER_CONFIGURAION_AUTH_URL_GROUP = "AuthUrlGroup";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String EM_ENABLED = "EnableEM";
    public static final String ADP_RECEIVER_URL = "receiverURL";
    public static final String ADP_AUTHENTICATOR_URL = "authenticatorURL";
    public static final String ADP_USERNAME = "username";
    public static final String ADP_PASSWORD = "password";
    public static final String ADP_PROTOCOL = "protocol";
    public static final String ADP_PUBLISHING_MODE = "publishingMode";
    public static final String ADP_PUBLISHING_TIME_OUT = "publishTimeout";
    public static final String ADP_NON_BLOCKING = "non-blocking";


}
