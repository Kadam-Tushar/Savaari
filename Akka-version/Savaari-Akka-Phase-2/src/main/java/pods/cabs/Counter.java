package pods.cabs;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ActorContext;

import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;

public class Counter extends
    EventSourcedBehavior<Counter.Command, Counter.CounterEvent, Counter.Count> {


    public static final EntityTypeKey<Command> TypeKey =
            EntityTypeKey.create(Command.class, "RideIdCounter");

    interface Command extends CborSerializable {
    }

    public static final class GetAndIncrement implements Command {
        ActorRef<RideService.Command> replyTo;

        public GetAndIncrement(ActorRef<RideService.Command> replyTo) {
            this.replyTo = replyTo;
        }

        public GetAndIncrement () {}
    }

    public static final class CounterValue implements RideService.Command {
        long value = 0;

        public CounterValue(long value) {
            this.value = value;
        }
        public CounterValue(){
            super();
        }
    }

    interface CounterEvent extends CborSerializable {
    }

    public static final class GetAndIncrementEvent implements CounterEvent {
        int dummy = 0;
        public GetAndIncrementEvent(){
            super();
        }
    }

    static final class Count implements CborSerializable {
        public long count = 0;
        public Count(){
            super();
        }
    }

    @Override
    public Count emptyState() {
        return new Count();
    }

    public Counter(ActorContext<Command> context, PersistenceId persistenceId) {
        super(persistenceId);
    }

    public static Behavior<Command> create(String entityId, PersistenceId persistenceId) {
        return Behaviors.setup(context ->
                new Counter(context, persistenceId)
        );
    }

    @Override
    public CommandHandler<Command, CounterEvent, Count> commandHandler() {
        return newCommandHandlerBuilder().forAnyState()
                .onCommand(GetAndIncrement.class, this::onGetAndIncrement)
                .build();
    }

    private Effect<CounterEvent, Count> onGetAndIncrement(GetAndIncrement getAndIncrement) {
        return Effect().persist(new GetAndIncrementEvent())
				.thenRun(newState -> getAndIncrement.replyTo.tell(new CounterValue(newState.count)));
    }

    @Override
    public EventHandler<Count, CounterEvent> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(GetAndIncrementEvent.class, (state, evt) -> {
                    state.count++;
                    return state;
                })
                .build();
    }
}