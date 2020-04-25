package app;

import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args) {
        //#actor-system
        final ActorSystem<Messages.RootMessages> auctionSystem =
                ActorSystem.create(RootAgent.create(), "auctionActorSystem");
        //#actor-system

        //#main-send-messages
        auctionSystem.tell(new Messages.InitialiseEnvironment(5));
        //#main-send-messages

    }
}
