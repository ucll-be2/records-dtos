package be.ucll.backend2.records_dtos.controller;

import be.ucll.backend2.records_dtos.controller.dto.CreatePostDto;
import be.ucll.backend2.records_dtos.controller.dto.ShortPostDto;
import be.ucll.backend2.records_dtos.exception.PostNotFoundException;
import be.ucll.backend2.records_dtos.model.Post;
import be.ucll.backend2.records_dtos.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Returnt lijst van ShortPostDto
     */
    @GetMapping
    public List<ShortPostDto> getAllPosts() {
        return postService.getAllPosts();
    }

    /**
     * Returnt volledige Post entity
     */
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable long id) throws PostNotFoundException {
        return postService.getPostWithId(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    /**
     * Request body is CreatePostDto
     * Response body is volledige Post entity
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post addPost(@RequestBody CreatePostDto post) {
        return postService.addPost(toPost(post));
    }

    private Post toPost(CreatePostDto createPostDto) {
        return new Post(createPostDto.title(), createPostDto.content());
    }

    @ExceptionHandler({PostNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private Map<String, String> handlePostNotFoundException(PostNotFoundException e) {
        return Map.of("message", e.getMessage());
    }
}
