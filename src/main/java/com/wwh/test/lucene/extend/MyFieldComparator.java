package com.wwh.test.lucene.extend;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.IntParser;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.util.BytesRef;

//根据条件，获取推荐时间 或者 更新时间
public class MyFieldComparator extends FieldComparator<Long> {

    private long[] values;
    private IntParser parser;
    private FieldCache.Longs currentRecommendTimeReader;
    private FieldCache.Longs currentUpdateTimeReader;
    private FieldCache.Ints currentCityReader;

    // 测试读取字符串
    private BinaryDocValues binaryDocValues;

    private long bottom;
    private long topValue;

    private int queryCity;

    public MyFieldComparator(int numHits, int qCity) {
        this.values = new long[numHits];
        this.queryCity = qCity;
        this.parser = FieldCache.DEFAULT_INT_PARSER;
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
        int city = currentCityReader.get(doc);
        long docValue = 0;
        if (city == queryCity) {
            docValue = currentRecommendTimeReader.get(doc);
        } else {
            docValue = currentUpdateTimeReader.get(doc);
        }

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
        int city = currentCityReader.get(doc);
        long docValue = 0;
        if (city == queryCity) {
            docValue = currentRecommendTimeReader.get(doc);
        } else {
            docValue = currentUpdateTimeReader.get(doc);
        }

        if (topValue < docValue) {
            return -1;
        } else if (topValue > docValue) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
//******************
        // 测试：
        // 1. fieldCache中的字段值是从倒排表中读出来的，所以排序的字段必须设为索引字段
        // 2. 用来排序的字段在索引的时候不能拆分（tokenized），因为fieldCache数组中，每个文档只对应一个字段值，拆分的话，cache中只会保存在词典中靠后的值。

        BytesRef bytesRef = new BytesRef();
        binaryDocValues.get(doc, bytesRef);
        String value = bytesRef.utf8ToString();
        System.out.println("这里读到的：文档" + doc + "  值：" + value);
//******************

        int city = currentCityReader.get(doc);
        long v2 = 0;
        if (city == queryCity) {
            v2 = currentRecommendTimeReader.get(doc);
        } else {
            v2 = currentUpdateTimeReader.get(doc);
        }

        values[slot] = v2;
    }

    @Override
    public FieldComparator<Long> setNextReader(AtomicReaderContext context) throws IOException {
        // 字段固定
        currentRecommendTimeReader = FieldCache.DEFAULT.getLongs(context.reader(), "recommendTime", false);
        currentUpdateTimeReader = FieldCache.DEFAULT.getLongs(context.reader(), "updateTime", false);
        currentCityReader = FieldCache.DEFAULT.getInts(context.reader(), "city", parser, false);// setDocsWithField 是什么意思没有看懂

        // 测试用的
        binaryDocValues = FieldCache.DEFAULT.getTerms(context.reader(), "soilLoctionIdSearch", false);

        return this;
    }

    @Override
    public Long value(int slot) {
        return Long.valueOf(values[slot]);
    }

}
