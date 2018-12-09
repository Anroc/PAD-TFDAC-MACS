package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.attributeauthority.attributes.AttributeController;
import de.tuberlin.tfdacmacs.attributeauthority.attributes.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.gpp.client.GPPFeignClient;
import de.tuberlin.tfdacmacs.basics.crypto.PairingGenerator;
import de.tuberlin.tfdacmacs.basics.factory.AttributeTestFactory;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AttributeAuthorityApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSuite {

    @Autowired
    protected TestRestTemplate restTemplate;

    // Mock beans
    @MockBean
    protected GPPFeignClient gppFeignClient;

    // Controller
    @Autowired
    protected AttributeController attributeController;

    // Utils and Services
    @Autowired
    protected PairingGenerator pairingGenerator;

    // DBs
    @Autowired
    protected AttributeDB attributeDB;

    // Factories
    @Autowired
    protected GPPTestFactory gppTestFactory;
    @Autowired
    protected AttributeTestFactory attributeTestFactory;

    @After
    public void cleanUp() {
        attributeDB.drop();
    }
}
