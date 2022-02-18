package pods.cabs;
import java.util.*;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import java.io.*;
public class Main{

    
    static ArrayList<Long> cabs,custs,balances;

    

    public static Behavior<Main.Started> create(ActorRef<Main.Started> replyTo) {
        System.out.println("Under Create of Main Actor");
        return Behaviors.setup(
	        context -> {
                takeInput();
                
                for(int i=0;i<custs.size();++i){
                    Long custId =  custs.get(i);
                    Long amt = balances.get(i);
                    ActorRef<Wallet.Command> walletRef = context.spawn(Wallet.create(custId+"",amt), "Wallet_"+ custId);   
                    Globals.wallets.put(custId+"",walletRef);   
                      
                }
                for(int i=0;i<cabs.size();++i){
                    Long cabId = cabs.get(i);
                    ActorRef<Cab.Command> cabRef = context.spawn(Cab.create(cabId+""), "Cab_"+ cabId);
                    Globals.cabs.put(cabId+"", cabRef);
                }

                for(int i=0;i<10;++i){
                    Globals.rideService.put((long)i, context.spawn(RideService.create(), "RideService_"+i));
                }

                
                pn(Globals.wallets);
                pn(Globals.cabs);
                pn(Globals.rideService);

                replyTo.tell(new Started("Done"));



            //Globals.wallet.get("101");
	        //   ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(ChatRoom.create(), "chatRoom");
	        //   ActorRef<ChatRoom.SessionEvent> gabbler1 = context.spawn(Gabbler.create(), "gabbler1");
	        //   ActorRef<ChatRoom.SessionEvent> gabbler2 = context.spawn(Gabbler.create(), "gabbler2");

	        //   //context.watch(gabbler1);
	        //   //context.watch(gabbler2);

	        //   chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler1));
	        //   chatRoom.tell(new ChatRoom.GetSession("nu’ Gabbler", gabbler2));

	          
	          return Behaviors.empty(); // don't want to receive any more messages
	       
	          // We can return an empty behavior, because we are returning the user-guardian actor, and
	          // normally this actor need not receive any messages.
	        });
    }
    
    public static class Started{
        public final String msg;
        public Started(String msg){
            this.msg = msg ; 
        }

    }

    public static void pn(Object o) {
        System.out.println(o);
        //pn("This is a example")
    }

    // private Main(ActorContext<SayHello> context) {
    //     super(context);
    //     //#create-actors
    //     greeter = context.spawn(Greeter.create(), "greeter");
    //     //#create-actors
    // }

    // @Override
    // public Receive<SayHello> createReceive() {
    //     return newReceiveBuilder().onMessage(Started.class, this::onStarted).build();
    // }

    // private Behavior<SayHello> onStarted (SayHello command) {
    //     //#create-actors
    //     ActorRef<Greeter.Greeted> replyTo =
    //             getContext().spawn(GreeterBot.create(3), command.name);
    //     greeter.tell(new Greeter.Greet(command.name, replyTo));
    //     //#create-actors
    //     return this;
    // }
    static void takeInput() throws Exception{
        Scanner sc = new Scanner(new File("IDs.txt"));
        String line = sc.nextLine();
        line  = sc.nextLine();
        custs = new ArrayList<>();
        balances = new ArrayList<>();
        cabs = new ArrayList<>();
        while(!line.contains("*")){
            cabs.add(Long.parseLong(line));
            line = sc.nextLine();
        }
        line = sc.nextLine();

        while(!line.contains("*")){
            custs.add(Long.parseLong(line));
            line = sc.nextLine();
        }

        line = sc.nextLine();
        Long bal = Long.parseLong(line);
        for(int i=0;i<custs.size();++i){
            balances.add(bal);
        }

        
    }
}
