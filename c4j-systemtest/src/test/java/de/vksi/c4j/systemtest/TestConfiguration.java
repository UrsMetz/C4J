package de.vksi.c4j.systemtest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.vksi.c4j.AbstractConfiguration;
import de.vksi.c4j.Configuration;

public class TestConfiguration extends AbstractConfiguration {

	@Override
	public Set<String> getRootPackages() {
		return Collections.singleton("de.vksi.c4j");
	}

	@Override
	public Set<Configuration> getConfigurations() {
		Set<Configuration> configurations = new HashSet<Configuration>();
		configurations.add(new AcceptanceTestConfiguration());
		configurations.add(new SystemTestConfiguration());
		return configurations;
	}

}
