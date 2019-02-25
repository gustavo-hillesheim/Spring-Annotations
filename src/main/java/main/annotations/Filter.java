package main.annotations;

public @interface Filter {

	String[] fields() default {};
	LikeType likeType() default LikeType.BOTH;

	enum LikeType {
		START, END, BOTH, NONE
	}
}
