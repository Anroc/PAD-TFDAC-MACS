package de.tuberlin.tfdacmacs;

import de.tuberlin.tfdacmacs.client.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.client.certificate.db.CertificateDB;
import de.tuberlin.tfdacmacs.client.config.ClientConfig;
import de.tuberlin.tfdacmacs.client.config.StandardStreams;
import de.tuberlin.tfdacmacs.client.db.CRUDOperations;
import de.tuberlin.tfdacmacs.client.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.client.gpp.factory.GPPDTOTestFactory;
import de.tuberlin.tfdacmacs.client.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.client.keypair.db.KeyPairDB;
import de.tuberlin.tfdacmacs.client.register.Session;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.client.rest.CSPClient;
import de.tuberlin.tfdacmacs.client.rest.template.RestTemplateFactory;
import de.tuberlin.tfdacmacs.crypto.GPPTestFactory;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.factory.CertificateTestFactory;
import lombok.Getter;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.ScriptShellApplicationRunner;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        ScriptShellApplicationRunner.SPRING_SHELL_SCRIPT_ENABLED + "=false",
        InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED + "=false",

})
@ActiveProfiles("test")
public abstract class CommandTestSuite {

    @MockBean
    protected CAClient caClient;
    @MockBean
    protected CSPClient cspClient;

    @SpyBean
    protected RestTemplateFactory restTemplateFactory;

    @SpyBean
    protected AttributeDB attributeDB;
    @Autowired
    protected CertificateDB certificateDB;
    @Autowired
    protected KeyPairDB keyPairDB;

    @Autowired
    protected GPPDTOTestFactory gppdtoTestFactory;
    @Autowired
    private PairingGenerator pairingGenerator;
    @Autowired
    protected StringAsymmetricCryptEngine cryptEngine;
    @Autowired
    protected KeyPairService keyPairService;
    @Autowired
    protected ClientConfig clientConfig;

    @Autowired
    protected CertificateTestFactory certificateTestFactory;
    @Autowired
    protected CertificateUtils certificateUtils;

    @Autowired
    protected Shell shell;

    protected GPPTestFactory gppTestFactory;
    @Autowired
    protected TestEventListener testEventListener;
    @Autowired
    protected ApplicationContext applicationContext;
    @MockBean
    protected StandardStreams standardStreams;
    @SpyBean
    protected Session session;

    @Getter
    private List<String> outContent = new ArrayList<>();
    @Getter
    private List<String> errorContent = new ArrayList<>();

    @PostConstruct
    public void init() {
        gppTestFactory= new GPPTestFactory(pairingGenerator, cryptEngine);
    }

    @Before
    public void initMocks() {
        GlobalPublicParameterDTO globalPublicParameterDTO = gppdtoTestFactory.create(gppTestFactory.create());
        doReturn(globalPublicParameterDTO).when(caClient).getGPP();

        outContent.clear();
        errorContent.clear();

        doAnswer(args -> {
                outContent.add(args.getArgument(0));
                return null;
            }
        ).when(standardStreams).out(anyString());

        doAnswer(args -> {
                    errorContent.add(args.getArgument(0));
                    return null;
                }
        ).when(standardStreams).error(anyString());
    }

    @After
    public void cleanUp() throws IOException {
        applicationContext.getBeansOfType(CRUDOperations.class).values()
                .forEach(CRUDOperations::drop);

        File p12Dir = Paths.get(clientConfig.getP12Certificate().getLocation()).toFile();
        if(p12Dir.exists()) {
            FileUtils.cleanDirectory(p12Dir);
        }
    }

    public void evaluate(String command) {
        Object ret = shell.evaluate(() -> command);

        if(ret instanceof Exception) {
            throw new RuntimeException((Exception) ret);
        }
    }
}
