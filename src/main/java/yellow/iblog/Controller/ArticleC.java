package yellow.iblog.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;

import yellow.iblog.model.Article;

import yellow.iblog.model.ArticleResponse;
import yellow.iblog.service.ArticleServiceImpl;
import yellow.iblog.service.FavorService;
import yellow.iblog.service.LikeService;
import yellow.iblog.service.LikeSyncService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
//@RequestMapping("/article")
public class ArticleC {
    private final ArticleServiceImpl articleService;
    private final LikeSyncService likeSyncService;
    private final LikeService likeService;
    private final FavorService favorService;

//    //取消收藏
//    @PostMapping("/article/undoFavor")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<ApiResponse<Integer>> undoArticleFavor(
//            @RequestParam Long aid,
//            @RequestParam Long uid){
////        Integer deltaLikes=articleService.likeArticleByAid(aid);
//        Integer deltaFavors=articleService.undoArticleFavor(aid,uid);
//        if(deltaFavors<=0){
//            log.error("{}取消收藏文章失败,aid:{}",uid,aid);
//            return ResponseEntity.internalServerError().body(ApiResponse.fail("error：取消收藏文章失败"));
//        }
//        log.info("文章{}被{}取消收藏了",aid,uid);
//        return ResponseEntity.ok(ApiResponse.success(deltaFavors));
//
//    }

    //取消点赞,需要改变缓存
    @PostMapping("/article/undoLike")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> undoArticleLike(
            @RequestParam Long aid,
            @RequestParam Long uid){
//        Integer deltaLikes=articleService.likeArticleByAid(aid);
        Integer res;
        res = articleService.undoArticleLike(aid,uid);
        if(res<=0){
            log.error("{}取消点赞文章失败,aid:{}",uid,aid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error：取消点赞文章失败"));
        }
        log.info("文章{}被{}取消点赞了",aid,uid);
        return ResponseEntity.ok(ApiResponse.success(res));

    }


    //点赞文章
    @PostMapping("/article/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> likeArticleByAid(
            @RequestParam Long aid,
            @RequestParam Long uid){
        Integer deltaLikes=articleService.likeArticleByAid(aid,uid);
        if(deltaLikes<=0){
            log.error("{}点赞文章失败,aid:{}",uid,aid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error：点赞文章失败"));
        }
        log.info("文章{}被{}点赞了",aid,uid);
        return ResponseEntity.ok(ApiResponse.success(deltaLikes));

    }
    //用户收藏文章
    // 参数是收藏的文章，收藏的执行人
    @PostMapping("/article/favor")
    public ResponseEntity<ApiResponse<Integer>> favorArticleByAid(
            @RequestParam Long aid,
            @RequestParam Long uid){
        Integer deltaFavors=articleService.favorArticleByAid(aid,uid);
        if(deltaFavors<=0){
            log.error("{}收藏文章失败,aid:{}",uid,aid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error：收藏文章失败"));
        }
        log.info("文章{}被{}收藏了",aid,uid);
        return ResponseEntity.ok(ApiResponse.success(deltaFavors));
    }
    //用户取消收藏文章
    // 参数是取消收藏的文章，取消收藏的执行人
    @PostMapping("/article/undoFavor")
    public ResponseEntity<ApiResponse<Integer>> unFavorArticleByAid(
            @RequestParam Long aid,
            @RequestParam Long uid){
        Integer deltaFavors=articleService.undoArticleFavor(aid,uid);
        if(deltaFavors<=0){
            log.error("{}收藏文章失败,aid:{}",uid,aid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error：收藏文章失败"));
        }
        log.info("文章{}被{}收藏了",aid,uid);
        return ResponseEntity.ok(ApiResponse.success(deltaFavors));
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
    //需要联系uid来看它有没有点赞
    @GetMapping("/article/{aid}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticleByAid(
            @PathVariable Long aid,
            @RequestParam Long uid) {
        log.info("调用了查看文章controller");
        Article a=articleService.getArticleByAid(aid);
        ArticleResponse response;
        if(a!=null){
            log.info("用户{}的文章{}被查看了",a.getUid(),a.getAid());
            response=new ArticleResponse(a);
            if(uid!=null){
                //去查redis有没有点赞
                response.setLiked(likeService.getArticleIsLiked(aid,uid));
                response.setFavored(favorService.getArticleIsFavored(aid,uid));
                log.info("isliked:{},isFavored:{}",response.isLiked(),response.isFavored());
            }
            else{
                log.info("未登陆用户查看文章");
                response.setLiked(false);
                response.setFavored(false);
            }
            return ResponseEntity.ok(ApiResponse.success(response));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }



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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Article>> updateArticle(
            @RequestParam Long aid,
            @RequestBody Article article){
        //这个article一定要包含uid
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        Long crtUid=Long.valueOf(authentication.getName());
        if(!crtUid.equals(article.getUid())){
            log.warn("用户{}想要修改其它人{}的文章,aid:{}", crtUid,article.getUid(), aid);
            throw new AccessDeniedException("用户"+crtUid+"想要修改其它人的文章,aid:"+aid);
        }
        article.setAid(aid);
        Article a=articleService.updateArticle(article);
        if(a!=null){
            log.info("用户{}修改了自己的文章",crtUid);
            return ResponseEntity.ok(ApiResponse.success(a));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }

    //用户删除自己的一篇文章
    @DeleteMapping("/article")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> deleteArticleByAid(
            @RequestParam Long aid,
            @RequestParam Long uid){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        Long crtUid=Long.valueOf(authentication.getName());
        if(!crtUid.equals(uid)){
            log.warn("用户{}想要删除其它人的文章,aid:{}", crtUid, aid);
            throw new AccessDeniedException("用户"+crtUid+"想要删除其它人的文章,aid:"+aid);
        }
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

    //获取n篇文章点赞数最高的文章：用于前端展示
    @GetMapping("/article/hot")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getArticleListOrderedByLikes(@RequestParam Integer num){
        List<Article> articles=articleService.getArticleListOrderedByLikes(num);
        if(articles!=null){
            log.info("返回了{}篇热门文章",num);
            List<ArticleResponse> articleList=new ArrayList<>();
            for (Article article : articles) {
                ArticleResponse r = new ArticleResponse(article);
                articleList.add(r);
            }
            return ResponseEntity.ok(ApiResponse.success(articleList));//返回的uid是字符串
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }


}
