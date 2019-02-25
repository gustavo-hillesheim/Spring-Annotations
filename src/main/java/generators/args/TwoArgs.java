package hava.annotation.spring.generators.args;

public class TwoArgs<A, B> extends Args<A> {

  private B two;

  public B two() {

    return this.two;
  }

  TwoArgs(A arg1, B arg2) {

    super(arg1);

    this.two = arg2;
  }

  public static <C, D> TwoArgs<C, D> of(C arg1, D arg2) {

    return new TwoArgs<>(arg1, arg2);
  }
}
