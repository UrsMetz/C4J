package de.andrena.next.systemtest.pure;

import org.junit.Rule;
import org.junit.Test;

import de.andrena.next.AllowPureAccess;
import de.andrena.next.Contract;
import de.andrena.next.Pure;
import de.andrena.next.systemtest.TransformerAwareRule;

public class AllowPureAccessSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testAllowPureAccessFromTargetClass() {
		new TargetClass().incrementValue();
	}

	@Test
	public void testAllowPureAccessFromContractClass() {
		new TargetClass().incrementValueInContract();
	}

	@Contract(TargetClassContract.class)
	public static class TargetClass {
		@AllowPureAccess
		protected int value;

		@Pure
		public void incrementValue() {
			value++;
		}

		@Pure
		public void incrementValueInContract() {
		}
	}

	public static class TargetClassContract extends TargetClass {
		@Override
		public void incrementValueInContract() {
			value++;
		}
	}
}
