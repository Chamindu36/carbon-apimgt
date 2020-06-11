/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.keymgr.extension.Exception;


/**
 * This is the custom exception class for Key Manager Component.
 */
public class KeyManagerException extends Exception {
    private KeyManagerErrorHandler errorHandler;

    /**
     * Get error handler object.
     *
     * @return ErrorHandler
     */

    public KeyManagerException(String msg) {
        super(msg);
        this.errorHandler = ExceptionCodesKeyManager.INTERNAL_ERROR;
    }

    public KeyManagerException(String msg, Throwable e) {
        super(msg, e);
        this.errorHandler = ExceptionCodesKeyManager.INTERNAL_ERROR;
    }

    public KeyManagerException(Throwable throwable) {
        super(throwable);
        this.errorHandler = ExceptionCodesKeyManager.INTERNAL_ERROR;
    }
}
