package org.mitre.authserver.infrastructure.adapter.out.persistence;

import org.mitre.authserver.domain.exception.OptimisticLockingException;
import org.mitre.authserver.domain.model.DeviceCode;
import org.mitre.authserver.domain.port.out.DeviceCodeRepository;
import org.mitre.authserver.infrastructure.adapter.out.persistence.document.DeviceCodeDocument;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoDeviceCodeRepository implements DeviceCodeRepository {

    private final SpringDataDeviceCodeRepository springDataRepo;

    public MongoDeviceCodeRepository(SpringDataDeviceCodeRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<DeviceCode> findById(String id) {
        return springDataRepo.findById(id).map(DeviceCodeDocument::toDomain);
    }

    @Override
    public Optional<DeviceCode> findByDeviceCode(String deviceCode) {
        return springDataRepo.findByDeviceCode(deviceCode).map(DeviceCodeDocument::toDomain);
    }

    @Override
    public Optional<DeviceCode> findByUserCode(String userCode) {
        return springDataRepo.findByUserCode(userCode).map(DeviceCodeDocument::toDomain);
    }

    @Override
    public DeviceCode save(DeviceCode deviceCode) {
        try {
            return springDataRepo.save(DeviceCodeDocument.fromDomain(deviceCode)).toDomain();
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingException("Concurrent modification detected for device code: " + deviceCode.getId(), e);
        }
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }
}
