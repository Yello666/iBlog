package yellow.iblog.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;
import yellow.iblog.model.TagRequest;
import yellow.iblog.model.Article;
import yellow.iblog.model.Tag;
import yellow.iblog.service.TARelationService;
import yellow.iblog.service.TagService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TagC {
    private final TagService tagService;
    private final TARelationService relationService;

    //创建tag
    @PostMapping("/tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Tag>> CreateTag(@RequestParam String tagName){
        Tag savedTag=tagService.createTag(tagName);
        if(savedTag==null){
            return ResponseEntity.internalServerError().body(ApiResponse.fail("创建tag失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(savedTag));

    }

    //获取单个tag信息
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<Tag>> GetTag(@RequestParam String tagName){
        Tag savedTag=tagService.getTagByTagName(tagName);
        if(savedTag==null){
            return ResponseEntity.internalServerError().body(ApiResponse.fail("查找tag失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(savedTag));

    }
    //将tag列表添加到文章
    @PostMapping("/tags/article")
    public ResponseEntity<ApiResponse<Integer>> AddTagsToArticle(@RequestBody TagRequest request){
        Integer successCount=relationService.addTagsToArticle(request.getTags(),request.getAid());
        if(successCount<=0){
            return ResponseEntity.badRequest().body(ApiResponse.fail("添加tags失败"));
        }
        if(successCount<request.getTags().size()){
            return ResponseEntity.ok(ApiResponse.fail("添加tags部分成功"));
        }
        return ResponseEntity.ok(ApiResponse.success(successCount));

    }
    //获取一个文章下面的所有tag
    @GetMapping("/tags/article")
    public ResponseEntity<ApiResponse<List<Tag>>> GetTagsOfArticle(@RequestParam Long aid){
        List<Tag> tags=relationService.getAllTagsOfOneArticle(aid);
        if(tags==null){
            return ResponseEntity.internalServerError().body(ApiResponse.fail("查找tags失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(tags));

    }
    //将某个tag移除出某个文章
    @DeleteMapping("/tags/article")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> RemoveTagFromArticle(
            @RequestBody TagRequest request) {
        Integer successCount = relationService.deleteTagsFromArticle(request.getTags(),request.getAid());
        if(successCount<=0){
            return ResponseEntity.internalServerError().body(ApiResponse.fail("删除tags失败"));
        }
        if(successCount<request.getTags().size()){
            return ResponseEntity.ok(ApiResponse.fail("删除tags部分成功"));
        }
        return ResponseEntity.ok(ApiResponse.success(successCount));

    }

    //TODO要做分页，到时候再看，搜索功能怎么做。
    //获取包含tag的所有文章
    @GetMapping("/article/tags")
    public ResponseEntity<ApiResponse<List<Article>>> GetArticlesIncludeTag(@RequestParam String tagName){
        List<Article> Articles=relationService.getArticlesIncludeTag(tagName);
        if(Articles==null){
            return ResponseEntity.internalServerError().body(ApiResponse.fail("查找包含tag的文章失败"));
        }
        return ResponseEntity.ok(ApiResponse.success(Articles));
    }





}
