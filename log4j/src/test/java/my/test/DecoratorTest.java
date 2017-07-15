package my.test;

public class DecoratorTest implements Test{
	private Test target;

	public DecoratorTest(Test target) {
		this.target = target;
	}

	@Override
	public int test(int i) {
		return target.test(i);
	}
}