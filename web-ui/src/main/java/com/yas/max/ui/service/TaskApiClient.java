package com.yas.max.ui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class TaskApiClient {

    private final RestClient restClient;

    public String uploadTask(Resource fileResource, String identifier, boolean isRegistered) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("identifier", identifier);
        body.add("isRegistered", isRegistered);

        return restClient.post()
                .uri("/api/tasks/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(String.class);
    }

    public String getTaskStatus(String taskId) {
        return restClient.get()
                .uri("/api/tasks/" + taskId)
                .retrieve()
                .body(String.class);
    }
}
