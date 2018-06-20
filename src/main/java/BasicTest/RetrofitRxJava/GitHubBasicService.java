package BasicTest.RetrofitRxJava;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitHubBasicService {

    private GitHubBasicApi gitHubApi;

    public GitHubBasicService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        gitHubApi = retrofit.create(GitHubBasicApi.class);
    }

    List<String> getTopContributors(String userName) throws IOException {
        List<Repository> repos = gitHubApi
                .listRepos(userName)
                .execute()
                .body();

        repos = repos != null ? repos : Collections.emptyList();
        System.out.println("repos: " + repos);

        return repos.stream()
                .flatMap(repo -> getContributors(userName, repo))
                .sorted((a, b) -> b.getContributions() - a.getContributions())
                .map(Contributor::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private Stream<Contributor> getContributors(String userName, Repository repo) {
        List<Contributor> contributors = null;

        try {
            contributors = gitHubApi
                    .listRepoContributors(userName, repo.getName())
                    .execute()
                    .body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        contributors = contributors != null ? contributors : Collections.emptyList();
//        System.out.println("contributors: " + contributors);

        return contributors.stream()
                .filter(c -> c.getContributions() > 100);
    }

    class Repository implements Serializable {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    class Contributor implements Serializable {
        String name;
        int contributions;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getContributions() {
            return contributions;
        }

        public void setContributions(int contributions) {
            this.contributions = contributions;
        }

        @Override
        public String toString() {
            return this.name + " Repositories contributions = " + this.contributions;
        }
    }
}
