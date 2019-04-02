package de.tuberlin.tfdacmacs.crypto.benchmark;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Data
public abstract class Group<USER extends  User, CIPHERTEXT extends CipherTextLength> {

    private List<USER> members;
    private Set<CIPHERTEXT> cipherTexts;

    public Group() {
        this.members = new ArrayList<>();
        this.cipherTexts = new HashSet<>();
    }

    public RunTimeResult encrypt(byte[] content, USER asMember) {
        CipherTextResult<CIPHERTEXT> result = measure(
            () -> doEncrypt(content, this.members, asMember)
        );
        cipherTexts.add(result.getCipherText());

        long size = result.getCipherText().getSize();
        long numberOfFileKeys = result.getCipherText().getNumberOfFileKeys();
        return new RunTimeResult(result.getTime(), size, numberOfFileKeys);
    }

    public RunTimeResult decrypt(CIPHERTEXT content, USER asMember) {
        long time = measure(
                () -> {
                    doDecrypt(content, asMember);
                    return null;
                }
        ).getTime();

        return new RunTimeResult(time, content.getSize(), content.getNumberOfFileKeys());
    }

    public RunTimeResult join(USER member) {
        long res = measure(() -> doJoin(member, members, cipherTexts));
        this.members.add(member);
        return new RunTimeResult(res, 0L, getCipherTexts().size());
    }

    public RunTimeResult leave(USER member) {
        this.members.remove(member);
        long res = measure(() -> doLeave(member, members, cipherTexts));
        return new RunTimeResult(res, 0L, getCipherTexts().size());
    }


    protected abstract CIPHERTEXT doEncrypt(byte[] content, List<USER> members, USER asMember);
    protected abstract byte[] doDecrypt(CIPHERTEXT content, USER asMember);
    protected abstract void doJoin(USER newMember, List<USER> existingMembers, Set<CIPHERTEXT> cipherTexts);
    protected abstract void doLeave(USER newMember, List<USER> existingMembers, Set<CIPHERTEXT> cipherTexts);

    private CipherTextResult<CIPHERTEXT> measure(Supplier<CIPHERTEXT> processor) {
        long start = System.nanoTime();
        CIPHERTEXT CIPHERTEXT = processor.get();
        return new CipherTextResult<>(System.nanoTime() - start, CIPHERTEXT);
    }

    private long measure(Runnable processor) {
        long start = System.nanoTime();
        processor.run();
        return System.nanoTime() - start;
    }

    public Group reset() {
        this.cipherTexts.clear();
        this.members.clear();
        return this;
    }

    @Data
    private class CipherTextResult<E extends CipherTextLength> {
        private final long time;
        private final E cipherText;
    }
}
