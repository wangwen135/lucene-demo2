package com.wwh.test.lucene.extend;

import java.io.IOException;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

public class MyFieldComparatorSource2 extends FieldComparatorSource {

    private int qCity;
    private int qRecommend;

    public MyFieldComparatorSource2(int queryCity, int queryRecommend) {
        this.qCity = queryCity;
        this.qRecommend = queryRecommend;
    }

    @Override
    public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
            throws IOException {

        return new MyFieldComparator2(numHits, qCity, qRecommend);
    }

}
