package yellow.iblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;

import yellow.iblog.model.Article;

import yellow.iblog.service.ArticleServiceImpl;

import java.util.List;

@Slf4j
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
            log.info("用户{}发布了一篇文章",a.getUid());
            return ResponseEntity.ok(ApiResponse.success(a));
        }
        //出错了一般不会返回这个，而是会被全局异常捕获器捕。
        // 只有业务出错但是代码没有错的时候才会返回
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }


    //用户查看某一篇文章
    //获取单条数据不需要分页，获取多条数据需要分页
    @GetMapping("/{aid}")
    public ResponseEntity<ApiResponse<Article>> getArticleByAid(@PathVariable Long aid) {
        Article a=articleService.getArticleByAid(aid);
        if(a!=null){
            log.info("用户{}查看了文章{}",a.getUid(),a.getAid());
            return ResponseEntity.ok(ApiResponse.success(a));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }

    //用户查看某个用户的所有文章列表
    //要做分页
    @GetMapping("/user/{uid}")
    public ResponseEntity<ApiResponse<Page<Article>>> getArticleByUid(
            @PathVariable Long uid,
            @RequestParam(defaultValue ="1") int page,
            @RequestParam(defaultValue ="10") int size) {
        Page<Article> articleList=articleService.getArticleByUid(uid,page,size);
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
    @DeleteMapping("/{aid}/user/{uid}")
    public ResponseEntity<ApiResponse<Boolean>> deleteArticleByAid(@PathVariable Long aid,Long uid){

        if(articleService.deleteArticleByAid(aid)){
            log.info("用户{}删除了文章{}",uid,aid);
            return ResponseEntity.ok(ApiResponse.success(true));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }


}
