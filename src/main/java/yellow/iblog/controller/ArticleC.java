package yellow.iblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;

import yellow.iblog.model.Article;

import yellow.iblog.service.ArticleServiceImpl;

@Slf4j
@RestController
//@RequestMapping("/article")
public class ArticleC {
    private final ArticleServiceImpl articleService;

    public ArticleC(ArticleServiceImpl articleService) {
        this.articleService = articleService;
    }


    //用户写文章
    @PostMapping("/article")
    @PreAuthorize("isAuthenticated()")
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
    @GetMapping("/article/{aid}")
    public ResponseEntity<ApiResponse<Article>> getArticleByAid(@PathVariable Long aid) {
        Article a=articleService.getArticleByAid(aid);
        if(a!=null){
            log.info("用户{}的文章{}被查看了",a.getUid(),a.getAid());
            return ResponseEntity.ok(ApiResponse.success(a));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }

    //用户查看某个用户的所有文章列表
    //要做分页
    @GetMapping("/article/user/{uid}")
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
    @PutMapping("/article")
    @PreAuthorize("isAuthenticated() and #article.uid==authentication.name")
    public ResponseEntity<ApiResponse<Article>> updateArticle(
            @RequestParam Long aid,
            @RequestBody Article article){
        article.setAid(aid);
        Article a=articleService.updateArticle(article);
        if(a!=null){
            return ResponseEntity.ok(ApiResponse.success(a));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }

    //用户删除自己的一篇文章
    @DeleteMapping("/article")
    @PreAuthorize("isAuthenticated() and #uid==authentication.name")
    public ResponseEntity<ApiResponse<Boolean>> deleteArticleByAid(
            @RequestParam Long aid,
            @RequestParam Long uid){

        if(articleService.deleteArticleByAid(aid)){
            log.info("用户{}删除了文章{}",uid,aid);
            return ResponseEntity.ok(ApiResponse.success(true));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }
    //管理员删除一篇文章
    //TODO可以设置成用户不可见
    @DeleteMapping("/admin/article")
    public ResponseEntity<ApiResponse<Boolean>> adminDeleteArticleByAid(
            @RequestParam Long aid){
        // 从 SecurityContext 获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long uid = Long.valueOf(authentication.getName()); // 获取用户ID
        if(articleService.deleteArticleByAid(aid)){

            log.info("管理员{}删除了文章{}",uid,aid);
            return ResponseEntity.ok(ApiResponse.success(true));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }

}
