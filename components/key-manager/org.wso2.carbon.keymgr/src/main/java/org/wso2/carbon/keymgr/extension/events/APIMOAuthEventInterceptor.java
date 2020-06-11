/*
 *Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.keymgr.extension.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.events.RevocationRequestPublisher;
import org.wso2.carbon.identity.oauth.event.AbstractOAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.ResponseHeader;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.RefreshTokenValidationDataDO;
import org.wso2.carbon.keymgr.extension.utils.KeyManagerApiUtils;
import org.wso2.carbon.keymgr.extension.utils.KeyManagerConstants;

import java.util.Map;

/**
 * This class provides an implementation of OAuthEventInterceptor interface in which
 * onPostTokenRevocationByClient method is overridden to handle token revocation feature logic
 */
public class APIMOAuthEventInterceptor extends AbstractOAuthEventInterceptor {

    private static final Log log = LogFactory.getLog(APIMOAuthEventInterceptor.class);
    private static final String REVOKED_ACCESS_TOKEN = "RevokedAccessToken";
    private final RevocationRequestPublisher revocationRequestPublisher;

    public APIMOAuthEventInterceptor() {
        revocationRequestPublisher = RevocationRequestPublisher.getInstance();
    }

    /**
     * Overridden method to handle the post processing of token revocation
     * Called after revoking a token by oauth client
     *
     * @param revokeRequestDTO  requested revoke request object
     * @param revokeResponseDTO requested revoke response object
     * @param accessTokenDO     requested access token object
     * @param refreshTokenDO    requested refresh token object
     * @param params            requested params Map<String,Object>
     */
    @Override
    public void onPostTokenRevocationByClient(OAuthRevocationRequestDTO revokeRequestDTO,
                                              OAuthRevocationResponseDTO revokeResponseDTO, AccessTokenDO accessTokenDO,
                                              RefreshTokenValidationDataDO refreshTokenDO, Map<String, Object> params) {

        // If the response header contains RevokedAccessToken header, it implies the token revocation was a success.
        ResponseHeader[] responseHeaders = revokeResponseDTO.getResponseHeaders();
        boolean isRevokedAccessTokenHeaderExists = false;
        if (responseHeaders != null) {
            for (ResponseHeader responseHeader : responseHeaders) {
                if (responseHeader.getKey().equals(REVOKED_ACCESS_TOKEN) && responseHeader.getValue() != null) {
                    isRevokedAccessTokenHeaderExists = true; // indicates a successful revocation
                    break;
                }
            }
        }

        if (isRevokedAccessTokenHeaderExists) {
            String revokedToken = revokeRequestDTO.getToken();
            Long expiryTime = 0L;
            if (revokedToken.contains(KeyManagerConstants.DOT) && KeyManagerApiUtils.isValidJWT(revokedToken)) {
                expiryTime = KeyManagerApiUtils.getExpiryifJWT(revokedToken);
            }
            revocationRequestPublisher.publishRevocationEvents(revokedToken, expiryTime, null);

        }

    }

    /**
     * Overridden method to handle the post processing of token revocation
     *
     * @param revokeRequestDTO requested revoke request object
     * @param revokeRespDTO    requested revoke request object
     * @param accessTokenDO    requested Access token object
     * @param params           requested params Map<String,Object>
     */
    @Override
    public void onPostTokenRevocationByResourceOwner(
            org.wso2.carbon.identity.oauth.dto.OAuthRevocationRequestDTO revokeRequestDTO,
            org.wso2.carbon.identity.oauth.dto.OAuthRevocationResponseDTO revokeRespDTO, AccessTokenDO accessTokenDO,
            Map<String, Object> params) {

        if (accessTokenDO != null) { // if accessTokenDO is not null, it implies the revocation was a success
            String revokedToken = accessTokenDO.getAccessToken();
            Long expiryTime = 0L;
            if (revokedToken.contains(KeyManagerConstants.DOT) && KeyManagerApiUtils.isValidJWT(revokedToken)) {
                expiryTime = KeyManagerApiUtils.getExpiryifJWT(revokedToken);
            }
            revocationRequestPublisher.publishRevocationEvents(revokedToken, expiryTime, null);

        }
    }

}
