package de.tuberlin.tfdacmacs.centralserver.key.db;

import de.tuberlin.tfdacmacs.basics.db.MemoryDB;
import de.tuberlin.tfdacmacs.centralserver.key.data.RsaKeyPair;
import org.springframework.stereotype.Component;

@Component
public class KeyDB extends MemoryDB<RsaKeyPair> {
}
