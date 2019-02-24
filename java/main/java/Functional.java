import java.util.Optional;

/**
 * Created by jeremy on 07/14/2018.
 */
public class Functional {

    public interface Function3<A, B, C, R> {
        R apply(A a, B b, C c) throws Throwable;
    }

    public static class Either<L, R> {

        public final Optional<L> left;
        public final Optional<R> right;

        private Either(Optional<L> left, Optional<R> right) {
            this.left = left;
            this.right = right;
        }

        public static <L, R> Either<L, R> Left(L value) {
            return new Either<>(Optional.of(value), Optional.empty());
        }

        public static <L, R> Either<L, R> Right(R value) {
            return new Either<>(Optional.empty(), Optional.of(value));
        }

        // TODO implements toString()
    }

    public static class Tuple<A, B> {

        public final A _1;
        public final B _2;

        public Tuple(A _1, B _2) {
            this._1 = _1;
            this._2 = _2;
        }

        // https://www.sitepoint.com/how-to-implement-javas-hashcode-correctly/
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
            result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
            return result;
        }

        // https://www.sitepoint.com/implement-javas-equals-method-correctly/
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof Tuple)) return false;
            Tuple other = (Tuple) obj;
            if (_1 == null) { if (other._1 != null) return false; }
            else if (!_1.equals(other._1)) return false;
            if (_2 == null) { if (other._2 != null) return false; }
            else if (!_2.equals(other._2)) return false;
            return true;
        }

        // TODO implements toString()
    }

    public static <A, B> Tuple<A, B> Tuple(A a, B b) { return new Tuple<>(a, b); }

}
