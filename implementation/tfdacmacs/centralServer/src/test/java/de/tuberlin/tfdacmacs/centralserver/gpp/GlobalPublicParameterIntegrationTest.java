package de.tuberlin.tfdacmacs.centralserver.gpp;

import com.sun.jersey.core.util.Base64;
import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.CurveParameterDTO;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GeneratorDTO;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalPublicParameterIntegrationTest extends IntegrationTestSuite {

    @Test
    public void getGPP() {
        GlobalPublicParameterDTO gpp = restTemplate.getForObject("/gpp", GlobalPublicParameterDTO.class);

        assertThat(gpp.getCurveParameter()).isNotNull();
        CurveParameterDTO curveParameter = gpp.getCurveParameter();
        assertThat(curveParameter.getExp1()).isNotZero();
        assertThat(curveParameter.getExp2()).isNotZero();
        assertThat(curveParameter.getH()).isNotBlank();
        assertThat(curveParameter.getQ()).isNotBlank();
        assertThat(curveParameter.getR()).isNotBlank();
        assertThat(curveParameter.getType()).isEqualTo('a');

        assertThat(gpp.getGenerator()).isNotNull();
        GeneratorDTO generatorDTO = gpp.getGenerator();
        assertThat(generatorDTO.getG()).isNotBlank();
        byte[] g = Base64.decode(generatorDTO.getG());
        assertThat(g).isNotEmpty();

        assertThat(globalPublicParameterDB.gppExist()).isTrue();
        assertThat(globalPublicParameterDTODB.findEntity(GlobalPublicParameterDTO.ID).get()).isEqualTo(gpp);
        assertThat(gppProvider.getGlobalPublicParameter()).isNotNull();
    }
}
