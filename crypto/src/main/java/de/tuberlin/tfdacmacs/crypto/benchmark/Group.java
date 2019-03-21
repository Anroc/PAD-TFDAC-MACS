package de.tuberlin.tfdacmacs.crypto.benchmark;

import lombok.Data;

import java.util.Set;

@Data
public abstract class Group<T extends  User, E> {

    private Set<T> members;
    private Set<E> cipherTexts;

    public long encrypt(byte[] content, T asMember) {
        return measure(
            () -> cipherTexts.add(
                    doEncrypt(content, this.members, asMember)
            )
        );
    }

    public long decrypt(E content, T asMember) {
        return measure(
                () -> doDecrypt(content, asMember)
        );
    }

    public long join(T member) {
        long res = measure(() -> doJoin(member, members));
        this.members.add(member);
        return res;
    }
    
    public long leave(T member) {
        this.members.remove(member);
        return measure(() -> doLeave(member, members));
    }


    protected abstract E doEncrypt(byte[] content, Set<T> members, T asMember);
    protected abstract byte[] doDecrypt(E content, T asMember);
    protected abstract void doLeave(T member, Set<T> members);
    protected abstract void doJoin(T member, Set<T> members);

    private long measure(Runnable processor) {
        long start = System.nanoTime();
        processor.run();
        return System.nanoTime() - start;
    }
}
