package yellow.iblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;
import yellow.iblog.Common.PageResult;
import yellow.iblog.model.Comment;
import yellow.iblog.service.CommentService;
import yellow.iblog.service.CommentServiceImpl;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/comments")
public class CommentC {

    private final CommentServiceImpl commentService;

    public CommentC(CommentServiceImpl commentService) {
        this.commentService = commentService;
    }

    /**
     * 发布评论
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Comment>> publishComment(@RequestBody Comment comment) {
        Comment saved = commentService.publishComment(comment);
        if(saved!=null){
            log.info("用户{}评论了文章{}",saved.getUid(),saved.getAid());
            return ResponseEntity.ok(ApiResponse.success(comment));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));

    }

    /**
     * 删除评论
     * @param cid 评论id
     * @param uid 用户id
     */
    @DeleteMapping("/{cid}")
    public ResponseEntity<ApiResponse<Boolean>> deleteComment(@PathVariable Long cid, @RequestParam Long uid) {
        Boolean deleted = commentService.deleteCommentByCidAndUid(cid, uid);
        if(deleted){
            return ResponseEntity.ok(ApiResponse.success(true));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
    }

    /**
     * 回复评论
     * @param cid 被回复的评论id
     */
    @PostMapping("/{cid}/reply")
    public ResponseEntity<ApiResponse<Comment>> replyComment(@PathVariable Long cid, @RequestBody Comment reply) {
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
    @GetMapping("/article/{aid}")
    public ResponseEntity<ApiResponse<Page<Comment>>> getCommentsByArticle(
            @PathVariable Long aid,
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
    @GetMapping("/{cid}/replies")
    public ResponseEntity<ApiResponse<List<Comment>>> getReplies(@PathVariable Long cid) {
        List<Comment> replies = commentService.getAllRepliesByCid(cid);
        if(replies!=null){
            return ResponseEntity.ok(ApiResponse.success(replies));
        }
        return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
    }
}
