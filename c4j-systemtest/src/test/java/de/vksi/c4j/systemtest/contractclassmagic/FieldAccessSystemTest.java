package de.vksi.c4j.systemtest.contractclassmagic;

import static de.vksi.c4j.Condition.preCondition;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.Target;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class FieldAccessSystemTest {
	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	private DummyClass dummy;

	@Before
	public void before() {
		dummy = new DummyClass();
	}

	@Test
	public void testPreConditionWithFieldAccess() {
		dummy.setValue(5);
		dummy.methodContractHasFieldAccess();
	}

	@ContractReference(DummyContract.class)
	public static class DummyClass {
		protected int value;

		public void methodContractHasFieldAccess() {
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	public static class DummyContract extends DummyClass {
		@Target
		private DummyClass target;

		@Override
		public void methodContractHasFieldAccess() {
			if (preCondition()) {
				assert target.value == 5;
			}
		}
	}

	@Test
	public void testFieldAccessWithSameFieldInSuperClass() {
		new TargetClass().method();
	}

	@ContractReference(TargetClassContract.class)
	public static class TargetClass extends SuperClass {
		@Override
		public void method() {
		}
	}

	public static class TargetClassContract extends TargetClass {
		@Target
		private TargetClass target;

		@Override
		public void method() {
			if (preCondition()) {
				assert target.field == 0;
			}
		}
	}

	@ContractReference(SuperClassContract.class)
	public static class SuperClass {
		protected int field;

		public void method() {
		}
	}

	public static class SuperClassContract extends SuperClass {
		@Target
		private SuperClass target;

		@Override
		public void method() {
			if (preCondition()) {
				assert target.field == 0;
			}
		}
	}
}
