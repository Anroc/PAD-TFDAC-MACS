package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.gpp.factory.GPPDTOTestFactory;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import de.tuberlin.tfdacmacs.crypto.GPPTestFactory;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ClientApplication.class)
@ActiveProfiles("test")
public abstract class CommandTestSuite {

    @MockBean
    protected CaClient caClient;

    @Autowired
    protected GPPDTOTestFactory gppdtoTestFactory;
    @Autowired
    private PairingGenerator pairingGenerator;
    @Autowired
    private StringAsymmetricCryptEngine cryptEngine;

    private GPPTestFactory gppTestFactory;

    @PostConstruct
    public void init() {
        gppTestFactory= new GPPTestFactory(pairingGenerator, cryptEngine);
    }

    @Before
    public void initMocks() {
        GlobalPublicParameterDTO globalPublicParameterDTO = gppdtoTestFactory.create(gppTestFactory.create());
        doReturn(globalPublicParameterDTO).when(caClient).getGPP();
    }

    @After
    public void cleanUp() {

    }
}
