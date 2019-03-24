package de.tuberlin.tfdacmacs.crypto.benchmark;

import lombok.Data;

@Data
public class RunTimeResult {

    private final long time;
    private final long cipherTextLength;
    private final long numberOfFileKeys;
}
