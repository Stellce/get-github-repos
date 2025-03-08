package com.atipera.test_task;

import com.atipera.test_task.records.Branch;
import com.atipera.test_task.records.GitHubBranchResponse;
import com.atipera.test_task.records.GitHubRepositoryResponse;
import com.atipera.test_task.records.UsersRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class TestTaskService {
	@Value("${github.api.url}")
	private final String gitHubApiUrl;
	private final WebClient webClient;

	public TestTaskService(@Value("${github.api.url}") String gitHubApiUrl, WebClient.Builder builder) {
		this.webClient = builder.baseUrl("http://localhost:8080").build();
		this.gitHubApiUrl = gitHubApiUrl;
	}

	public Mono<List<UsersRepo>> getUsersRepos(String userId) {
		return getRepos(userId)
			.flatMapMany(Flux::fromArray)
			.filter(gitHubRepo -> !gitHubRepo.fork())
			.flatMap(gitHubRepo -> getBranches(userId, gitHubRepo.name()).map(gitHubBranches -> {
				List<Branch> branches = Arrays.stream(gitHubBranches)
					.map(gitHubBranch -> new Branch(gitHubBranch.name(), gitHubBranch.commit().sha())).toList();
				return new UsersRepo(gitHubRepo.name(), gitHubRepo.owner().login(), branches);
			}))
			.collectList();
	}

	private Mono<GitHubRepositoryResponse[]> getRepos(String userId) {
		final String url = gitHubApiUrl + "/users/" + userId + "/repos";
		return webClient.get()
			.uri(url).accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, response -> {
				String message = "Error occurred";
				if (response.statusCode() == HttpStatus.NOT_FOUND) {
					message = "User does not exist";
				}
				return Mono.error(new ApiException(response.statusCode().value(), message));
			})
			.toEntity(GitHubRepositoryResponse[].class)
			.mapNotNull(HttpEntity::getBody);
	}

	private Mono<GitHubBranchResponse[]> getBranches(String userId, String repoId) {
		String url = gitHubApiUrl + "/repos/" + userId + "/" + repoId + "/branches";
		return webClient.get()
			.uri(url).accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.bodyToMono(GitHubBranchResponse[].class);
	}
}
