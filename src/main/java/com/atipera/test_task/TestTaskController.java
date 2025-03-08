package com.atipera.test_task;

import com.atipera.test_task.records.UsersRepo;
import io.smallrye.mutiny.Uni;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.Flow;

@RestController
public class TestTaskController {
    private final TestTaskService testTaskService;

    public TestTaskController(TestTaskService testTaskService) {
        this.testTaskService = testTaskService;
    }

    @GetMapping(value = "/repos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Uni<List<UsersRepo>> GetUsersRepos(@RequestParam String userId) {
        Flow.Publisher<List<UsersRepo>> monoAsPublisher = AdaptersToFlow.publisher(testTaskService.getUsersRepos(userId));
        return Uni.createFrom().publisher(monoAsPublisher);
    }

    @ExceptionHandler({ApiException.class})
    public ResponseEntity<ExceptionRes> exceptionHandler(ApiException ex) {
        ExceptionRes exceptionRes = new ExceptionRes(ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(404).body(exceptionRes);
    }
}