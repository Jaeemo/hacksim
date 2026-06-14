package com.capstone.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posture")
public class PostureController {

    private final PostureService postureService;

    public PostureController(PostureService postureService) {
        this.postureService = postureService;
    }

    @PostMapping
    public PostureDtos.ReportResponse submit(@RequestBody PostureDtos.ReportRequest request) {
        return postureService.ingest(request);
    }

    @GetMapping
    public List<PostureDtos.ReportResponse> recent() {
        return postureService.recent();
    }
}
