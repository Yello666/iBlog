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
import yellow.iblog.model.CommentLikeResponse;
import yellow.iblog.model.CommentResponse;
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

//    //取消点赞
//    @PostMapping("/comments/unlike")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<ApiResponse<Integer>> UnLikeCommentByCid(@RequestParam Long cid){
//        Integer deltaLikes=commentService.UnLikeComment(cid);
//        if(deltaLikes<=0){
//            log.error("取消点赞失败,cid:{}",cid);
//            return ResponseEntity.internalServerError().body(ApiResponse.fail("取消点赞失败"));
//        }
//        return ResponseEntity.ok(ApiResponse.success(deltaLikes));
//    }

    //点赞评论
    @PostMapping("/comments/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentLikeResponse>> LikeCommentByCid(@RequestParam Long cid){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        Long uid=Long.valueOf(authentication.getName());
        CommentLikeResponse response =commentService.LikeComment(cid,uid);

        if(response==null||response.getCrtLikes()<0){
            log.error("点赞失败,cid:{}",cid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("点赞失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(response));

    }
    //获取单条评论
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> GetCommentByCid(@RequestParam Long cid){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String watcherUidStr=authentication.getName();
        Long watcherUid;
        if(watcherUidStr!=null){
            watcherUid=Long.parseLong(watcherUidStr);//登陆了的用户（可以查看自己有没有赞过评论）
        } else{
            watcherUid=-1L;//未登陆用户
        }
        CommentResponse response=commentService.getCommentByCid(cid,watcherUid);
        if(response==null){
            log.error("获取评论失败,cid:{}",cid);
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error:获取评论失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(response));

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
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getCommentsByArticle(
            @RequestParam Long aid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 1. 打印传入的查询参数（确认aid、page、size是否正确）
        log.info("查询文章评论：aid={}, 页码={}, 每页大小={}", aid, page, size);
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String uidStr=authentication.getName();
        long watcherUid;
        if (uidStr == null || "anonymousUser".equals(uidStr) || !uidStr.matches("\\d+")) {
            watcherUid = -1L; // 用-1表示匿名用户或无效ID
            log.info("用户未登陆，查看评论");
        } else {
            try {
                watcherUid = Long.parseLong(uidStr);
                log.info("用户{}查看评论",watcherUid);
            } catch (NumberFormatException e) {
                // 兜底处理，防止意外的数字格式错误
                watcherUid = -1L;
            }
        }
        Page<CommentResponse> comments = commentService.getCommentsByAid(aid, watcherUid,page, size);
        if(comments!=null){
            // 3. 打印查询结果的关键信息（核心：总记录数、当前页记录数）
            log.info("查询结果：总记录数={}, 当前页记录数={}", comments.getTotal(), comments.getRecords().size());

            // 4. 若有记录，可打印第一条记录的ID（确认是否查询到具体数据）
            if (!comments.getRecords().isEmpty()) {
                log.info("第一条评论ID：{}", comments.getRecords().getFirst().getCid());
            } else {
                log.warn("未查询到符合条件的评论"); // 无记录时警告
            }
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
