package de.tuberlin.tfdacmacs.centralserver.gpp.db;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
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
        return globalPublicParameterDTODB.insert(GlobalPublicParameterDTO.from(globalPublicParameter));
    }
}
