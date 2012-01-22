package de.andrena.next.internal.transformer;

import javassist.CtClass;
import de.andrena.next.internal.RootTransformer;
import de.andrena.next.internal.util.ContractRegistry.ContractInfo;

public class ContractClassTransformer extends AbstractContractClassTransformer {

	private AbstractContractClassTransformer[] transformers;

	public ContractClassTransformer(RootTransformer rootTransformer) {
		this.transformers = new AbstractContractClassTransformer[] { new ContractExpressionTransformer(),
				new ConstructorTransformer() };
	}

	@Override
	public void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception {
		for (AbstractContractClassTransformer transformer : transformers) {
			transformer.transform(contractInfo, contractClass);
		}
	}

	protected AbstractContractClassTransformer[] getTransformers() {
		return transformers;
	}

}
