package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Wallet extends AbstractBehavior<Wallet.Command> {




    String custId;
    Long amt, initialBal ; 
    private final ActorContext<Command> context;


    public static Behavior<Command> create(String custId,Long amt) {
	    return Behaviors.setup(context -> new Wallet(context,custId,amt));
	}


    public Wallet(ActorContext<Command> context,String custId,Long amt){
        super(context);
        this.context = context ;
        this.custId = custId;
        this.amt = amt ;
        this.initialBal = amt;
    }

    //=======================================================================================================================

    public interface Command{}
    
    public static final class GetBalance implements Command {

        public final ActorRef<ResponseBalance> replyTo;

        public GetBalance(ActorRef<ResponseBalance> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static final class DeductBalance implements Command {
        public final Long toDeduct;
        public final ActorRef<ResponseBalance> replyTo;

        public DeductBalance(Long toDeduct, ActorRef<ResponseBalance> replyTo) {
            this.toDeduct = toDeduct;
            this.replyTo = replyTo;
        }
    }

    public static final class AddBalance implements Command {
        public final Long toAdd;

        public AddBalance(Long toAdd) {
            this.toAdd = toAdd;
        }
    }

    public static final class Reset implements Command {
        public final ActorRef<ResponseBalance> replyTo;

        public Reset(ActorRef<ResponseBalance> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class ResponseBalance implements Command {
        // Long balance;
        // public ResponseBalance(Long balance) {this.balance = balance;}

        // @Override
        // public boolean equals(Object o){
        //     if(this == o) return true;
        //     if (!(o instanceof ResponseBalance)) return false;
        //     ResponseBalance responseBalance = (ResponseBalance) o;
        //     return balance == responseBalance.balance;
        // }

        // @Override
        // public int hashCode() {
        //     return Objects.hash(balance);
        // }

        Long balance;
        public ResponseBalance(Long balance) {
            this.balance = balance;
        }

        @Override
        public boolean equals(Object o) {
            ResponseBalance r = (ResponseBalance) o;
            return r.balance.equals(this.balance);
        }

    }

    //=======================================================================================================================

    @Override
	public Receive<Wallet.Command> createReceive() {
		ReceiveBuilder<Wallet.Command> builder = newReceiveBuilder();
		return builder
				.onMessage(Wallet.GetBalance.class, this::onGetBalance)
                .onMessage(DeductBalance.class, this::onDeductBalance)
                .onMessage(AddBalance.class, this::onAddBalance)
                .onMessage(Reset.class, this::onReset)
				.build();
	}

    private Behavior<Command> onGetBalance(GetBalance getBalance) {
        ActorRef<ResponseBalance> client = getBalance.replyTo;
        client.tell(new ResponseBalance(amt));
        pn("Got GetBalance call on custId:"+custId+" and balance: "+amt);
        return this;
    }

    private Behavior<Command> onDeductBalance(DeductBalance deductBalance) {
        ActorRef<ResponseBalance> client = deductBalance.replyTo;
        pn("Got DeductBalance call on custId:"+custId+" and balance: "+amt+" and toDeduct: "+deductBalance.toDeduct);
        if (deductBalance.toDeduct > 0 && amt >= deductBalance.toDeduct) {
            amt -= deductBalance.toDeduct;
            client.tell(new ResponseBalance(amt));
        } else {
            client.tell(new ResponseBalance(-1L));
        }
        return this;
    }

    private Behavior<Command> onAddBalance(AddBalance addBalance) {
        pn("Got AddBalance call on custId:"+custId+" and balance: "+amt+" and toAdd: "+addBalance.toAdd);
        if (addBalance.toAdd >= 0)
            amt += addBalance.toAdd;
        return this;
    }

    private Behavior<Command> onReset(Reset reset) {
        pn("Got call on custId:"+custId+" and balance: "+amt);
        ActorRef<ResponseBalance> client = reset.replyTo;
        amt = initialBal;
        client.tell(new ResponseBalance(amt));
        return this;
    }

    // public static class GetBalance implements Command {
    //     public final ActorRef<ResponseBalance> replyTo;

    //     public GetBalance(ActorRef<ResponseBalance> replyTo) {
    //         this.replyTo = replyTo;
    //     }
    // }


    // private final ActorRef<Greeter.Greet> greeter;

    // public static Behavior<SayHello> create() {
    //     return Behaviors.setup(Wallet::new);
    // }

    // private GreeterWallet(ActorContext<SayHello> context) {
    //     super(context);
    //     //#create-actors
    //     greeter = context.spawn(Greeter.create(), "greeter");
    //     //#create-actors
    // }

    // @Override
    // public Receive<SayHello> createReceive() {
    //     return newReceiveBuilder().onMessage(SayHello.class, this::onSayHello).build();
    // }

    // private Behavior<SayHello> onSayHello(SayHello command) {
    //     //#create-actors
    //     ActorRef<Greeter.Greeted> replyTo =
    //             getContext().spawn(GreeterBot.create(3), command.name);
    //     greeter.tell(new Greeter.Greet(command.name, replyTo));
    //     //#create-actors
    //     return this;
    // }

    public static void pn(Object o) {
        System.out.println(o);
        //pn("This is a example")
    }
}
