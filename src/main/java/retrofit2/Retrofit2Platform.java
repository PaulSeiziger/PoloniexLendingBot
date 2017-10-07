package retrofit2;
import java.util.concurrent.Executor;

import retrofit2.CallAdapter.Factory;

public final class Retrofit2Platform {

    private Retrofit2Platform() {
    }

    public static Factory defaultCallAdapterFactory(Executor executor) {
        Executor defaultExecutor = executor;
        if (defaultExecutor == null) {
            defaultExecutor = defaultCallbackExecutor();
        }
        return Platform.get().defaultCallAdapterFactory(defaultExecutor);
    }

    public static Executor defaultCallbackExecutor() {
        return Platform.get().defaultCallbackExecutor();
    }
}
