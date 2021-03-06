package de.vksi.c4j.internal.transformer;

import javassist.CtClass;
import de.vksi.c4j.internal.util.ContractRegistry.ContractInfo;

public abstract class AbstractContractClassTransformer extends ClassTransformer {
	public abstract void transform(ContractInfo contractInfo, CtClass contractClass) throws Exception;
}
