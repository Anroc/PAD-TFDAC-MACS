# TF-DAC-MACS demo

The full demo can be found [here](./tfdacmacs/crypto/src/test/java/de/tuberlin/tfdacmacs/crypto/pairing/TFDACMACSDemo.java).

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
    AttributeValuekey attributeValuekey = attributeValueKeyGenerator.generateNew(gpp, "aa.tu-berlin.de.role:Student");
```

## Register User

In the next setup we register a new user. This user will be registered in the TU-Berlin domain and get the newly
created attribute assigned. 

```java
    UserAttributeValueKey userAttributeValueKey = attributeValueKeyGenerator.generateUserKey(gpp, "genesisUser@tu-berlin.de", authorityKey.getPrivateKey(), attributeValueKey.getPrivateKey());
```

## Encrypt

To encrypt for a user we first ned to create a policy for this cipher text. 
The `AccessPolicyParser` requires two dependencencies: The `AttributeValueKeyProvider` interface implementaiton 
and the `AuthorityKeyPorivder` implementation. An application can implement thouse interfaces to retrieve the 
desired attribute-value-keys or authority-keys. An appropiate exception should be thrown if they could not be found
indicating, that the user specified a policy that relates to attributes/authorities that do not exist. 

Further the `AccessPolicyParser` can parse any DNF formular. Meaning that the passed fomular should be present in 
the form `'('(ATTR_ID (and ATTR_ID)*)')' (or ('(' ATTR_ID (and ATTR_ID)*) ')')*`. 

```java
    AttributeValueKeyProvider attributeValueKeyProvider = (attributeValueId) -> attributeValueKey.getPublicKey();
    AuthorityKeyProvider authorityKeyProvider = (authorityId) -> authorityKey.getPublicKey();
    DnfAccessPolicy dnfAccessPolicy = new AccessPolicyParser(attributeValueKeyProvider, authorityKeyProvider)
        .parse("(aa.tu-berlin.de.role:Student)");
```

Lets choose a plain text that we want to encrypt: "No, Eve please :(". 

```java
    byte[] message = "No, Eve please :(".getBytes();
    DNFCipherText dnfCipherText = pairingCryptEngine.encrypt(message, dnfAccessPolicy, gpp, null);
```

The `null` element here referres to the `DataOwner` object which we are currently not using. If we would do so we would also secure the cipher text with a two-factor authentication key. 

The cipher text object now contains all needed cipher text components and also the encyrpted message.

## Decrypt

To decrypt the encrypted message we now use the `userAttributeValueSecretKey`. 

Please note that we do not need to filter for the policy this user satisfies, since we only have one policy element in 
our ciphertext. So we can simply use `dnfCipherText.getCipherTexts().get(0)`. In the complete example this is properly 
implemented with the function `findSatisfyingCipherText(...)`. 

```java
    Set<UserAttributeSecretComponents> userAttributeKeys = new HashSet<>();
    userAttributeKeys.add(new UserAttributeSecretComponents(userAttributeValueKey, attributeValueKey.getPublicKey(), aid));

    byte[] recoveredMessage = pairingCryptEngine.decrypt(dnfCipherText.getFile().getData(), dnfCipherText.getCipherTexts().get(0), gpp, uid, userAttributeKeys, null);
```

Finally we can convert the `byte[]` back to a `String` with `new String(recoveredMessage)`. 


## Encrypt using two factor authentication

We need to add another dependency to enable two-factor authentication (2FA) and create a new identifier for the data owner. 

```java
    String oid = "dataowner@tu-berlin.de";
    TwoFactorKeyGenerator twoFactorKeyGenerator = new twoFactorKeyGenerator(hashGenerator);
    TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generateNew(gpp);
    DataOwner dataOwner = new DataOwner(oid, twoFactorKey.getPrivateKey());
```

We then can encrypt the plain text additionally with the data owner object. 

```java
   CipherText cipherText = pairingCryptEngine.encrypt(message, dnfAccessPolicy, gpp, dataOwner);
```


## Decrypt using two factor authentication

Let's generate the user 2FA key for this cipher text and decipher this message using this key. 

```java
   twoFactorKey = twoFactorKeyGenerator.generatePublicKeyForUser(gpp, twoFactorKey, uid);
   byte[] recoveredMessage = decrypt(gpp, uid, cipherText, userAttributeKeys, twoFactorKey.getPublicKeyOfUser(uid));
```








