package main.generators.args;

public class ThreeArgs<A, B, C> extends TwoArgs<A, B> {

  
  private C three;
  
  public C three() {
    
    return this.three;
  }
  
  ThreeArgs(A arg1, B arg2, C arg3) {
    
    super(arg1, arg2);
    this.three = arg3;
  }
  
  public static <D, E, F> ThreeArgs<D, E, F> of(D arg1, E arg2, F arg3) {
    
    return new ThreeArgs<>(arg1, arg2, arg3);
  }
}
