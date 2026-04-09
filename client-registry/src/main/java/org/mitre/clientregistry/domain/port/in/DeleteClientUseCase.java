package org.mitre.clientregistry.domain.port.in;

public interface DeleteClientUseCase {

    void delete(String clientId, String traceId);
}
