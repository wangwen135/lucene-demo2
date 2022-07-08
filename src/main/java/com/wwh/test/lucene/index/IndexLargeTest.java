package com.wwh.test.lucene.index;

import java.io.File;
import java.util.Random;

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

/**
 * 生成一个很大的索引
 * 
 */
public class IndexLargeTest {
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

        // id,标题 ,状态 ,省份 ,城市,区县 ,计算得分 ,备注 ,推荐 ,推荐时间 ,更新时间
        // sid,title,status,province,city,county,calcScore,remark,recommend,recommendTime,updateTime

        Random random = new Random();
        int contentLength = content.length();

        String[] provinceArray = provinces.split(" ");

        // 几十万的模拟数据
        for (int i = 0; i < 600000; i++) {

            if (i % 100 == 0) {
                System.out.println("生成文档：" + i);
            }

            Document doc = new Document();

            doc.add(new IntField("sid", i, Field.Store.YES));
            StringBuffer sbufTitle = new StringBuffer();
            sbufTitle.append("测试文档：");
            sbufTitle.append(i);
            sbufTitle.append(" ");
            sbufTitle.append(provinceArray[random.nextInt(provinceArray.length)]);
            sbufTitle.append(random.nextInt(500));
            sbufTitle.append("亩地出租、出让");

            doc.add(new TextField("title", sbufTitle.toString(), Field.Store.YES));
            int status = random.nextInt(5);
            doc.add(new IntField("status", status, Field.Store.YES));
            doc.add(new StringField("statusDesc", "状态是：" + status, Field.Store.YES));

            // 模拟字段可能为空的情况，2%的数据没有省市区
            int x = random.nextInt(100);
            if (x > 2) {
                int province = i % 34 + 1;
                int city = i % 20 + 1;
                city += province * 100;
                int county = i % 18 + 1;
                county += city * 100;

                // 省份 ,城市,区县
                doc.add(new StringField("province", province + "", Field.Store.YES));
                doc.add(new IntField("provinceInt", province, Field.Store.YES));

                doc.add(new StringField("city", city + "", Field.Store.YES));
                doc.add(new IntField("cityInt", city, Field.Store.YES));

                doc.add(new StringField("county", county + "", Field.Store.YES));
                doc.add(new IntField("countyInt", county, Field.Store.YES));

                // 现在的搜索是省市区加空格拼接的，这里模拟一下
                doc.add(new TextField("soilLoctionIdSearch", province + " " + city + " " + county, Field.Store.NO));

            }

            int calcScore = i % 100;
            // 计算得分
            doc.add(new IntField("calcScore", calcScore, Field.Store.YES));
            // 备注
            doc.add(new TextField("remark", "计算得分为：" + calcScore, Field.Store.YES));

            // 随意补充内容
            int contentSubIndex = random.nextInt(contentLength - 100);
            doc.add(new TextField("supplement", content.substring(contentSubIndex, contentSubIndex + 100),
                    Field.Store.YES));

            int n = random.nextInt(100);
            if (n < 10) {// 百分之十的推荐
                // 推荐
                doc.add(new StringField("recommend", "1", Field.Store.YES));
                doc.add(new IntField("recommendInt", 1, Field.Store.YES));
                // 推荐时间
                doc.add(new LongField("recommendTime", 1656500000 + random.nextInt(99999), Field.Store.YES));
            }

            // 更新时间
            doc.add(new LongField("updateTime", 1656500000 + random.nextInt(99999), Field.Store.YES));

            writer.addDocument(doc);
        }

        writer.close();
        System.out.println("索引生成完毕");
    }

    private static final String provinces = "北京市 天津市 河北省 山西省 内蒙古 辽宁省 吉林省 黑龙江 上海市 江苏省 浙江省 安徽省 福建省 江西省 山东省 河南省 湖北省 湖南省 广东省 广西  海南省 重庆市 四川省 贵州省 云南省 西藏  陕西省 甘肃省 青海省 宁夏  新疆  台湾省 香港  澳门";

    private static final String content = "lucene搜索之facet查询原理和facet查询实例 - ShenWenFanghttps://swenfang.github.io › 2019/03/16 › 14、lucene...\n"
            + "2019年3月16日 — lucene提供了facet查询用于对同一类的document进行聚类化，这样在查询的时候先关注某一个方面，这种显然缩小了查询 ... 测试facet功能的测试类： ...\n"
            + "\n"
            + "Lucene-18-facet lucene搜索之facet查询原理和facet查询实例https://houbb.github.io › 2022/01/10 › lucene-18-facet\n"
            + "测试代码 — lucene 提供了facet 查询用于对同一类的document进行聚类化，这样在查询的时候先关注某一个方面，这种显然缩小了查询范围，进而提升了查询效率；. facet 是 ...\n"
            + "\n" + "lucene使用facet搜索 - CSDN博客https://blog.csdn.net › article › details\n"
            + "2018年9月17日 — Lucene中的facet查询其实就是对事物的方面查询。 ... 对facet查询进行测试* @throws Exception */ @Test public void testFacetSearch() throws ...\n"
            + "\n" + "Lucene5学习之Facet(续) - 阿里云开发者社区https://developer.aliyun.com › article\n"
            + "NumericDocValuesField;; import org.apache.lucene.facet. ... nowSec, true);; /**; * 创建测试索引; * @throws IOException; */; public void index() throws ...\n"
            + "\n" + "Lucene5学习之Facet简单入门 - 阿里云开发者社区https://developer.aliyun.com › article\n"
            + "FacetsCollector;; import org.apache.lucene.facet. ... setHierarchical(\"Publish Date\", true);; }; /**; * 创建测试索引; *; * @throws IOException ...\n"
            + "\n" + "Lucene系列-facet--转- 一天不进步，就是退步 - 博客园https://www.cnblogs.com › davidwang456\n"
            + "2018年11月22日 — https://blog.csdn.net/whuqin/article/details/42524825 1.facet的直观认识facet：面、切面、方面。个人理解就是维度，在满足query的前.\n"
            + "\n" + "lucene搜索之facet查询原理和facet查询实例——TODO - boneleehttps://www.cnblogs.com › bonelee\n"
            + "2017年1月24日 — lucene提供了facet查询用于对同一类的document进行聚类化，这样在查询的时候先关注某一个方面，这种显然缩小了查询范围，进而提升了查询效率；\n" + "\n"
            + "一步一步跟我学习lucene（16）---lucene搜索之facet查询查询示例 ...https://cxybb.com › article › wuyinggui10000\n"
            + "1.CANopen的一致性测试概念可从CiA协会组织官方链接https://www.can-cia.org/services/test-center/了解相关信息，官方的测试中心中有CANopen的一致性测试服...\n"
            + "\n" + "lucene使用facet搜索_tinysakura的博客-程序员ITS404 - 程序员ITS404";

}
