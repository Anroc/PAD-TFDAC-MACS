package de.tuberlin.tfdacmacs.attributeauthority.init.gpp.client;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
@RequiredArgsConstructor
public class GPPClient {

    private final GPPFeignClient gppFeignClient;
    private final PairingGenerator pairingGenerator;

    public GlobalPublicParameter getGPP() {
        return gppFeignClient.getGPP().toGlobalPublicParameter();
    }
}
