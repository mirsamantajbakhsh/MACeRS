/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MACeRS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 *
 * @author Mir Saman
 */
public class Main {

    static int numberOfCelebs = 50;
    static int numberOfNonCelebs = 100;
    static int celebThreshold = 5;
    static int topN = 10;
    static List<TwitterUser> users = new ArrayList<TwitterUser>();
    static PayOffCell[][] payoff;
    static Map<TwitterUser, Result> results = new HashMap<>();

    public static void main(String[] args) {
        createNetwork(); //It is a simple code. You may import your own network.
        createPayoffs();
        createPayoffsForCelebs();
        results.forEach((k, v) -> System.out.println(k + "\t" + v));
    }

    private static void createNetwork() {
        createUsers();
        Random rnd = new Random();
        Random userRnd = new Random();

        //Assign follower and followings
        int follower, following;
        TwitterUser u;

        ProgressBar pb = new ProgressBar("Creating Non Celebs Network", numberOfNonCelebs, ProgressBarStyle.ASCII);
        for (int i = 0; i < numberOfNonCelebs; i++) {
            u = users.get(i);

            //+ 1 is for prevent divide by zero.
            follower = rnd.nextInt(celebThreshold - 1) + 1; //The user is not a celeb. So its followers should be less than celebThreshold.
            following = rnd.nextInt((int) (numberOfNonCelebs * 0.2d)) + 1;

            for (int j = 0; j < follower; j++) {
                u.getFollowers().add(users.get(userRnd.nextInt(numberOfNonCelebs))); //NonCelebs follows NonCelebs + Celebs
            }

            for (int j = 0; j < following; j++) {
                u.getFollowings().add(users.get(userRnd.nextInt(users.size()))); //Celebs and NonCelebs may be followed
            }
            pb.step();
        }

        pb.stop();
        System.out.println("");
        pb = new ProgressBar("Creating Celebs Network", numberOfCelebs, ProgressBarStyle.ASCII);
        for (int i = 0; i < numberOfCelebs; i++) {
            u = users.get(numberOfNonCelebs + i);
            follower = rnd.nextInt(numberOfNonCelebs - celebThreshold) + celebThreshold; //Minimum follower is 10000
            following = rnd.nextInt(99) + 1; //max following is 100 (+ 1 is for preventing divide by zero)

            for (int j = 0; j < follower; j++) {
                u.getFollowers().add(users.get(userRnd.nextInt(users.size()))); //NonCelebs follows NonCelebs
            }

            for (int j = 0; j < following; j++) {
                u.getFollowings().add(users.get(userRnd.nextInt(users.size()))); //Celebs and NonCelebs may be followed
            }
            pb.step();
        }
        pb.stop();
        System.out.println("");
    }

    private static void createUsers() {
        TwitterUser u;
        int postsCount = 0;
        int tempPostCount;
        Random rnd = new Random();

        //Creat Non Celebs
        ProgressBar pb = new ProgressBar("Creating Non Celebs", numberOfNonCelebs, ProgressBarStyle.ASCII);
        for (int i = 0; i < numberOfNonCelebs; i++) {
            u = new TwitterUser();
            tempPostCount = rnd.nextInt(126); //average user post count is 126.
            postsCount += tempPostCount;
            u.setStatusRate(tempPostCount);
            users.add(u);
            pb.step();
        }

        //Normalize status rates
        pb.stop();
        System.out.println("");
        pb = new ProgressBar("Normalizing Non Celebs", numberOfNonCelebs, ProgressBarStyle.ASCII);
        for (int i = 0; i < numberOfNonCelebs; i++) {
            u = users.get(i);
            u.setStatusRate((double) u.getStatusRate() / (double) postsCount);
            pb.step();
        }

        //Create Celebs
        postsCount = 0;
        pb.stop();
        System.out.println("");
        pb = new ProgressBar("Creating Celebs", numberOfCelebs, ProgressBarStyle.ASCII);
        for (int i = 0; i < numberOfCelebs; i++) {
            u = new TwitterUser();
            tempPostCount = rnd.nextInt(126); //average user post count is 126.
            postsCount += tempPostCount;
            u.setStatusRate(tempPostCount);
            users.add(u);
        }

        pb.stop();
        System.out.println("");
        pb = new ProgressBar("Normalizing Celebs", numberOfCelebs, ProgressBarStyle.ASCII);
        for (int i = 0; i < numberOfCelebs; i++) {
            u = users.get(numberOfNonCelebs + i);
            u.setStatusRate((double) u.getStatusRate() / (double) postsCount);
            pb.step();
        }

        pb.stop();
        System.out.println("");
    }

    public static double sim(TwitterUser u1, TwitterUser u2) {
        return u1.weights[0] * u2.getFollowerCount() + u1.weights[1] * u2.getFollowingCount() + u1.weights[2] * u2.getStatusRate();
    }

    private static void createPayoffs() {
        ProgressBar pb = new ProgressBar("Creating payoff", numberOfNonCelebs, ProgressBarStyle.ASCII);

        int k = 0;
        for (TwitterUser u : users) {
            //create payoff
            payoff = new PayOffCell[numberOfCelebs][numberOfCelebs];

            k++;
            if (k > numberOfNonCelebs) {
                break;
            }

            //if (!u.isCeleb) {
            for (int i = 0; i < numberOfCelebs; i++) {
                double AC = 0.0d;
                TwitterUser celeb1 = users.get(numberOfNonCelebs + i); //Celeb i same as T

                for (TwitterUser celeb : u.getCelebs()) {
                    AC += sim(celeb, celeb1) * celeb.getTetha();
                }

                for (int j = 0; j < numberOfCelebs; j++) {
                    TwitterUser celeb2 = users.get(numberOfNonCelebs + j); //Celeb j same as S

                    //Calculate only for unfollowed celebs
                    /*
                        while (u.getFollowings().contains(celeb1)) {
                            i++;
                            if (i < numberOfCelebs) {
                                celeb1 = users.get(numberOfNonCelebs + i);
                            }
                        }

                        while (u.getFollowings().contains(celeb2)) {
                            j++;
                            if (j < numberOfCelebs) {
                                celeb2 = users.get(numberOfNonCelebs + j);
                            }
                        }
                     */
                    //Ready for raw payoff
                    double ANC = 0.0d;

                    for (TwitterUser nonceleb : u.getNonCelebs()) {
                        ANC += sim(nonceleb, celeb2) * nonceleb.getTetha();
                    }
                    payoff[i][j] = new PayOffCell();
                    payoff[i][j].AC = AC;
                    payoff[i][j].ANC = ANC;
                    payoff[i][j].i = i;
                    payoff[i][j].j = j;

                    //more payoffs
                    double t_ac = 0.0d;
                    double t_anc = 0.0d;

                    //pi_AC
                    double count = 0.0d;
                    for (TwitterUser nc : u.getNonCelebs()) {
                        if (nc.getCelebs().contains(celeb1)) {
                            count++;
                        }
                    }

                    t_ac = (double) count / (u.getNonCelebs().size() == 0 ? 1 : u.getNonCelebs().size());

                    //pi_ANC
                    count = 0.0d;
                    for (TwitterUser c : u.getCelebs()) {
                        if (c.getCelebs().contains(celeb2)) {
                            count++;
                        }
                    }

                    t_anc = (double) count / (u.getCelebs().size() == 0 ? 1 : u.getCelebs().size());

                    payoff[i][j].AC += t_ac;
                    payoff[i][j].ANC += t_anc;
                }
            }
            pb.step();
            //}
            //System.out.println(k + " is done.");
            calculateNash(u); //for each user
            calculateBestRates(u); //for each user
            calculateCosine(u);
            calculateManhattan(u);
            calculateCosineForCelebs(u);
        }
        pb.stop();
    }

    private static void calculateNash(TwitterUser u) {
        PayOffCell c;
        List<PayOffCell> nash = new ArrayList<>();

        double numberOfCorrectSuggestions = 0.0d;

        for (int i = 0; i < numberOfCelebs; i++) {
            for (int j = 0; j < numberOfCelebs; j++) {
                c = payoff[i][j];

                if (isGoodPayoff(c, i, j)) {
                    TwitterUser c1 = users.get(numberOfNonCelebs + i);
                    TwitterUser c2 = users.get(numberOfNonCelebs + j);
                    nash.add(c);

                    if (u.getCelebs().contains(c1)) {
                        numberOfCorrectSuggestions++;
                    }

                    if (u.getCelebs().contains(c2)) {
                        numberOfCorrectSuggestions++;
                    }
                }
            }
        }

        Result r = results.getOrDefault(u, new Result());
        r.result.put("NashEquilibrium", (numberOfCorrectSuggestions / nash.size()));
        //r.result.put("NashEquilibrium", (numberOfCorrectSuggestions / numberOfCelebs));
        results.put(u, r);

        calculateSocialEquilibrium(u, nash);

    }

    private static boolean isGoodPayoff(PayOffCell c, int row, int column) {
        //check row
        for (int j = 0; j < numberOfCelebs; j++) {
            if (j == column) {
                continue;
            }

            if (c.ANC < payoff[row][j].ANC) {
                return false;
            }
        }

        //check column
        for (int i = 0; i < numberOfCelebs; i++) {
            if (i == row) {
                continue;
            }

            if (c.AC < payoff[i][column].AC) {
                return false;
            }
        }

        return true;
    }

    private static void calculateSocialEquilibrium(TwitterUser u, List<PayOffCell> nash) {
        PayOffCell c;

        double numberOfCorrectSuggestions = 0.0d;
        double numberOfSocialCells = 0.0d;

        for (int i = 0; i < numberOfCelebs; i++) {
            for (int j = 0; j < numberOfCelebs; j++) {
                c = payoff[i][j];
                TwitterUser c1 = users.get(numberOfNonCelebs + i);
                TwitterUser c2 = users.get(numberOfNonCelebs + j);

                for (PayOffCell n : nash) {
                    if ((c.AC > n.AC) && (c.ANC > n.ANC)) {
                        numberOfSocialCells++;
                        //n is Social Equilibrium
                        if (u.getCelebs().contains(c1)) {
                            numberOfCorrectSuggestions++;
                        }

                        if (u.getCelebs().contains(c2)) {
                            numberOfCorrectSuggestions++;
                        }
                    }
                }
            }
        }

        Result r = results.getOrDefault(u, new Result());
        r.result.put("SocialEquilibrium", (numberOfCorrectSuggestions / (numberOfSocialCells + 0.01d)));
        results.put(u, r);
    }

    private static void calculateBestRates(TwitterUser u) {
        PayOffCell c;
        List<PayOffCell> bestCells = new Vector<>();

        for (int i = 0; i < numberOfCelebs; i++) {
            double max = 0;
            PayOffCell maxC = null;
            int maxI = i;
            int maxJ = 0;

            for (int j = 0; j < numberOfCelebs; j++) {
                c = payoff[i][j];
                TwitterUser c1 = users.get(numberOfNonCelebs + i);
                TwitterUser c2 = users.get(numberOfNonCelebs + j);

                if (c.AC > max) {
                    maxJ = j;
                    max = c.AC;
                    maxC = c;
                }

            }

            //Max row in AC
            bestCells.add(maxC);
        }

        for (int j = 0; j < numberOfCelebs; j++) {
            double max = 0;
            PayOffCell maxC = null;
            int maxI = 0;
            int maxJ = j;

            for (int i = 0; i < numberOfCelebs; i++) {
                c = payoff[i][j];
                TwitterUser c1 = users.get(numberOfNonCelebs + i);
                TwitterUser c2 = users.get(numberOfNonCelebs + j);

                if (c.ANC > max) {
                    maxI = i;
                    max = c.ANC;
                    maxC = c;
                }

            }

            //Max coloum in AC
            bestCells.add(maxC);
        }

        //bestCells contains the best of each row
        double numberOfCorrectSuggestions = 0.0d;

        for (PayOffCell p : bestCells) {
            if (p != null && p.i < numberOfCelebs && p.j < numberOfNonCelebs) {
                TwitterUser c1 = users.get(numberOfNonCelebs + p.i);
                TwitterUser c2 = users.get(numberOfNonCelebs + p.j);

                if (u.getCelebs().contains(c1)) {
                    numberOfCorrectSuggestions++;
                }

                if (u.getCelebs().contains(c2)) {
                    numberOfCorrectSuggestions++;
                }
            }
        }
        Result r = results.getOrDefault(u, new Result());
        r.result.put("BestRate", (numberOfCorrectSuggestions / bestCells.size()));
        results.put(u, r);
    }

    private static void calculateCosine(TwitterUser u) {
        double numberOfCorrectSuggestions = 0.0d;

        Map<TwitterUser, Double> rates = new HashMap<>();

        for (int i = 0; i < numberOfCelebs; i++) {
            TwitterUser celeb = users.get(numberOfNonCelebs + i);
            rates.put(celeb, cosine(u, celeb));
        }
        rates = MapUtil.sortByValue(rates);
        Iterator<TwitterUser> it = rates.keySet().iterator();
        for (int i = 0; i < topN; i++) {
            if (u.getCelebs().contains(it.next())) {
                numberOfCorrectSuggestions++;
            }
        }
        Result r = results.getOrDefault(u, new Result());
        r.result.put("Cosine", (numberOfCorrectSuggestions / topN));
        results.put(u, r);
    }

    private static Double cosine(TwitterUser u, TwitterUser celeb) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        dotProduct += (u.getStatusRate() * celeb.getStatusRate()) + (u.getFollowerCount() * celeb.getFollowerCount()) + (u.getFollowingCount() * celeb.getFollowingCount());
        normA += Math.pow(u.getStatusRate(), 2) + Math.pow(u.getFollowerCount(), 2) + Math.pow(u.getFollowingCount(), 2);
        normB += Math.pow(celeb.getStatusRate(), 2) + Math.pow(celeb.getFollowerCount(), 2) + Math.pow(celeb.getFollowingCount(), 2);
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static void calculateManhattan(TwitterUser u) {
        double numberOfCorrectSuggestions = 0.0d;
        Map<TwitterUser, Double> rates = new HashMap<>();

        for (int i = 0; i < numberOfCelebs; i++) {
            TwitterUser celeb = users.get(numberOfNonCelebs + i);
            rates.put(celeb, manhattan(u, celeb));
        }

        rates = MapUtil.sortByValueReverse(rates);

        Iterator<TwitterUser> it = rates.keySet().iterator();
        for (int i = 0; i < topN; i++) {
            if (u.getCelebs().contains(it.next())) {
                numberOfCorrectSuggestions++;
            }
        }

        Result r = results.getOrDefault(u, new Result());
        r.result.put("Manhattan", (numberOfCorrectSuggestions / topN));
        results.put(u, r);
    }

    private static Double manhattan(TwitterUser u, TwitterUser celeb) {
        double response = 0.0;

        response = Math.abs(u.getStatusRate() - celeb.getStatusRate()) + Math.abs(u.getFollowerCount() - celeb.getFollowerCount()) + Math.abs(u.getFollowingCount() - celeb.getFollowingCount());
        return response;
    }

    //Celebs Count Analysis
    private static void createPayoffsForCelebs() {
        ProgressBar pb = new ProgressBar("Creating payoff for Celebs", users.size(), ProgressBarStyle.ASCII);

        int k = 0;
        for (TwitterUser u : users) {
            //create payoff
            payoff = new PayOffCell[users.size()][users.size()];

            //if (!u.isCeleb) {
            for (int i = 0; i < users.size(); i++) {
                double AC = 0.0d;
                TwitterUser user1 = users.get(i); //Celeb i same as T

                for (TwitterUser celeb : u.getCelebs()) {
                    AC += sim(celeb, user1) * celeb.getTetha();
                }

                for (int j = 0; j < users.size(); j++) {
                    TwitterUser user2 = users.get(j); //Celeb j same as S

                    //Calculate only for unfollowed celebs
                    /*
                        while (u.getFollowings().contains(celeb1)) {
                            i++;
                            if (i < numberOfCelebs) {
                                celeb1 = users.get(numberOfNonCelebs + i);
                            }
                        }

                        while (u.getFollowings().contains(celeb2)) {
                            j++;
                            if (j < numberOfCelebs) {
                                celeb2 = users.get(numberOfNonCelebs + j);
                            }
                        }
                     */
                    //Ready for raw payoff
                    double ANC = 0.0d;

                    for (TwitterUser nonceleb : u.getNonCelebs()) {
                        ANC += sim(nonceleb, user2) * nonceleb.getTetha();
                    }
                    payoff[i][j] = new PayOffCell();
                    payoff[i][j].AC = AC;
                    payoff[i][j].ANC = ANC;
                    payoff[i][j].i = i;
                    payoff[i][j].j = j;

                    //more payoffs
                    double t_ac = 0.0d;
                    double t_anc = 0.0d;

                    //pi_AC
                    double count = 0.0d;
                    for (TwitterUser nc : u.getNonCelebs()) {
                        if (nc.getCelebs().contains(user1)) {
                            count++;
                        }
                    }

                    t_ac = (double) count / (u.getNonCelebs().size() == 0 ? 1 : u.getNonCelebs().size());

                    //pi_ANC
                    count = 0.0d;
                    for (TwitterUser c : u.getCelebs()) {
                        if (c.getCelebs().contains(user2)) {
                            count++;
                        }
                    }

                    t_anc = (double) count / (u.getCelebs().size() == 0 ? 1 : u.getCelebs().size());

                    payoff[i][j].AC += t_ac;
                    payoff[i][j].ANC += t_anc;
                }
            }
            pb.step();
            //}
            //System.out.println(k + " is done.");
            calculateNashForCelebs(u); //for each user
            calculateBestRatesForCelebs(u); //for each user
            calculateCosineForCelebs(u);
            calculateManhattanForCelebs(u);
            calculateCosineForCelebs(u);
        }
        pb.stop();
    }

    private static void calculateCosineForCelebs(TwitterUser u) {
        double numberOfCorrectSuggestions = 0.0d;

        Map<TwitterUser, Double> rates = new HashMap<>();

        for (int i = 0; i < users.size(); i++) {
            TwitterUser user = users.get(i);
            rates.put(user, cosine(u, user));
        }
        rates = MapUtil.sortByValue(rates);
        Iterator<TwitterUser> it = rates.keySet().iterator();
        for (int i = 0; i < topN; i++) {
            if (it.next().isCeleb()) {
                numberOfCorrectSuggestions++;
            }
        }
        Result r = results.getOrDefault(u, new Result());
        r.result.put("IsCeleb_Cosine", (numberOfCorrectSuggestions / topN));
        results.put(u, r);
    }

    private static void calculateNashForCelebs(TwitterUser u) {
        PayOffCell c;
        List<PayOffCell> nash = new ArrayList<>();

        double numberOfCorrectSuggestions = 0.0d;

        for (int i = 0; i < users.size(); i++) {
            for (int j = 0; j < users.size(); j++) {
                c = payoff[i][j];

                if (isGoodPayoff(c, i, j)) {
                    TwitterUser c1 = users.get(i);
                    TwitterUser c2 = users.get(j);
                    nash.add(c);

                    if (c1.isCeleb()) {
                        numberOfCorrectSuggestions++;
                    }

                    if (c2.isCeleb()) {
                        numberOfCorrectSuggestions++;
                    }
                }
            }
        }

        Result r = results.getOrDefault(u, new Result());
        r.result.put("NashEquilibriumForCelebs", (numberOfCorrectSuggestions / nash.size()));
        //r.result.put("NashEquilibrium", (numberOfCorrectSuggestions / numberOfCelebs));
        results.put(u, r);

        calculateSocialEquilibriumForCelebs(u, nash);
    }

    private static void calculateSocialEquilibriumForCelebs(TwitterUser u, List<PayOffCell> nash) {
        PayOffCell c;

        double numberOfCorrectSuggestions = 0.0d;
        double numberOfSocialCells = 0.0d;

        for (int i = 0; i < users.size(); i++) {
            for (int j = 0; j < users.size(); j++) {
                c = payoff[i][j];
                TwitterUser c1 = users.get(i);
                TwitterUser c2 = users.get(j);

                for (PayOffCell n : nash) {
                    if ((c.AC > n.AC) && (c.ANC > n.ANC)) {
                        numberOfSocialCells++;
                        //n is Social Equilibrium
                        if (c1.isCeleb()) {
                            numberOfCorrectSuggestions++;
                        }

                        if (c2.isCeleb()) {
                            numberOfCorrectSuggestions++;
                        }
                    }
                }
            }
        }

        Result r = results.getOrDefault(u, new Result());
        r.result.put("SocialEquilibriumForCelebs", (numberOfCorrectSuggestions / (numberOfSocialCells + 0.01d)));
        results.put(u, r);
    }

    private static void calculateBestRatesForCelebs(TwitterUser u) {
        PayOffCell c;
        List<PayOffCell> bestCells = new Vector<>();

        for (int i = 0; i < users.size(); i++) {
            double max = 0;
            PayOffCell maxC = null;
            int maxI = i;
            int maxJ = 0;

            for (int j = 0; j < users.size(); j++) {
                c = payoff[i][j];
                TwitterUser c1 = users.get(i);
                TwitterUser c2 = users.get(j);

                if (c.AC > max) {
                    maxJ = j;
                    max = c.AC;
                    maxC = c;
                }

            }

            //Max row in AC
            bestCells.add(maxC);
        }

        for (int j = 0; j < users.size(); j++) {
            double max = 0;
            PayOffCell maxC = null;
            int maxI = 0;
            int maxJ = j;

            for (int i = 0; i < users.size(); i++) {
                c = payoff[i][j];
                TwitterUser c1 = users.get(i);
                TwitterUser c2 = users.get(j);

                if (c.ANC > max) {
                    maxI = i;
                    max = c.ANC;
                    maxC = c;
                }

            }

            //Max coloum in AC
            bestCells.add(maxC);
        }

        //bestCells contains the best of each row
        double numberOfCorrectSuggestions = 0.0d;

        for (PayOffCell p : bestCells) {
            if (p != null && p.i < numberOfCelebs && p.j < numberOfNonCelebs) {
                TwitterUser c1 = users.get(p.i);
                TwitterUser c2 = users.get(p.j);

                if (c1.isCeleb()) {
                    numberOfCorrectSuggestions++;
                }

                if (c2.isCeleb()) {
                    numberOfCorrectSuggestions++;
                }
            }
        }
        Result r = results.getOrDefault(u, new Result());
        r.result.put("BestRateForCelebs", (numberOfCorrectSuggestions / bestCells.size()));
        results.put(u, r);
    }

    private static void calculateManhattanForCelebs(TwitterUser u) {
        double numberOfCorrectSuggestions = 0.0d;
        Map<TwitterUser, Double> rates = new HashMap<>();

        for (int i = 0; i < users.size(); i++) {
            TwitterUser user = users.get(i);
            rates.put(user, manhattan(u, user));
        }

        rates = MapUtil.sortByValueReverse(rates);

        Iterator<TwitterUser> it = rates.keySet().iterator();
        for (int i = 0; i < topN; i++) {
            if (it.next().isCeleb()) {
                numberOfCorrectSuggestions++;
            }
        }

        Result r = results.getOrDefault(u, new Result());
        r.result.put("ManhattanForCelebs", (numberOfCorrectSuggestions / topN));
        results.put(u, r);
    }

}
