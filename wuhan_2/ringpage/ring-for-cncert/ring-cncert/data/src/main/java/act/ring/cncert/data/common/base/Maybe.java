package act.ring.cncert.data.common.base;

import java.io.Serializable;

/**
 * Maybe<T> = Just<T> | Nothing
 */
public final class Maybe<T> implements Serializable {

    private T value = null;

    public Maybe() {
    }

    public Maybe(T value) {
        this.value = value;
    }

    public boolean isJust() {
        return this.value != null;
    }

    public T fromJust() {
        return this.value;
    }
}
