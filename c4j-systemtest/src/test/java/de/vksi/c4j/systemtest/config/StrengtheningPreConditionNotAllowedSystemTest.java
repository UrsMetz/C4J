package de.vksi.c4j.systemtest.config;

import static de.vksi.c4j.Condition.preCondition;

import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

import de.vksi.c4j.ContractReference;
import de.vksi.c4j.systemtest.TransformerAwareRule;

public class StrengtheningPreConditionNotAllowedSystemTest {
	@Rule
	public TransformerAwareRule transformerAwareRule = new TransformerAwareRule();

	@Test
	public void testPreConditionUndefined() {
		transformerAwareRule.expectGlobalLog(Level.ERROR, "Found strengthening pre-condition in "
						+ ContractClass.class.getName() + ".method(int)" + " which is already defined from" + " "
						+ SuperClassContract.class.getName() + " - ignoring the pre-condition.");
		new TargetClass().method(0);
	}

	@ContractReference(ContractClass.class)
	public static class TargetClass extends SuperClass {
	}

	public static class ContractClass extends TargetClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > 0;
			}
		}
	}

	@ContractReference(SuperClassContract.class)
	public static class SuperClass {
		public void method(int arg) {
		}
	}

	public static class SuperClassContract extends SuperClass {
		@Override
		public void method(int arg) {
			if (preCondition()) {
				assert arg > -1;
			}
		}
	}
}
