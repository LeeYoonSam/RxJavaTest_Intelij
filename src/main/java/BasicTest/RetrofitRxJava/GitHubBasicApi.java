package BasicTest.RetrofitRxJava;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface GitHubBasicApi {
    // Intergrating with RxJava
    @GET("users/{user}/repos")
    Observable<List<GitHubBasicService.Repository>> listRepos(@Path("user") String user);

    @GET("repos/{user}/{repo}/contributors")
    Observable<List<GitHubBasicService.Contributor>> listRepoContributors(
            @Path("user") String user,
            @Path("repo") String repo);



//    @GET("users/{user}/repos")
//    Call<List<GitHubBasicService.Repository>> listRepos(@Path("user") String user);
//
//    @GET("repos/{user}/{repo}/contributors")
//    Call<List<GitHubBasicService.Contributor>> listRepoContributors(
//            @Path("user") String user,
//            @Path("repo") String repo);
}
