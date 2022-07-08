package com.wwh.test.lucene.index;

import java.io.File;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexTest {
    // 索引指定的文档路径
    public static final String indexDir = "./src/main/resources/index";

    public static void main(String[] args) throws Exception {

        System.out.println("开始生成索引");

        Directory directory = FSDirectory.open(new File(indexDir));
        // 标准分词器
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_47, analyzer);

        IndexWriter writer = new IndexWriter(directory, writerConfig);

        System.out.println("删除所有document");
        writer.deleteAll();

        List<String[]> dataList = ReadCSVFile.getFileContent();
        // id,标题 ,状态 ,省份 ,城市,区县 ,计算得分 ,备注 ,推荐 ,推荐时间 ,更新时间
        // sid,title,status,province,city,county,calcScore,remark,recommend,recommendTime,updateTime
        for (int i = 0; i < dataList.size(); i++) {
            String[] ss = dataList.get(i);
            System.out.println("生成文档：" + i);
            Document doc = new Document();

            doc.add(new IntField("sid", Integer.parseInt(ss[0]), Field.Store.YES));
            doc.add(new TextField("title", ss[1], Field.Store.YES));
            doc.add(new IntField("status", Integer.parseInt(ss[2]), Field.Store.YES));
            // 省份 ,城市,区县
            doc.add(new StringField("province", ss[3], Field.Store.YES));
            doc.add(new IntField("provinceInt", Integer.parseInt(ss[3]), Field.Store.YES));
            
            doc.add(new StringField("city", ss[4], Field.Store.YES));
            doc.add(new IntField("cityInt", Integer.parseInt(ss[4]), Field.Store.YES));
            
            doc.add(new StringField("county", ss[5], Field.Store.YES));
            doc.add(new IntField("countyInt", Integer.parseInt(ss[5]), Field.Store.YES));

            // 现在的搜索是省市区加空格拼接的，这里模拟一下
            doc.add(new TextField("soilLoctionIdSearch", ss[3] + " " + ss[4] + " " + ss[5], Field.Store.NO));

            // 计算得分
            doc.add(new IntField("calcScore", Integer.parseInt(ss[6]), Field.Store.YES));
            // 备注
            doc.add(new StringField("remark", ss[7], Field.Store.YES));
            // 推荐
            doc.add(new StringField("recommend", ss[8], Field.Store.YES));

            // 推荐时间
            doc.add(new LongField("recommendTime", Long.parseLong(ss[9]), Field.Store.YES));
            // 更新时间
            doc.add(new LongField("updateTime", Long.parseLong(ss[10]), Field.Store.YES));

            writer.addDocument(doc);
        }

        writer.close();
        System.out.println("索引生成完毕");
    }
}
