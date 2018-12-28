package de.tuberlin.tfdacmacs.centralserver.gpp.db;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.basics.gpp.events.GlobalPublicParameterChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GlobalPublicParameterDB {

    private final GlobalPublicParameterDTODB globalPublicParameterDTODB;
    private final PairingGenerator pairingGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Cacheable("gpp")
    public Optional<GlobalPublicParameter> findEntity() {
        return globalPublicParameterDTODB.findEntity(GlobalPublicParameterDTO.ID).map(
                (GlobalPublicParameterDTO gpp) -> gpp.toGlobalPublicParameter(pairingGenerator));
    }

    public boolean gppExist() {
        return globalPublicParameterDTODB.exist(GlobalPublicParameterDTO.ID);
    }

    public String insert(GlobalPublicParameter globalPublicParameter) {
        String result = globalPublicParameterDTODB.insert(GlobalPublicParameterDTO.from(globalPublicParameter));
        applicationEventPublisher.publishEvent(new GlobalPublicParameterChangedEvent(globalPublicParameter));
        return result;
    }
}
