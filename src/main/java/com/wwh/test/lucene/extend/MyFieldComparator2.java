package com.wwh.test.lucene.extend;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.IntParser;
import org.apache.lucene.search.FieldComparator;

//根据条件，获取推荐时间 或者 更新时间
public class MyFieldComparator2 extends FieldComparator<Long> {

    private long[] values;
    private IntParser parser;
    private FieldCache.Longs currentRecommendTimeReader;
    private FieldCache.Longs currentUpdateTimeReader;
    private FieldCache.Ints currentCityReader;
    private FieldCache.Ints currentRecommendReader;

    private long bottom;
    private long topValue;

    private int queryCity;
    private int queryRecommend;

    public MyFieldComparator2(int numHits, int qCity, int recommend) {
        this.values = new long[numHits];
        this.queryCity = qCity;
//        this.parser = FieldCache.DEFAULT_INT_PARSER;
        this.parser = FieldCache.NUMERIC_UTILS_INT_PARSER;
        this.queryRecommend = recommend;
    }

    @Override
    public int compare(int slot1, int slot2) {
        final long v1 = values[slot1];
        final long v2 = values[slot2];
        if (v1 > v2) {
            return 1;
        } else if (v1 < v2) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public void setBottom(int slot) {
        this.bottom = values[slot];
    }

    @Override
    public void setTopValue(Long value) {
        topValue = value;
    }

    @Override
    public int compareBottom(int doc) throws IOException {
        long docValue = getCorrectValue(doc);
        if (bottom > docValue) {
            return 1;
        } else if (bottom < docValue) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public int compareTop(int doc) throws IOException {
        long docValue = getCorrectValue(doc);

        if (topValue < docValue) {
            return -1;
        } else if (topValue > docValue) {
            return 1;
        } else {
            return 0;
        }
    }

    private int count = 0;

    private long getCorrectValue(int doc) {
        count++;
        if (count % 10 == 0) {
            System.out.println(count);
        }

        int recommend = currentRecommendReader.get(doc);
        int city = currentCityReader.get(doc);
        long datetime = 0;
        if (city == queryCity && recommend == this.queryRecommend) {
            datetime = currentRecommendTimeReader.get(doc);
        } else {
            datetime = currentUpdateTimeReader.get(doc);
        }

        return datetime;
    }

    @Override
    public void copy(int slot, int doc) throws IOException {

        long v2 = getCorrectValue(doc);

        values[slot] = v2;
    }

    @Override
    public FieldComparator<Long> setNextReader(AtomicReaderContext context) throws IOException {
        // 字段固定
        currentRecommendTimeReader = FieldCache.DEFAULT.getLongs(context.reader(), "recommendTime", false);
        currentUpdateTimeReader = FieldCache.DEFAULT.getLongs(context.reader(), "updateTime", false);
        currentCityReader = FieldCache.DEFAULT.getInts(context.reader(), "cityInt", false);
        currentRecommendReader = FieldCache.DEFAULT.getInts(context.reader(), "recommendInt", false);
        return this;
    }

    @Override
    public Long value(int slot) {
        return Long.valueOf(values[slot]);
    }

}
