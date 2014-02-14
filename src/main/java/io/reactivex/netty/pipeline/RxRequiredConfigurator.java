package io.reactivex.netty.pipeline;

import io.netty.channel.ChannelPipeline;
import io.reactivex.netty.ConnectionHandler;
import rx.Observable;

/**
 * An implementation of {@link PipelineConfigurator} which is ALWAYS added at the end of the pipeline. This
 * pipeline configurator brides between netty's pipeline processing and Rx {@link Observable}
 *
 * @param <I> Input type for the pipeline. This is the type one writes to this pipeline.
 * @param <O> Output type of the emitted observable.  This is the type one reads from this pipeline.
 *
 * @author Nitesh Kant
 */
public class RxRequiredConfigurator<I, O> implements PipelineConfigurator<Object, Object> {

    public static final String CONN_LIFECYCLE_HANDLER_NAME = "conn_lifecycle_handler";
    public static final String NETTY_OBSERVABLE_ADAPTER_NAME = "netty_observable_adapter";

    private final ConnectionHandler<I, O> connectionHandler;

    public RxRequiredConfigurator(final ConnectionHandler<I, O> connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void configureNewPipeline(ChannelPipeline pipeline) {

        /**
         * This method is called for each new connection & the following two channel handlers are not shareable, so
         * we need to create a new instance every time.
         */
        ObservableAdapter observableAdapter = new ObservableAdapter();
        ConnectionLifecycleHandler<I, O> lifecycleHandler =
                new ConnectionLifecycleHandler<I, O>(connectionHandler, observableAdapter);
        pipeline.addLast(CONN_LIFECYCLE_HANDLER_NAME, lifecycleHandler);
        pipeline.addLast(NETTY_OBSERVABLE_ADAPTER_NAME, observableAdapter);
    }
}
