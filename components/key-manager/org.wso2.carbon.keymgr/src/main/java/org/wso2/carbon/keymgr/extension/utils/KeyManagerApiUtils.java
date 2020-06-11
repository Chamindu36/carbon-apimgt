/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.keymgr.extension.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.keymgr.extension.Exception.KeyManagerException;
import org.wso2.carbon.keymgr.extension.internal.KeyManagerServiceReferenceHolder;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;

import javax.cache.Caching;
import java.io.File;
import java.util.Collections;
import java.util.Map;


public class KeyManagerApiUtils {

    private static final Log log = LogFactory.getLog(KeyManagerApiUtils.class);
    private static boolean isPublisherRoleCacheEnabled = true;

    /**
     * Get expiry time of a given jwt token. This method should be called only after validating whether the token is
     * JWT via isValidJWT method.
     *
     * @param token jwt token.
     * @return the expiry time.
     */
    public static Long getExpiryifJWT(String token) {

        String[] jwtParts = token.split("\\.");
        org.json.JSONObject jwtPayload = new org.json.JSONObject(new String(java.util.Base64.getUrlDecoder().
                decode(jwtParts[1])));
        return jwtPayload.getLong("exp"); // extract expiry time and return
    }

    /**
     * Checks whether the given token is a valid JWT by parsing header and validating the
     * header,payload,signature format
     *
     * @param token the token to be validated
     * @return true if valid JWT
     */
    public static boolean isValidJWT(String token) {

        boolean isJwtToken = false;
        try {
            org.json.JSONObject decodedHeader = new org.json.JSONObject(new String(java.util.Base64.getUrlDecoder()
                    .decode(token.split("\\.")[0])));
            // Check if the decoded header contains type as 'JWT'.
            if (KeyManagerConstants.JWT.equals(decodedHeader.getString(KeyManagerConstants.JwtTokenConstants.TOKEN_TYPE))
                    && (StringUtils.countMatches(token, KeyManagerConstants.DOT) == 2)) {
                isJwtToken = true;
            } else {
                log.debug("Not a valid JWT token. " + getMaskedToken(token));
            }
        } catch (JSONException | IllegalArgumentException e) {
            isJwtToken = false;
            log.debug("Not a valid JWT token. " + getMaskedToken(token), e);
        }
        return isJwtToken;
    }

    /**
     * Returns a masked token for a given token.
     *
     * @param token token to be masked
     * @return masked token.
     */
    public static String getMaskedToken(String token) {

        if (token.length() >= 10) {
            return "XXXXX" + token.substring(token.length() - 10);
        } else {
            return "XXXXX" + token.substring(token.length() / 2);
        }
    }


    public static void publishEvent(String eventName, Map dynamicProperties, Event event) {

        boolean tenantFlowStarted = false;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            tenantFlowStarted = true;
            KeyManagerServiceReferenceHolder.getInstance().getOutputEventAdapterService()
                    .publish(eventName, dynamicProperties, event);
        } finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    /**
     * To clear the publisherRoleCache for certain users.
     *
     * @param userName Names of the user.
     */
    public static void clearRoleCache(String userName) {
        if (isPublisherRoleCacheEnabled) {
            Caching.getCacheManager(KeyManagerConstants.API_MANAGER_CACHE_MANAGER).getCache(
                    KeyManagerConstants.API_PUBLISHER_ADMIN_PERMISSION_CACHE).remove(userName);
            Caching.getCacheManager(KeyManagerConstants.API_MANAGER_CACHE_MANAGER).getCache(
                    KeyManagerConstants.API_USER_ROLE_CACHE).remove(userName);
        }
    }


    public static void publishEventToStream(String streamId, String eventPublisherName, Object[] eventData) {

        boolean tenantFlowStarted = false;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            tenantFlowStarted = true;
            OutputEventAdapterService eventAdapterService =
                    KeyManagerServiceReferenceHolder.getInstance().getOutputEventAdapterService();
            Event blockingMessage = new Event(streamId, System.currentTimeMillis(),
                    null, null, eventData);
            eventAdapterService.publish(eventPublisherName, Collections.EMPTY_MAP, blockingMessage);
        } finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    /**
     * Resolves system properties and replaces in given in text
     *
     * @param text
     * @return System properties resolved text
     */
    public static String replaceSystemProperty(String text) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${")
                && (indexOfStartingChars = text.indexOf("${")) != -1
                && (indexOfClosingBrace = text.indexOf('}')) != -1) { // Is a
            // property
            // used?
            String sysProp = text.substring(indexOfStartingChars + 2,
                    indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);

            if (propValue == null) {
                if ("carbon.context".equals(sysProp)) {
                    propValue = KeyManagerServiceReferenceHolder.getInstance().getContextService().getServerConfigContext().getContextRoot();
                } else if ("admin.username".equals(sysProp) || "admin.password".equals(sysProp)) {
                    try {
                        RealmConfiguration realmConfig =
                                new RealmConfigXMLProcessor().buildRealmConfigurationFromFile();
                        if ("admin.username".equals(sysProp)) {
                            propValue = realmConfig.getAdminUserName();
                        } else {
                            propValue = realmConfig.getAdminPassword();
                        }
                    } catch (UserStoreException e) {
                        // Can't throw an exception because the server is
                        // starting and can't be halted.
                        log.error("Unable to build the Realm Configuration", e);
                        return null;
                    }
                }
            }
            //Derive original text value with resolved system property value
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue
                        + text.substring(indexOfClosingBrace + 1);
            }
            if ("carbon.home".equals(sysProp) && propValue != null
                    && ".".equals(propValue)) {
                text = new File(".").getAbsolutePath() + File.separator + text;
            }
        }
        return text;
    }

}
