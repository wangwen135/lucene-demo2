package com.wwh.test.lucene.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.wwh.test.lucene.extend.MyFieldComparatorSource2;
import com.wwh.test.lucene.index.IndexTest;

public class SearchLargeTest2 {

    public static void main(String[] args) throws Exception {

        Directory directory = FSDirectory.open(new File(IndexTest.indexDir));

        DirectoryReader ireader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(ireader);

        BooleanQuery bQuery = new BooleanQuery();

        System.out.println("## 假设用户的所在(省市)为：1-103");

        int qProvince = 1;
        int qCity = 103;

        // ##########################################
        // 优先级1：优先取后台做了手动推荐到app首页，且推荐土地区域包括在当前城市范围内的土地，按照推荐时间降序排
        BooleanQuery condition1 = new BooleanQuery();
//        condition1.add(new TermQuery(new Term("recommend", "1")), BooleanClause.Occur.MUST);
        condition1.add(NumericRangeQuery.newIntRange("recommendInt", 1, 1, true, true), BooleanClause.Occur.MUST);

        condition1.add(NumericRangeQuery.newIntRange("cityInt", qCity, qCity, true, true), BooleanClause.Occur.MUST);
        // condition1.add(new TermQuery(new Term("city", "16")), BooleanClause.Occur.MUST);
        condition1.setBoost(200);
        bQuery.add(condition1, BooleanClause.Occur.SHOULD);

        // ##########################################
        // 优先级2：优先本城市的
        Query condition2 = NumericRangeQuery.newIntRange("cityInt", qCity, qCity, true, true);
        condition2.setBoost(100);
        bQuery.add(condition2, BooleanClause.Occur.SHOULD);

        // ##########################################
        // 优先级3：再取土地区域为所选区域上一级（省级）的土地数据的，根据更新时间排序
        // 省份相同
        Query condition3 = NumericRangeQuery.newIntRange("provinceInt", qProvince, qProvince, true, true);
        condition3.setBoost(50);// 权重50
        bQuery.add(condition3, BooleanClause.Occur.SHOULD);

        // ##########################################
        // 优先级4：再取全部的土地数据的，根据更新时间排序
        // 全部的土地
        bQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD);

        // 过滤-只保留待交易的
        // TermFilter termFilter = new TermFilter(new Term("status","2"));
        Filter filter = NumericRangeFilter.newIntRange("status", 2, 2, true, true);

        // 得分排序优先
        // 自定义排序
        // Sort sort = new Sort(SortField.FIELD_SCORE);// 查询城市为2

        Sort sort = new Sort(SortField.FIELD_SCORE, new SortField("", new MyFieldComparatorSource2(qCity, 1), true));
        // 更新时间排序
        // Sort sort = new Sort(SortField.FIELD_SCORE, new SortField("updateTime", Type.LONG, true));

        long t1 = System.currentTimeMillis();
        TopDocs rt = searcher.search(bQuery, filter, 3000, sort, true, false);
        System.out.println("总耗时：" + (System.currentTimeMillis() - t1));
        printResult(searcher, rt);
        System.out.println("+输出结果总耗时：" + (System.currentTimeMillis() - t1));
        ireader.close();
        directory.close();

    }

    private static void printResult(IndexSearcher searcher, TopDocs rt) throws IOException {
        System.out.println("总结果数：" + rt.totalHits);
        ScoreDoc[] hits = rt.scoreDocs;
//        for (int i = 900; i < hits.length; i++) {
        for (int i = 0; i < 3000; i += 100) {

            ScoreDoc scoreDoc = hits[i];

            System.out.println("文档：" + scoreDoc.doc + " 得分：" + scoreDoc.score);
            Document hitDoc = searcher.doc(scoreDoc.doc);
            // System.out.println(hitDoc.toString());

            StringBuffer sbf = new StringBuffer();
            sbf.append("sid=");
            sbf.append(hitDoc.get("sid"));

            sbf.append("    province=");
            sbf.append(hitDoc.get("province"));

            sbf.append("    city=");
            sbf.append(hitDoc.get("city"));

            sbf.append("    recommend=");
            sbf.append(hitDoc.get("recommend"));

            sbf.append("    recommendTime=");
            sbf.append(hitDoc.get("recommendTime"));

            sbf.append("    updateTime=");
            sbf.append(hitDoc.get("updateTime"));

            System.out.println(sbf.toString()

            );
        }
        System.out.println("====================================================");
        System.out.println("");
    }
}
