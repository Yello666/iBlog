package yellow.iblog.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;

import yellow.iblog.model.Comment;
import yellow.iblog.service.CommentServiceImpl;

import java.util.List;
@Slf4j
@RestController
//@RequestMapping("/comments")
public class CommentC {

    private final CommentServiceImpl commentService;

    public CommentC(CommentServiceImpl commentService) {
        this.commentService = commentService;
    }

    //取消点赞
    @PostMapping("/comments/unlike")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> UnLikeCommentByCid(@RequestParam Long cid){
        Integer deltaLikes=commentService.UnLikeComment(cid);
        if(deltaLikes<=0){
            log.error("取消点赞失败,cid:{}",cid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("取消点赞失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(deltaLikes));
    }

    //点赞评论
    @PostMapping("/comments/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> LikeCommentByCid(@RequestParam Long cid){
        Integer deltaLikes=commentService.LikeComment(cid);
        if(deltaLikes<=0){
            log.error("点赞失败,cid:{}",cid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("点赞失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(deltaLikes));

    }
    //获取单条评论
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<Comment>> GetCommentByCid(@RequestParam Long cid){
        Comment c=commentService.getCommentByCid(cid);
        if(c==null){
            log.error("获取评论失败,cid:{}",cid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error:获取评论失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(c));

    }
    /**
     * 发布评论
     */
    @PostMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Comment>> publishComment(@RequestBody Comment comment) {
        Comment saved = commentService.publishComment(comment);
        if(saved!=null){
            log.info("用户{}评论了文章{}",saved.getUid(),saved.getAid());
            return ResponseEntity.ok(ApiResponse.success(comment));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error:评论失败"));

    }

    /**
     * 删除评论
     * @param cid 评论id
     * @param uid 要删除的评论的所属用户的id
     */
    @DeleteMapping("/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> deleteComment(
            @RequestParam Long cid, @RequestParam Long uid) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        Long crtUid=Long.valueOf(authentication.getName());
        if(crtUid.equals(uid)){
            Boolean deleted = commentService.deleteCommentByCid(cid);
            if(deleted){
                return ResponseEntity.ok(ApiResponse.success(true));
            } else{
                return ResponseEntity.badRequest().body(ApiResponse.fail(400,"评论已经不存在"));
            }
        }
        throw new AccessDeniedException("权限不足，用户"+crtUid+"试图删除评论"+cid);

    }
    //管理员删除评论
    @DeleteMapping("/admin/comments")
    public ResponseEntity<ApiResponse<Boolean>> adminDeleteComment(
            @RequestParam Long cid, @RequestParam Long uid) {
        Boolean deleted = commentService.deleteCommentByCid(cid);
        if(deleted){
            log.info("管理员{}删除了评论{}",uid,cid);
            return ResponseEntity.ok(ApiResponse.success(true));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
    }

    /**
     * 回复评论
     * @param cid 被回复的评论id
     */
    @PostMapping("/comments/reply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Comment>> replyComment(
            @RequestParam Long cid, @RequestBody Comment reply) {
        Comment savedReply = commentService.replyCommentByCid(cid, reply);
        if(savedReply!=null){
            log.info("用户{}回复了文章{}的评论{}",savedReply.getUid(),savedReply.getAid(),savedReply.getParentCid());
            return ResponseEntity.ok(ApiResponse.success(savedReply));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
    }

    /**
     * 获取某篇文章的所有评论（分页 + 时间排序）
     * @param aid 文章id
     * @param page 页码（默认 1）
     * @param size 每页大小（默认 10）
     */
    @GetMapping("/comments/article")
    public ResponseEntity<ApiResponse<Page<Comment>>> getCommentsByArticle(
            @RequestParam Long aid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Comment> comments = commentService.getCommentsByAid(aid, page, size);
        if(comments!=null){

            return ResponseEntity.ok(ApiResponse.success(comments));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
    }

    /**
     * 获取某个评论的所有直接回复
     * @param cid 评论id
     */
    @GetMapping("/comments/replies")
    public ResponseEntity<ApiResponse<List<Comment>>> getReplies(
            @RequestParam Long cid) {
        List<Comment> replies = commentService.getAllRepliesByCid(cid);
        if(replies!=null){
            return ResponseEntity.ok(ApiResponse.success(replies));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
    }
}
