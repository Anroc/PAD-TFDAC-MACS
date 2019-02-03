package de.tuberlin.tfdacmacs.client.rest.session;

import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;

import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;

public interface Session {

    String getEmail();

    Certificate getCertificate();

    KeyPair getKeyPair();

    boolean isActive();
}
