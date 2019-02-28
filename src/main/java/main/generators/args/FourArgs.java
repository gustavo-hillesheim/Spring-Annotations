package main.generators.args;

public class FourArgs<A, B, C, D> extends ThreeArgs<A, B, C> {


	private D four;

	public D four() {

		return this.four;
	}

	FourArgs(A arg1, B arg2, C arg3, D arg4) {

		super(arg1, arg2, arg3);
		this.four = arg4;
	}

	public static <E, F, G, H> FourArgs of(E arg1, F arg2, G arg3, H arg4) {

		return new FourArgs<>(arg1, arg2, arg3, arg4);
	}
}
