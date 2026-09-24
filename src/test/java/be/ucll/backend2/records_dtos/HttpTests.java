package be.ucll.backend2.records_dtos;

import be.ucll.backend2.records_dtos.controller.dto.CreatePostDto;
import be.ucll.backend2.records_dtos.repository.DbInitializer;
import be.ucll.backend2.records_dtos.repository.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Sql("classpath:schema.sql")
public class HttpTests {
    @Autowired
    private RestTestClient client;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private DbInitializer dbInitializer;

    @BeforeEach
    public void setUp() {
        dbInitializer.initialize();
    }

    @Test
    public void given2PostsInDb_whenInvokingGetPost_then2PostsAreReturned() {
        client.get()
                .uri("/api/v1/posts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(
                        """
                        [
                          {
                            "id": 1,
                            "title": "Welcome to my blog!"
                          },
                          {
                            "id": 2,
                            "title": "What makes cats such great pets?"
                          }
                        ]
                        """,
                        JsonCompareMode.STRICT
                );
    }

    @Test
    public void given2PostsInDb_whenInvokingGetPostById_then1PostIsReturned() {
        client.get()
                .uri("/api/v1/posts/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(
                        """
                        {
                          "id": 1,
                          "title": "Welcome to my blog!",
                          "content": "I hope you enjoy it!"
                        }
                        """,
                        JsonCompareMode.STRICT
                );
    }

    @Test
    public void given2PostsInDb_whenInvokingPostPost_thenPostIsAddedToDb() {
        client.post()
                .uri("/api/v1/posts")
                .body(new CreatePostDto("Post title", "Post content"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .json(
                        """
                        {
                          "id": 3,
                          "title": "Post title",
                          "content": "Post content"
                        }
                        """,
                        JsonCompareMode.STRICT
                );

        // Test whether the post is actually saved in the database
        final var post = postRepository.findById(3L);
        Assertions.assertTrue(post.isPresent());
        Assertions.assertEquals(3L, post.get().getId());
        Assertions.assertEquals("Post title", post.get().getTitle());
        Assertions.assertEquals("Post content", post.get().getContent());
    }
}
