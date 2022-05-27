package mb.pcf.task;

import mb.pcf.PcfClassLoaderResources;
import mb.pcf.PcfScope;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.stratego.pie.AstStrategoTransformTaskDef;

import javax.inject.Inject;
import java.io.IOException;

@PcfScope
public class PcfEvaluateTask extends AstStrategoTransformTaskDef {
	private final PcfClassLoaderResources classloaderResources;

	@Inject
	public PcfEvaluateTask(PcfClassLoaderResources classloaderResources,
			PcfGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
		super(getStrategoRuntimeProvider, "pre-analyze", "post-analyze", "beta-reduce");
		this.classloaderResources = classloaderResources;
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	@Override
	protected void createDependencies(ExecContext context) throws IOException {
		context.require(classloaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
	}
}
