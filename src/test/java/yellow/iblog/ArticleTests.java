package yellow.iblog;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yellow.iblog.model.Article;
import yellow.iblog.service.ArticleService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ArticleTests {
    @Autowired
    ArticleService articleService;
    private final Long testUid=1967172287496998913L;

    //用户创建文章
    @Test
    public void testCreateArticle(){
        Article article=new Article(testUid,"不重逢","我没有说告别，眼看他仓皇走在风中");
        Article a=articleService.createArticle(article);
        assertThat(a,instanceOf(Article.class));//类型断言
        System.out.println(a);
    }

    @Test
    //通过文章id访问一篇文章
    public void testGetArticleByAid(){
        Article a=articleService.getArticleByAid(7L);
        assertThat(a,instanceOf(Article.class));
        System.out.println(a);
    }

    @Test
    void testGetArticleByUid() throws InterruptedException {
        Long uid = 100L;

        // 清理数据（可选，看测试数据库是否隔离）
//        articleService.delete(null);

        // 插入测试数据
        Article a1 = new Article();
        a1.setUid(uid);
        a1.setTitle("文章1");
        articleService.createArticle(a1);

        Article a2 = new Article();
        a2.setUid(uid);
        a2.setTitle("文章2");
        articleService.createArticle(a2);

        Article a3 = new Article();
        a3.setUid(uid);
        a3.setTitle("文章3");
        articleService.createArticle(a3);

        // 分页查询（每页2条，取第1页）
        Page<Article> page = articleService.getArticleByUid(uid, 1, 2);

        // 类型断言
        assertThat(page, instanceOf(Page.class));
        // 结果大小
        assertEquals(2, page.getRecords().size());
//        // 值断言
//        assertEquals("文章3", page.getRecords().getFirst().getTitle());
    }


//    用户修改文章
    @Test
    public void testUpdateArticle(){
        Article a=new Article(2L,testUid,"永不熄灭的火焰","I'll fight private sight");
        Article updated=articleService.updateArticle(a);
        assertThat(a,instanceOf(Article.class));
        System.out.println(updated);
    }

    @Test
    public void testDeleteArticle(){
        Article article=new Article(testUid,"不重逢","我没有说告别，眼看他仓皇走在风中");
        Article a=articleService.createArticle(article);
        Boolean ok=articleService.deleteArticleByAid(a.getAid());
        assertEquals(true,ok);//前一个值是期待，后一个值是返回值
    }



}
