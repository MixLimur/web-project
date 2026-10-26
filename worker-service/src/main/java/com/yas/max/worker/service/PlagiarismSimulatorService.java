package com.yas.max.worker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

import static org.apache.commons.codec.digest.DigestUtils.digest;

@Slf4j
@Service
public class PlagiarismSimulatorService {


    public double performHeavyAnalysis(String text, IntConsumer progressCallback, BooleanSupplier isCancelled) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = text.getBytes();

            long totalIterations = 1_000_000L + (long) text.length() * 7_000L;

            for (int i = 1; i <= totalIterations; i++) {
                if (i % 100_000 == 0 && isCancelled.getAsBoolean()) {
                    log.info("Обчислення перервано користувачем.");
                    return -1.0;
                }

                if (i % (totalIterations / 100) == 0) {
                    int progress = (int) (((double) i / totalIterations) * 100);
                    progressCallback.accept(progress);
                }

                hash = digest.digest(hash);
            }

            int pseudoRandomMatch = Math.abs(hash[0] % 101);
            return (double) pseudoRandomMatch;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм хешування не знайдено", e);
        }
    }

}
