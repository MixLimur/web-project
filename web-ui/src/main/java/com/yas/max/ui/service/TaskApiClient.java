package com.yas.max.ui.service;

import com.yas.max.dto.TaskHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

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

    public TaskHistoryDto getTaskStatus(String taskId) {
        return restClient.get()
                .uri("/api/tasks/" + taskId)
                .retrieve()
                .body(TaskHistoryDto.class);
    }

    public List<?> getUserTasksHistory(String identifier) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/tasks/history")
                        .queryParam("identifier", identifier)
                        .build())
                .retrieve()
                .body(List.class);
    }

    public void cancelTask(String taskId, String identifier) {
        restClient.post()
                .uri("/api/tasks/" + taskId + "/cancel?identifier=" + identifier)
                .retrieve()
                .toBodilessEntity();
    }


}
