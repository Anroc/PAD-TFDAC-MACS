package de.tuberlin.tfdacmacs.attributeauthority.gpp;

import de.tuberlin.tfdacmacs.attributeauthority.gpp.client.GPPClient;
import de.tuberlin.tfdacmacs.basics.gpp.data.GlobalPublicParameter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Data
@Service
@RequiredArgsConstructor
public class GlobalPublicParameterService {

    private GlobalPublicParameter globalPublicParameter;

    private final GPPClient gppClient;

    public GlobalPublicParameter createOrRetrieveGPP() {
        if (globalPublicParameter == null) {
            this.globalPublicParameter = retrieveGPP();
        }
        return this.getGlobalPublicParameter();
    }

    private GlobalPublicParameter retrieveGPP() {
        return gppClient.getGPP();
    }
}
