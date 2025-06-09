package org.example.fitpass.domain.search.service;

import java.util.Random;

public class GymNameGenerator {

    private static final String[] PREFIXES = {
            "스파르타", "파워", "헬스킹", "피트존", "올라운드", "아이언", "빅", "코어", "슈퍼핏", "더짐"
    };

    private static final String[] MIDDLES = {
            "짐", "피트니스", "센터", "클럽", "스튜디오", "헬스"
    };

    private static final String[] SUFFIXES = {
            "강남점", "홍대점", "본점", "역삼점", "잠실점", "지점", "프리미엄", "24시", "에디션", ""
    };

    private static final Random random = new Random();

    public static String generate() {
        String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
        String middle = MIDDLES[random.nextInt(MIDDLES.length)];
        String suffix = SUFFIXES[random.nextInt(SUFFIXES.length)];
        return prefix + " " + middle + (suffix.isEmpty() ? "" : " " + suffix);
    }
}