package de.vksi.c4j.acceptancetest.s5b;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.vksi.c4j.systemtest.TransformerAwareRule;
import de.vksi.c4j.AllowPureAccess;
import de.vksi.c4j.acceptancetest.subclasses.Bottom;

public class NNUnchangedS5bTest {

	@Rule
	public TransformerAwareRule transformerAware = new TransformerAwareRule();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void returnsValueWhenUnchanged() {
		assertThat(new Bottom(0).unchanged(), is(0));
	}

	@Test
	public void failsWhenClassInvariantConditionIsNotMet() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("unchanged");
		new Bottom(0) {
			@AllowPureAccess
			private int unchangedDestroyer;

			@Override
			public int unchanged() {
				return value + unchangedDestroyer++;
			};
		}.unchanged();
	}

}
