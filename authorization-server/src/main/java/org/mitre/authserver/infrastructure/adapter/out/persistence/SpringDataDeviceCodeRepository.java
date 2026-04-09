package org.mitre.authserver.infrastructure.adapter.out.persistence;

import org.mitre.authserver.infrastructure.adapter.out.persistence.document.DeviceCodeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataDeviceCodeRepository extends MongoRepository<DeviceCodeDocument, String> {
    Optional<DeviceCodeDocument> findByDeviceCode(String deviceCode);
    Optional<DeviceCodeDocument> findByUserCode(String userCode);
}
