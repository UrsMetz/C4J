package de.vksi.c4j.systemtest.staticmethods;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.UsageError;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class UnmatchedStaticMethodSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test(expected = UsageError.class)
	public void testUnmatchedStaticMethod() {
		new UnmatchedStaticMethodTargetClass().method(1);
	}

	@Test
	public void testUnmatchedPrivateStaticMethod() {
		new UnmatchedPrivateStaticMethodTargetClass().method(1);
	}

	@ContractReference(UnmatchedStaticMethodContractClass.class)
	public static class UnmatchedStaticMethodTargetClass {
		public void method(int arg) {
		}
	}

	public static class UnmatchedStaticMethodContractClass extends UnmatchedStaticMethodTargetClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}

		public static void unmatchedStaticMethod() {
		}
	}

	@ContractReference(UnmatchedPrivateStaticMethodContractClass.class)
	public static class UnmatchedPrivateStaticMethodTargetClass {
		public void method(int arg) {
		}
	}

	public static class UnmatchedPrivateStaticMethodContractClass extends UnmatchedPrivateStaticMethodTargetClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}

		@SuppressWarnings("unused")
		private static void unmatchedStaticMethod() {
		}
	}
}
