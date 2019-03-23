package de.tuberlin.tfdacmacs.crypto.benchmark.factory;

import de.tuberlin.tfdacmacs.crypto.benchmark.User;

import java.util.Set;

public abstract class UserFactory<T extends User> {

    abstract public Set<T> create(int num);
}
