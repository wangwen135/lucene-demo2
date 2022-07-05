package com.wwh.test.lucene.extend;

import java.io.IOException;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

public class MyFieldComparatorSource extends FieldComparatorSource {

    private int qCity;

    public MyFieldComparatorSource(int queryCity) {
        this.qCity = queryCity;
    }

    @Override
    public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
            throws IOException {

        return new MyFieldComparator(numHits, qCity);
    }

}
