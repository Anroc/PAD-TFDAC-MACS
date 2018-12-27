# TF-DAC-MACS demo

The full demo can be found [here](./tfdacmacs/basics/src/test/java/de/tuberlin/tfdacmacs/basics/crypto/pairing/TFDACMACSDemo.java).

## Initialize the dependencies

To initialize the dependencies we need to setup the ABE en- and decryptor. This clases implement the code for TF-DAC-MACS encryption and
decryption. To encrypt an arbitrary string message using ABE we first habe to generate a encryption key using the ABEEncryptor and then using this key to encrypt the message using AES. For that the AES en- and decryptor are used. 

The classes `StringSymmetricCryptEngine` and `HashGenerator` are just interface classes to bouncy castels AES encryption and SHA-256 hashes. Further, the `HashGenerator` provides a method to hash an element into the field *G1*. 

```java
	// basic dependencies
    private final StringSymmetricCryptEngine symmetricCryptEngine = new StringSymmetricCryptEngine();
    private final HashGenerator hashGenerator = new HashGenerator();

    // dependencies of pairing crypt engine
    private final AESEncryptor aesEncryptor = new AESEncryptor(hashGenerator, symmetricCryptEngine);
    private final AESDecryptor aesDecryptor = new AESDecryptor(hashGenerator, symmetricCryptEngine);
    private final ABEEncryptor abeEncryptor = new ABEEncryptor();
    private final ABEDecryptor abeDecryptor = new ABEDecryptor(hashGenerator);
```

## Initialize the PairingCryptEngine

```java
    private final PairingCryptEngine pairingCryptEngine = new PairingCryptEngine(
            aesEncryptor,
            aesDecryptor,
            abeEncryptor,
            abeDecryptor
    );
```

All dependencies can also be autowired using the spring context. 

## Global Setup

We frist need to initialize the pairing and calcuate the public paramter. 

```java
    PairingGenerator pairingGenerator = new PairingGenerator();
    PairingParameters pairingParameters = pairingGenerator.generateNewTypeACurveParameter();
	Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
	// this object saves the pairing field, curve paramter and the generator object g
    GlobalPublicParameter gpp = new GlobalPublicParameter(
            pairing, pairingParameters, pairing.getG1().newRandomElement().getImmutable(), null);
```

## Authority Setup and register an Attribute

To setup a new authority we need to generate a new public/private key pair for this authority. 

```java
    AuthorityKeyGenerator authorityKeyGenerator = new AuthorityKeyGenerator();
    AuthorityKey authorityKey = authorityKeyGenerator.generate(gpp);
```

In the next setup we register a new attribute with the identifier `aa.tu-berlin.de.role:Student`.

```java
    AttributeValueKeyGenerator attributeValueKeyGenerator = new AttributeValueKeyGenerator(hashGenerator);
    AttributeValuekey attributeValuekey = attributeValueKeyGenerator.generate(gpp, "aa.tu-berlin.de.role:Student");
```

## Register User

In the next setup we register a new user. This user will be registered in the TU-Berlin domain and get the newly
created attribute assigned. 

```java
	UserAttributeValueKey userAttributeValueKey = attributeValueKeyGenerator.generateUserKey(gpp, "genesisUser@tu-berlin.de", authorityKey.getPrivateKey(), attributeValueKey.getPrivateKey());
```

## Encrypt

To encrypt for a user we first ned to create a policy for this cipher text. 

```java
    Set<AccessPolicyElement> policy = new HashSet<>();
    policy.add(new AccessPolicyElement(authorityKey.getPublicKey(), attributeValueKey.getPublicKey(), "aa.tu-berlin.de.role:Student"));
    AndAccessPolicy andAccessPolicy = new AndAccessPolicy(policy);
```

Lets choose a plain text that we want to encrypt: "No, Eve please :(". 

```java
	byte[] message = "No, Eve please :(".getBytes();
    CipherText cipherText = pairingCryptEngine.encrypt(message, andAccessPolicy, gpp, null);
```

The `null` element here referres to the `DataOwner` object which we are currently not using. If we would do so we would also secure the cipher text with a two-factor authentication key. 

The cipher text object now contains all needed cipher text components and also the encyrpted message.

## Decrypt

To decrypt the encrypted message we now use the `userAttributeValueSecretKey`. 

```java
    Set<UserAttributeSecretComponents> userAttributeKeys = new HashSet<>();
	userAttributeKeys.add(new UserAttributeSecretComponents(userAttributeValueKey, attributeValueKey.getPublicKey(), aid));

	byte[] recoveredMessage = pairingCryptEngine.decrypt(cipherText, gpp, uid, userAttributeKeys, null);
```

Finally we can convert the `byte[]` back to a `String` with `new String(recoveredMessage)`. 







