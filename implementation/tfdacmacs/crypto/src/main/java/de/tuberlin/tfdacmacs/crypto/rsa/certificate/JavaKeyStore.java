package de.tuberlin.tfdacmacs.crypto.rsa.certificate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

@RequiredArgsConstructor
public class JavaKeyStore {

    private KeyStore keyStore;

    private final String keyStoreName;
    private final String keyStorePassword;

    public void createEmptyKeyStore(String keyStoreType) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        if(keyStoreType ==null || keyStoreType.isEmpty()){
            keyStoreType = KeyStore.getDefaultType();
        }
        keyStore = KeyStore.getInstance(keyStoreType);
        //load
        char[] pwdArray = keyStorePassword.toCharArray();
        keyStore.load(null, pwdArray);
        save(pwdArray);
        return;

    }

    public void save(char[] pwdArray)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        // Save the keyStore
        FileOutputStream fos = new FileOutputStream(keyStoreName);
        keyStore.store(fos, pwdArray);
        fos.close();
    }

    public void loadKeyStore() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] pwdArray = keyStorePassword.toCharArray();
        keyStore.load(new FileInputStream(keyStoreName), pwdArray);
    }

    public void loadFromClassPath(String name)
            throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] pwdArray = keyStorePassword.toCharArray();
        keyStore.load(ClassLoader.getSystemResourceAsStream(name), pwdArray);
    }

    public void setEnty(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protectionParameter) throws KeyStoreException {
        keyStore.setEntry(alias, entry, protectionParameter);
    }

    public void setEntry(String alias, KeyStore.SecretKeyEntry secretKeyEntry, KeyStore.ProtectionParameter protectionParameter) throws KeyStoreException {
        keyStore.setEntry(alias, secretKeyEntry, protectionParameter);
    }

    public KeyStore.Entry getEntry(String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keyStorePassword.toCharArray());
        return keyStore.getEntry(alias, protParam);
    }

    public KeyStore.Entry getEntry(String alias, KeyStore.ProtectionParameter protectionParameter) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
        return keyStore.getEntry(alias, protectionParameter);
    }

    public void setKeyEntry(String alias, PrivateKey privateKey, String keyPassword, Certificate[] certificateChain) throws KeyStoreException {
        keyStore.setKeyEntry(alias, privateKey, keyPassword.toCharArray(), certificateChain);
    }

    public Key getKeyEntry(String alias, String keyPassword)
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return getKeyStore().getKey(alias, keyPassword.toCharArray());
    }

    public void setCertificateEntry(String alias, Certificate certificate) throws KeyStoreException {
        keyStore.setCertificateEntry(alias, certificate);
    }

    public Certificate getCertificate(String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias);
    }

    public boolean containEntry(@NonNull String alias) throws KeyStoreException {
        return keyStore.containsAlias(alias);
    }

    public void deleteEntry(String alias) throws KeyStoreException {
        keyStore.deleteEntry(alias);
    }

    public void deleteKeyStore() throws KeyStoreException, IOException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            keyStore.deleteEntry(alias);
        }
        keyStore = null;
        Files.delete(Paths.get(keyStoreName));
    }

    public KeyStore getKeyStore() {
        return this.keyStore;
    }
}


