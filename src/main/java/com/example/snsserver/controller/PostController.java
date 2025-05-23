package com.example.snsserver.controller;

import com.example.snsserver.dto.auth.request.PageRequestDto;
import com.example.snsserver.dto.auth.response.PageResponseDto;
import com.example.snsserver.dto.post.request.PostRequestDto;
import com.example.snsserver.dto.post.request.SearchPostRequestDto;
import com.example.snsserver.dto.post.request.SearchPostByTagRequestDto;
import com.example.snsserver.dto.post.response.PostResponseDto;
import com.example.snsserver.dto.post.response.SearchPostResponseDto;
import com.example.snsserver.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "게시물 관련 API")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "게시물 생성 (파일 포함)", description = "JSON 형식의 게시물 데이터와 함께 선택적으로 파일을 업로드하여 새 게시물을 생성합니다.")
    public ResponseEntity<PostResponseDto> createPost(
            @RequestPart(name = "post") @Valid PostRequestDto requestDto,
            @RequestPart(name = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(postService.createPost(requestDto, file));
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시물 생성 (JSON 전용)",
            description = "JSON 데이터로만 게시물을 생성합니다 (Swagger 테스트용).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시물 생성 성공", content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    public ResponseEntity<PostResponseDto> createPostJson(
            @RequestBody @Valid
            @Schema(description = "게시물 데이터", example = "{\"title\": \"Test Title\", \"content\": \"Test Content\", \"tags\": [\"tag1\", \"tag2\"]}")
            PostRequestDto requestDto) {
        log.info("Received createPostJson request - title: {}", requestDto.getTitle());
        PostResponseDto responseDto = postService.createPost(requestDto, null);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    @Operation(
            summary = "모든 게시물 조회",
            description = "모든 게시물을 페이지 단위로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시물 목록 반환", content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
            }
    )
    public ResponseEntity<PageResponseDto<PostResponseDto>> getAllPosts(
            @Parameter(description = "페이지 요청 정보 (page, size)", required = true)
            @ModelAttribute PageRequestDto pageRequestDto) {
        return ResponseEntity.ok(postService.getAllPosts(pageRequestDto));
    }

    @GetMapping("/{postId}")
    @Operation(
            summary = "특정 게시물 조회",
            description = "특정 게시물을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시물 반환", content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "게시물 없음")
            }
    )
    public ResponseEntity<PostResponseDto> getPost(
            @Parameter(description = "조회할 게시물 ID", required = true, example = "1")
            @PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @GetMapping("/search")
    @Operation(
            summary = "타이틀 검색",
            description = "게시물 타이틀로 검색합니다. 페이지네이션 지원 (기본값: page=0, size=10).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 결과 반환", content = @Content(schema = @Schema(implementation = SearchPostResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
            }
    )
    public ResponseEntity<SearchPostResponseDto> searchPosts(
            @Valid @ModelAttribute SearchPostRequestDto requestDto) {
        return ResponseEntity.ok(postService.searchPosts(requestDto));
    }

    @GetMapping("/search-tag")
    @Operation(
            summary = "태그 검색",
            description = "태그로 게시물을 검색합니다. 페이지네이션 지원 (기본값: page=0, size=10).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 결과 반환", content = @Content(schema = @Schema(implementation = SearchPostResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
            }
    )
    public ResponseEntity<SearchPostResponseDto> searchPostsByTag(
            @Valid @ModelAttribute SearchPostByTagRequestDto requestDto) {
        return ResponseEntity.ok(postService.searchPostsByTag(requestDto));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시물 수정 (파일 포함)",
            description = "특정 게시물을 JSON 데이터와 선택적으로 파일을 사용하여 수정합니다. (postman 테스트용).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시물 수정 성공", content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "게시물 없음")
            }
    )
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestPart(name = "post", required = true)
            @Valid
            @Schema(description = "수정할 게시물 데이터 (JSON)", example = "{\"title\": \"Updated Title\", \"content\": \"Updated Content\", \"tags\": [\"tag1\", \"tag2\"]}", implementation = PostRequestDto.class, contentMediaType = "application/json")
            PostRequestDto requestDto,
            @RequestPart(name = "file", required = false)
            @Schema(description = "업로드할 파일 (선택적)", type = "string", format = "binary")
            MultipartFile file) throws IOException {
        return ResponseEntity.ok(postService.updatePost(postId, requestDto, file));
    }

    @PutMapping(value = "/{postId}/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시물 수정 (JSON 전용)",
            description = "JSON 데이터로만 특정 게시물을 수정합니다 (Swagger 테스트용).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시물 수정 성공", content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "게시물 없음")
            }
    )
    public ResponseEntity<PostResponseDto> updatePostJson(
            @PathVariable Long postId,
            @RequestBody @Valid
            @Schema(description = "수정할 게시물 데이터", example = "{\"title\": \"Updated Title\", \"content\": \"Updated Content\", \"tags\": [\"tag1\", \"tag2\"]}")
            PostRequestDto requestDto) {
        log.info("Received updatePostJson request - postId: {}, title: {}", postId, requestDto.getTitle());
        return ResponseEntity.ok(postService.updatePost(postId, requestDto, null));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시물 삭제",
            description = "특정 게시물을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "게시물 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "게시물 없음")
            }
    )
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "삭제할 게시물 ID", required = true, example = "1")
            @PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}