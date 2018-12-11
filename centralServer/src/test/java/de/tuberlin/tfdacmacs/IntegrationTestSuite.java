package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.centralserver.key.db.KeyDB;
import de.tuberlin.tfdacmacs.centralserver.user.db.UserDB;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CentralServerApplication.class,
        webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestSuite {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected KeyDB keyDB;
    @Autowired
    protected UserDB userDB;

    @Autowired
    protected StringAsymmetricCryptEngine cryptEngine;
}
