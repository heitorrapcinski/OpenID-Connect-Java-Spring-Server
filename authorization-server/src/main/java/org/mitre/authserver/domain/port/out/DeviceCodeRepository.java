package org.mitre.authserver.domain.port.out;

import org.mitre.authserver.domain.model.DeviceCode;

import java.util.Optional;

public interface DeviceCodeRepository {
    Optional<DeviceCode> findById(String id);
    Optional<DeviceCode> findByDeviceCode(String deviceCode);
    Optional<DeviceCode> findByUserCode(String userCode);
    DeviceCode save(DeviceCode deviceCode);
    void deleteById(String id);
}
