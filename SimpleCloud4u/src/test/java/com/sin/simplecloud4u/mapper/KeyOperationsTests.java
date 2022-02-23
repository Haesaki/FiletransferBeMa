package com.sin.simplecloud4u.mapper;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Show usage of operations on redis keys using low level API provided by {@link RedisConnection}.
 *
 * @author Christoph Strobl
 */
@DataRedisTest
public class KeyOperationsTests {

    private static final String PREFIX = KeyOperationsTests.class.getSimpleName();
    private static final String KEY_PATTERN = PREFIX + "*";

    @Autowired RedisConnectionFactory connectionFactory;

    private RedisConnection connection;
    private RedisSerializer<String> serializer = new StringRedisSerializer();

    @BeforeEach
    void setUp() {
        this.connection = connectionFactory.getConnection();
    }

    /**
     * Uses {@code KEYS} command for loading all matching keys. <br />
     * Note that {@code KEYS} is a blocking command that potentially might affect other operations execution time. <br />
     * All keys will be loaded within <strong>one single</strong> operation.
     */
    @Test
    void iterateOverKeysMatchingPrefixUsingKeysCommand() {

        generateRandomKeys(1000);

        var keys = this.connection.keys(serializer.serialize(KEY_PATTERN));
    }

    /**
     * Uses {@code SCAN} command for loading all matching keys. <br />
     * {@code SCAN} uses a cursor on server side returning only a subset of the available data with the possibility to
     * ripple load further elements using the cursors position. <br />
     * All keys will be loaded using <strong>multiple</strong> operations.
     */
    @Test
    void iterateOverKeysMatchingPrefixUsingScanCommand() {

        generateRandomKeys(1000);

        this.connection.scan(ScanOptions.scanOptions().match(KEY_PATTERN).build());
    }

    private void generateRandomKeys(int nrKeys) {

        for (var i = 0; i < nrKeys; i++) {
            this.connection.set((PREFIX + "-" + i).getBytes(), UUID.randomUUID().toString().getBytes());
        }
    }
}
