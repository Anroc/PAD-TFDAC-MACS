package de.tuberlin.tfdacmacs.centralserver.gpp.db;

import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.gpp.events.GlobalPublicParameterChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GlobalPublicParameterDB {

    private final GlobalPublicParameterDTODB globalPublicParameterDTODB;
    private final PairingGenerator pairingGenerator;

    @Cacheable("gpp")
    public Optional<GlobalPublicParameter> findEntity() {
        return globalPublicParameterDTODB.findEntity(GlobalPublicParameterDTO.ID).map(
                (GlobalPublicParameterDTO gpp) -> gpp.toGlobalPublicParameter(pairingGenerator));
    }

    public boolean gppExist() {
        return globalPublicParameterDTODB.exist(GlobalPublicParameterDTO.ID);
    }

    public String insert(GlobalPublicParameter globalPublicParameter) {
        GlobalPublicParameterDTO gpp = GlobalPublicParameterDTO.from(globalPublicParameter);
        gpp.registerDomainEvent(new GlobalPublicParameterChangedEvent(globalPublicParameter));
        String result = globalPublicParameterDTODB.insert(gpp);
        return result;
    }
}
