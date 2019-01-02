package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeController;
import de.tuberlin.tfdacmacs.attributeauthority.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.db.UserDB;
import de.tuberlin.tfdacmacs.attributes.factory.AttributeTestFactory;
import de.tuberlin.tfdacmacs.gpp.factory.BasicsGPPTestFactory;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.lib.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.lib.gpp.events.GlobalPublicParameterChangedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AttributeAuthorityApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSuite {

    @Autowired
    protected TestRestTemplate restTemplate;

    // Mock beans
    @MockBean
    protected CAClient gppFeignClient;

    // Controller
    @Autowired
    protected AttributeController attributeController;

    // Utils and Services
    @Autowired
    protected PairingGenerator pairingGenerator;
    @Autowired
    protected AttributeValueKeyGenerator attributeValueKeyGenerator;

    // DBs
    @Autowired
    protected AttributeDB attributeDB;
    @Autowired
    protected UserDB userDB;

    // Factories
    @Autowired
    protected BasicsGPPTestFactory gppTestFactory;
    @Autowired
    protected AttributeTestFactory attributeTestFactory;

    // Test usages
    @Autowired
    protected ApplicationEventPublisher publisher;

    @Before
    public void mockGPPRequest() {
        GlobalPublicParameterDTO globalPublicParameterDTO = gppTestFactory.createDTO();
        doReturn(globalPublicParameterDTO).when(gppFeignClient).getGPP();
        publisher.publishEvent(
                new GlobalPublicParameterChangedEvent(globalPublicParameterDTO.toGlobalPublicParameter(pairingGenerator))
        );
    }

    @After
    public void cleanUp() {
        attributeDB.drop();
        userDB.drop();
    }
}
