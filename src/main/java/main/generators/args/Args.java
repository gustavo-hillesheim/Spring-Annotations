package main.generators.args;

public class Args<A> {
  
  
  private A one;
  
  public A one() {
    
    return this.one;
  }
  
  Args(A arg1) {
    
    this.one = arg1;
  }
  
  public static NoArgs of() {
    
    return new NoArgs();
  }
  
  public static <B> Args<B> of(B arg1) {
    
    return new Args<>(arg1);
  }
  
  public static <B, C> TwoArgs<B, C> of (B arg1, C arg2) {
    
    return new TwoArgs<>(arg1, arg2);
  }
  
  public static <B, C, D> ThreeArgs<B, C, D> of (B arg1, C arg2, D arg3) {
    
    return new ThreeArgs<>(arg1, arg2, arg3);
  }

  public static <B, C, D, E> FourArgs<B, C, D, E> of (B arg1, C arg2, D arg3, E arg4) {

    return new FourArgs<>(arg1, arg2, arg3, arg4);
  }
}