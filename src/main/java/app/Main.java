package app;

import akka.actor.typed.ActorSystem;

import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //#actor-system
        final ActorSystem<Messages.RootMessages> auctionSystem =
                ActorSystem.create(RootAgent.create(new EnglishAuction()), "auctionActorSystem");
        //#actor-system
        List<BiddingStrategy> strategies = new LinkedList<>();
        for (int i = 0; i < 1; i++) {
            strategies.add(new BigStepsStrategy());
            strategies.add(new RoundToIntegerStrategy());
            strategies.add(new SmallStepsStrategy());
        }
        //#main-send-messages
        auctionSystem.tell(new Messages.InitialiseEnvironment(strategies));
        //#main-send-messages

    }
}
