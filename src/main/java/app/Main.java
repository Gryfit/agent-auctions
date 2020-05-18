package app;

import akka.actor.typed.ActorSystem;

import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //#actor-system
        for (int j = 0; j < 200; j++)
        {
            final ActorSystem<Messages.RootMessages> auctionSystem =
                    ActorSystem.create(RootAgent.create(), "auctionActorSystem"+j);
            //#actor-system
            List<BiddingStrategy> strategies = new LinkedList<>();
            for (int i = 0; i < 2; i++) {
                strategies.add(new BigStepsStrategy());
                strategies.add(new RoundToIntegerStrategy());
                strategies.add(new SmallStepsStrategy());
            }
            //#main-send-messages
            auctionSystem.tell(new Messages.InitialiseEnvironment(strategies));
            Thread.sleep(100);
        }
        //#main-send-messages

    }
}
