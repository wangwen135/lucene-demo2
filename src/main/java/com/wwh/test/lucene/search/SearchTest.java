package com.wwh.test.lucene.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.wwh.test.lucene.extend.MyFieldComparatorSource;
import com.wwh.test.lucene.index.IndexTest;

public class SearchTest {

    public static void main(String[] args) throws Exception {

        Directory directory = FSDirectory.open(new File(IndexTest.indexDir));

        DirectoryReader ireader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(ireader);

        BooleanQuery bQuery = new BooleanQuery();

        System.out.println("## 假设用户的所在(省市区)为：1-2-3");

        // 优先级1：优先取后台做了手动推荐到app首页，且推荐土地区域包括在当前城市范围内的土地，按照推荐时间降序排
        BooleanQuery condition1 = new BooleanQuery();
        condition1.add(new TermQuery(new Term("recommend", "2")), BooleanClause.Occur.MUST);
        condition1.add(new TermQuery(new Term("city", "12")), BooleanClause.Occur.MUST);
        condition1.setBoost(100);// 权重100
        bQuery.add(condition1, BooleanClause.Occur.SHOULD);

        // 优先级2：再取土地区域为所选区域上一级（省级）的土地数据的，根据更新时间排序
        // 省份相同
        TermQuery condition2 = new TermQuery(new Term("province", "1"));
        condition2.setBoost(50);// 权重50
        bQuery.add(condition2, BooleanClause.Occur.SHOULD);
        // 优先级3：再取全部的土地数据的，根据更新时间排序
        // 全部的土地
        bQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD);

        // 过滤-只保留待交易的
        // TermFilter termFilter = new TermFilter(new Term("status","2"));
        Filter filter = NumericRangeFilter.newIntRange("status", 2, 2, true, true);

        // 得分排序优先
        // 自定义排序
        // Sort sort = new Sort(SortField.FIELD_SCORE);// 查询城市为2
        Sort sort = new Sort(SortField.FIELD_SCORE, new SortField("", new MyFieldComparatorSource(12), true));// 查询城市为2

        TopDocs rt = searcher.search(bQuery, filter, 100, sort, true, false);
        printResult(searcher, rt);

        ireader.close();
        directory.close();

    }

    public static void main2(String[] args) throws Exception {

        Directory directory = FSDirectory.open(new File(IndexTest.indexDir));

        DirectoryReader ireader = DirectoryReader.open(directory);

        // 标准分词器
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

        IndexSearcher searcher = new IndexSearcher(ireader);

        System.out.println("########### 查询全部 ##############");
        TopDocs rt = searcher.search(new MatchAllDocsQuery(), 100);
        printResult(searcher, rt);

/*
System.out.println("########### 查询推荐的 ##############");
rt = searcher.search(new TermQuery(new Term("recommend", "2")), 100);
printResult(searcher, rt);
*/

        // 优先级1：优先取后台做了手动推荐到app首页，且推荐土地区域包括在当前城市范围内的土地，按照推荐时间降序排

        System.out.println("########### 推荐-区域-推荐时间降序排列-过滤待交易的 ##############");
        BooleanQuery bQuery = new BooleanQuery();
        bQuery.add(new TermQuery(new Term("recommend", "2")), BooleanClause.Occur.MUST);
        bQuery.add(new TermQuery(new Term("city", "2")), BooleanClause.Occur.MUST);
        // 推荐时间降序
        Sort sort = new Sort(new SortField("recommendTime", SortField.Type.LONG, true));
        // 过滤-只保留待交易的
        // TermFilter termFilter = new TermFilter(new Term("status","2"));
        Filter f = NumericRangeFilter.newIntRange("status", 2, 2, true, true);
        rt = searcher.search(bQuery, f, 100, sort);
        printResult(searcher, rt);

        /*System.out.println("########### 查询推荐的，按照得分排序 ##############");
        sort = new Sort(new SortField("calcScore", SortField.Type.INT, true));
        rt = searcher.search(new TermQuery(new Term("recommend", "2")), 100, sort);
        printResult(searcher, rt);*/
        // calcScore

        // 优先级2：再取土地区域为所选区域上一级（省级）的土地数据的，根据更新时间排序
        // 优先级3：再取全部的土地数据的，根据更新时间排序
        System.out.println("########### 查询优先级2~3 ##############");

        bQuery = new BooleanQuery();
        // 省份相同
        bQuery.add(new TermQuery(new Term("province", "1")), BooleanClause.Occur.SHOULD);
        // 全部的土地
        bQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD);
        // 得分排序优先
        // 更新时间降序排列
//        sort = new Sort(SortField.FIELD_SCORE, new SortField("updateTime", SortField.Type.LONG, true));
//        sort = new Sort(SortField.FIELD_SCORE);
        // Sort.RELEVANCE
        // 过滤-只保留待交易的

        sort = new Sort(SortField.FIELD_SCORE, new SortField("", new MyFieldComparatorSource(1), true));

        rt = searcher.search(bQuery, f, 100, sort, true, false);// doDocScores 为真，则将计算并返回每个命中的分数
        printResult(searcher, rt);

        ireader.close();
        directory.close();

    }

    private static void printResult(IndexSearcher searcher, TopDocs rt) throws IOException {
        System.out.println("总结果数：" + rt.totalHits);
        ScoreDoc[] hits = rt.scoreDocs;
        for (ScoreDoc scoreDoc : hits) {
            System.out.println("文档：" + scoreDoc.doc + " 得分：" + scoreDoc.score);
            Document hitDoc = searcher.doc(scoreDoc.doc);
            System.out.println(hitDoc.toString());

            StringBuffer sbf = new StringBuffer();
            sbf.append("sid=");
            sbf.append(hitDoc.get("sid"));

            sbf.append("    title=");
            sbf.append(hitDoc.get("title"));

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
