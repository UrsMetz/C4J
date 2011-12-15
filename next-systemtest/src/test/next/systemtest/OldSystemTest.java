package next.systemtest;

import static next.Condition.old;
import static next.Condition.post;

import java.io.InputStream;

import next.Contract;

import org.junit.Before;
import org.junit.Test;

public class OldSystemTest extends TransformerAwareTest {

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testOldWithField() {
		dummy.setValue(5);
		dummy.incrementValueCheckField();
	}

	@Test
	public void testOldWithMethod() {
		dummy.setValue(5);
		dummy.incrementValueCheckMethod();
	}

	@Contract(DummyContract.class)
	public static class DummyClass {
		protected int value;
		protected OtherClass otherValue;

		public void setValue(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public void incrementValueCheckField() {
			value++;
		}

		public void incrementValueCheckMethod() {
			value++;
		}
	}

	public static class OtherClass {
		public InputStream stream;

		public int otherMethod() {
			return 0;
		}
	}

	public static class DummyContract extends DummyClass {

		@Override
		public void incrementValueCheckField() {
			if (post()) {
				assert value == old(value) + 1;
			}
		}

		@Override
		public void incrementValueCheckMethod() {
			if (post()) {
				assert getValue() == old(getValue()) + 1;
			}
		}
	}
}