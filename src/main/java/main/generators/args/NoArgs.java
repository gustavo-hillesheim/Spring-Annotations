package main.generators.args;

public class NoArgs extends Args<Object> {

  NoArgs() {
    super(null);
  }

  public static NoArgs of() {
    
    return new NoArgs();
  }
}
