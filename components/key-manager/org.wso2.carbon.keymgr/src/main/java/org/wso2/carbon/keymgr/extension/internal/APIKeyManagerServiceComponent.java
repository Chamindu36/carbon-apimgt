package org.wso2.carbon.keymgr.extension.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.identity.oauth.event.OAuthEventInterceptor;
import org.wso2.carbon.keymgr.extension.Model.EventManager;
import org.wso2.carbon.keymgr.extension.events.APIMOAuthEventInterceptor;
import org.wso2.carbon.keymgr.extension.listeners.KeyManagerUserOperationListener;
import org.wso2.carbon.keymgr.extension.utils.KeyManagerExtensionConfiguration;
import org.wso2.carbon.keymgr.extension.utils.KeyManagerConstants;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class APIKeyManagerServiceComponent {

    private static Log log = LogFactory.getLog(APIKeyManagerServiceComponent.class);

    private static KeyManagerUserOperationListener listener = null;

    private ServiceRegistration serviceRegistration = null;

    /**
     * Method to activate OSGI services and load configurations.
     */
    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            //Loading the configuration
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "key-manager-extension.xml";
            KeyManagerExtensionConfiguration.load(filePath);
            listener = new KeyManagerUserOperationListener();
            serviceRegistration = ctxt.getBundleContext().registerService(UserOperationEventListener.class.getName(), listener, null);
            log.debug("Key Manager User Operation Listener is enabled.");

            // object creation for implemented OAuthEventInterceptor interface in IS
            APIMOAuthEventInterceptor interceptor = new APIMOAuthEventInterceptor();

            // registering the interceptor class to the bundle
            serviceRegistration = ctxt.getBundleContext().registerService(OAuthEventInterceptor.class.getName(), interceptor, null);
            // Creating an event adapter to receive token revocation messages
            configureTokenRevocationEventPublisher();
            configureCacheInvalidationEventPublisher();
            log.debug("Key Manager OAuth Event Interceptor is enabled.");
        } catch (Exception e) {
            log.error("Failed to initialize key management service.", e);
        }
    }

    /**
     * Method to deactivate OSGI services and load configurations.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (log.isDebugEnabled()) {
            log.info("Key Manager User Operation Listener is deactivated.");
        }
    }


    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        KeyManagerServiceReferenceHolder.getInstance().setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        KeyManagerServiceReferenceHolder.getInstance().setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is unset in the API KeyMgt bundle.");
        }
    }

    /**
     * Method to configure wso2event type event adapter to be used for token revocation.
     */
    private void configureTokenRevocationEventPublisher() {
        OutputEventAdapterConfiguration adapterConfiguration = new OutputEventAdapterConfiguration();
        adapterConfiguration.setName(KeyManagerConstants.TOKEN_REVOCATION_EVENT_PUBLISHER);
        adapterConfiguration.setType(KeyManagerConstants.BLOCKING_EVENT_TYPE);
        adapterConfiguration.setMessageFormat(KeyManagerConstants.BLOCKING_EVENT_TYPE);
        Map<String, String> adapterParametersForTokenRevocationEventPublisher = new HashMap<>();
        configureAdapterParams(adapterParametersForTokenRevocationEventPublisher);
        adapterConfiguration.setStaticProperties(adapterParametersForTokenRevocationEventPublisher);
        try {
            KeyManagerServiceReferenceHolder.getInstance().getOutputEventAdapterService().create(adapterConfiguration);
        } catch (OutputEventAdapterException e) {
            log.warn("Exception occurred while creating token revocation event adapter. Token Revocation may not " + "work properly", e);
        }
    }

    /**
     * Method to configure wso2event type event adapter to be used for token revocation.
     */
    private void configureCacheInvalidationEventPublisher() {
        OutputEventAdapterConfiguration adapterConfiguration = new OutputEventAdapterConfiguration();
        adapterConfiguration.setName(KeyManagerConstants.CACHE_INVALIDATION_EVENT_PUBLISHER);
        adapterConfiguration.setType(KeyManagerConstants.BLOCKING_EVENT_TYPE);
        adapterConfiguration.setMessageFormat(KeyManagerConstants.BLOCKING_EVENT_TYPE);
        Map<String, String> adapterParametersForCacheInvalidationPublisher = new HashMap<>();
        configureAdapterParams(adapterParametersForCacheInvalidationPublisher);
        adapterConfiguration.setStaticProperties(adapterParametersForCacheInvalidationPublisher);
        try {
            KeyManagerServiceReferenceHolder.getInstance().getOutputEventAdapterService().create(adapterConfiguration);
        } catch (OutputEventAdapterException e) {
            log.warn("Exception occurred while creating cache invalidation event adapter. Cache invalidation may not " +
                    "work properly", e);
        }

    }

    /**
     * Method to set wso2event type event adapter to be used for operations.
     */
    private Map<String, String> configureAdapterParams(Map<String, String> adapterParameters) {
        KeyManagerExtensionConfiguration configuration = KeyManagerServiceReferenceHolder.getInstance().getKeyManagerExtensionConfiguration();
        EventManager eventManager = configuration.getEventManager();
        adapterParameters.put(KeyManagerConstants.ADP_RECEIVER_URL, eventManager.getReceiverUrlGroup());
        adapterParameters.put(KeyManagerConstants.ADP_AUTHENTICATOR_URL, eventManager.getAuthUrlGroup());
        adapterParameters.put(KeyManagerConstants.ADP_USERNAME, eventManager.getUsername());
        adapterParameters.put(KeyManagerConstants.ADP_PASSWORD, eventManager.getPassword());
        adapterParameters.put(KeyManagerConstants.ADP_PROTOCOL, eventManager.getType());
        adapterParameters.put(KeyManagerConstants.ADP_PUBLISHING_MODE, KeyManagerConstants.ADP_NON_BLOCKING);
        adapterParameters.put(KeyManagerConstants.ADP_PUBLISHING_TIME_OUT, "0");
        return adapterParameters;
    }

    /**
     * Initialize the Output EventAdapter Service dependency
     *
     * @param outputEventAdapterService Output EventAdapter Service reference
     */
    @Reference(
            name = "event.output.adapter.service",
            service = org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOutputEventAdapterService")
    protected void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        KeyManagerServiceReferenceHolder.getInstance().setOutputEventAdapterService(outputEventAdapterService);
    }

    /**
     * De-reference the Output EventAdapter Service dependency.
     *
     * @param outputEventAdapterService
     */
    protected void unsetOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        KeyManagerServiceReferenceHolder.getInstance().setOutputEventAdapterService(null);
    }

    @Reference(
            name = "api.manager.config.service",
            service = KeyManagerExtensionConfiguration.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfiguration(KeyManagerExtensionConfiguration keyManagerExtensionConfiguration) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API handlers");
        }
        KeyManagerServiceReferenceHolder.getInstance().setKeyManagerExtensionConfiguration(keyManagerExtensionConfiguration);
    }

    protected void unsetAPIManagerConfiguration(KeyManagerExtensionConfiguration keyManagerExtensionConfiguration) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        KeyManagerServiceReferenceHolder.getInstance().setKeyManagerExtensionConfiguration(null);
    }

}
