package BasicTest.RetrofitRxJava;

import java.io.IOException;

/**
 * Integrating Retrofit with RxJava
 *
 * http://www.baeldung.com/retrofit-rxjava
 */
public class RetrofitWithRxJava {
    public static void main(String[] args) {
        GitHubBasicService gitHubService = new GitHubBasicService();

        try {
            System.out.println(gitHubService.getTopContributors("Google"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
