package de.tuberlin.tfdacmacs.crypto.benchmark;

import javafx.util.Pair;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Data
public abstract class Group<T extends  User, E extends CipherTextLength> {

    private Set<T> members;
    private Set<E> cipherTexts;

    public Group() {
        this.members = new HashSet<>();
        this.cipherTexts = new HashSet<>();
    }

    public RunTimeResult encrypt(byte[] content, T asMember) {
        Pair<Long, E> result = measure(
            () -> doEncrypt(content, this.members, asMember)
        );
        long size = result.getValue().getSize();
        long numberOfFileKeys = result.getValue().getNumberOfFileKeys();
        return new RunTimeResult(result.getKey(), size, numberOfFileKeys);
    }

    public RunTimeResult decrypt(E content, T asMember) {
        long time = measure(
                () -> {
                    doDecrypt(content, asMember);
                    return null;
                }
        ).getKey();

        return new RunTimeResult(time, content.getSize(), content.getNumberOfFileKeys());
    }

//    public long join(T member) {
//        long res = measure(() -> doJoin(member, members));
//        this.members.add(member);
//        return res;
//    }
//
//    public long leave(T member) {
//        this.members.remove(member);
//        return measure(() -> doLeave(member, members));
//    }


    protected abstract E doEncrypt(byte[] content, Set<T> members, T asMember);
    protected abstract byte[] doDecrypt(E content, T asMember);

    private Pair<Long, E> measure(Supplier<E> processor) {
        long start = System.nanoTime();
        E e = processor.get();
        return new Pair<>(System.nanoTime() - start, e);
    }

    public Group reset() {
        this.cipherTexts.clear();
        this.members.clear();
        return this;
    }
}
