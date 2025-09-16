package yellow.iblog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;

import yellow.iblog.model.Article;

import yellow.iblog.service.ArticleServiceImpl;

import java.util.List;


@RestController
@RequestMapping("/article")
public class ArticleC {
    private final ArticleServiceImpl articleService;

    public ArticleC(ArticleServiceImpl articleService) {
        this.articleService = articleService;
    }


    //用户写文章
    @PostMapping("")
    public ResponseEntity<ApiResponse<Article>> addArticle(@RequestBody Article article) {
        Article a=articleService.createArticle(article);
        if(a!=null){
            return ResponseEntity.ok(ApiResponse.success(a));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }


    //用户查看某一篇文章
    @GetMapping("/{aid}")
    public ResponseEntity<ApiResponse<Article>> getArticleByAid(@PathVariable Long aid) {
        Article a=articleService.getArticleByAid(aid);
        if(a!=null){
            return ResponseEntity.ok(ApiResponse.success(a));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }

    //用户查看某个用户的所有文章
    @GetMapping("/user/{uid}")
    public ResponseEntity<ApiResponse<List<Article>>> getArticleByUid(@PathVariable Long uid) {
        List<Article> articleList=articleService.getArticleByUid(uid);
        if(articleList!=null){
            return ResponseEntity.ok(ApiResponse.success(articleList));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }



    //用户修改自己的文章
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Article>> updateArticle(@RequestBody Article article){

        Article a=articleService.updateArticle(article);
        if(a!=null){
            return ResponseEntity.ok(ApiResponse.success(a));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }

    //用户删除自己的一篇文章
    @DeleteMapping("/{aid}")
    public ResponseEntity<ApiResponse<Boolean>> deleteArticleByAid(@PathVariable Long aid){

        if(articleService.deleteArticleByAid(aid)){
            return ResponseEntity.ok(ApiResponse.success(true));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }


}
