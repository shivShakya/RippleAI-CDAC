package com.ripple.post_service.controller;

import com.ripple.post_service.kafka.PostProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostProducer producer;

    public PostController(PostProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/create")
    public String createPost(@RequestParam String content) {
        producer.sendPost(content);
        return "Post Created Successfully ✅";
    }
}
