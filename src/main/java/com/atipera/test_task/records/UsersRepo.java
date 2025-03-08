package com.atipera.test_task.records;

import java.util.List;

public record UsersRepo(String repositoryName, String ownerLogin, List<Branch> branches) {}