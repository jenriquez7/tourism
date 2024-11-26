package com.tourism.util.helpers;

public class AuthenticationHelper {

    public static final String ADMIN_ROLE = "hasRole(T(com.tourism.model.Role).ADMIN)";
    public static final String TOURIST_ROLE = "hasRole(T(com.tourism.model.Role).TOURIST)";
    public static final String LODGING_OWNER_ROLE = "hasRole(T(com.tourism.model.Role).LODGING_OWNER)";
    public static final String EVERY_ROLE = "isAuthenticated()";
}
