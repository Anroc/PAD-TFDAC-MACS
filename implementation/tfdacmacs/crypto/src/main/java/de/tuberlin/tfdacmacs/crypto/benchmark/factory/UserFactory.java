package de.tuberlin.tfdacmacs.crypto.benchmark.factory;

import de.tuberlin.tfdacmacs.crypto.benchmark.User;

import java.util.List;

public abstract class UserFactory<T extends User> {

    abstract public List<T> create(int num);
}
