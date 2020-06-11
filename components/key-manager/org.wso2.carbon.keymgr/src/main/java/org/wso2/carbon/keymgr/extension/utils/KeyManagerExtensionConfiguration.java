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
package org.wso2.carbon.keymgr.extension.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.keymgr.extension.Exception.KeyManagerException;
import org.wso2.carbon.keymgr.extension.Model.EventManager;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class KeyManagerExtensionConfiguration {


    private static final Log log = LogFactory.getLog(KeyManagerExtensionConfiguration.class);
    private static boolean initialized;
    private static SecretResolver secretResolver;
    private static EventManager eventManager = new EventManager();
    private final Map<String, List<String>> configuration = new ConcurrentHashMap<String, List<String>>();
    private KeyManagerExtensionConfiguration keyManagerExtensionConfiguration;

    public KeyManagerExtensionConfiguration(KeyManagerExtensionConfiguration configuration) {
        this.keyManagerExtensionConfiguration = keyManagerExtensionConfiguration;
    }

    /**
     * Populate this configuration by reading an XML file at the given location. This method
     * can be executed only once on a given APIManagerConfiguration instance. Once invoked and
     * successfully populated, it will ignore all subsequent invocations.
     *
     * @param filePath Path of the XML descriptor file
     * @throws KeyManagerException If an error occurs while reading the XML descriptor
     */
    public static void load(String filePath) throws KeyManagerException {

        if (initialized) {
            return;
        }
        InputStream in = null;

        try {
            in = FileUtils.openInputStream(new File(filePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            secretResolver = SecretResolverFactory.create(builder.getDocumentElement(), true);
            readChildElements(builder.getDocumentElement(), new Stack<String>());
            initialized = true;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new KeyManagerException("I/O error while reading the API manager " +
                    "configuration: " + filePath, e);
        } catch (XMLStreamException e) {
            log.error(e.getMessage());
            throw new KeyManagerException("Error while parsing the API manager " +
                    "configuration: " + filePath, e);
        } catch (OMException e) {
            log.error(e.getMessage());
            throw new KeyManagerException("Error while parsing API Manager configuration: " + filePath, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new KeyManagerException("Unexpected error occurred while parsing configuration: " + filePath, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Read child elements of OMElements to extract their configurations
     *
     * @param nameStack    Stack<String>
     * @param serverConfig OMElement list of OMElements with configurations
     * @return EventManger eventManager instance with extracted values
     */
    private static void readChildElements(OMElement serverConfig,
                                          Stack<String> nameStack) throws KeyManagerException {

        for (Iterator childElements = serverConfig.getChildElements(); childElements
                .hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            String localName = element.getLocalName();
            if (KeyManagerConstants.EVENT_MANAGER.equals(localName)) {
                setEventManager(serverConfig);
            }
        }
    }

    /**
     * Get event manager object with given configuration values
     *
     * @return EventManger eventManager instance with extracted values
     */
    public static EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Set event manager object with given configuration values
     *
     * @param element OM Element extracted from XML reader
     */
    private static void setEventManager(OMElement element) {

        OMElement eventManagerConfigurations = element.getFirstChildWithName(new QName(KeyManagerConstants.EVENT_MANAGER));
        if (eventManagerConfigurations != null) {
            // Check advance throttling enabled
            OMElement enableEventManager = eventManagerConfigurations
                    .getFirstChildWithName(new QName(KeyManagerConstants.EM_ENABLED));
            if (enableEventManager != null) {
                eventManager.setEnabled(JavaUtils.isTrueExplicitly(enableEventManager
                        .getText()));
            }
            if (eventManager.isEnabled()) {
                // Reading TrafficManager configuration

                OMElement receiverUrlGroupElement = eventManagerConfigurations.getFirstChildWithName(new
                        QName
                        (KeyManagerConstants.DATA_PUBLISHER_CONFIGURAION_REVEIVER_URL_GROUP));
                if (receiverUrlGroupElement != null) {
                    eventManager.setReceiverUrlGroup(KeyManagerApiUtils.replaceSystemProperty(receiverUrlGroupElement
                            .getText()));
                }
                OMElement authUrlGroupElement = eventManagerConfigurations.getFirstChildWithName(new QName
                        (KeyManagerConstants.DATA_PUBLISHER_CONFIGURAION_AUTH_URL_GROUP));
                if (authUrlGroupElement != null) {
                    eventManager.setAuthUrlGroup(KeyManagerApiUtils.replaceSystemProperty(authUrlGroupElement.getText()));
                }
                OMElement dataPublisherUsernameElement = eventManagerConfigurations.getFirstChildWithName
                        (new QName(KeyManagerConstants.USERNAME));
                if (dataPublisherUsernameElement != null) {
                    eventManager.setUsername(KeyManagerApiUtils.replaceSystemProperty(dataPublisherUsernameElement.getText
                            ()));
                }
                OMElement dataPublisherTypeElement = eventManagerConfigurations.getFirstChildWithName(new
                        QName
                        (KeyManagerConstants.DATA_PUBLISHER_CONFIGURAION_TYPE));
                if (dataPublisherTypeElement != null) {
                    eventManager.setType(dataPublisherTypeElement.getText());
                }
                String dataPublisherConfigurationPassword;
                OMElement dataPublisherConfigurationPasswordOmElement = eventManagerConfigurations
                        .getFirstChildWithName(new QName(KeyManagerConstants.PASSWORD));
                dataPublisherConfigurationPassword = MiscellaneousUtil.
                        resolve(dataPublisherConfigurationPasswordOmElement, secretResolver);
                eventManager.setPassword(KeyManagerApiUtils.replaceSystemProperty(dataPublisherConfigurationPassword));
            }
        }
    }

    public String getFirstProperty(String key) {

        List<String> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

}
