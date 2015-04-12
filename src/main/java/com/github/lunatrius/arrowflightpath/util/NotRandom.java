package com.github.lunatrius.arrowflightpath.util;

import java.util.Random;

public class NotRandom extends Random {
    public static final NotRandom INSTANCE = new NotRandom();

    @Override
    public float nextFloat() {
        return 0;
    }

    @Override
    public synchronized double nextGaussian() {
        return 0;
    }
}
