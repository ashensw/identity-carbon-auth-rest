package org.wso2.carbon.identity.auth.service.util;


import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.auth.service.module.ResourceConfig;
import org.wso2.carbon.identity.auth.service.module.ResourceConfigKey;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AuthConfigurationUtil {

    private static AuthConfigurationUtil authConfigurationUtil = new AuthConfigurationUtil();

    private Map<ResourceConfigKey, ResourceConfig> resourceConfigMap = new HashMap<>();
    private Map<String, String> applicationConfigMap = new HashMap<>();


    private AuthConfigurationUtil() {
    }

    public static AuthConfigurationUtil getInstance() {
        return AuthConfigurationUtil.authConfigurationUtil;
    }

    public ResourceConfig getSecuredConfig(ResourceConfigKey resourceConfigKey) {
        ResourceConfig resourceConfig = null;
        for ( Map.Entry<ResourceConfigKey, ResourceConfig> entry : resourceConfigMap.entrySet() ) {
            if ( entry.getKey().equals(resourceConfigKey) ) {
                resourceConfig = entry.getValue();
                break;
            }
        }
        return resourceConfig;
    }

    /**
     * Build rest api resource control config.
     */
    public void buildResourceAccessControlData() {

        OMElement resourceAccessControl = IdentityConfigParser.getInstance().getConfigElement(Constants
                .RESOURCE_ACCESS_CONTROL_ELE);
        if ( resourceAccessControl != null ) {

            Iterator<OMElement> resources = resourceAccessControl.getChildrenWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, Constants.RESOURCE_ELE));
            if ( resources != null ) {

                while ( resources.hasNext() ) {
                    OMElement resource = resources.next();
                    ResourceConfig resourceConfig = new ResourceConfig();
                    String httpMethod = resource.getAttributeValue(
                            new QName(Constants.RESOURCE_HTTP_METHOD_ATTR));
                    String context = resource.getAttributeValue(new QName(Constants.RESOURCE_CONTEXT_ATTR));


                    String isSecured = resource.getAttributeValue(new QName(Constants.RESOURCE_SECURED_ATTR));

                    StringBuilder permissionBuilder = new StringBuilder();
                    Iterator<OMElement> permissionsIterator = resource.getChildrenWithName(
                            new QName(Constants.RESOURCE_PERMISSION_ELE));
                    if ( permissionsIterator != null ) {
                        while ( permissionsIterator.hasNext() ) {
                            OMElement permissionElement = permissionsIterator.next();
                            String permission = permissionElement.getText();
                            if ( StringUtils.isNotEmpty(permissionBuilder.toString()) &&
                                    StringUtils.isNotEmpty(permission) ) {
                                permissionBuilder.append(",");
                            }
                            if ( StringUtils.isNotEmpty(permission) ) {
                                permissionBuilder.append(permission);
                            }
                        }
                    }

                    resourceConfig.setContext(context);
                    resourceConfig.setHttpMethod(httpMethod);
                    if ( StringUtils.isNotEmpty(isSecured) && (Boolean.TRUE.toString().equals(isSecured) ||
                            Boolean.FALSE.toString().equals(isSecured)) ) {
                        resourceConfig.setIsSecured(Boolean.parseBoolean(isSecured));
                    }
                    resourceConfig.setPermissions(permissionBuilder.toString());
                    resourceConfigMap.put(new ResourceConfigKey(context, httpMethod), resourceConfig);
                }
            }
        }
    }


    /**
     * Build rest api resource control config.
     */
    public void buildClientAuthenticationHandlerControlData() {

        OMElement resourceAccessControl = IdentityConfigParser.getInstance().getConfigElement(Constants
                .CLIENT_APP_AUTHENTICATION_ELE);
        if ( resourceAccessControl != null ) {

            Iterator<OMElement> applications = resourceAccessControl.getChildrenWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, Constants.APPLICATION_ELE));
            if ( applications != null ) {
                while ( applications.hasNext() ) {
                    OMElement resource = applications.next();
                    String appName = resource.getAttributeValue(new QName(Constants.APPLICATION_NAME_ATTR));
                    String hash = resource.getAttributeValue(new QName(Constants.APPLICATION_HASH_ATTR));
                    applicationConfigMap.put(appName, hash);
                }
            }
        }
    }

    public String getClientAuthenticationHash(String appName) {
        return applicationConfigMap.get(appName);
    }
}
