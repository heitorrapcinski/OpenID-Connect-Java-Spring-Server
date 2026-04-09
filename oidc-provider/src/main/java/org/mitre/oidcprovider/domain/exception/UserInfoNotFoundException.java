package org.mitre.oidcprovider.domain.exception;

public class UserInfoNotFoundException extends DomainException {
    public UserInfoNotFoundException(String sub) {
        super("UserInfo not found for subject: " + sub);
    }
}
