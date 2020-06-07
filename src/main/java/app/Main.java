package app;

import akka.actor.typed.ActorSystem;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //#actor-system
        final ActorSystem<Messages.RootMessages> auctionSystem =
                ActorSystem.create(RootAgent.create(new CombinatorialAuction()), "auctionActorSystem");
        //#actor-system
        List<BiddingStrategy> strategies = new LinkedList<>();
        for (int i = 0; i < 1; i++) {
            strategies.add(new MaximizationStrategy());
            strategies.add(new RandomStrategy());
            strategies.add(new ImpatientStrategy());
        }
        //#main-send-messages
        auctionSystem.tell(new Messages.InitialiseEnvironment(strategies));
        //#main-send-messages

//        Bid a = new Bid(null, Map.of(Resource.r1, 6.0));
//        Bid b = new Bid(null, Map.of(Resource.r1, 4.0));
//        Bid c = new Bid(null, Map.of(Resource.r1, 5.0));
//        List<Bid> bids = setCover(Arrays.asList());
//        for (Bid bid: bids)
//        {
//            for (Map.Entry e: bid.bidWith.entrySet()) System.out.print(e.getKey() + ":" + e.getValue() + " ");
//            System.out.println("// " + bid.sum);
//        }

    }

    static void printList(List<Boolean> a) {
        for (Boolean b: a) System.out.print(b + " ");
        System.out.println("");
    }

    public static <A, B> List<Pair<A, B>> zip(List<A> as, List<B> bs) {
        return IntStream.range(0, Math.min(as.size(), bs.size()))
                .mapToObj(i -> new Pair<>(as.get(i), bs.get(i)))
                .collect(Collectors.toList());
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s).replace(' ', '0');
    }

    public static List<Bid> setCover(List<Bid> lastBids) {
        return IntStream
                .range(0, Double.valueOf(Math.pow(2, lastBids.size())).intValue())
                .mapToObj(Integer::toBinaryString)
                .map(s -> padLeft(s, lastBids.size()))
                .map(s -> s.chars().mapToObj(c -> c == ((int) '1')).collect(Collectors.toList()))
                .map(mask -> zip(mask, lastBids).stream().filter(Pair::getKey).map(Pair::getValue).collect(Collectors.toList()))
                .filter(bids -> bids.stream().reduce(new Bid(null, new HashMap<>()), Bid::addNoConflict).bidWith != null)
                .max(Comparator.comparingDouble(bids -> bids.stream().map(b -> b.sum).reduce(0.0, Double::sum)))
                .get();
    }
}
