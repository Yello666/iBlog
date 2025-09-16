package yellow.iblog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yellow.iblog.model.Article;
import yellow.iblog.service.ArticleService;

import java.util.List;

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
    //通过用户id访问文章列表
    public void testGetArticleByUid(){
        List<Article> articleList=articleService.getArticleByUid(testUid);
        for(Article a:articleList){
            System.out.println(a);
        }

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
        Boolean ok=articleService.deleteArticleByAid(3L);
        assertEquals(true,ok);//前一个值是期待，后一个值是返回值
    }



}
