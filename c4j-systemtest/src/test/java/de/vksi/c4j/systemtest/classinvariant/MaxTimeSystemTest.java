package de.vksi.c4j.systemtest.classinvariant;

import static de.vksi.c4j.Condition.maxTime;

import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ClassInvariant;
import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class MaxTimeSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Test
	public void testMaxTimeSuccess() throws Throwable {
		// takes up to 0.1 seconds on Jenkins...
		new TargetClass().sleep(0);
	}

	@Test(expected = AssertionError.class)
	public void testMaxTimeFailure() throws Throwable {
		new TargetClass().sleep(201);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass {
		public void sleep(long milliSeconds) throws InterruptedException {
			if (milliSeconds > 0) {
				Thread.sleep(milliSeconds);
			}
		}
	}

	public static class ContractClass extends TargetClass {
		@ClassInvariant
		public void invariant() {
			assert maxTime(0.2);
		}
	}

}
