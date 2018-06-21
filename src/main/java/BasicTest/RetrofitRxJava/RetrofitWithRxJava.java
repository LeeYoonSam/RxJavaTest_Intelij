package BasicTest.RetrofitRxJava;

/**
 * Integrating Retrofit with RxJava
 *
 * http://www.baeldung.com/retrofit-rxjava
 */
public class RetrofitWithRxJava {
    public static void main(String[] args) {
        GitHubBasicService gitHubService = new GitHubBasicService();

        // Intergrating with RxJava
        System.out.println(gitHubService.getTopContributors("Google"));

//        try {
//            System.out.println(gitHubService.getTopContributors("Google"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
