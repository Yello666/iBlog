package yellow.iblog;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yellow.iblog.model.Comment;
import yellow.iblog.mapper.CommentMapper;
import yellow.iblog.service.CommentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentServiceImplTest {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentService commentService;



    @Test
    void testPublishComment() {
        Comment c = new Comment();
        c.setAid(1L);
        c.setUid(100L);
        c.setContents("测试评论");

        Comment saved = commentService.publishComment(c);

        assertNotNull(saved.getCid());
        assertEquals("测试评论", saved.getContents());
    }

    @Test
    void testDeleteCommentByCidAndUid() {
        Comment c = new Comment();
        c.setAid(1L);
        c.setUid(100L);
        c.setContents("待删除评论");
        commentMapper.insert(c);

        Boolean deleted = commentService.deleteCommentByCid(c.getCid());
        assertTrue(deleted);

        // 验证数据库里已经不存在
        Comment check = commentMapper.selectById(c.getCid());
        assertNull(check);
    }

    @Test
    void testReplyCommentByCid() {
        Comment parent = new Comment();
        parent.setAid(1L);
        parent.setUid(100L);
        parent.setContents("父评论");
        commentMapper.insert(parent);

        Comment reply = new Comment();
        reply.setAid(1L);
        reply.setUid(101L);
        reply.setContents("回复内容");

        Comment savedReply = commentService.replyCommentByCid(parent.getCid(), reply);

        assertNotNull(savedReply.getCid());
        assertEquals(parent.getCid(), savedReply.getParentCid());
    }

    @Test
    void testGetCommentsByAid() {
        for (int i = 0; i < 15; i++) {
            Comment c = new Comment();
            c.setAid(1L);
            c.setUid(100L + i);
            c.setContents("评论 " + i);
            commentMapper.insert(c);
        }

        Page<Comment> page = commentService.getCommentsByAid(1L, 1, 10);
        assertEquals(10, page.getRecords().size());
        assertEquals(15, page.getTotal());
    }

    @Test
    void testGetAllRepliesByCid() {
        Comment parent = new Comment();
        parent.setAid(1L);
        parent.setUid(100L);
        parent.setContents("父评论");
        commentMapper.insert(parent);

        for (int i = 0; i < 3; i++) {
            Comment reply = new Comment();
            reply.setAid(1L);
            reply.setUid(101L + i);
            reply.setContents("回复 " + i);
            reply.setParentCid(parent.getCid());
            commentMapper.insert(reply);
        }

        List<Comment> replies = commentService.getAllRepliesByCid(parent.getCid());
        assertEquals(3, replies.size());
    }
}
